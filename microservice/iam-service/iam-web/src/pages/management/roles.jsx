import { useCallback, useState } from "react"
import { Link, useNavigate, useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { PlusIcon, ShieldCheckIcon, Trash2Icon } from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { ItemPicker } from "@/components/management/permission-picker"
import { DataBody, DataHead, DataRow, DataTable, revealOnRow, RowChevron, rowLink, Td, Th } from "@/components/management/data-table"
import { KeyTag, ListPanel, PageHeader, Pager, PanelError, SearchBox, StatusDot, TableSkeleton } from "@/components/management/states"
import { EmptyState } from "@/components/page-states"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { Textarea } from "@/components/ui/textarea"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { useAccess } from "@/lib/access"
import { allPermissions, createRole, deleteRole, listRoles } from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const PAGE_SIZE = 10

export function ActiveBadge({ active }) {
  const { t } = useI18n()
  return <StatusDot on={active} label={active ? t("mgmt.common.active") : t("mgmt.common.inactive")} />
}

function CreateRoleDialog({ open, onOpenChange, onCreated }) {
  const { t } = useI18n()
  const [name, setName] = useState("")
  const [description, setDescription] = useState("")
  const [selected, setSelected] = useState(new Set())
  const [error, setError] = useState(null)
  const [pending, setPending] = useState(false)
  const catalog = useAsync(() => (open ? allPermissions() : Promise.resolve([])), [open])

  const submit = async (event) => {
    event.preventDefault()
    if (!name.trim()) {
      setError(t("mgmt.common.required"))
      return
    }
    setError(null)
    setPending(true)
    try {
      const role = await createRole({ name: name.trim(), description: description.trim(), permissionUuids: [...selected] })
      toast.success(t("mgmt.roles.created"))
      setName("")
      setDescription("")
      setSelected(new Set())
      onOpenChange(false)
      onCreated(role)
    } catch (err) {
      toast.error(errorText(err, t))
    } finally {
      setPending(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => !pending && onOpenChange(v)}>
      <DialogContent className="max-h-[90svh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{t("mgmt.roles.create")}</DialogTitle>
          <DialogDescription>{t("mgmt.roles.intro")}</DialogDescription>
        </DialogHeader>
        <form id="create-role" onSubmit={submit} noValidate>
          <FieldGroup>
            <Field data-invalid={!!error || undefined}>
              <FieldLabel htmlFor="role-name">{t("mgmt.roles.name")}</FieldLabel>
              <Input
                id="role-name"
                value={name}
                maxLength={50}
                className="font-mono"
                aria-invalid={!!error || undefined}
                onChange={(e) => setName(e.target.value.toUpperCase())}
              />
              {error ? <FieldError>{error}</FieldError> : <FieldDescription>{t("mgmt.roles.nameHint")}</FieldDescription>}
            </Field>
            <Field>
              <FieldLabel htmlFor="role-description">{t("mgmt.roles.description")}</FieldLabel>
              <Textarea id="role-description" value={description} maxLength={255} onChange={(e) => setDescription(e.target.value)} />
            </Field>
            <Field>
              <FieldLabel>{t("mgmt.roles.permissions")}</FieldLabel>
              <ItemPicker
                items={catalog.data}
                loading={catalog.status === "loading"}
                selected={selected}
                onChange={setSelected}
                label={(p) => p.name}
                sublabel={(p) => p.description}
                groupOf={(p) => p.resource}
              />
            </Field>
          </FieldGroup>
        </form>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            {t("mgmt.common.cancel")}
          </Button>
          <Button type="submit" form="create-role" disabled={pending}>
            {pending && <Spinner data-icon="inline-start" />}
            {t("mgmt.roles.create")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default function RolesPage() {
  const { t } = useI18n()
  const { can } = useAccess()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const search = params.get("q") ?? ""
  const status = params.get("status") ?? "all"
  const page = Number(params.get("page") ?? 1)
  const [deleting, setDeleting] = useState(null)

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

  const roles = useAsync(
    () =>
      listRoles({
        page,
        size: PAGE_SIZE,
        search: search || undefined,
        active: status === "active" ? true : status === "inactive" ? false : undefined,
      }),
    [page, search, status]
  )

  const filtered = !!search || status !== "all"

  const body = () => {
    if (roles.status === "loading") return <TableSkeleton rows={5} />
    if (roles.status === "error") return <PanelError onRetry={roles.reload} />
    const { items } = roles.data
    if (items.length === 0) {
      return (
        <EmptyState
          icon={ShieldCheckIcon}
          title={filtered ? t("mgmt.common.noResults") : t("mgmt.roles.empty")}
          description={filtered ? t("mgmt.common.noResultsBody") : null}
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
          <Th>{t("mgmt.roles.name")}</Th>
          <Th className="w-40">{t("mgmt.common.status")}</Th>
          <Th className="w-28">
            <span className="sr-only">{t("mgmt.common.actions")}</span>
          </Th>
        </DataHead>
        <DataBody>
          {items.map((r) => (
            <DataRow key={r.uuid} onOpen={() => navigate(`/management/roles/${r.uuid}`)}>
              <Td>
                <Link
                  to={`/management/roles/${r.uuid}`}
                  className={cn("inline-flex", rowLink)}
                  onClick={(e) => e.stopPropagation()}
                >
                  <KeyTag name={r.name} />
                </Link>
              </Td>
              <Td>
                <ActiveBadge active={r.active} />
              </Td>
              <Td>
                <div className="flex items-center justify-end gap-1" onClick={(e) => e.stopPropagation()}>
                  {can("role:delete") && (
                    <Button
                      variant="ghost"
                      size="icon-sm"
                      className={cn("text-muted-foreground hover:text-destructive", revealOnRow)}
                      aria-label={`${t("mgmt.common.delete")} ${r.name}`}
                      onClick={() => setDeleting(r)}
                    >
                      <Trash2Icon />
                    </Button>
                  )}
                  <RowChevron />
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
      <PageHeader title={t("mgmt.roles.title")} intro={t("mgmt.roles.intro")}>
        {can("role:create") && (
          <Button onClick={() => update({ new: 1 })}>
            <PlusIcon data-icon="inline-start" />
            {t("mgmt.roles.create")}
          </Button>
        )}
      </PageHeader>

      <ListPanel
        toolbar={
          <>
            <SearchBox className="sm:max-w-sm" value={search} onChange={(q) => update({ q, page: 1 })} />
            <ToggleGroup
              type="single"
              variant="outline"
              spacing={0}
              value={status}
              onValueChange={(v) => v && update({ status: v, page: 1 })}
              aria-label={t("mgmt.common.status")}
            >
              <ToggleGroupItem value="all">{t("mgmt.common.all")}</ToggleGroupItem>
              <ToggleGroupItem value="active">{t("mgmt.common.active")}</ToggleGroupItem>
              <ToggleGroupItem value="inactive">{t("mgmt.common.inactive")}</ToggleGroupItem>
            </ToggleGroup>
            {roles.status === "refreshing" && <Spinner className="text-muted-foreground" />}
          </>
        }
        footer={
          roles.data?.items?.length > 0 && roles.status !== "loading" ? (
            <Pager page={page} pages={roles.data.pages} total={roles.data.total} size={PAGE_SIZE} onPage={(p) => update({ page: p })} />
          ) : null
        }
      >
        {body()}
      </ListPanel>

      {can("role:create") && (
        <CreateRoleDialog
          open={params.get("new") === "1"}
          onOpenChange={(v) => update({ new: v ? 1 : null })}
          onCreated={(role) => navigate(`/management/roles/${role.uuid}`)}
        />
      )}
      <ConfirmDialog
        open={!!deleting}
        onOpenChange={(v) => !v && setDeleting(null)}
        icon={Trash2Icon}
        destructive
        title={t("mgmt.roles.deleteTitle", { name: deleting?.name ?? "" })}
        description={t("mgmt.roles.deleteBody")}
        confirmLabel={t("mgmt.common.delete")}
        onConfirm={async () => {
          await deleteRole(deleting.uuid)
          toast.success(t("mgmt.common.deleted"))
          roles.reload()
        }}
      />
    </>
  )
}
