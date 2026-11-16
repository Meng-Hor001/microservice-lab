import { useState } from "react"
import { toast } from "sonner"
import { InfoIcon } from "lucide-react"
import { PageHeader, PanelError } from "@/components/management/states"
import { CardSkeleton } from "@/components/page-states"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { NumberStepper } from "@/components/number-stepper"
import { Separator } from "@/components/ui/separator"
import { Spinner } from "@/components/ui/spinner"
import { Switch } from "@/components/ui/switch"
import { useAccess } from "@/lib/access"
import { getSettings, updateSettings } from "@/lib/admin-api"
import { errorText } from "@/lib/errors"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

// Same ranges as UpdateSystemSettingsRequest on the server.
const NUMBERS = {
  maxFailedAttempts: [1, 20],
  lockoutMinutes: [1, 1440],
  passwordMinLength: [6, 128],
  passwordExpiryDays: [0, 365],
  sessionIdleMinutes: [5, 1440],
  sessionMaxHours: [1, 720],
  accessTokenMinutes: [1, 1440],
  refreshTokenDays: [1, 365],
}
const SWITCHES = [
  "allowSelfRegistration",
  "allowSocialLogin",
  "passwordRequireUppercase",
  "passwordRequireLowercase",
  "passwordRequireDigit",
  "passwordRequireSymbol",
]

const SECTIONS = [
  { key: "auth", switches: ["allowSelfRegistration", "allowSocialLogin"], numbers: ["maxFailedAttempts", "lockoutMinutes"] },
  {
    key: "password",
    numbers: ["passwordMinLength", "passwordExpiryDays"],
    switches: ["passwordRequireUppercase", "passwordRequireLowercase", "passwordRequireDigit", "passwordRequireSymbol"],
    // Short labels, so they sit two per row and keep this card close to the height of the one beside it.
    grid: true,
  },
  { key: "sessions", numbers: ["sessionIdleMinutes", "sessionMaxHours"], switches: [] },
  { key: "tokens", numbers: ["accessTokenMinutes", "refreshTokenDays"], switches: [] },
]

// Paired so both columns end at about the same height: sign-in rules on the left, password and token rules on the right.
const COLUMNS = [
  ["auth", "sessions"],
  ["password", "tokens"],
]

const toForm = (s) => ({
  ...Object.fromEntries(SWITCHES.map((k) => [k, !!s[k]])),
  ...Object.fromEntries(Object.keys(NUMBERS).map((k) => [k, String(s[k])])),
})

