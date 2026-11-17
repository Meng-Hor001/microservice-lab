import { useCallback, useRef, useState } from "react"
import { useSearchParams } from "react-router-dom"
import { toast } from "sonner"
import { AppWindowIcon, CheckIcon, CopyIcon, EllipsisIcon, KeyRoundIcon, PencilIcon, PlusIcon, Trash2Icon, XIcon } from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { DataBody, DataHead, DataRow, DataTable, revealOnRow, Td, Th } from "@/components/management/data-table"
import { KeyTag, ListPanel, PageHeader, Pager, PanelError, SearchBox, TableSkeleton } from "@/components/management/states"
import { EmptyState } from "@/components/page-states"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { Switch } from "@/components/ui/switch"
import { useAccess } from "@/lib/access"
import { createClient, deleteClient, listClients, rotateClientSecret, updateClient } from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const PAGE_SIZE = 10
const GRANTS = ["authorization_code", "refresh_token", "client_credentials"]
const METHODS = ["client_secret_basic", "client_secret_post", "none"]

const EMPTY = {
  clientName: "",
  authenticationMethod: "client_secret_basic",
  grantTypes: ["authorization_code", "refresh_token"],
  redirectUris: [""],
  postLogoutRedirectUris: [""],
  scopes: "openid profile",
  requireProofKey: true,
  requireConsent: false,
  accessTokenMinutes: "15",
  refreshTokenMinutes: "1440",
}

// The URL rows as sent to the API: trimmed, blanks dropped.
const lines = (rows) => rows.map((s) => s.trim()).filter(Boolean)
// Always keep one row to type into.
const toRows = (list) => (list?.length ? [...list] : [""])

const isUrl = (value) => {
  try {
    const u = new URL(value)
    return u.protocol === "http:" || u.protocol === "https:" || u.protocol.endsWith(":")
  } catch {
    return false
  }
}

function toForm(client) {
  if (!client) return EMPTY
  return {
    clientName: client.clientName ?? "",
    authenticationMethod: client.authenticationMethod,
    grantTypes: [...client.grantTypes],
    redirectUris: toRows(client.redirectUris),
    postLogoutRedirectUris: toRows(client.postLogoutRedirectUris),
    scopes: [...client.scopes].join(" "),
    requireProofKey: client.requireProofKey,
    requireConsent: client.requireConsent,
    accessTokenMinutes: String(client.accessTokenMinutes),
    refreshTokenMinutes: String(client.refreshTokenMinutes),
  }
}

export function ClientType({ method }) {
  const { t } = useI18n()
  const isPublic = method === "none"
  return (
    <span className={cn("whitespace-nowrap", isPublic ? "text-muted-foreground" : "text-foreground")}>
      {isPublic ? t("mgmt.clients.public") : t("mgmt.clients.confidential")}
    </span>
  )
}

/**
 * One input per URL. The last row has a + button (enabled once it has text, Enter works too)
 * that adds an empty row below; every other row has an x to remove it.
 */
function UrlListField({ id, label, hint, rows, onChange, error }) {
  const { t } = useI18n()
  const listRef = useRef(null)
  const last = rows.length - 1

  const focusRow = (i) =>
    requestAnimationFrame(() => listRef.current?.querySelectorAll("input")[i]?.focus())

  const setRow = (i, value) => onChange(rows.map((r, j) => (j === i ? value : r)))
  const add = () => {
    if (!rows[last].trim()) return
    onChange([...rows, ""])
    focusRow(rows.length)
  }
  const remove = (i) => {
    const next = rows.filter((_, j) => j !== i)
    onChange(next.length ? next : [""])
    focusRow(Math.min(i, next.length - 1))
  }

  return (
    <Field data-invalid={!!error || undefined}>
      <FieldLabel htmlFor={`${id}-0`}>{label}</FieldLabel>
      <ul ref={listRef} className="flex flex-col gap-2">
        {rows.map((value, i) => {
          const bad = !!error && !!value.trim() && !isUrl(value.trim())
          return (
            <li key={i} className="flex items-center gap-2">
              <Input
                id={`${id}-${i}`}
                type="url"
                inputMode="url"
                autoComplete="off"
                spellCheck={false}
                className="font-mono"
                placeholder="https://app.example.com/callback"
                value={value}
                aria-invalid={bad || (!!error && rows.length === 1) || undefined}
                onChange={(e) => setRow(i, e.target.value)}
                onKeyDown={(e) => {
                  // Enter adds a row instead of submitting the whole form.
                  if (e.key === "Enter") {
                    e.preventDefault()
                    if (i === last) add()
                    else focusRow(i + 1)
                  }
                }}
              />
              {i === last ? (
                <Button
                  type="button"
                  variant="outline"
                  size="icon"
                  className="shrink-0"
                  disabled={!value.trim()}
                  aria-label={t("mgmt.clients.addUri")}
                  onClick={add}
                >
                  <PlusIcon />
                </Button>
              ) : (
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="shrink-0 text-muted-foreground hover:text-destructive"
                  aria-label={`${t("mgmt.clients.removeUri")} ${value}`}
                  onClick={() => remove(i)}
                >
                  <XIcon />
                </Button>
              )}
            </li>
          )
        })}
      </ul>
      {error ? <FieldError>{error}</FieldError> : hint && <FieldDescription>{hint}</FieldDescription>}
    </Field>
  )
}

