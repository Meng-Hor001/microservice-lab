import { useMemo, useState } from "react"
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import {
  KeyRoundIcon,
  LockIcon,
  PlusIcon,
  PowerIcon,
  Trash2Icon,
  UnlinkIcon,
  UnlockIcon,
  XIcon,
} from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { ItemPicker } from "@/components/management/permission-picker"
import { CardSkeleton, ErrorState } from "@/components/page-states"
import { ProviderLogo } from "@/components/provider-logo"
import { StatusBadge } from "@/pages/management/users"
import { BackLink, KeyTag, StatusDot } from "@/components/management/states"
import { UserSessionsTab, UserSignInsTab } from "@/components/management/user-security"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { useAccess } from "@/lib/access"
import {
  allPermissions,
  allRoles,
  assignUserRoles,
  deleteUser,
  getUser,
  getUserAuthorities,
  getUserIdentities,
  grantUserPermissions,
  resetUserPassword,
  revokeUserPermission,
  revokeUserRole,
  setUserStatus,
  unlinkUserIdentity,
  updateUser,
} from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { usePasswordRules } from "@/lib/password"
import { initials, PROVIDERS } from "@/lib/providers"
import { useAsync } from "@/lib/use-async"

const E164 = /^\+[1-9]\d{1,14}$/
const TABS = ["details", "credentials", "roles", "permissions", "sessions", "history"]

/* ---------- shared dialog for picking roles or permissions ---------- */

