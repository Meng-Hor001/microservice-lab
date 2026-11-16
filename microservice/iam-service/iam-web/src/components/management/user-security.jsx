import { useState } from "react"
import { toast } from "sonner"
import { HistoryIcon, LaptopIcon, LogOutIcon, MonitorSmartphoneIcon, SmartphoneIcon, TabletIcon } from "lucide-react"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { DataBody, DataHead, DataRow, DataTable, revealOnRow, Td, Th } from "@/components/management/data-table"
import { Pager, PanelError, StatusDot, TableSkeleton } from "@/components/management/states"
import { EmptyState } from "@/components/page-states"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { FilterSelect } from "@/components/filter-select"
import { useAccess } from "@/lib/access"
import { getUserLoginHistory, getUserSessions, revokeAllUserSessions, revokeUserSession } from "@/lib/admin-api"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

const DEVICE_ICONS = { mobile: SmartphoneIcon, tablet: TabletIcon, desktop: LaptopIcon }
const HISTORY_SIZE = 10

/** Admin view of one user's signed-in devices, with sign-out per device or everywhere. */
export function UserSessionsTab({ user }) {
  const { t, fmt } = useI18n()
  const { can } = useAccess()
  const canRevoke = can("user:update")
  const sessions = useAsync(() => getUserSessions(user.uuid), [user.uuid])
  const [target, setTarget] = useState(null)
  const [allOpen, setAllOpen] = useState(false)
  const items = sessions.data ?? []

  const body = () => {
    if (sessions.status === "loading") return <TableSkeleton rows={3} />
    if (sessions.status === "error") return <PanelError onRetry={sessions.reload} />
    if (items.length === 0) {
      return (
        <EmptyState
          icon={MonitorSmartphoneIcon}
          title={t("mgmt.userSecurity.noSessions")}
          description={t("mgmt.userSecurity.noSessionsBody")}
        />
      )
    }
    return (
      <DataTable>
        <DataHead>
          <Th>{t("sessions.device")}</Th>
          <Th className="hidden md:table-cell">{t("sessions.location")}</Th>
          <Th className="hidden lg:table-cell">{t("sessions.signedIn")}</Th>
          <Th>{t("sessions.lastActive")}</Th>
          <Th className="w-28">
            <span className="sr-only">{t("mgmt.common.actions")}</span>
          </Th>
        </DataHead>
        <DataBody>
          {items.map((s) => {
            const Icon = DEVICE_ICONS[s.type] ?? LaptopIcon
            return (
              <DataRow key={s.id}>
                <Td>
                  <div className="flex items-center gap-3">
                    <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground">
                      <Icon className="size-4" />
                    </span>
                    <div className="flex min-w-0 flex-col leading-snug">
                      <span className="truncate font-medium">{s.device}</span>
                      <span className="truncate text-muted-foreground">
                        {s.browser}, {s.os}
                      </span>
                    </div>
                  </div>
                </Td>
                <Td className="hidden md:table-cell">
                  <div className="flex flex-col leading-snug">
                    <span>{s.location}</span>
                    <span className="tabular font-mono text-sm text-muted-foreground">{s.ip}</span>
                  </div>
                </Td>
                <Td className="tabular hidden whitespace-nowrap text-muted-foreground lg:table-cell">{fmt.dateTime(s.signedIn)}</Td>
                <Td className="tabular whitespace-nowrap text-muted-foreground">{fmt.relative(s.lastActive)}</Td>
                <Td className="text-right">
                  {canRevoke && (
                    <Button variant="ghost" size="sm" className={revealOnRow} onClick={() => setTarget(s)}>
                      {t("sessions.signOut")}
                    </Button>
                  )}
                </Td>
              </DataRow>
            )
          })}
        </DataBody>
      </DataTable>
    )
  }

  return (
    <Card className="gap-0 py-0">
      <CardHeader className="border-b py-5">
        <CardTitle className="flex items-center gap-2">
          {t("mgmt.userSecurity.sessionsTitle")}
          {sessions.status === "refreshing" && <Spinner className="text-muted-foreground" />}
        </CardTitle>
        <CardDescription>{t("mgmt.userSecurity.sessionsCount", { n: items.length })}</CardDescription>
        {canRevoke && items.length > 0 && (
          <CardAction>
            <Button variant="outline" onClick={() => setAllOpen(true)}>
              <LogOutIcon data-icon="inline-start" />
              {t("mgmt.userSecurity.signOutAll")}
            </Button>
          </CardAction>
        )}
      </CardHeader>
      {body()}

      <ConfirmDialog
        open={!!target}
        onOpenChange={(v) => !v && setTarget(null)}
        icon={LogOutIcon}
        destructive
        title={t("mgmt.userSecurity.confirmOneTitle", { device: target?.device ?? "" })}
        description={t("mgmt.userSecurity.confirmOneBody")}
        confirmLabel={t("sessions.signOut")}
        onConfirm={async () => {
          await revokeUserSession(user.uuid, target.id)
          toast.success(t("mgmt.userSecurity.signedOutOne"))
          sessions.reload()
        }}
      />
      <ConfirmDialog
        open={allOpen}
        onOpenChange={setAllOpen}
        icon={LogOutIcon}
        destructive
        title={t("mgmt.userSecurity.confirmAllTitle", { name: user.name })}
        description={t("mgmt.userSecurity.confirmAllBody")}
        confirmLabel={t("mgmt.userSecurity.signOutAll")}
        onConfirm={async () => {
          const result = await revokeAllUserSessions(user.uuid)
          toast.success(t("mgmt.userSecurity.signedOutAll", { n: result?.count ?? items.length }))
          sessions.reload()
        }}
      />
    </Card>
  )
}

