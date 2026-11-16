import { useCallback, useState } from "react"
import { Link, useNavigate, useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { UserPlusIcon, UsersIcon } from "lucide-react"
import { EmptyState } from "@/components/page-states"
import { ItemPicker } from "@/components/management/permission-picker"
import { DataBody, DataHead, DataRow, DataTable, RowChevron, rowLink, Td, Th } from "@/components/management/data-table"
import { ListPanel, PageHeader, Pager, PanelError, SearchBox, StatusDot, TableSkeleton } from "@/components/management/states"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { Switch } from "@/components/ui/switch"
import { FilterSelect } from "@/components/filter-select"
import { useAccess } from "@/lib/access"
import { allRoles, createUser, listUsers, setUserStatus } from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { usePasswordRules } from "@/lib/password"
import { initials } from "@/lib/providers"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const PAGE_SIZE = 10
const E164 = /^\+[1-9]\d{1,14}$/
const USERNAME = /^[A-Za-z0-9._-]{3,50}$/
const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function StatusBadge({ enabled, locked }) {
  const { t } = useI18n()
  if (locked) return <StatusDot tone="danger" label={t("mgmt.users.locked")} />
  return <StatusDot on={enabled} label={enabled ? t("mgmt.users.enabled") : t("mgmt.users.disabled")} />
}

const EMPTY = {
  givenName: "",
  familyName: "",
  username: "",
  email: "",
  phoneNumber: "",
  pictureUrl: "",
  locale: "en",
  password: "",
  enabled: true,
}

function CreateUserDialog({ open, onOpenChange, onCreated }) {
  const passwordRules = usePasswordRules()
  const { t } = useI18n()
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [roles, setRoles] = useState(new Set())
  const [pending, setPending] = useState(false)
  const roleList = useAsync(() => (open ? allRoles() : Promise.resolve([])), [open])
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e?.target ? e.target.value : e }))

  const validate = () => {
    const req = t("mgmt.common.required")
    const e = {}
    if (!form.givenName.trim()) e.givenName = req
    if (!form.familyName.trim()) e.familyName = req
    if (!USERNAME.test(form.username)) e.username = t("validation.usernameFormat")
    if (!EMAIL.test(form.email)) e.email = t("validation.emailFormat")
    if (form.phoneNumber && !E164.test(form.phoneNumber)) e.phoneNumber = t("validation.phoneFormat")
    if (!passwordRules.every((r) => r.test(form.password))) e.password = t("validation.passwordWeak")
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!validate()) return
    setPending(true)
    try {
      const { enabled, ...body } = form
      const user = await createUser({ ...body, roleUuids: [...roles] })
      if (!enabled) await setUserStatus(user.uuid, "disable")
      toast.success(t("mgmt.users.created"))
      setForm(EMPTY)
      setRoles(new Set())
      onOpenChange(false)
      onCreated(user)
    } catch (error) {
      toast.error(errorText(error, t))
    } finally {
      setPending(false)
    }
  }

  const field = (key, label, props = {}, hint) => (
    <Field data-invalid={!!errors[key] || undefined}>
      <FieldLabel htmlFor={`new-${key}`}>{label}</FieldLabel>
      <Input id={`new-${key}`} value={form[key]} onChange={set(key)} aria-invalid={!!errors[key] || undefined} {...props} />
      {errors[key] ? <FieldError>{errors[key]}</FieldError> : hint && <FieldDescription>{hint}</FieldDescription>}
    </Field>
  )

  return (
    <Dialog open={open} onOpenChange={(v) => !pending && onOpenChange(v)}>
      <DialogContent className="max-h-[90svh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{t("mgmt.users.create")}</DialogTitle>
          <DialogDescription>{t("mgmt.users.intro")}</DialogDescription>
        </DialogHeader>
        <form id="create-user" onSubmit={submit} noValidate>
          <FieldGroup>
            <div className="grid gap-4 sm:grid-cols-2">
              {field("givenName", t("mgmt.users.firstName"), { autoComplete: "off" })}
              {field("familyName", t("mgmt.users.lastName"), { autoComplete: "off" })}
              {field("username", t("mgmt.users.username"), { autoComplete: "off" })}
              {field("email", t("mgmt.users.email"), { type: "email", autoComplete: "off" })}
              {field("phoneNumber", t("mgmt.users.phone"), { type: "tel" }, t("mgmt.users.phoneHint"))}
              <Field>
                <FieldLabel htmlFor="new-locale">{t("mgmt.users.locale")}</FieldLabel>
                <Select value={form.locale} onValueChange={set("locale")}>
                  <SelectTrigger id="new-locale" className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectGroup>
                      <SelectItem value="en">English</SelectItem>
                      <SelectItem value="km">ភាសាខ្មែរ</SelectItem>
                    </SelectGroup>
                  </SelectContent>
                </Select>
              </Field>
            </div>
            {field("pictureUrl", t("mgmt.users.picture"), { type: "url", placeholder: "https://" })}
            {field("password", t("mgmt.users.password"), { type: "password", autoComplete: "new-password" }, t("mgmt.users.passwordHint"))}
            <Field orientation="horizontal">
              <Switch id="new-enabled" checked={form.enabled} onCheckedChange={set("enabled")} />
              <FieldLabel htmlFor="new-enabled">{t("mgmt.users.enabled")}</FieldLabel>
            </Field>
            <Field>
              <FieldLabel>{t("mgmt.users.initialRoles")}</FieldLabel>
              <ItemPicker
                items={roleList.data}
                loading={roleList.status === "loading"}
                selected={roles}
                onChange={setRoles}
                label={(r) => r.name}
              />
            </Field>
          </FieldGroup>
        </form>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            {t("mgmt.common.cancel")}
          </Button>
          <Button type="submit" form="create-user" disabled={pending}>
            {pending && <Spinner data-icon="inline-start" />}
            {t("mgmt.users.create")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default function UsersPage() {
  const { t } = useI18n()
  const { can } = useAccess()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const search = params.get("q") ?? ""
  const status = params.get("status") ?? "all"
  const page = Number(params.get("page") ?? 1)
  const creating = params.get("new") === "1"

  const update = useCallback(
    (patch) =>
      setParams((p) => {
        const next = new URLSearchParams(p)
        for (const [k, v] of Object.entries(patch)) {
          if (v === null || v === "" || v === "all" || (k === "page" && v === 1)) next.delete(k)
          else next.set(k, String(v))
        }
        return next
      }),
    [setParams]
  )

  const users = useAsync(
    () =>
      listUsers({
        page,
        size: PAGE_SIZE,
        search: search || undefined,
        enabled: status === "active" ? true : status === "inactive" ? false : undefined,
      }),
    [page, search, status]
  )

  const filtered = !!search || status !== "all"

  const body = () => {
    if (users.status === "loading") return <TableSkeleton rows={6} />
    if (users.status === "error") return <PanelError onRetry={users.reload} />
    const { items } = users.data
    if (items.length === 0) {
      return (
        <EmptyState
          icon={UsersIcon}
          title={filtered ? t("mgmt.common.noResults") : t("mgmt.users.empty")}
          description={filtered ? t("mgmt.common.noResultsBody") : t("mgmt.users.emptyBody")}
        >
          {filtered && (
            <Button variant="outline" onClick={() => update({ q: null, status: null, page: 1 })}>
              {t("mgmt.common.clearFilters")}
            </Button>
          )}
        </EmptyState>
      )
    }
    return (
      <DataTable>
        <DataHead>
          <Th>{t("mgmt.users.name")}</Th>
          <Th className="hidden md:table-cell">{t("mgmt.users.username")}</Th>
          <Th className="w-40">{t("mgmt.common.status")}</Th>
          <Th className="w-12">
            <span className="sr-only">{t("mgmt.common.actions")}</span>
          </Th>
        </DataHead>
        <DataBody>
          {items.map((u) => (
            <DataRow key={u.uuid} onOpen={() => navigate(`/management/users/${u.uuid}`)}>
              <Td>
                <div className="flex items-center gap-3">
                  <Avatar className="size-9 rounded-md">
                    <AvatarFallback className="rounded-md">{initials(u.name)}</AvatarFallback>
                  </Avatar>
                  <div className="flex min-w-0 flex-col leading-snug">
                    <Link
                      to={`/management/users/${u.uuid}`}
                      className={cn("truncate font-medium", rowLink)}
                      onClick={(e) => e.stopPropagation()}
                    >
                      {u.name}
                    </Link>
                    <span className="truncate text-muted-foreground">{u.email}</span>
                  </div>
                </div>
              </Td>
              <Td className="hidden font-mono text-muted-foreground md:table-cell">{u.username}</Td>
              <Td>
                <StatusBadge enabled={u.enabled} locked={u.locked} />
              </Td>
              <Td>
                <RowChevron />
              </Td>
            </DataRow>
          ))}
        </DataBody>
      </DataTable>
    )
  }

  return (
    <>
      <PageHeader title={t("mgmt.users.title")} intro={t("mgmt.users.intro")}>
        {can("user:create") && (
          <Button onClick={() => update({ new: 1 })}>
            <UserPlusIcon data-icon="inline-start" />
            {t("mgmt.users.create")}
          </Button>
        )}
      </PageHeader>

      <ListPanel
        toolbar={
          <>
            <SearchBox
              className="sm:max-w-sm"
              value={search}
              placeholder={t("mgmt.users.searchPlaceholder")}
              onChange={(q) => update({ q, page: 1 })}
            />
            {users.status === "refreshing" && <Spinner className="text-muted-foreground" />}
            <FilterSelect
              className="sm:ml-auto"
              value={status}
              onValueChange={(v) => update({ status: v, page: 1 })}
              label={t("mgmt.common.status")}
              options={[
                { value: "all", label: t("mgmt.common.all") },
                { value: "active", label: t("mgmt.common.active") },
                { value: "inactive", label: t("mgmt.common.inactive") },
              ]}
            />
          </>
        }
        footer={
          users.data?.items?.length > 0 && users.status !== "loading" ? (
            <Pager page={page} pages={users.data.pages} total={users.data.total} size={PAGE_SIZE} onPage={(p) => update({ page: p })} />
          ) : null
        }
      >
        {body()}
      </ListPanel>

      {can("user:create") && (
        <CreateUserDialog
          open={creating}
          onOpenChange={(v) => update({ new: v ? 1 : null })}
          onCreated={(user) => navigate(`/management/users/${user.uuid}`)}
        />
      )}
    </>
  )
}
