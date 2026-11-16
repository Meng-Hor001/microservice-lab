import { useCallback, useState } from "react"
import { useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { KeyRoundIcon, PencilIcon, PlusIcon, PowerIcon, Trash2Icon } from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { DataBody, DataHead, DataRow, DataTable, revealOnRow, Td, Th } from "@/components/management/data-table"
import { KeyTag, ListPanel, PageHeader, Pager, PanelError, SearchBox, TableSkeleton } from "@/components/management/states"
import { EmptyState } from "@/components/page-states"
import { ActiveBadge } from "@/pages/management/roles"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { Textarea } from "@/components/ui/textarea"
import { EllipsisIcon } from "lucide-react"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useAccess } from "@/lib/access"
import {
  allPermissions,
  createPermission,
  deletePermission,
  listPermissions,
  setPermissionActive,
  updatePermission,
} from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const PAGE_SIZE = 15
const SLUG = /^[a-z][a-z0-9_-]*$/

function PermissionDialog({ open, permission, onOpenChange, onSaved }) {
  const { t } = useI18n()
  const editing = !!permission
  const [form, setForm] = useState({
    resource: permission?.resource ?? "",
    action: permission?.action ?? "",
    description: permission?.description ?? "",
  })
  const [errors, setErrors] = useState({})
  const [pending, setPending] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value.toLowerCase().replace(/\s+/g, "") }))

  const submit = async (event) => {
    event.preventDefault()
    const e = {}
    if (!SLUG.test(form.resource)) e.resource = t("mgmt.common.required")
    if (!SLUG.test(form.action)) e.action = t("mgmt.common.required")
    setErrors(e)
    if (Object.keys(e).length) return
    setPending(true)
    try {
      const body = { resource: form.resource, action: form.action, description: form.description.trim() }
      if (editing) await updatePermission(permission.uuid, { ...body, version: permission.version })
      else await createPermission(body)
      toast.success(editing ? t("mgmt.common.saved") : t("mgmt.permissions.created"))
      onOpenChange(false)
      onSaved()
    } catch (err) {
      toast.error(errorText(err, t))
    } finally {
      setPending(false)
    }
  }

  const code = form.resource && form.action ? `${form.resource}:${form.action}` : null

  return (
    <Dialog open={open} onOpenChange={(v) => !pending && onOpenChange(v)}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{editing ? t("mgmt.permissions.editTitle") : t("mgmt.permissions.create")}</DialogTitle>
          <DialogDescription>{t("mgmt.permissions.intro")}</DialogDescription>
        </DialogHeader>
        <form id="permission-form" onSubmit={submit} noValidate>
          <FieldGroup>
            <div className="grid gap-4 sm:grid-cols-2">
              {["resource", "action"].map((k) => (
                <Field key={k} data-invalid={!!errors[k] || undefined}>
                  <FieldLabel htmlFor={`perm-${k}`}>{t(`mgmt.permissions.${k}`)}</FieldLabel>
                  <Input
                    id={`perm-${k}`}
                    className="font-mono"
                    maxLength={50}
                    placeholder={k === "resource" ? "user" : "read"}
                    value={form[k]}
                    aria-invalid={!!errors[k] || undefined}
                    onChange={set(k)}
                  />
                  {errors[k] && <FieldError>{errors[k]}</FieldError>}
                </Field>
              ))}
            </div>
            {code && (
              <FieldDescription>
                {t("mgmt.permissions.codePreview", { code: "" })}
                <KeyTag name={code} className="ml-1" />
              </FieldDescription>
            )}
            <Field>
              <FieldLabel htmlFor="perm-description">{t("mgmt.permissions.description")}</FieldLabel>
              <Textarea
                id="perm-description"
                maxLength={255}
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              />
            </Field>
          </FieldGroup>
        </form>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            {t("mgmt.common.cancel")}
          </Button>
          <Button type="submit" form="permission-form" disabled={pending}>
            {pending && <Spinner data-icon="inline-start" />}
            {editing ? t("mgmt.common.save") : t("mgmt.permissions.create")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default function PermissionsPage() {
  const { t } = useI18n()
  const { can } = useAccess()
  const [params, setParams] = useSearchParams()
  const search = params.get("q") ?? ""
  const resource = params.get("resource") ?? ""
  const page = Number(params.get("page") ?? 1)
  const [editing, setEditing] = useState(null)
  const [confirm, setConfirm] = useState(null)

  const update = useCallback(
    (patch) =>
      setParams((p) => {
        const next = new URLSearchParams(p)
        for (const [k, v] of Object.entries(patch)) {
          if (v === null || v === "" || (k === "page" && v === 1)) next.delete(k)
          else next.set(k, String(v))
        }
        return next
      }),
    [setParams]
  )

  const perms = useAsync(
    () => listPermissions({ page, size: PAGE_SIZE, search: search || undefined, resource: resource || undefined }),
    [page, search, resource]
  )
  const creating = params.get("new") === "1"

  const filtered = !!search || !!resource
  // Every resource name, for the filter dropdown (independent of the current page and search).
  const catalog = useAsync(() => allPermissions(), [perms.data?.total])
  const resources = [...new Set((catalog.data ?? []).map((p) => p.resource))].sort()

  const body = () => {
    if (perms.status === "loading") return <TableSkeleton rows={8} />
    if (perms.status === "error") return <PanelError onRetry={perms.reload} />
    const { items } = perms.data
    if (items.length === 0) {
      return (
        <EmptyState
          icon={KeyRoundIcon}
          title={filtered ? t("mgmt.common.noResults") : t("mgmt.permissions.empty")}
          description={filtered ? t("mgmt.common.noResultsBody") : null}
        >
          {filtered && (
            <Button variant="outline" onClick={() => update({ q: null, resource: null, page: 1 })}>
              {t("mgmt.common.clearFilters")}
            </Button>
          )}
        </EmptyState>
      )
    }
    return (
      <DataTable>
        <DataHead>
          <Th>{t("mgmt.permissions.code")}</Th>
          <Th className="w-40">{t("mgmt.common.status")}</Th>
          <Th className="w-12">
            <span className="sr-only">{t("mgmt.common.actions")}</span>
          </Th>
        </DataHead>
        <DataBody>
          {items.map((p) => (
            <DataRow key={p.uuid}>
              <Td className="max-w-0 whitespace-normal">
                <div className="flex min-w-0 flex-col items-start gap-1 py-1">
                  <div className="flex items-center gap-3">
                    <KeyTag name={p.name} />
                    {p.system && <span className="text-muted-foreground">{t("mgmt.common.system")}</span>}
                  </div>
                  {p.description && p.description.trim().toLowerCase() !== `${p.action} ${p.resource}` && (
                    <span className="w-full truncate text-muted-foreground">{p.description}</span>
                  )}
                </div>
              </Td>
              <Td>
                <ActiveBadge active={p.active} />
              </Td>
              <Td className="text-right">
                {(can("permission:update") || can("permission:delete")) && (
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        className={cn("text-muted-foreground", revealOnRow)}
                        aria-label={`${t("mgmt.common.actions")} ${p.name}`}
                      >
                        <EllipsisIcon />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      {can("permission:update") && (
                        <DropdownMenuGroup>
                          <DropdownMenuItem disabled={p.system} onSelect={() => setEditing(p)}>
                            <PencilIcon />
                            {t("mgmt.common.edit")}
                          </DropdownMenuItem>
                          <DropdownMenuItem onSelect={() => setConfirm({ type: "status", p })}>
                            <PowerIcon />
                            {p.active ? t("mgmt.permissions.deactivate") : t("mgmt.permissions.activate")}
                          </DropdownMenuItem>
                        </DropdownMenuGroup>
                      )}
                      {can("permission:delete") && !p.system && (
                        <>
                          <DropdownMenuSeparator />
                          <DropdownMenuGroup>
                            <DropdownMenuItem variant="destructive" onSelect={() => setConfirm({ type: "delete", p })}>
                              <Trash2Icon />
                              {t("mgmt.common.delete")}
                            </DropdownMenuItem>
                          </DropdownMenuGroup>
                        </>
                      )}
                    </DropdownMenuContent>
                  </DropdownMenu>
                )}
              </Td>
            </DataRow>
          ))}
        </DataBody>
      </DataTable>
    )
  }

  return (
    <>
      <PageHeader title={t("mgmt.permissions.title")} intro={t("mgmt.permissions.intro")}>
        {can("permission:create") && (
          <Button onClick={() => update({ new: 1 })}>
            <PlusIcon data-icon="inline-start" />
            {t("mgmt.permissions.create")}
          </Button>
        )}
      </PageHeader>

      <ListPanel
        toolbar={
          <>
            <SearchBox className="sm:max-w-sm" value={search} onChange={(q) => update({ q, page: 1 })} />
            {perms.status === "refreshing" && <Spinner className="text-muted-foreground" />}
            <Select value={resource || "all"} onValueChange={(v) => update({ resource: v === "all" ? null : v, page: 1 })}>
              <SelectTrigger className="w-full sm:ml-auto sm:w-48" aria-label={t("mgmt.permissions.resourceFilter")}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="all">{t("mgmt.permissions.allResources")}</SelectItem>
                  {resources.map((r) => (
                    <SelectItem key={r} value={r} className="font-mono">
                      {r}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </>
        }
        footer={
          perms.data?.items?.length > 0 && perms.status !== "loading" ? (
            <Pager page={page} pages={perms.data.pages} total={perms.data.total} size={PAGE_SIZE} onPage={(n) => update({ page: n })} />
          ) : null
        }
      >
        {body()}
      </ListPanel>

      {(creating || editing) && (
        <PermissionDialog
          key={editing?.uuid ?? "new"}
          open
          permission={editing}
          onOpenChange={(v) => {
            if (v) return
            setEditing(null)
            update({ new: null })
          }}
          onSaved={perms.reload}
        />
      )}
      <ConfirmDialog
        open={confirm?.type === "status"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={PowerIcon}
        destructive={confirm?.p.active}
        title={confirm?.p.active ? t("mgmt.permissions.deactivate") : t("mgmt.permissions.activate")}
        description={confirm?.p.name}
        confirmLabel={confirm?.p.active ? t("mgmt.permissions.deactivate") : t("mgmt.permissions.activate")}
        onConfirm={async () => {
          await setPermissionActive(confirm.p.uuid, !confirm.p.active)
          toast.success(t("mgmt.common.saved"))
          perms.reload()
        }}
      />
      <ConfirmDialog
        open={confirm?.type === "delete"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={Trash2Icon}
        destructive
        title={t("mgmt.permissions.deleteTitle", { name: confirm?.p.name ?? "" })}
        description={t("mgmt.permissions.deleteBody")}
        confirmLabel={t("mgmt.common.delete")}
        onConfirm={async () => {
          await deletePermission(confirm.p.uuid)
          toast.success(t("mgmt.common.deleted"))
          perms.reload()
        }}
      />
    </>
  )
}