/** Admin view of one user's successful and failed sign-in attempts. */
export function UserSignInsTab({ user }) {
  const { t, fmt } = useI18n()
  const [result, setResult] = useState("all")
  const [range, setRange] = useState("30")
  const [page, setPage] = useState(1)
  const history = useAsync(
    () =>
      getUserLoginHistory(user.uuid, {
        result,
        days: range === "all" ? undefined : Number(range),
        page,
        size: HISTORY_SIZE,
      }),
    [user.uuid, result, range, page]
  )

  // t() returns the key itself when there's no translation, so fall back to the raw value then.
  const label = (key, raw) => {
    const text = t(key)
    return text === key ? raw : text
  }
  const detail = (e) =>
    e.result === "failed" ? label(`history.reasons.${e.reason}`, e.reason) : label(`history.methods.${e.method}`, e.method)

  const body = () => {
    if (history.status === "loading") return <TableSkeleton rows={5} />
    if (history.status === "error") return <PanelError onRetry={history.reload} />
    const { items } = history.data
    if (items.length === 0) {
      return (
        <EmptyState icon={HistoryIcon} title={t("history.emptyTitle")} description={t("history.emptyBody")}>
          {(result !== "all" || range !== "all") && (
            <Button
              variant="outline"
              onClick={() => {
                setResult("all")
                setRange("all")
                setPage(1)
              }}
            >
              {t("history.clearFilters")}
            </Button>
          )}
        </EmptyState>
      )
    }
    return (
      <DataTable>
        <DataHead>
          <Th className="w-48">{t("history.when")}</Th>
          <Th className="w-40">{t("history.result")}</Th>
          <Th className="hidden md:table-cell">{t("history.device")}</Th>
          <Th className="hidden lg:table-cell">{t("sessions.ip")}</Th>
        </DataHead>
        <DataBody>
          {items.map((e) => (
            <DataRow key={e.id}>
              <Td className="tabular whitespace-nowrap">{fmt.dateTime(e.at)}</Td>
              <Td>
                <div className="flex flex-col leading-snug">
                  {e.result === "success" ? (
                    <StatusDot on label={t("history.success")} />
                  ) : (
                    <StatusDot tone="danger" label={t("history.failed")} />
                  )}
                  <span className="text-sm text-muted-foreground">{detail(e)}</span>
                </div>
              </Td>
              <Td className="hidden md:table-cell">
                <div className="flex flex-col leading-snug">
                  <span>{e.device}</span>
                  <span className="text-muted-foreground">
                    {e.browser}, {e.os}
                  </span>
                </div>
              </Td>
              <Td className="tabular hidden font-mono text-sm text-muted-foreground lg:table-cell">{e.ip}</Td>
            </DataRow>
          ))}
        </DataBody>
      </DataTable>
    )
  }

  return (
    <Card className="gap-0 py-0">
      <CardHeader className="border-b py-5">
        <CardTitle className="flex items-center gap-2">
          {t("mgmt.userSecurity.historyTitle")}
          {history.status === "refreshing" && <Spinner className="text-muted-foreground" />}
        </CardTitle>
        {history.data && <CardDescription>{t("history.count", { n: history.data.total })}</CardDescription>}
      </CardHeader>
      <div className="flex flex-col gap-3 border-b px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
        <FilterSelect
          value={result}
          onValueChange={(v) => {
            setResult(v)
            setPage(1)
          }}
          label={t("history.resultFilter")}
          options={[
            { value: "all", label: t("history.all") },
            { value: "success", label: t("history.success") },
            { value: "failed", label: t("history.failed") },
          ]}
        />
        <Select
          value={range}
          onValueChange={(v) => {
            setRange(v)
            setPage(1)
          }}
        >
          <SelectTrigger className="w-full sm:w-48" aria-label={t("history.rangeFilter")}>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              {["7", "30", "90", "all"].map((r) => (
                <SelectItem key={r} value={r}>
                  {t(`history.ranges.${r}`)}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>
      {body()}
      {history.data?.items?.length > 0 && history.status !== "loading" && (
        <Pager page={page} pages={history.data.pages} total={history.data.total} size={HISTORY_SIZE} onPage={setPage} />
      )}
    </Card>
  )
}
