import { useCallback, useState } from "react"
import { Link, useNavigate, useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { BadgeCheckIcon, EllipsisIcon, EyeIcon, LockIcon, PowerIcon, Trash2Icon, UnlockIcon, UserPlusIcon, UsersIcon } from "lucide-react"
import { EmptyState } from "@/components/page-states"
import { ConfirmDialog } from "@/components/confirm-dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ItemPicker } from "@/components/management/permission-picker"
import { DataBody, DataHead, DataRow, DataTable, revealOnRow, rowLink, Td, Th } from "@/components/management/data-table"
import { KeyTag, ListPanel, PageHeader, Pager, PanelError, SearchBox, StatusDot, TableSkeleton } from "@/components/management/states"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { Switch } from "@/components/ui/switch"
import { FilterSelect } from "@/components/filter-select"
import { useAccess } from "@/lib/access"
import { allRoles, createUser, deleteUser, listUsers, setUserStatus } from "@/lib/admin-api"
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
                sublabel={(r) => r.description}
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
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const search = params.get("q") ?? ""
  const status = params.get("status") ?? "all"
  const page = Number(params.get("page") ?? 1)
  const creating = params.get("new") === "1"
  // { type: "enable" | "disable" | "lock" | "unlock" | "delete", user }
  const [confirm, setConfirm] = useState(null)

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
          <Th className="w-[22%]">{t("mgmt.users.name")}</Th>
          <Th className="hidden w-[15%] md:table-cell">{t("mgmt.users.username")}</Th>
          <Th className="hidden w-[15%] lg:table-cell">{t("mgmt.users.roles")}</Th>
          <Th className="hidden w-[15%] lg:table-cell">{t("mgmt.users.lastLogin")}</Th>
          <Th className="hidden w-[14%] xl:table-cell">{t("mgmt.users.createdAt")}</Th>
          <Th className="w-[14%]">{t("mgmt.common.status")}</Th>
          <Th className="w-16 text-right">{t("mgmt.common.actions")}</Th>
        </DataHead>
        <DataBody>
          {items.map((u) => (
            <DataRow key={u.uuid} onOpen={() => navigate(`/management/users/${u.uuid}`)}>
              <Td>
                <div className="flex items-center gap-3">
                  <Avatar className="size-9 rounded-md">
                    {u.pictureUrl && <AvatarImage src={u.pictureUrl} alt="" className="rounded-md" />}
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
                    <span className="flex min-w-0 items-center gap-1 text-muted-foreground">
                      <span className="truncate">{u.email}</span>
                      {u.emailVerified && (
                        <BadgeCheckIcon className="size-3.5 shrink-0 text-primary" aria-label={t("mgmt.users.emailVerified")}>
                          <title>{t("mgmt.users.emailVerified")}</title>
                        </BadgeCheckIcon>
                      )}
                    </span>
                  </div>
                </div>
              </Td>
              <Td className="hidden font-mono text-muted-foreground md:table-cell">{u.username}</Td>
              <Td className="hidden whitespace-normal lg:table-cell">
                {u.roles?.length ? (
                  <span className="flex flex-wrap gap-1">
                    {u.roles.map((r) => (
                      <KeyTag key={r} name={r} />
                    ))}
                  </span>
                ) : (
                  <span className="text-muted-foreground">–</span>
                )}
              </Td>
              <Td className="tabular hidden text-muted-foreground lg:table-cell">
                {u.lastLoginAt ? fmt.relative(u.lastLoginAt) : t("mgmt.users.neverSignedIn")}
              </Td>
              <Td className="tabular hidden text-muted-foreground xl:table-cell">
                {u.createdAt ? fmt.date(u.createdAt) : "–"}
              </Td>
              <Td>
                <StatusBadge enabled={u.enabled} locked={u.locked} />
              </Td>
              <Td>
                <div className="flex justify-end" onClick={(e) => e.stopPropagation()}>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        className={cn("text-muted-foreground", revealOnRow)}
                        aria-label={`${t("mgmt.common.actions")} ${u.name}`}
                      >
                        <EllipsisIcon />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuGroup>
                        <DropdownMenuItem onSelect={() => navigate(`/management/users/${u.uuid}`)}>
                          <EyeIcon />
                          {t("mgmt.common.open")}
                        </DropdownMenuItem>
                        {can("user:update") && (
                          <>
                            <DropdownMenuItem onSelect={() => setConfirm({ type: u.enabled ? "disable" : "enable", user: u })}>
                              <PowerIcon />
                              {u.enabled ? t("mgmt.users.deactivate") : t("mgmt.users.activate")}
                            </DropdownMenuItem>
                            <DropdownMenuItem onSelect={() => setConfirm({ type: u.locked ? "unlock" : "lock", user: u })}>
                              {u.locked ? <UnlockIcon /> : <LockIcon />}
                              {u.locked ? t("mgmt.users.unlock") : t("mgmt.users.lock")}
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuGroup>
                      {can("user:delete") && (
                        <>
                          <DropdownMenuSeparator />
                          <DropdownMenuGroup>
                            <DropdownMenuItem variant="destructive" onSelect={() => setConfirm({ type: "delete", user: u })}>
                              <Trash2Icon />
                              {t("mgmt.common.delete")}
                            </DropdownMenuItem>
                          </DropdownMenuGroup>
                        </>
                      )}
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
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
      <UserActionConfirm confirm={confirm} onClose={() => setConfirm(null)} onDone={users.reload} />
    </>
  )
}

/** The confirm step for every action in the row menu, so each one says what it does before it runs. */
function UserActionConfirm({ confirm, onClose, onDone }) {
  const { t } = useI18n()
  const type = confirm?.type
  const user = confirm?.user
  const copy = {
    enable: { icon: PowerIcon, title: t("mgmt.users.activate"), body: t("mgmt.users.statusBody"), done: t("mgmt.users.activated") },
    disable: { icon: PowerIcon, title: t("mgmt.users.deactivate"), body: t("mgmt.users.statusBody"), done: t("mgmt.users.deactivated"), danger: true },
    lock: { icon: LockIcon, title: t("mgmt.users.lock"), body: t("mgmt.users.lockBody"), done: t("mgmt.users.lockedDone"), danger: true },
    unlock: { icon: UnlockIcon, title: t("mgmt.users.unlock"), body: t("mgmt.users.lockBody"), done: t("mgmt.users.unlockedDone") },
    delete: {
      icon: Trash2Icon,
      title: t("mgmt.users.deleteTitle", { name: user?.name ?? "" }),
      body: t("mgmt.users.deleteBody"),
      done: t("mgmt.common.deleted"),
      confirmLabel: t("mgmt.common.delete"),
      danger: true,
    },
  }[type] ?? { icon: PowerIcon, title: "", body: "" }

  return (
    <ConfirmDialog
      open={!!confirm}
      onOpenChange={(v) => !v && onClose()}
      icon={copy.icon}
      destructive={!!copy.danger}
      title={copy.title}
      description={type === "delete" ? copy.body : `${user?.name ?? ""} · ${copy.body}`}
      confirmLabel={copy.confirmLabel ?? copy.title}
      onConfirm={async () => {
        if (type === "delete") await deleteUser(user.uuid)
        else await setUserStatus(user.uuid, type)
        toast.success(copy.done)
        onDone()
      }}
    />
  )
}