function AssignDialog({ open, onOpenChange, title, description, items, loading, exclude, label, sublabel, groupOf, withEffect, onSubmit }) {
  const { t } = useI18n()
  const [selected, setSelected] = useState(new Set())
  const [reason, setReason] = useState("")
  const [expires, setExpires] = useState("")
  const [effect, setEffect] = useState("GRANT")
  const [pending, setPending] = useState(false)
  const [tomorrow] = useState(() => new Date(Date.now() + 86400000).toISOString().slice(0, 10))

  const submit = async () => {
    setPending(true)
    try {
      await onSubmit({
        uuids: [...selected],
        reason: reason.trim() || undefined,
        expiresAt: expires ? new Date(`${expires}T23:59:59`).toISOString() : undefined,
        effect,
      })
      setSelected(new Set())
      setReason("")
      setExpires("")
      onOpenChange(false)
    } catch (error) {
      toast.error(errorText(error, t))
    } finally {
      setPending(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => !pending && onOpenChange(v)}>
      <DialogContent className="max-h-[90svh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          {description && <DialogDescription>{description}</DialogDescription>}
        </DialogHeader>
        <FieldGroup>
          {withEffect && (
            <Field>
              <FieldLabel>{t("mgmt.users.effect")}</FieldLabel>
              <ToggleGroup type="single" variant="outline" spacing={0} value={effect} onValueChange={(v) => v && setEffect(v)}>
                <ToggleGroupItem value="GRANT">{t("mgmt.users.allow")}</ToggleGroupItem>
                <ToggleGroupItem value="DENY">{t("mgmt.users.deny")}</ToggleGroupItem>
              </ToggleGroup>
            </Field>
          )}
          <ItemPicker
            items={items}
            loading={loading}
            selected={selected}
            onChange={setSelected}
            exclude={exclude}
            label={label}
            sublabel={sublabel}
            groupOf={groupOf}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Field>
              <FieldLabel htmlFor="assign-reason">
                {t("mgmt.common.reason")} <span className="font-normal text-muted-foreground">({t("mgmt.common.optional")})</span>
              </FieldLabel>
              <Input id="assign-reason" value={reason} maxLength={255} onChange={(e) => setReason(e.target.value)} />
            </Field>
            <Field>
              <FieldLabel htmlFor="assign-expires">{t("mgmt.users.expiresAt")}</FieldLabel>
              <Input
                id="assign-expires"
                type="date"
                min={tomorrow}
                value={expires}
                onChange={(e) => setExpires(e.target.value)}
              />
              <FieldDescription>{t("mgmt.users.expiresHint")}</FieldDescription>
            </Field>
          </div>
        </FieldGroup>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            {t("mgmt.common.cancel")}
          </Button>
          <Button onClick={submit} disabled={pending || selected.size === 0}>
            {pending && <Spinner data-icon="inline-start" />}
            {title}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

/* ---------- Details tab ---------- */

function DetailsTab({ user, onSaved, onDeleted }) {
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const [first, ...rest] = (user.name ?? "").replace(/\bnull\b/g, "").trim().split(/\s+/)
  const initial = {
    givenName: first ?? "",
    familyName: rest.join(" "),
    username: user.username ?? "",
    email: user.email ?? "",
    phoneNumber: user.phoneNumber ?? "",
    pictureUrl: user.pictureUrl ?? "",
  }
  const [form, setForm] = useState(initial)
  const [errors, setErrors] = useState({})
  const [pending, setPending] = useState(false)
  const [confirm, setConfirm] = useState(null)
  const editable = can("user:update")
  const dirty = Object.keys(initial).some((k) => initial[k] !== form[k])

  const save = async (event) => {
    event.preventDefault()
    const e = {}
    if (!form.givenName.trim()) e.givenName = t("mgmt.common.required")
    if (!/^[A-Za-z0-9._-]{3,50}$/.test(form.username)) e.username = t("validation.usernameFormat")
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = t("validation.emailFormat")
    if (form.phoneNumber && !E164.test(form.phoneNumber)) e.phoneNumber = t("validation.phoneFormat")
    setErrors(e)
    if (Object.keys(e).length) return
    setPending(true)
    try {
      const body = { ...form }
      if (!body.familyName.trim()) delete body.familyName
      onSaved(await updateUser(user.uuid, body))
      toast.success(t("mgmt.common.saved"))
    } catch (error) {
      toast.error(errorText(error, t))
    } finally {
      setPending(false)
    }
  }

  const input = (key, label, props = {}) => (
    <Field data-invalid={!!errors[key] || undefined}>
      <FieldLabel htmlFor={`edit-${key}`}>{label}</FieldLabel>
      <Input
        id={`edit-${key}`}
        value={form[key]}
        disabled={!editable}
        aria-invalid={!!errors[key] || undefined}
        onChange={(e) => setForm((f) => ({ ...f, [key]: e.target.value }))}
        {...props}
      />
      {errors[key] && <FieldError>{errors[key]}</FieldError>}
    </Field>
  )

  return (
    <div className="grid gap-6 lg:grid-cols-3">
      <Card className="lg:col-span-2">
        <CardHeader>
          <CardTitle>{t("mgmt.users.detailsTitle")}</CardTitle>
          <CardDescription className="tabular flex flex-wrap gap-x-6 gap-y-1">
            <span>
              {t("mgmt.users.createdAt")} {user.createdAt ? fmt.dateTime(user.createdAt) : "–"}
            </span>
            <span>
              {t("mgmt.users.updatedAt")} {user.updatedAt ? fmt.relative(user.updatedAt) : "–"}
            </span>
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form id="edit-user" onSubmit={save} noValidate>
            <FieldGroup>
              <div className="grid gap-4 sm:grid-cols-2">
                {input("givenName", t("mgmt.users.firstName"))}
                {input("familyName", t("mgmt.users.lastName"))}
                {input("username", t("mgmt.users.username"))}
                {input("email", t("mgmt.users.email"), { type: "email" })}
                {input("phoneNumber", t("mgmt.users.phone"), { type: "tel", placeholder: "+855…" })}
                {input("pictureUrl", t("mgmt.users.picture"), { type: "url", placeholder: "https://" })}
              </div>
            </FieldGroup>
          </form>
        </CardContent>
        {editable && (
          <CardFooter className="justify-end gap-2 border-t">
            <Button variant="outline" disabled={!dirty || pending} onClick={() => { setForm(initial); setErrors({}) }}>
              {t("mgmt.common.cancel")}
            </Button>
            <Button type="submit" form="edit-user" disabled={!dirty || pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {t("mgmt.common.save")}
            </Button>
          </CardFooter>
        )}
      </Card>

      <div className="flex flex-col gap-6">
        <Card>
          <CardHeader>
            <CardTitle>{t("mgmt.users.statusTitle")}</CardTitle>
            <CardDescription>{t("mgmt.users.statusBody")}</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-start gap-4">
            <div className="flex flex-col gap-2">
              <StatusBadge enabled={user.enabled} />
              <StatusDot
                on={user.emailVerified}
                label={user.emailVerified ? t("mgmt.users.emailVerified") : t("mgmt.users.emailUnverified")}
              />
            </div>
            {editable && (
              <Button variant="outline" onClick={() => setConfirm(user.enabled ? "disable" : "enable")}>
                <PowerIcon data-icon="inline-start" />
                {user.enabled ? t("mgmt.users.deactivate") : t("mgmt.users.activate")}
              </Button>
            )}
          </CardContent>
        </Card>
        {can("user:delete") && (
          <Card className="ring-destructive/25">
            <CardHeader>
              <CardTitle>{t("mgmt.users.deleteAction")}</CardTitle>
              <CardDescription>{t("mgmt.users.deleteBody")}</CardDescription>
            </CardHeader>
            <CardContent>
              <Button variant="destructive" onClick={() => setConfirm("delete")}>
                <Trash2Icon data-icon="inline-start" />
                {t("mgmt.users.deleteAction")}
              </Button>
            </CardContent>
          </Card>
        )}
      </div>

      <ConfirmDialog
        open={confirm === "enable" || confirm === "disable"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={PowerIcon}
        destructive={confirm === "disable"}
        title={confirm === "disable" ? t("mgmt.users.deactivate") : t("mgmt.users.activate")}
        description={t("mgmt.users.statusBody")}
        confirmLabel={confirm === "disable" ? t("mgmt.users.deactivate") : t("mgmt.users.activate")}
        onConfirm={async () => {
          await setUserStatus(user.uuid, confirm)
          toast.success(confirm === "disable" ? t("mgmt.users.deactivated") : t("mgmt.users.activated"))
          onSaved({ ...user, enabled: confirm === "enable" })
        }}
      />
      <ConfirmDialog
        open={confirm === "delete"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={Trash2Icon}
        destructive
        title={t("mgmt.users.deleteTitle", { name: user.name })}
        description={t("mgmt.users.deleteBody")}
        confirmLabel={t("mgmt.users.deleteAction")}
        onConfirm={async () => {
          await deleteUser(user.uuid)
          toast.success(t("mgmt.common.deleted"))
          onDeleted()
        }}
      />
    </div>
  )
}

/* ---------- Credentials tab ---------- */

function CredentialsTab({ user, onChanged }) {
  const passwordRules = usePasswordRules()
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const [password, setPassword] = useState("")
  const [error, setError] = useState(null)
  const [pending, setPending] = useState(false)
  const [confirm, setConfirm] = useState(null)
  const identities = useAsync(() => getUserIdentities(user.uuid), [user.uuid])
  const editable = can("user:update")

  const reset = async (event) => {
    event.preventDefault()
    if (!passwordRules.every((r) => r.test(password))) {
      setError(t("validation.passwordWeak"))
      return
    }
    setError(null)
    setPending(true)
    try {
      await resetUserPassword(user.uuid, password)
      setPassword("")
      toast.success(t("mgmt.users.resetDone"))
    } catch (err) {
      toast.error(errorText(err, t))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>{t("mgmt.users.resetTitle")}</CardTitle>
          <CardDescription>{t("mgmt.users.resetBody")}</CardDescription>
        </CardHeader>
        <CardContent>
          <form id="reset-password" onSubmit={reset} noValidate>
            <Field data-invalid={!!error || undefined}>
              <FieldLabel htmlFor="admin-new-password">{t("mgmt.users.newPassword")}</FieldLabel>
              <Input
                id="admin-new-password"
                type="password"
                autoComplete="new-password"
                value={password}
                disabled={!editable}
                aria-invalid={!!error || undefined}
                onChange={(e) => setPassword(e.target.value)}
              />
              {error ? <FieldError>{error}</FieldError> : <FieldDescription>{t("mgmt.users.passwordHint")}</FieldDescription>}
            </Field>
          </form>
        </CardContent>
        {editable && (
          <CardFooter className="justify-end border-t">
            <Button type="submit" form="reset-password" disabled={pending || !password}>
              {pending && <Spinner data-icon="inline-start" />}
              <KeyRoundIcon data-icon="inline-start" />
              {t("mgmt.users.resetAction")}
            </Button>
          </CardFooter>
        )}
      </Card>

      <div className="flex flex-col gap-6">
        <Card>
          <CardHeader>
            <CardTitle>{t("mgmt.users.lockTitle")}</CardTitle>
            <CardDescription>{t("mgmt.users.lockBody")}</CardDescription>
          </CardHeader>
          <CardContent className="flex flex-col items-start gap-3">
            {user.locked ? (
              <StatusDot tone="danger" label={t("mgmt.users.locked")} />
            ) : (
              <StatusDot on label={t("mgmt.users.notLocked")} />
            )}
            {editable &&
              (user.locked ? (
                <Button variant="outline" onClick={() => setConfirm("unlock")}>
                  <UnlockIcon data-icon="inline-start" />
                  {t("mgmt.users.unlock")}
                </Button>
              ) : (
                <Button variant="outline" onClick={() => setConfirm("lock")}>
                  <LockIcon data-icon="inline-start" />
                  {t("mgmt.users.lock")}
                </Button>
              ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>{t("mgmt.users.identities")}</CardTitle>
          </CardHeader>
          <CardContent>
            {identities.status === "loading" ? (
              <Spinner className="text-muted-foreground" />
            ) : identities.status === "error" ? (
              <p className="text-sm text-muted-foreground">{t("mgmt.common.failed")}</p>
            ) : (identities.data ?? []).length === 0 ? (
              <p className="text-sm text-muted-foreground">{t("mgmt.users.noIdentities")}</p>
            ) : (
              <ul className="flex flex-col divide-y">
                {identities.data.map((id) => {
                  const key = id.provider?.toLowerCase()
                  return (
                    <li key={id.uuid} className="flex items-center gap-3 py-3 first:pt-0 last:pb-0">
                      <span className="flex size-9 shrink-0 items-center justify-center rounded-md border bg-muted/40">
                        <ProviderLogo id={key} className="size-4" />
                      </span>
                      <span className="flex min-w-0 flex-1 flex-col">
                        <span className="font-medium">{PROVIDERS[key]?.name ?? id.provider}</span>
                        <span className="flex min-w-0 gap-3 text-muted-foreground">
                          <span className="truncate">{id.email ?? id.displayName}</span>
                          <span className="tabular shrink-0">{fmt.date(id.linkedAt)}</span>
                        </span>
                      </span>
                      {editable && (
                        <Button variant="ghost" size="sm" onClick={() => setConfirm({ unlink: id })}>
                          <UnlinkIcon data-icon="inline-start" />
                          {t("mgmt.users.unlink")}
                        </Button>
                      )}
                    </li>
                  )
                })}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>

      <ConfirmDialog
        open={confirm === "lock" || confirm === "unlock"}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={confirm === "lock" ? LockIcon : UnlockIcon}
        destructive={confirm === "lock"}
        title={confirm === "lock" ? t("mgmt.users.lock") : t("mgmt.users.unlock")}
        description={t("mgmt.users.lockBody")}
        confirmLabel={confirm === "lock" ? t("mgmt.users.lock") : t("mgmt.users.unlock")}
        onConfirm={async () => {
          await setUserStatus(user.uuid, confirm)
          onChanged({ locked: confirm === "lock" })
          toast.success(confirm === "lock" ? t("mgmt.users.lockedDone") : t("mgmt.users.unlockedDone"))
        }}
      />
      <ConfirmDialog
        open={!!confirm?.unlink}
        onOpenChange={(v) => !v && setConfirm(null)}
        icon={UnlinkIcon}
        destructive
        title={`${t("mgmt.users.unlink")} ${PROVIDERS[confirm?.unlink?.provider?.toLowerCase()]?.name ?? ""}`}
        description={confirm?.unlink?.email}
        confirmLabel={t("mgmt.users.unlink")}
        onConfirm={async () => {
          await unlinkUserIdentity(user.uuid, confirm.unlink.uuid)
          toast.success(t("mgmt.users.unlinked"))
          identities.reload()
        }}
      />
    </div>
  )
}

/* ---------- Roles tab ---------- */

function RolesTab({ user, authorities }) {
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const [assigning, setAssigning] = useState(false)
  const [revoking, setRevoking] = useState(null)
  const roles = useAsync(() => (assigning ? allRoles() : Promise.resolve(null)), [assigning])
  const assigned = authorities.data?.roles ?? []
  const editable = can("user:assign")

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("mgmt.users.rolesTitle")}</CardTitle>
        <CardDescription>{t("mgmt.users.rolesBody")}</CardDescription>
        {editable && (
          <CardAction>
            <Button size="sm" onClick={() => setAssigning(true)}>
              <PlusIcon data-icon="inline-start" />
              {t("mgmt.users.assignRoles")}
            </Button>
          </CardAction>
        )}
      </CardHeader>
      <CardContent>
        {authorities.status === "loading" ? (
          <Spinner className="text-muted-foreground" />
        ) : assigned.length === 0 ? (
          <p className="py-4 text-sm text-muted-foreground">{t("mgmt.users.noRoles")}</p>
        ) : (
          <ul className="flex flex-col divide-y">
            {assigned.map((r) => (
              <li key={r.uuid} className="flex items-center gap-3 py-3 first:pt-0 last:pb-0">
                <span className="flex min-w-0 flex-1 flex-wrap items-center gap-x-4 gap-y-1">
                  <Link
                    to={`/management/roles/${r.uuid}`}
                    className="rounded-md focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none [&>code]:transition-colors hover:[&>code]:border-foreground/30"
                  >
                    <KeyTag name={r.name} />
                  </Link>
                  <span className="text-muted-foreground">
                    {t("mgmt.common.expires")}: {r.expiresAt ? fmt.date(r.expiresAt) : t("mgmt.common.never")}
                  </span>
                </span>
                {editable && (
                  <Button variant="ghost" size="sm" onClick={() => setRevoking(r)}>
                    <XIcon data-icon="inline-start" />
                    {t("mgmt.users.revoke")}
                  </Button>
                )}
              </li>
            ))}
          </ul>
        )}
      </CardContent>

      <AssignDialog
        open={assigning}
        onOpenChange={setAssigning}
        title={t("mgmt.users.assignRoles")}
        items={roles.data}
        loading={roles.status === "loading"}
        exclude={assigned.map((r) => r.uuid)}
        label={(r) => r.name}
        sublabel={(r) => r.description}
        onSubmit={async ({ uuids, reason, expiresAt }) => {
          await assignUserRoles(user.uuid, { roleUuids: uuids, reason, expiresAt })
          toast.success(t("mgmt.users.assigned"))
          authorities.reload()
        }}
      />
      <ConfirmDialog
        open={!!revoking}
        onOpenChange={(v) => !v && setRevoking(null)}
        icon={XIcon}
        destructive
        title={t("mgmt.users.revokeRoleTitle", { role: revoking?.name ?? "" })}
        description={t("mgmt.users.revokeRoleBody")}
        confirmLabel={t("mgmt.users.revoke")}
        onConfirm={async () => {
          await revokeUserRole(user.uuid, revoking.uuid)
          toast.success(t("mgmt.users.revoked"))
          authorities.reload()
        }}
      />
    </Card>
  )
}

/* ---------- Permissions tab ---------- */

function PermissionsTab({ user, authorities }) {
  const { t } = useI18n()
  const { can } = useAccess()
  const [granting, setGranting] = useState(false)
  const [revoking, setRevoking] = useState(null)
  const catalog = useAsync(allPermissions)
  const editable = can("user:assign")

  const direct = authorities.data?.directPermissions ?? []
  const effective = authorities.data?.effectivePermissions ?? []
  const directNames = new Set(direct.map((d) => d.name))
  const inherited = effective.filter((p) => !directNames.has(p)).sort()
  const byName = useMemo(() => new Map((catalog.data ?? []).map((p) => [p.name, p])), [catalog.data])

  return (
    <div className="grid gap-6 lg:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>{t("mgmt.users.directTitle")}</CardTitle>
          <CardDescription>{t("mgmt.users.directBody")}</CardDescription>
          {editable && (
            <CardAction>
              <Button size="sm" onClick={() => setGranting(true)}>
                <PlusIcon data-icon="inline-start" />
                {t("mgmt.users.grant")}
              </Button>
            </CardAction>
          )}
        </CardHeader>
        <CardContent>
          {authorities.status === "loading" ? (
            <Spinner className="text-muted-foreground" />
          ) : direct.length === 0 ? (
            <p className="py-4 text-sm text-muted-foreground">{t("mgmt.users.noDirect")}</p>
          ) : (
            <ul className="flex flex-col divide-y">
              {direct.map((d) => (
                <li key={`${d.name}-${d.effect}`} className="flex items-center gap-3 py-3 first:pt-0 last:pb-0">
                  <span className="min-w-0 flex-1">
                    <KeyTag name={d.name} tone={d.effect === "DENY" ? "deny" : "default"} />
                  </span>
                  <span className={d.effect === "DENY" ? "text-destructive" : "text-muted-foreground"}>
                    {d.effect === "DENY" ? t("mgmt.users.deny") : t("mgmt.users.allow")}
                  </span>
                  {editable && byName.get(d.name) && (
                    <Button variant="ghost" size="icon-sm" aria-label={t("mgmt.users.revoke")} onClick={() => setRevoking(d)}>
                      <XIcon />
                    </Button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t("mgmt.users.inheritedTitle")}</CardTitle>
          <CardDescription>{t("mgmt.users.inheritedBody")}</CardDescription>
        </CardHeader>
        <CardContent>
          {authorities.status === "loading" ? (
            <Spinner className="text-muted-foreground" />
          ) : inherited.length === 0 ? (
            <p className="py-4 text-sm text-muted-foreground">{t("mgmt.users.noInherited")}</p>
          ) : (
            <ul className="flex flex-wrap gap-2">
              {inherited.map((p) => (
                <li key={p}>
                  <KeyTag name={p} />
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <AssignDialog
        open={granting}
        onOpenChange={setGranting}
        title={t("mgmt.users.grant")}
        withEffect
        items={catalog.data}
        loading={catalog.status === "loading"}
        exclude={direct.map((d) => byName.get(d.name)?.uuid).filter(Boolean)}
        label={(p) => p.name}
        sublabel={(p) => p.description}
        groupOf={(p) => p.resource}
        onSubmit={async ({ uuids, reason, expiresAt, effect }) => {
          await grantUserPermissions(user.uuid, { permissionUuids: uuids, effect, reason, expiresAt })
          toast.success(t("mgmt.users.granted"))
          authorities.reload()
        }}
      />
      <ConfirmDialog
        open={!!revoking}
        onOpenChange={(v) => !v && setRevoking(null)}
        icon={XIcon}
        destructive
        title={t("mgmt.users.revokePermTitle", { permission: revoking?.name ?? "" })}
        description={t("mgmt.users.revokePermBody")}
        confirmLabel={t("mgmt.users.revoke")}
        onConfirm={async () => {
          await revokeUserPermission(user.uuid, byName.get(revoking.name).uuid)
          toast.success(t("mgmt.users.revoked"))
          authorities.reload()
        }}
      />
    </div>
  )
}

/* ---------- page ---------- */

export default function UserDetailPage() {
  const { id } = useParams()
  const { t } = useI18n()
  const navigate = useNavigate()
  const [params, setParams] = useSearchParams()
  const tab = TABS.includes(params.get("tab")) ? params.get("tab") : "details"
  const user = useAsync(() => getUser(id), [id])
  const authorities = useAsync(() => getUserAuthorities(id), [id])

  if (user.status === "loading") return <CardSkeleton rows={5} />
  if (user.status === "error") return <ErrorState onRetry={user.reload} />
  const u = user.data

  return (
    <>
      <BackLink to="/management/users">{t("mgmt.users.title")}</BackLink>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
        <Avatar className="size-14 rounded-md text-lg">
          {u.pictureUrl && <AvatarImage src={u.pictureUrl} alt="" className="rounded-md" />}
          <AvatarFallback className="rounded-md">{initials(u.name)}</AvatarFallback>
        </Avatar>
        <div className="flex min-w-0 flex-1 flex-col gap-1">
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1">
            <h1 className="truncate text-3xl leading-tight font-medium tracking-tight">{u.name}</h1>
            <StatusBadge enabled={u.enabled} locked={u.locked} />
          </div>
          <p className="flex min-w-0 flex-wrap gap-x-4 text-muted-foreground">
            <span className="font-mono">{u.username}</span>
            <span className="truncate">{u.email}</span>
          </p>
        </div>
        {(u.roles ?? []).length > 0 && (
          <div className="flex flex-wrap gap-2">
            {u.roles.map((r) => (
              <KeyTag key={r.uuid} name={r.name} />
            ))}
          </div>
        )}
      </div>

      <Tabs value={tab} onValueChange={(v) => setParams({ tab: v }, { replace: true })} className="gap-6">
        <div className="-mx-4 overflow-x-auto px-4">
          <TabsList variant="line">
            {TABS.map((k) => (
              <TabsTrigger key={k} value={k}>{t(`mgmt.users.tabs.${k}`)}</TabsTrigger>
            ))}
          </TabsList>
        </div>
        <TabsContent value="details">
          <DetailsTab
            key={u.version}
            user={u}
            onSaved={(next) => user.setData({ ...u, ...next })}
            onDeleted={() => navigate("/management/users", { replace: true })}
          />
        </TabsContent>
        <TabsContent value="credentials">
          <CredentialsTab user={u} onChanged={(patch) => user.setData({ ...u, ...patch })} />
        </TabsContent>
        <TabsContent value="roles">
          <RolesTab user={u} authorities={authorities} />
        </TabsContent>
        <TabsContent value="permissions">
          <PermissionsTab user={u} authorities={authorities} />
        </TabsContent>
        <TabsContent value="sessions">
          <UserSessionsTab user={u} />
        </TabsContent>
        <TabsContent value="history">
          <UserSignInsTab user={u} />
        </TabsContent>
      </Tabs>
    </>
  )
}
