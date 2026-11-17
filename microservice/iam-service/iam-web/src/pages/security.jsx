import { useEffect, useId, useState } from "react"
import { CheckIcon, CircleIcon, MailCheckIcon, MailIcon } from "lucide-react"
import { toast } from "sonner"
import { PageIntro } from "@/components/page-states"
import { PasswordInput } from "@/components/password-input"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Spinner } from "@/components/ui/spinner"
import { changePassword, requestPasswordReset } from "@/lib/api"
import { useAccount } from "@/lib/account"
import { useI18n } from "@/lib/i18n-context"
import { usePasswordRules } from "@/lib/password"
import { cn } from "@/lib/utils"

const EMPTY = { current: "", next: "", confirm: "" }

function ResetDialog({ open, onOpenChange }) {
  const { t } = useI18n()
  const { user } = useAccount()
  const [step, setStep] = useState("start") // start | sending | sent
  const [error, setError] = useState(false)
  const [cooldown, setCooldown] = useState(0)

  useEffect(() => {
    if (!cooldown) return
    const id = setTimeout(() => setCooldown((c) => c - 1), 1000)
    return () => clearTimeout(id)
  }, [cooldown])

  const masked = user?.email?.replace(/^(.)(.*)(@.*)$/, (_, a, b, c) => a + "•".repeat(Math.min(b.length, 6)) + c)

  const send = async () => {
    setStep((s) => (s === "sent" ? "sent" : "sending"))
    setError(false)
    try {
      await requestPasswordReset()
      setStep("sent")
      setCooldown(30)
    } catch {
      setError(true)
      setStep((s) => (s === "sent" ? "sent" : "start"))
    }
  }

  const change = (next) => {
    onOpenChange(next)
    if (!next) {
      setStep("start")
      setError(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={change}>
      <DialogContent>
        <DialogHeader>
          <span className="mb-1 flex size-10 items-center justify-center rounded-md bg-accent text-accent-foreground">
            {step === "sent" ? <MailCheckIcon className="size-5" /> : <MailIcon className="size-5" />}
          </span>
          <DialogTitle>{step === "sent" ? t("security.resetSentTitle") : t("security.resetTitle")}</DialogTitle>
          <DialogDescription>
            {step === "sent" ? t("security.resetSentBody", { email: masked }) : t("security.resetBody", { email: masked })}
          </DialogDescription>
        </DialogHeader>
        {error && (
          <Alert variant="destructive">
            <AlertDescription>{t("states.actionFailed")}</AlertDescription>
          </Alert>
        )}
        <DialogFooter>
          {step === "sent" ? (
            <>
              <Button variant="outline" onClick={send} disabled={cooldown > 0}>
                {cooldown > 0 ? t("security.resendIn", { s: cooldown }) : t("security.resend")}
              </Button>
              <Button onClick={() => change(false)}>{t("common.done")}</Button>
            </>
          ) : (
            <>
              <Button variant="outline" onClick={() => change(false)} disabled={step === "sending"}>
                {t("common.cancel")}
              </Button>
              <Button onClick={send} disabled={step === "sending"}>
                {step === "sending" && <Spinner data-icon="inline-start" />}
                {t("security.sendLink")}
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default function SecurityPage() {
  const RULES = usePasswordRules()
  const { t } = useI18n()
  const rulesId = useId()
  const [values, setValues] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [submitted, setSubmitted] = useState(false)
  const [saving, setSaving] = useState(false)
  const [failed, setFailed] = useState(false)
  const [success, setSuccess] = useState(false)
  const [resetOpen, setResetOpen] = useState(false)

  const validate = (v) => {
    const e = {}
    if (!v.current) e.current = "required"
    if (!v.next) e.next = "required"
    else if (!RULES.every((r) => r.test(v.next))) e.next = "passwordWeak"
    if (!v.confirm) e.confirm = "required"
    else if (v.confirm !== v.next) e.confirm = "passwordMismatch"
    return e
  }

  const set = (key) => (ev) => {
    const next = { ...values, [key]: ev.target.value }
    setValues(next)
    setSuccess(false)
    setFailed(false)
    if (submitted) setErrors(validate(next))
    else if (key === "confirm" || key === "next")
      setErrors((e) => ({ ...e, confirm: next.confirm && next.confirm.length >= next.next.length && next.confirm !== next.next ? "passwordMismatch" : undefined }))
  }

  const submit = async (ev) => {
    ev.preventDefault()
    setSubmitted(true)
    const found = validate(values)
    setErrors(found)
    const first = Object.keys(found).find((k) => found[k])
    if (first) {
      document.getElementById(`pw-${first}`)?.focus()
      return
    }
    setSaving(true)
    setFailed(false)
    try {
      await changePassword({ current: values.current, next: values.next })
      setValues(EMPTY)
      setErrors({})
      setSubmitted(false)
      setSuccess(true)
      toast.success(t("security.changed"))
    } catch (err) {
      if (err.field) setErrors({ [err.field]: err.code })
      else setFailed(true)
    } finally {
      setSaving(false)
    }
  }

  const err = (key) => errors[key] && <FieldError>{t(`validation.${errors[key]}`)}</FieldError>

  return (
    <>
      <PageIntro>{t("security.intro")}</PageIntro>
      {/* Wide screens: change-password on the left, the reset-by-email card beside it. */}
      <div className="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)] xl:items-start">
      <Card>
        <CardHeader>
          <CardTitle>{t("security.changeTitle")}</CardTitle>
          <CardDescription>{t("security.changeHint")}</CardDescription>
        </CardHeader>
        <form onSubmit={submit} noValidate className="flex flex-col gap-6">
          <CardContent className="flex flex-col gap-5">
            {success && (
              <Alert className="border-success/30 bg-success-soft text-success">
                <CheckIcon />
                <AlertTitle>{t("security.changed")}</AlertTitle>
                <AlertDescription className="text-success/90">{t("security.changedBody")}</AlertDescription>
              </Alert>
            )}
            {failed && (
              <Alert variant="destructive">
                <AlertTitle>{t("security.failedTitle")}</AlertTitle>
                <AlertDescription>{t("states.actionFailed")}</AlertDescription>
              </Alert>
            )}
            <div className="grid gap-8 md:grid-cols-[1fr_15rem]">
              <FieldGroup>
                <Field data-invalid={!!errors.current || undefined}>
                  <div className="flex items-center justify-between gap-2">
                    <FieldLabel htmlFor="pw-current">{t("security.current")}</FieldLabel>
                    <Button type="button" variant="link" size="sm" className="h-auto px-0" onClick={() => setResetOpen(true)}>
                      {t("security.forgot")}
                    </Button>
                  </div>
                  <PasswordInput id="pw-current" value={values.current} onChange={set("current")} invalid={!!errors.current} autoComplete="current-password" />
                  {err("current")}
                </Field>
                <Field data-invalid={!!errors.next || undefined}>
                  <FieldLabel htmlFor="pw-next">{t("security.new")}</FieldLabel>
                  <PasswordInput id="pw-next" value={values.next} onChange={set("next")} invalid={!!errors.next} autoComplete="new-password" describedBy={rulesId} />
                  {err("next")}
                </Field>
                <Field data-invalid={!!errors.confirm || undefined}>
                  <FieldLabel htmlFor="pw-confirm">{t("security.confirm")}</FieldLabel>
                  <PasswordInput id="pw-confirm" value={values.confirm} onChange={set("confirm")} invalid={!!errors.confirm} autoComplete="new-password" />
                  {err("confirm")}
                </Field>
              </FieldGroup>

              <div id={rulesId} className="flex flex-col gap-3 rounded-md border bg-muted/50 p-4 md:self-start">
                <p className="text-sm font-medium">{t("security.rulesTitle")}</p>
                <ul className="flex flex-col gap-2">
                  {RULES.map((r) => {
                    const ok = r.test(values.next)
                    return (
                      <li key={r.key} className={cn("flex items-start gap-2 text-sm", ok ? "text-success" : "text-muted-foreground")}>
                        {ok ? <CheckIcon className="mt-0.5 size-4 shrink-0" /> : <CircleIcon className="mt-1 size-3 shrink-0 opacity-60" />}
                        <span>
                          {t(`security.rules.${r.key}`, { n: r.n })}
                          <span className="sr-only">{ok ? ` (${t("security.ruleMet")})` : ""}</span>
                        </span>
                      </li>
                    )
                  })}
                </ul>
              </div>
            </div>
          </CardContent>
          <CardFooter className="justify-end gap-2 border-t">
            <Button
              type="button"
              variant="outline"
              disabled={saving || (!values.current && !values.next && !values.confirm)}
              onClick={() => {
                setValues(EMPTY)
                setErrors({})
                setSubmitted(false)
              }}
            >
              {t("common.cancel")}
            </Button>
            <Button type="submit" disabled={saving}>
              {saving && <Spinner data-icon="inline-start" />}
              {t("security.update")}
            </Button>
          </CardFooter>
        </form>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>{t("security.resetCardTitle")}</CardTitle>
          <CardDescription>{t("security.resetCardHint")}</CardDescription>
        </CardHeader>
        <CardFooter>
          <Button variant="outline" onClick={() => setResetOpen(true)}>
            <MailIcon data-icon="inline-start" />
            {t("security.sendLink")}
          </Button>
        </CardFooter>
      </Card>
      </div>
      <ResetDialog open={resetOpen} onOpenChange={setResetOpen} />
    </>
  )
}