function SettingsForm({ settings, canEdit, onSaved }) {
  const { t, fmt } = useI18n()
  const saved = toForm(settings)
  const [form, setForm] = useState(saved)
  const [errors, setErrors] = useState({})
  const [pending, setPending] = useState(false)
  const dirty = Object.keys(saved).some((k) => saved[k] !== form[k])

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }))

  const validate = () => {
    const e = {}
    for (const [k, [min, max]] of Object.entries(NUMBERS)) {
      const v = form[k]
      if (!/^\d+$/.test(v) || Number(v) < min || Number(v) > max) e[k] = t("mgmt.settings.errors.range", { min, max })
    }
    if (!e.sessionIdleMinutes && !e.sessionMaxHours && Number(form.sessionIdleMinutes) > Number(form.sessionMaxHours) * 60) {
      e.sessionIdleMinutes = t("mgmt.settings.errors.idle")
    }
    if (!e.accessTokenMinutes && !e.refreshTokenDays && Number(form.accessTokenMinutes) > Number(form.refreshTokenDays) * 1440) {
      e.accessTokenMinutes = t("mgmt.settings.errors.tokens")
    }
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const submit = async (event) => {
    event.preventDefault()
    if (!validate()) return
    setPending(true)
    try {
      const body = {
        ...Object.fromEntries(SWITCHES.map((k) => [k, form[k]])),
        ...Object.fromEntries(Object.keys(NUMBERS).map((k) => [k, Number(form[k])])),
      }
      const result = await updateSettings(body)
      toast.success(t("mgmt.settings.saved"))
      onSaved(result)
    } catch (error) {
      toast.error(errorText(error, t))
    } finally {
      setPending(false)
    }
  }

  const numberField = (k) => {
    const [min, max] = NUMBERS[k]
    const hint = k === "passwordExpiryDays" ? t("mgmt.settings.passwordExpiryHint") : t("mgmt.settings.range", { min, max })
    return (
      <Field key={k} data-invalid={!!errors[k] || undefined}>
        <FieldLabel htmlFor={`setting-${k}`}>{t(`mgmt.settings.${k}`)}</FieldLabel>
        <NumberStepper
          id={`setting-${k}`}
          min={min}
          max={max}
          value={form[k]}
          disabled={!canEdit}
          onChange={(v) => set(k, v)}
          aria-invalid={!!errors[k] || undefined}
        />
        {errors[k] ? <FieldError>{errors[k]}</FieldError> : <FieldDescription>{hint}</FieldDescription>}
      </Field>
    )
  }

  const switchField = (k) => (
    <Field key={k} orientation="horizontal" data-disabled={!canEdit || undefined}>
      <Switch id={`setting-${k}`} checked={form[k]} disabled={!canEdit} onCheckedChange={(v) => set(k, v)} />
      <FieldLabel htmlFor={`setting-${k}`} className="font-normal">
        {t(`mgmt.settings.${k}`)}
      </FieldLabel>
    </Field>
  )

  return (
    <form onSubmit={submit} noValidate className="flex flex-col gap-6">
      {/* Two columns of stacked cards; the last card in each column fills the rest so both columns end level. */}
      <div className="grid gap-6 lg:grid-cols-2">
        {COLUMNS.map((keys) => (
          <div key={keys[0]} className="flex flex-col gap-6">
            {keys.map((key, i) => {
              const s = SECTIONS.find((x) => x.key === key)
              return (
                <Card key={s.key} className={i === keys.length - 1 ? "lg:flex-1" : undefined}>
                  <CardHeader>
                    <CardTitle>{t(`mgmt.settings.${s.key}`)}</CardTitle>
                    <CardDescription>{t(`mgmt.settings.${s.key}Hint`)}</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <FieldGroup>
                      {s.grid ? (
                        <div className="grid gap-x-4 gap-y-5 sm:grid-cols-2">{s.switches.map(switchField)}</div>
                      ) : (
                        s.switches.map(switchField)
                      )}
                      <div className="grid gap-4 sm:grid-cols-2">{s.numbers.map(numberField)}</div>
                    </FieldGroup>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        ))}
      </div>

      <Separator />
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <span className="text-muted-foreground">
          {settings.updatedAt &&
            t("mgmt.settings.lastChanged", { actor: settings.updatedBy, when: fmt.relative(settings.updatedAt) })}
        </span>
        {canEdit && (
          <div className="flex gap-2">
            <Button
              type="button"
              variant="outline"
              className="border-primary/50 text-primary hover:border-primary hover:bg-primary/5 hover:text-primary disabled:opacity-60"
              disabled={!dirty || pending}
              onClick={() => {
                setForm(saved)
                setErrors({})
              }}
            >
              {t("mgmt.settings.reset")}
            </Button>
            <Button
              type="submit"
              className="border-primary shadow-sm hover:border-[color-mix(in_oklch,var(--primary),black_20%)] hover:bg-[color-mix(in_oklch,var(--primary),black_20%)] hover:shadow-md disabled:border-primary/40 disabled:bg-primary/60 disabled:text-primary-foreground disabled:shadow-none dark:hover:border-[color-mix(in_oklch,var(--primary),white_20%)] dark:hover:bg-[color-mix(in_oklch,var(--primary),white_20%)]"
              disabled={!dirty || pending}
            >
              {pending && <Spinner data-icon="inline-start" />}
              {t("mgmt.common.save")}
            </Button>
          </div>
        )}
      </div>
    </form>
  )
}

export default function SettingsPage() {
  const { t } = useI18n()
  const { can } = useAccess()
  const canEdit = can("settings:update")
  const settings = useAsync(() => getSettings())

  return (
    <>
      <PageHeader title={t("mgmt.settings.title")} intro={t("mgmt.settings.intro")} />
      <Alert>
        <InfoIcon />
        <AlertDescription>{canEdit ? t("mgmt.settings.note") : t("mgmt.settings.readOnly")}</AlertDescription>
      </Alert>
      {settings.status === "loading" ? (
        <CardSkeleton rows={6} />
      ) : settings.status === "error" && !settings.data ? (
        <PanelError onRetry={settings.reload} />
      ) : (
        <SettingsForm
          // Remount after a save so the form's baseline is the stored values.
          key={settings.data.updatedAt}
          settings={settings.data}
          canEdit={canEdit}
          onSaved={settings.setData}
        />
      )}
    </>
  )
}
