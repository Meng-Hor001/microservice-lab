import { useCallback, useState } from "react"
import { Link, useNavigate, useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { EllipsisIcon, EyeIcon, PencilIcon, PlusIcon, PowerIcon, ShieldCheckIcon, Trash2Icon } from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { ItemPicker } from "@/components/management/permission-picker"
import { DataBody, DataHead, DataRow, DataTable, revealOnRow, rowLink, Td, Th } from "@/components/management/data-table"
import { KeyTag, ListPanel, PageHeader, Pager, PanelError, SearchBox, StatusDot, TableSkeleton } from "@/components/management/states"
import { EmptyState } from "@/components/page-states"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { Textarea } from "@/components/ui/textarea"
import { FilterSelect } from "@/components/filter-select"
import { useAccess } from "@/lib/access"
import { allPermissions, createRole, deleteRole, listRoles, setRoleActive } from "@/lib/admin-api"
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
              {error && <FieldError>{error}</FieldError>}
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
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const search = params.get("q") ?? ""
  const status = params.get("status") ?? "all"
  const page = Number(params.get("page") ?? 1)
  const [deleting, setDeleting] = useState(null)
  const [toggling, setToggling] = useState(null)

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
          <Th className="w-[19%]">{t("mgmt.roles.name")}</Th>
          <Th className="hidden w-[19%] lg:table-cell">{t("mgmt.roles.permissions")}</Th>
          <Th className="hidden w-[19%] lg:table-cell">{t("mgmt.roles.users")}</Th>
          <Th className="hidden w-[19%] md:table-cell">{t("mgmt.roles.updatedAt")}</Th>
          <Th className="w-[19%]">{t("mgmt.common.status")}</Th>
          <Th className="w-16 text-right">{t("mgmt.common.actions")}</Th>
        </DataHead>
        <DataBody>
          {items.map((r) => (
            <DataRow key={r.uuid} onOpen={() => navigate(`/management/roles/${r.uuid}`)}>
              <Td>
                <div className="flex min-w-0 flex-col items-start gap-1">
                  <span className="flex items-center gap-2">
                    <Link
                      to={`/management/roles/${r.uuid}`}
                      className={cn("inline-flex", rowLink)}
                      onClick={(e) => e.stopPropagation()}
                    >
                      <KeyTag name={r.name} />
                    </Link>
                    {r.system && <span className="text-muted-foreground">{t("mgmt.common.system")}</span>}
                  </span>
                  {r.description && <span className="max-w-xl truncate text-muted-foreground">{r.description}</span>}
                </div>
              </Td>
              <Td className="tabular hidden lg:table-cell">{r.permissionCount ?? "–"}</Td>
              <Td className="tabular hidden lg:table-cell">{r.userCount ?? "–"}</Td>
              <Td className="hidden md:table-cell">
                <div className="flex flex-col text-muted-foreground">
                  <span className="tabular">{r.updatedAt ? fmt.relative(r.updatedAt) : "–"}</span>
                  {r.updatedBy && <span className="truncate text-xs">{t("mgmt.roles.updatedBy", { name: r.updatedBy })}</span>}
                </div>
              </Td>
              <Td>
                <ActiveBadge active={r.active} />
              </Td>
              <Td>
                <div className="flex justify-end" onClick={(e) => e.stopPropagation()}>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        className={cn("text-muted-foreground", revealOnRow)}
                        aria-label={`${t("mgmt.common.actions")} ${r.name}`}
                      >
                        <EllipsisIcon />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuGroup>
                        <DropdownMenuItem onSelect={() => navigate(`/management/roles/${r.uuid}`)}>
                          <EyeIcon />
                          {t("mgmt.common.open")}
                        </DropdownMenuItem>
                        {can("role:update") && (
                          <>
                            {/* The backend refuses any change to a system role, so these are greyed out for them. */}
                            <DropdownMenuItem disabled={r.system} onSelect={() => navigate(`/management/roles/${r.uuid}`)}>
                              <PencilIcon />
                              {t("mgmt.common.edit")}
                            </DropdownMenuItem>
                            <DropdownMenuItem disabled={r.system} onSelect={() => setToggling(r)}>
                              <PowerIcon />
                              {r.active ? t("mgmt.roles.deactivate") : t("mgmt.roles.activate")}
                            </DropdownMenuItem>
                          </>
                        )}
                      </DropdownMenuGroup>
                      {can("role:delete") && !r.system && (
                        <>
                          <DropdownMenuSeparator />
                          <DropdownMenuGroup>
                            <DropdownMenuItem variant="destructive" onSelect={() => setDeleting(r)}>
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
            {roles.status === "refreshing" && <Spinner className="text-muted-foreground" />}
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
        open={!!toggling}
        onOpenChange={(v) => !v && setToggling(null)}
        icon={PowerIcon}
        destructive={!!toggling?.active}
        title={toggling?.active ? t("mgmt.roles.deactivate") : t("mgmt.roles.activate")}
        description={t("mgmt.roles.statusBody")}
        confirmLabel={toggling?.active ? t("mgmt.roles.deactivate") : t("mgmt.roles.activate")}
        onConfirm={async () => {
          await setRoleActive(toggling.uuid, !toggling.active)
          toast.success(t("mgmt.common.saved"))
          roles.reload()
        }}
      />
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
