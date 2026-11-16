import { useState } from "react"
import { useNavigate, useParams, useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { MinusIcon, PlusIcon, PowerIcon, Trash2Icon } from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { ItemPicker } from "@/components/management/permission-picker"
import { CardSkeleton, ErrorState } from "@/components/page-states"
import { ActiveBadge } from "@/pages/management/roles"
import { BackLink } from "@/components/management/states"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { useAccess } from "@/lib/access"
import { allPermissions, assignRolePermissions, deleteRole, getRole, revokeRolePermission, setRoleActive, updateRole } from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

const TABS = ["details", "permissions"]

function DetailsTab({ role, onSaved, onDeleted }) {
  const { t } = useI18n()
  const { can } = useAccess()
  const [name, setName] = useState(role.name)
  const [description, setDescription] = useState(role.description ?? "")
  const [error, setError] = useState(null)
  const [pending, setPending] = useState(false)
  const [confirm, setConfirm] = useState(null)
  const editable = can("role:update") && !role.system
  const dirty = name !== role.name || description !== (role.description ?? "")

  const save = async (event) => {
    event.preventDefault()
    if (!name.trim()) {
      setError(t("mgmt.common.required"))
      return
    }
    setError(null)
    setPending(true)
    try {
      onSaved(await updateRole(role.uuid, { name: name.trim(), description: description.trim(), version: role.version }))
      toast.success(t("mgmt.common.saved"))
    } catch (err) {
      toast.error(errorText(err, t))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle>{t("mgmt.roles.tabs.details")}</CardTitle>
          {role.system && <CardDescription>{t("mgmt.common.systemHint")}</CardDescription>}
        </CardHeader>
        <CardContent>
          <form id="edit-role" onSubmit={save} noValidate>
            <FieldGroup>
              <Field data-invalid={!!error || undefined}>
                <FieldLabel htmlFor="edit-role-name">{t("mgmt.roles.name")}</FieldLabel>
                <Input
                  id="edit-role-name"
                  className="font-mono"
                  value={name}
                  maxLength={50}
                  disabled={!editable}
                  aria-invalid={!!error || undefined}
                  onChange={(e) => setName(e.target.value.toUpperCase())}
                />
                {error ? <FieldError>{error}</FieldError> : <FieldDescription>{t("mgmt.roles.nameHint")}</FieldDescription>}
              </Field>
              <Field>
                <FieldLabel htmlFor="edit-role-description">{t("mgmt.roles.description")}</FieldLabel>
                <Textarea
                  id="edit-role-description"
                  value={description}
                  maxLength={255}
                  disabled={!editable}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </Field>
            </FieldGroup>
          </form>
        </CardContent>
        {editable && (
          <CardFooter className="justify-end gap-2 border-t">
            <Button
              variant="outline"
              disabled={!dirty || pending}
              onClick={() => {
                setName(role.name)
                setDescription(role.description ?? "")
              }}
            >
              {t("mgmt.common.cancel")}
            </Button>
            <Button type="submit" form="edit-role" disabled={!dirty || pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {t("mgmt.common.save")}
            </Button>
          </CardFooter>
        )}
      </Card>

      <div className="flex flex-col gap-6">
        <Card>
          <CardHeader>
            <CardTitle>{t("mgmt.common.status")}</CardTitle>
            <CardDescription>{t("mgmt.roles.statusBody")}</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-start gap-4">
            <ActiveBadge active={role.active} />
            {can("role:update") && (
              <Button variant="outline" onClick={() => setConfirm("status")}>
                <PowerIcon data-icon="inline-start" />
                {role.active ? t("mgmt.roles.deactivate") : t("mgmt.roles.activate")}
              </Button>
            )}
          </CardContent>
        </Card>
        {can("role:delete") && !role.system && (
          <Card className="ring-destructive/25">
            <CardHeader>
              <CardTitle>{t("mgmt.common.delete")}</CardTitle>
              <CardDescription>{t("mgmt.roles.deleteBody")}</CardDescription>
            </CardHeader>
            <CardContent>
              <Button variant="destructive" onClick={() => setConfirm("delete")}>
                <Trash2Icon data-icon="inline-start" />
                {t("mgmt.common.delete")}
              </Button>
            </CardContent>
          </Card>
        )}
      </div>

      <ConfirmDialog
        open={confirm === "status"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={PowerIcon}
        destructive={role.active}
        title={role.active ? t("mgmt.roles.deactivate") : t("mgmt.roles.activate")}
        description={t("mgmt.roles.statusBody")}
        confirmLabel={role.active ? t("mgmt.roles.deactivate") : t("mgmt.roles.activate")}
        onConfirm={async () => {
          await setRoleActive(role.uuid, !role.active)
          toast.success(t("mgmt.common.saved"))
          onSaved({ ...role, active: !role.active })
        }}
      />
      <ConfirmDialog
        open={confirm === "delete"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={Trash2Icon}
        destructive
        title={t("mgmt.roles.deleteTitle", { name: role.name })}
        description={t("mgmt.roles.deleteBody")}
        confirmLabel={t("mgmt.common.delete")}
        onConfirm={async () => {
          await deleteRole(role.uuid)
          toast.success(t("mgmt.common.deleted"))
          onDeleted()
        }}
      />
    </div>
  )
}

function PermissionsDialog({ role, mode, onOpenChange }) {
  const { t } = useI18n()
  const [selected, setSelected] = useState(new Set())
  const [pending, setPending] = useState(false)
  const catalog = useAsync(() => (mode ? allPermissions() : Promise.resolve([])), [mode])
  const adding = mode === "add"

  const submit = async () => {
    setPending(true)
    try {
      if (adding) {
        await assignRolePermissions(role.uuid, { permissionUuids: [...selected] })
      } else {
        for (const uuid of selected) await revokeRolePermission(role.uuid, uuid)
      }
      toast.success(adding ? t("mgmt.roles.added") : t("mgmt.roles.removed"))
      setSelected(new Set())
      onOpenChange(false)
    } catch (err) {
      toast.error(errorText(err, t))
    } finally {
      setPending(false)
    }
  }

  return (
    <Dialog open={!!mode} onOpenChange={(v) => !pending && onOpenChange(v)}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{adding ? t("mgmt.roles.assignTitle") : t("mgmt.roles.removeTitle")}</DialogTitle>
          <DialogDescription>{adding ? t("mgmt.roles.assignBody") : t("mgmt.roles.removeBody")}</DialogDescription>
        </DialogHeader>
        <ItemPicker
          items={catalog.data}
          loading={catalog.status === "loading"}
          selected={selected}
          onChange={setSelected}
          label={(p) => p.name}
          sublabel={(p) => p.description}
          groupOf={(p) => p.resource}
        />
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            {t("mgmt.common.cancel")}
          </Button>
          <Button variant={adding ? "default" : "destructive"} onClick={submit} disabled={pending || selected.size === 0}>
            {pending && <Spinner data-icon="inline-start" />}
            {adding ? t("mgmt.roles.assignTitle") : t("mgmt.roles.removeTitle")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default function RoleDetailPage() {
  const { id } = useParams()
  const { t } = useI18n()
  const { can } = useAccess()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const [mode, setMode] = useState(null)
  const tab = TABS.includes(params.get("tab")) ? params.get("tab") : "details"
  const role = useAsync(() => getRole(id), [id])

  if (role.status === "loading") return <CardSkeleton rows={4} />
  if (role.status === "error") return <ErrorState onRetry={role.reload} />
  const r = role.data

  return (
    <>
      <BackLink to="/management/roles">{t("mgmt.roles.title")}</BackLink>
      <div className="flex flex-col gap-2">
        <div className="flex flex-wrap items-center gap-x-4 gap-y-1">
          <h1 className="font-mono text-3xl leading-tight font-medium tracking-tight">{r.name}</h1>
          <ActiveBadge active={r.active} />
          {r.system && <span className="text-muted-foreground">{t("mgmt.common.system")}</span>}
        </div>
        {r.description && <p className="max-w-[62ch] text-muted-foreground">{r.description}</p>}
      </div>

      <Tabs value={tab} onValueChange={(v) => setParams({ tab: v }, { replace: true })} className="gap-6">
        <TabsList variant="line">
          {TABS.map((k) => (
            <TabsTrigger key={k} value={k}>{t(`mgmt.roles.tabs.${k}`)}</TabsTrigger>
          ))}
        </TabsList>
        <TabsContent value="details">
          <DetailsTab
            key={r.version}
            role={r}
            onSaved={(next) => role.setData({ ...r, ...next })}
            onDeleted={() => navigate("/management/roles", { replace: true })}
          />
        </TabsContent>
        <TabsContent value="permissions">
          <Card>
            <CardHeader>
              <CardTitle>{t("mgmt.roles.permissions")}</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              <Alert>
                <AlertDescription>{t("mgmt.roles.currentUnknown")}</AlertDescription>
              </Alert>
              {can("role:assign") && (
                <div className="flex flex-wrap gap-2">
                  <Button onClick={() => setMode("add")}>
                    <PlusIcon data-icon="inline-start" />
                    {t("mgmt.roles.assignTitle")}
                  </Button>
                  <Button variant="outline" onClick={() => setMode("remove")}>
                    <MinusIcon data-icon="inline-start" />
                    {t("mgmt.roles.removeTitle")}
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
          <PermissionsDialog role={r} mode={mode} onOpenChange={(v) => !v && setMode(null)} />
        </TabsContent>
      </Tabs>
    </>
  )
}