function ClientDialog({ open, client, onOpenChange, onSaved }) {
  const { t } = useI18n()
  const editing = !!client
  const [form, setForm] = useState(() => toForm(client))
  const [errors, setErrors] = useState({})
  const [pending, setPending] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e?.target ? e.target.value : e }))
  const isPublic = form.authenticationMethod === "none"

  const toggleGrant = (grant, on) =>
    setForm((f) => ({ ...f, grantTypes: on ? [...new Set([...f.grantTypes, grant])] : f.grantTypes.filter((g) => g !== grant) }))

  const setMethod = (method) =>
    setForm((f) => ({
      ...f,
      authenticationMethod: method,
      // Public clients must use PKCE and can't hold client credentials.
      requireProofKey: method === "none" ? true : f.requireProofKey,
      grantTypes: method === "none" ? f.grantTypes.filter((g) => g !== "client_credentials") : f.grantTypes,
    }))

  const validate = () => {
    const e = {}
    const redirects = lines(form.redirectUris)
    const logouts = lines(form.postLogoutRedirectUris)
    const minutes = (v, max) => /^\d+$/.test(v) && Number(v) >= 1 && Number(v) <= max
    if (!form.clientName.trim()) e.clientName = t("mgmt.common.required")
    if (form.grantTypes.length === 0) e.grantTypes = t("mgmt.clients.errors.grants")
    else if (isPublic && form.grantTypes.includes("client_credentials")) e.grantTypes = t("mgmt.clients.errors.publicGrant")
    if (form.grantTypes.includes("authorization_code") && redirects.length === 0) e.redirectUris = t("mgmt.clients.errors.redirect")
    else if (!redirects.every(isUrl)) e.redirectUris = t("mgmt.clients.errors.uri")
    if (!logouts.every(isUrl)) e.postLogoutRedirectUris = t("mgmt.clients.errors.uri")
    if (form.scopes.trim().split(/\s+/).filter(Boolean).length === 0) e.scopes = t("mgmt.clients.errors.scopes")
    if (!minutes(form.accessTokenMinutes, 1440)) e.accessTokenMinutes = t("mgmt.clients.errors.minutes")
    if (!minutes(form.refreshTokenMinutes, 525600)) e.refreshTokenMinutes = t("mgmt.clients.errors.minutes")
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!validate()) return
    setPending(true)
    const body = {
      clientName: form.clientName.trim(),
      authenticationMethod: form.authenticationMethod,
      grantTypes: form.grantTypes,
      redirectUris: lines(form.redirectUris),
      postLogoutRedirectUris: lines(form.postLogoutRedirectUris),
      scopes: form.scopes.trim().split(/\s+/),
      requireProofKey: isPublic || form.requireProofKey,
      requireConsent: form.requireConsent,
      accessTokenMinutes: Number(form.accessTokenMinutes),
      refreshTokenMinutes: Number(form.refreshTokenMinutes),
    }
    try {
      const saved = editing ? await updateClient(client.clientId, body) : await createClient(body)
      toast.success(editing ? t("mgmt.clients.saved") : t("mgmt.clients.created"))
      onOpenChange(false)
      onSaved(saved)
    } catch (error) {
      toast.error(errorText(error, t))
    } finally {
      setPending(false)
    }
  }

  const textField = (key, label, hint, props = {}) => (
    <Field data-invalid={!!errors[key] || undefined}>
      <FieldLabel htmlFor={`client-${key}`}>{label}</FieldLabel>
      <Input id={`client-${key}`} value={form[key]} onChange={set(key)} aria-invalid={!!errors[key] || undefined} {...props} />
      {errors[key] ? <FieldError>{errors[key]}</FieldError> : hint && <FieldDescription>{hint}</FieldDescription>}
    </Field>
  )

  const urlField = (key, label, hint) => (
    <UrlListField
      id={`client-${key}`}
      label={label}
      hint={hint}
      rows={form[key]}
      onChange={(rows) => setForm((f) => ({ ...f, [key]: rows }))}
      error={errors[key]}
    />
  )

  return (
    <Dialog open={open} onOpenChange={(v) => !pending && onOpenChange(v)}>
      <DialogContent className="max-h-[90svh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{editing ? t("mgmt.clients.editTitle", { name: client.clientName }) : t("mgmt.clients.register")}</DialogTitle>
          <DialogDescription>{editing ? t("mgmt.clients.editIntro") : t("mgmt.clients.createIntro")}</DialogDescription>
        </DialogHeader>
        <form id="client-form" onSubmit={submit} noValidate>
          <FieldGroup>
            {textField("clientName", t("mgmt.clients.name"), null, { autoComplete: "off" })}

            <Field>
              <FieldLabel htmlFor="client-method">{t("mgmt.clients.authMethod")}</FieldLabel>
              <Select
                value={form.authenticationMethod}
                onValueChange={setMethod}
                // Switching between public and confidential needs a new client (the server refuses it).
                disabled={editing}
              >
                <SelectTrigger id="client-method" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {METHODS.map((m) => (
                      <SelectItem key={m} value={m}>
                        {t(`mgmt.clients.methods.${m}`)}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
            </Field>

            <FieldSet data-invalid={!!errors.grantTypes || undefined}>
              <FieldLegend variant="label">{t("mgmt.clients.grants")}</FieldLegend>
              <div className="flex flex-col gap-3">
                {GRANTS.map((g) => {
                  const disabled = isPublic && g === "client_credentials"
                  return (
                    <Field key={g} orientation="horizontal" data-disabled={disabled || undefined}>
                      <Checkbox
                        id={`grant-${g}`}
                        checked={form.grantTypes.includes(g)}
                        disabled={disabled}
                        onCheckedChange={(v) => toggleGrant(g, v === true)}
                      />
                      <FieldLabel htmlFor={`grant-${g}`} className="font-normal">
                        {t(`mgmt.clients.grantNames.${g}`)}
                        <KeyTag name={g} className="ml-2" />
                      </FieldLabel>
                    </Field>
                  )
                })}
              </div>
              {errors.grantTypes && <FieldError>{errors.grantTypes}</FieldError>}
            </FieldSet>

            {urlField("redirectUris", t("mgmt.clients.redirectUris"))}
            {urlField("postLogoutRedirectUris", t("mgmt.clients.logoutUris"))}
            {textField("scopes", t("mgmt.clients.scopes"), null, { className: "font-mono" })}

            <div className="grid gap-4 sm:grid-cols-2">
              {textField("accessTokenMinutes", `${t("mgmt.clients.accessToken")} (${t("mgmt.clients.minutes")})`, null, {
                inputMode: "numeric",
                className: "tabular",
              })}
              {textField("refreshTokenMinutes", `${t("mgmt.clients.refreshToken")} (${t("mgmt.clients.minutes")})`, null, {
                inputMode: "numeric",
                className: "tabular",
              })}
            </div>

            <Field orientation="horizontal" data-disabled={isPublic || undefined}>
              <Switch
                id="client-pkce"
                checked={isPublic || form.requireProofKey}
                disabled={isPublic}
                onCheckedChange={set("requireProofKey")}
              />
              <div className="flex flex-col gap-1">
                <FieldLabel htmlFor="client-pkce">{t("mgmt.clients.pkce")}</FieldLabel>
                <FieldDescription>{t("mgmt.clients.pkceHint")}</FieldDescription>
              </div>
            </Field>
            <Field orientation="horizontal">
              <Switch id="client-consent" checked={form.requireConsent} onCheckedChange={set("requireConsent")} />
              <div className="flex flex-col gap-1">
                <FieldLabel htmlFor="client-consent">{t("mgmt.clients.consent")}</FieldLabel>
                <FieldDescription>{t("mgmt.clients.consentHint")}</FieldDescription>
              </div>
            </Field>
          </FieldGroup>
        </form>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} disabled={pending}>
            {t("mgmt.common.cancel")}
          </Button>
          <Button type="submit" form="client-form" disabled={pending}>
            {pending && <Spinner data-icon="inline-start" />}
            {editing ? t("mgmt.common.save") : t("mgmt.clients.register")}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

function CopyValue({ label, value }) {
  const { t } = useI18n()
  const [copied, setCopied] = useState(false)
  const copy = async () => {
    try {
      await navigator.clipboard.writeText(value)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch {
      toast.error(t("mgmt.common.failed"))
    }
  }
  return (
    <Field>
      <FieldLabel>{label}</FieldLabel>
      <div className="flex items-center gap-2">
        <code className="min-w-0 flex-1 truncate rounded-md border bg-muted/60 px-3 py-2 font-mono text-sm select-all">{value}</code>
        <Button variant="outline" size="sm" onClick={copy}>
          {copied ? <CheckIcon data-icon="inline-start" /> : <CopyIcon data-icon="inline-start" />}
          {copied ? t("mgmt.clients.copied") : t("mgmt.clients.copy")}
        </Button>
      </div>
    </Field>
  )
}

/** Shows the plain secret once, right after registering or rotating. */
function SecretDialog({ credentials, onClose }) {
  const { t } = useI18n()
  if (!credentials) return null
  const { client, clientSecret } = credentials
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>{t("mgmt.clients.secretTitle")}</DialogTitle>
          <DialogDescription>{client.clientName}</DialogDescription>
        </DialogHeader>
        <FieldGroup>
          <CopyValue label={t("mgmt.clients.clientId")} value={client.clientId} />
          {clientSecret ? (
            <>
              <CopyValue label={t("mgmt.clients.secret")} value={clientSecret} />
              <Alert>
                <KeyRoundIcon />
                <AlertDescription>{t("mgmt.clients.secretBody")}</AlertDescription>
              </Alert>
            </>
          ) : (
            <p className="text-muted-foreground">{t("mgmt.clients.noSecret")}</p>
          )}
        </FieldGroup>
        <DialogFooter>
          <Button onClick={onClose}>{t("mgmt.clients.done")}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default function ClientsPage() {
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const [params, setParams] = useSearchParams()
  const search = params.get("q") ?? ""
  const page = Number(params.get("page") ?? 1)
  const [editing, setEditing] = useState(null) // null | "new" | client
  const [credentials, setCredentials] = useState(null)
  const [rotateTarget, setRotateTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)

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

  const clients = useAsync(() => listClients({ page, size: PAGE_SIZE, search: search || undefined }), [page, search])

  const onSaved = (saved) => {
    // Create returns { client, clientSecret }; update returns the client itself.
    if (saved?.client) setCredentials(saved)
    clients.reload()
  }

  const body = () => {
    if (clients.status === "loading") return <TableSkeleton rows={5} />
    if (clients.status === "error") return <PanelError onRetry={clients.reload} />
    const { items } = clients.data
    if (items.length === 0) {
      return (
        <EmptyState
          icon={AppWindowIcon}
          title={search ? t("mgmt.common.noResults") : t("mgmt.clients.empty")}
          description={search ? t("mgmt.common.noResultsBody") : t("mgmt.clients.emptyBody")}
        >
          {search && (
            <Button variant="outline" onClick={() => update({ q: null, page: 1 })}>
              {t("mgmt.common.clearFilters")}
            </Button>
          )}
        </EmptyState>
      )
    }
    const canEdit = can("client:update")
    const canDelete = can("client:delete")
    return (
      <DataTable>
        <DataHead>
          <Th>{t("mgmt.clients.name")}</Th>
          <Th className="hidden w-36 sm:table-cell">{t("mgmt.clients.type")}</Th>
          <Th className="hidden lg:table-cell">{t("mgmt.clients.grants")}</Th>
          <Th className="hidden w-40 md:table-cell">{t("mgmt.clients.issued")}</Th>
          <Th className="w-12">
            <span className="sr-only">{t("mgmt.common.actions")}</span>
          </Th>
        </DataHead>
        <DataBody>
          {items.map((c) => (
            <DataRow key={c.clientId} onOpen={canEdit ? () => setEditing(c) : undefined}>
              <Td>
                <div className="flex min-w-0 flex-col leading-snug">
                  <span className="truncate font-medium">{c.clientName}</span>
                  <span className="truncate font-mono text-sm text-muted-foreground">{c.clientId}</span>
                </div>
              </Td>
              <Td className="hidden sm:table-cell">
                <ClientType method={c.authenticationMethod} />
              </Td>
              <Td className="hidden lg:table-cell">
                <div className="flex flex-wrap gap-1.5">
                  {[...c.grantTypes].sort().map((g) => (
                    <KeyTag key={g} name={g} />
                  ))}
                </div>
              </Td>
              <Td className="tabular hidden text-muted-foreground md:table-cell">
                {c.issuedAt ? fmt.relative(c.issuedAt) : "—"}
              </Td>
              <Td onClick={(e) => e.stopPropagation()}>
                {(canEdit || canDelete) && (
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon-sm" aria-label={t("mgmt.common.actions")} className={revealOnRow}>
                        <EllipsisIcon />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuGroup>
                        {canEdit && (
                          <DropdownMenuItem onSelect={() => setEditing(c)}>
                            <PencilIcon />
                            {t("mgmt.common.edit")}
                          </DropdownMenuItem>
                        )}
                        {canEdit && c.authenticationMethod !== "none" && (
                          <DropdownMenuItem onSelect={() => setRotateTarget(c)}>
                            <KeyRoundIcon />
                            {t("mgmt.clients.rotate")}
                          </DropdownMenuItem>
                        )}
                      </DropdownMenuGroup>
                      {canDelete && (
                        <>
                          {canEdit && <DropdownMenuSeparator />}
                          <DropdownMenuGroup>
                            <DropdownMenuItem variant="destructive" onSelect={() => setDeleteTarget(c)}>
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
      <PageHeader title={t("mgmt.clients.title")} intro={t("mgmt.clients.intro")}>
        {can("client:create") && (
          <Button onClick={() => setEditing("new")}>
            <PlusIcon data-icon="inline-start" />
            {t("mgmt.clients.register")}
          </Button>
        )}
      </PageHeader>

      <ListPanel
        toolbar={
          <>
            <SearchBox
              className="sm:max-w-sm"
              value={search}
              placeholder={t("mgmt.clients.searchPlaceholder")}
              onChange={(q) => update({ q, page: 1 })}
            />
            {clients.status === "refreshing" && <Spinner className="text-muted-foreground" />}
          </>
        }
        footer={
          clients.data?.items?.length > 0 && clients.status !== "loading" ? (
            <Pager page={page} pages={clients.data.pages} total={clients.data.total} size={PAGE_SIZE} onPage={(p) => update({ page: p })} />
          ) : null
        }
      >
        {body()}
      </ListPanel>

      {editing && (
        <ClientDialog
          // Remount per client so the form starts from that client's values.
          key={editing === "new" ? "new" : editing.clientId}
          open
          client={editing === "new" ? null : editing}
          onOpenChange={(v) => !v && setEditing(null)}
          onSaved={onSaved}
        />
      )}

      <SecretDialog credentials={credentials} onClose={() => setCredentials(null)} />

      <ConfirmDialog
        open={!!rotateTarget}
        onOpenChange={(v) => !v && setRotateTarget(null)}
        icon={KeyRoundIcon}
        title={t("mgmt.clients.rotateTitle", { name: rotateTarget?.clientName ?? "" })}
        description={t("mgmt.clients.rotateBody")}
        confirmLabel={t("mgmt.clients.rotateConfirm")}
        onConfirm={async () => {
          const result = await rotateClientSecret(rotateTarget.clientId)
          setCredentials(result)
        }}
      />

      <ConfirmDialog
        open={!!deleteTarget}
        onOpenChange={(v) => !v && setDeleteTarget(null)}
        icon={Trash2Icon}
        destructive
        title={t("mgmt.clients.deleteTitle", { name: deleteTarget?.clientName ?? "" })}
        description={t("mgmt.clients.deleteBody")}
        confirmLabel={t("mgmt.common.delete")}
        onConfirm={async () => {
          try {
            await deleteClient(deleteTarget.clientId)
            toast.success(t("mgmt.common.deleted"))
            clients.reload()
          } catch (error) {
            // e.g. the protected iam-web client: show the server's reason instead of a generic failure.
            toast.error(errorText(error, t))
            throw error
          }
        }}
      />
    </>
  )
}
