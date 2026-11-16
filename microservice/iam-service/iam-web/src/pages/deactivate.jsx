import { useState } from "react"
import { Link } from "react-router-dom"
import { ClockIcon, PowerIcon, TriangleAlertIcon } from "lucide-react"
import { toast } from "sonner"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { CardSkeleton, ErrorState, PageIntro } from "@/components/page-states"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldDescription, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { cancelDeactivation, getDeactivation, requestDeactivation } from "@/lib/api"
import { useAccount } from "@/lib/account"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

const REASONS = ["not_needed", "other_account", "privacy", "left_me", "other"]

export default function DeactivatePage() {
  const { t, fmt } = useI18n()
  const { user } = useAccount()
  const state = useAsync(getDeactivation)
  const [reason, setReason] = useState("")
  const [open, setOpen] = useState(false)
  const [typed, setTyped] = useState("")
  const [cancelling, setCancelling] = useState(false)

  const cancel = async () => {
    setCancelling(true)
    try {
      const next = await cancelDeactivation()
      state.setData(next)
      toast.success(t("deactivate.cancelledToast"))
    } catch {
      toast.error(t("states.actionFailed"))
    } finally {
      setCancelling(false)
    }
  }

  const body = () => {
    if (state.status === "loading") return <CardSkeleton rows={3} />
    if (state.status === "error") return <ErrorState onRetry={state.reload} />

    if (state.data?.status === "requested") {
      return (
        <Card>
          <CardContent className="flex flex-col gap-4">
            <Alert className="border-warning/30 bg-warning-soft text-warning">
              <ClockIcon />
              <AlertTitle>{t("deactivate.requestedTitle")}</AlertTitle>
              <AlertDescription className="text-warning/90">
                {t("deactivate.requestedBody", { date: fmt.dateTime(state.data.requestedAt) })}
              </AlertDescription>
            </Alert>
            <p className="max-w-[62ch] text-sm text-muted-foreground">{t("deactivate.requestedNext")}</p>
          </CardContent>
          <CardFooter className="border-t">
            <Button variant="outline" onClick={cancel} disabled={cancelling}>
              {cancelling && <Spinner data-icon="inline-start" />}
              {t("deactivate.cancelRequest")}
            </Button>
          </CardFooter>
        </Card>
      )
    }

    return (
      <Card className="border-destructive/30 ring-destructive/20">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-destructive">
            <TriangleAlertIcon className="size-4.5" />
            {t("deactivate.dangerTitle")}
          </CardTitle>
          <CardDescription>{t("deactivate.dangerHint")}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          <div className="flex flex-col gap-2">
            <p className="text-sm font-medium">{t("deactivate.whatHappens")}</p>
            <ul className="flex list-disc flex-col gap-1.5 pl-5 text-sm text-muted-foreground marker:text-destructive/60">
              <li>{t("deactivate.effects.signin")}</li>
              <li>{t("deactivate.effects.sessions")}</li>
              <li>{t("deactivate.effects.apps")}</li>
              <li>{t("deactivate.effects.restore")}</li>
            </ul>
          </div>
          <p className="max-w-[62ch] text-sm text-muted-foreground">
            {t("deactivate.alternative")}{" "}
            <Link to="/sessions" className="font-medium text-foreground underline underline-offset-4">
              {t("deactivate.alternativeLink")}
            </Link>
          </p>
          <FieldGroup className="max-w-sm">
            <Field>
              <FieldLabel htmlFor="deactivate-reason">
                {t("deactivate.reason")} <span className="font-normal text-muted-foreground">{t("common.optional")}</span>
              </FieldLabel>
              <Select value={reason} onValueChange={setReason}>
                <SelectTrigger id="deactivate-reason" className="w-full">
                  <SelectValue placeholder={t("deactivate.reasonPlaceholder")} />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    {REASONS.map((r) => (
                      <SelectItem key={r} value={r}>{t(`deactivate.reasons.${r}`)}</SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
            </Field>
          </FieldGroup>
        </CardContent>
        <CardFooter className="border-t border-destructive/20 bg-destructive/[0.03]">
          <Button variant="destructive" onClick={() => { setTyped(""); setOpen(true) }}>
            <PowerIcon data-icon="inline-start" />
            {t("deactivate.request")}
          </Button>
        </CardFooter>
      </Card>
    )
  }

  const username = user?.username ?? ""

  return (
    <>
      <PageIntro>{t("deactivate.intro")}</PageIntro>
      {body()}
      <ConfirmDialog
        open={open}
        onOpenChange={setOpen}
        icon={PowerIcon}
        destructive
        title={t("deactivate.confirmTitle")}
        description={t("deactivate.confirmBody")}
        confirmLabel={t("deactivate.request")}
        confirmDisabled={!username || typed.trim() !== username}
        onConfirm={async () => {
          const next = await requestDeactivation(reason || null)
          state.setData(next)
        }}
      >
        <Field>
          <FieldLabel htmlFor="deactivate-confirm">{t("deactivate.typeToConfirm", { username })}</FieldLabel>
          <Input id="deactivate-confirm" autoComplete="off" spellCheck={false} value={typed} onChange={(e) => setTyped(e.target.value)} />
          <FieldDescription>{t("deactivate.typeHint")}</FieldDescription>
        </Field>
      </ConfirmDialog>
    </>
  )
}
