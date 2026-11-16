import { useState } from "react"
import { CheckIcon, CircleIcon, KeyRoundIcon, LogInIcon } from "lucide-react"
import { PasswordInput } from "@/components/password-input"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { LanguageSwitcher } from "@/components/language-switcher"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Spinner } from "@/components/ui/spinner"
import { confirmPasswordReset } from "@/lib/api"
import { login } from "@/lib/auth"
import { useI18n } from "@/lib/i18n-context"
import { usePasswordRules } from "@/lib/password"
import { cn } from "@/lib/utils"

export default function ResetPasswordPage() {
  const passwordRules = usePasswordRules()
  const { t } = useI18n()
  const token = new URLSearchParams(window.location.search).get("token") ?? ""
  const [values, setValues] = useState({ next: "", confirm: "" })
  const [errors, setErrors] = useState({})
  const [status, setStatus] = useState(token ? "idle" : "invalid")
  const [failed, setFailed] = useState(false)

  const set = (key) => (ev) => {
    setValues((v) => ({ ...v, [key]: ev.target.value }))
    setErrors({})
    setFailed(false)
  }

  const submit = async (ev) => {
    ev.preventDefault()
    const found = {}
    if (!values.next) found.next = "required"
    else if (!passwordRules.every((r) => r.test(values.next))) found.next = "passwordWeak"
    if (!values.confirm) found.confirm = "required"
    else if (values.confirm !== values.next) found.confirm = "passwordMismatch"
    setErrors(found)
    if (Object.keys(found).length) return

    setStatus("saving")
    try {
      await confirmPasswordReset(token, values.next)
      setStatus("done")
    } catch (error) {
      if (error.code === "resetLinkInvalid") setStatus("invalid")
      else {
        setFailed(true)
        setStatus("idle")
      }
    }
  }

  const err = (key) => errors[key] && <FieldError>{t(`validation.${errors[key]}`)}</FieldError>

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-6 px-4 py-10">
      <LanguageSwitcher align="center" />
      <Card className="w-full max-w-md">
        <CardHeader>
          <span className="mb-1 flex size-10 items-center justify-center rounded-md bg-accent text-accent-foreground">
            <KeyRoundIcon className="size-5" />
          </span>
          <CardTitle>{t("reset.title")}</CardTitle>
          <CardDescription>{t("reset.intro")}</CardDescription>
        </CardHeader>

        {status === "done" ? (
          <>
            <CardContent>
              <Alert className="border-success/30 bg-success-soft text-success">
                <CheckIcon />
                <AlertTitle>{t("reset.doneTitle")}</AlertTitle>
                <AlertDescription className="text-success/90">{t("reset.doneBody")}</AlertDescription>
              </Alert>
            </CardContent>
            <CardFooter>
              <Button onClick={login}>
                <LogInIcon data-icon="inline-start" />
                {t("signedOut.signIn")}
              </Button>
            </CardFooter>
          </>
        ) : status === "invalid" ? (
          <>
            <CardContent>
              <Alert variant="destructive">
                <AlertTitle>{t("reset.invalidTitle")}</AlertTitle>
                <AlertDescription>{t("reset.invalidBody")}</AlertDescription>
              </Alert>
            </CardContent>
            <CardFooter>
              <Button variant="outline" onClick={login}>
                <LogInIcon data-icon="inline-start" />
                {t("signedOut.signIn")}
              </Button>
            </CardFooter>
          </>
        ) : (
          <form onSubmit={submit} noValidate className="flex flex-col gap-6">
            <CardContent className="flex flex-col gap-5">
              {failed && (
                <Alert variant="destructive">
                  <AlertDescription>{t("states.actionFailed")}</AlertDescription>
                </Alert>
              )}
              <FieldGroup>
                <Field data-invalid={!!errors.next || undefined}>
                  <FieldLabel htmlFor="reset-next">{t("security.new")}</FieldLabel>
                  <PasswordInput id="reset-next" value={values.next} onChange={set("next")} invalid={!!errors.next} autoComplete="new-password" />
                  {err("next")}
                </Field>
                <Field data-invalid={!!errors.confirm || undefined}>
                  <FieldLabel htmlFor="reset-confirm">{t("security.confirm")}</FieldLabel>
                  <PasswordInput id="reset-confirm" value={values.confirm} onChange={set("confirm")} invalid={!!errors.confirm} autoComplete="new-password" />
                  {err("confirm")}
                </Field>
              </FieldGroup>
              <ul className="flex flex-col gap-2 rounded-md border bg-muted/50 p-4">
                {passwordRules.map((r) => {
                  const ok = r.test(values.next)
                  return (
                    <li key={r.key} className={cn("flex items-start gap-2 text-sm", ok ? "text-success" : "text-muted-foreground")}>
                      {ok ? <CheckIcon className="mt-0.5 size-4 shrink-0" /> : <CircleIcon className="mt-1 size-3 shrink-0 opacity-60" />}
                      {t(`security.rules.${r.key}`, { n: r.n })}
                    </li>
                  )
                })}
              </ul>
            </CardContent>
            <CardFooter className="border-t">
              <Button type="submit" className="w-full" disabled={status === "saving"}>
                {status === "saving" && <Spinner data-icon="inline-start" />}
                {t("reset.submit")}
              </Button>
            </CardFooter>
          </form>
        )}
      </Card>
    </div>
  )
}
