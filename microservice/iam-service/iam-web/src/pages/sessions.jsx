import { useState } from "react"
import { LaptopIcon, LogOutIcon, MonitorSmartphoneIcon, ShieldCheckIcon, SmartphoneIcon, TabletIcon } from "lucide-react"
import { toast } from "sonner"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { CardSkeleton, EmptyState, ErrorState, PageIntro } from "@/components/page-states"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { getSessions, terminateOtherSessions, terminateSession } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

const DEVICE_ICON = { desktop: LaptopIcon, mobile: SmartphoneIcon, tablet: TabletIcon }

function DeviceCell({ s }) {
  const Icon = DEVICE_ICON[s.type] ?? LaptopIcon
  return (
    <div className="flex items-center gap-3">
      <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-muted text-muted-foreground">
        <Icon className="size-4.5" />
      </span>
      <div className="flex min-w-0 flex-col">
        <span className="truncate font-medium">{s.device}</span>
        <span className="truncate text-xs text-muted-foreground">
          {s.browser}, {s.os}
        </span>
      </div>
    </div>
  )
}

export default function SessionsPage() {
  const { t, fmt } = useI18n()
  const sessions = useAsync(getSessions)
  const [target, setTarget] = useState(null)
  const [allOpen, setAllOpen] = useState(false)

  const refreshing = sessions.status === "refreshing"

  const content = () => {
    if (sessions.status === "loading") {
      return (
        <>
          <CardSkeleton rows={1} />
          <CardSkeleton rows={4} />
        </>
      )
    }
    if (sessions.status === "error" && !sessions.data) return <ErrorState onRetry={sessions.reload} />

    const list = sessions.data ?? []
    const current = list.find((s) => s.current)
    const others = list.filter((s) => !s.current)

    return (
      <>
        {current && (
          <Card className="relative overflow-hidden">
            <span aria-hidden="true" className="absolute inset-y-0 left-0 w-1 bg-primary" />
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShieldCheckIcon className="size-4.5 text-primary" />
                {t("sessions.thisDevice")}
              </CardTitle>
              <CardDescription>{t("sessions.thisDeviceHint")}</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-4 sm:grid-cols-[1.4fr_1fr_1fr]">
              <DeviceCell s={current} />
              <div className="flex flex-col text-sm">
                <span className="text-muted-foreground">{t("sessions.location")}</span>
                <span>{current.location}</span>
                <span className="tabular text-xs text-muted-foreground">{current.ip}</span>
              </div>
              <div className="flex flex-col text-sm">
                <span className="text-muted-foreground">{t("sessions.signedIn")}</span>
                <span>{fmt.dateTime(current.signedIn)}</span>
              </div>
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              {t("sessions.otherTitle")}
              {refreshing && <Spinner className="text-muted-foreground" />}
            </CardTitle>
            <CardDescription>{t("sessions.otherHint", { n: others.length })}</CardDescription>
            {others.length > 0 && (
              <CardAction>
                <Button variant="destructive" onClick={() => setAllOpen(true)}>
                  <LogOutIcon data-icon="inline-start" />
                  <span className="hidden sm:inline">{t("sessions.signOutAll")}</span>
                  <span className="sm:hidden">{t("sessions.signOutAllShort")}</span>
                </Button>
              </CardAction>
            )}
          </CardHeader>
          <CardContent>
            {others.length === 0 ? (
              <EmptyState icon={MonitorSmartphoneIcon} title={t("sessions.emptyTitle")} description={t("sessions.emptyBody")} />
            ) : (
              <>
                <div className="hidden md:block">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>{t("sessions.device")}</TableHead>
                        <TableHead>{t("sessions.location")}</TableHead>
                        <TableHead>{t("sessions.ip")}</TableHead>
                        <TableHead>{t("sessions.lastActive")}</TableHead>
                        <TableHead className="text-right">
                          <span className="sr-only">{t("common.actions")}</span>
                        </TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {others.map((s) => (
                        <TableRow key={s.id}>
                          <TableCell><DeviceCell s={s} /></TableCell>
                          <TableCell>{s.location}</TableCell>
                          <TableCell className="tabular text-muted-foreground">{s.ip}</TableCell>
                          <TableCell title={fmt.dateTime(s.lastActive)}>{fmt.relative(s.lastActive)}</TableCell>
                          <TableCell className="text-right">
                            <Button variant="ghost" size="sm" onClick={() => setTarget(s)}>
                              {t("sessions.signOut")}
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
                <ul className="flex flex-col gap-3 md:hidden">
                  {others.map((s) => (
                    <li key={s.id} className="flex flex-col gap-3 rounded-lg border p-3">
                      <DeviceCell s={s} />
                      <dl className="grid grid-cols-2 gap-x-3 gap-y-2 text-sm">
                        <div className="flex flex-col">
                          <dt className="text-xs text-muted-foreground">{t("sessions.location")}</dt>
                          <dd>{s.location}</dd>
                        </div>
                        <div className="flex flex-col">
                          <dt className="text-xs text-muted-foreground">{t("sessions.lastActive")}</dt>
                          <dd>{fmt.relative(s.lastActive)}</dd>
                        </div>
                        <div className="flex flex-col">
                          <dt className="text-xs text-muted-foreground">{t("sessions.ip")}</dt>
                          <dd className="tabular">{s.ip}</dd>
                        </div>
                      </dl>
                      <Button variant="outline" size="sm" onClick={() => setTarget(s)}>
                        <LogOutIcon data-icon="inline-start" />
                        {t("sessions.signOut")}
                      </Button>
                    </li>
                  ))}
                </ul>
              </>
            )}
          </CardContent>
        </Card>
      </>
    )
  }

  return (
    <>
      <PageIntro>{t("sessions.intro")}</PageIntro>
      {content()}

      <ConfirmDialog
        open={!!target}
        onOpenChange={(o) => !o && setTarget(null)}
        icon={LogOutIcon}
        destructive
        title={t("sessions.confirmOneTitle")}
        description={t("sessions.confirmOneBody", { device: target ? `${target.device} (${target.browser})` : "", location: target?.location ?? "" })}
        confirmLabel={t("sessions.signOut")}
        onConfirm={async () => {
          await terminateSession(target.id)
          toast.success(t("sessions.signedOutOne", { device: target.device }))
          sessions.reload()
        }}
      />
      <ConfirmDialog
        open={allOpen}
        onOpenChange={setAllOpen}
        icon={LogOutIcon}
        destructive
        title={t("sessions.confirmAllTitle")}
        description={t("sessions.confirmAllBody")}
        confirmLabel={t("sessions.signOutAll")}
        onConfirm={async () => {
          const count = (sessions.data ?? []).filter((s) => !s.current).length
          const result = await terminateOtherSessions()
          toast.success(t("sessions.signedOutAll", { n: result?.count ?? count }))
          sessions.reload()
        }}
      />
    </>
  )
}

