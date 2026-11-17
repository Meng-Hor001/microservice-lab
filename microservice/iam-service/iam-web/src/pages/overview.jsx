import { useState } from "react"
import { Link, useSearchParams } from "react-router-dom"
import {
  ActivityIcon,
  ArrowRightIcon,
  LaptopIcon,
  SmartphoneIcon,
  TabletIcon,
} from "lucide-react"
import { ProviderLogo } from "@/components/provider-logo"
import { TopbarTabs } from "@/components/top-tabs"
import { Checkup, SignInChart } from "@/components/overview-cards"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useAccount } from "@/lib/account"
import { getActivity, getLoginHistory, getProviders, getSessions } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { PROVIDERS } from "@/lib/providers"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const DAY = 86400000
const DEVICE_ICON = { desktop: LaptopIcon, laptop: LaptopIcon, mobile: SmartphoneIcon, phone: SmartphoneIcon, tablet: TabletIcon }
const SIX_MONTHS = 183 * DAY
const TABS = ["checkup", "signIns", "devices", "activity", "linked"]

// The API caps a page at 100 rows; read up to 5 pages so the chart covers busy accounts too.
async function loadHistory(days) {
  const items = []
  for (let page = 1; page <= 5; page++) {
    const { items: rows, total } = await getLoginHistory({ range: String(days), page, pageSize: 100 })
    items.push(...rows)
    if (items.length >= total || rows.length === 0) break
  }
  return items
}

/* ---------- bottom row ---------- */

function Devices({ sessions }) {
  const { t, fmt } = useI18n()
  const list = [...(sessions.data ?? [])].sort((a, b) => Number(b.current) - Number(a.current)).slice(0, 4)

  return (
    <section className="motion-rise flex flex-col gap-5 rounded-md bg-card p-4 text-card-foreground ring-1 ring-foreground/10 lg:col-span-5" style={{ "--i": 3 }}>
      <div className="flex items-center justify-between gap-3">
        <h2 className="font-medium">{t("overview.devices.title")}</h2>
        <span className="tabular rounded-md px-3 py-1 text-xs text-muted-foreground ring-1 ring-border">
          {sessions.data?.length ?? "–"}
        </span>
      </div>
      <ul className="grid gap-x-8 gap-y-1 md:grid-cols-2">
        {sessions.status === "loading" &&
          [0, 1, 2].map((i) => <Skeleton key={i} className="h-14" />)}
        {list.map((s) => {
          const Icon = DEVICE_ICON[s.type] ?? LaptopIcon
          return (
            <li key={s.id} className="flex items-center gap-3 rounded-md py-2">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground [&_svg]:size-4.5">
                <Icon />
              </span>
              <span className="flex min-w-0 flex-1 flex-col">
                <span className="truncate text-sm font-medium">{s.device}</span>
                <span className="truncate text-xs text-muted-foreground">
                  {s.location} · {fmt.relative(s.lastActive)}
                </span>
              </span>
              {s.current && (
                <span className="flex shrink-0 items-center gap-2 text-xs text-primary">
                  <span className="size-2 rounded-full bg-success" />
                  {t("overview.devices.thisDevice")}
                </span>
              )}
            </li>
          )
        })}
      </ul>
      <Button asChild variant="outline" className="mt-auto self-start">
        <Link to="/sessions">
          {t("overview.devices.manage")}
          <ArrowRightIcon data-icon="inline-end" />
        </Link>
      </Button>
    </section>
  )
}

function RecentActivity({ activity }) {
  const { t, fmt } = useI18n()
  const list = (activity.data ?? []).slice(0, 5)

  return (
    <Card className="motion-rise lg:col-span-4" style={{ "--i": 4 }}>
      <CardHeader>
        <CardTitle>{t("overview.activity.title")}</CardTitle>
        <CardAction>
          <Button asChild variant="link" size="sm" className="px-0">
            <Link to="/activity">{t("overview.activity.all")}</Link>
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent>
        {activity.status === "loading" ? (
          <div className="flex flex-col gap-3">
            {[0, 1, 2, 3].map((i) => <Skeleton key={i} className="h-10" />)}
          </div>
        ) : list.length === 0 ? (
          <p className="py-6 text-sm text-muted-foreground">{t("overview.activity.empty")}</p>
        ) : (
          <ul className="flex flex-col">
            {list.map((e) => (
              <li key={e.id} className="flex items-center gap-3 border-b border-foreground/6 py-3 last:border-0">
                <span className="flex size-8 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground [&_svg]:size-4">
                  <ActivityIcon />
                </span>
                <span className="min-w-0 flex-1 truncate text-sm">{t(`activity.events.${e.type}`)}</span>
                <time dateTime={e.at} className="tabular shrink-0 text-xs text-muted-foreground">
                  {fmt.relative(e.at)}
                </time>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

function LinkedAccounts({ providers }) {
  const { t } = useI18n()
  return (
    <Card className="motion-rise lg:col-span-3" style={{ "--i": 5 }}>
      <CardHeader>
        <CardTitle>{t("overview.linked.title")}</CardTitle>
        <CardAction>
          <Button asChild variant="link" size="sm" className="px-0">
            <Link to="/linked-accounts">{t("overview.linked.manage")}</Link>
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent>
        {providers.status === "loading" ? (
          <div className="flex flex-col gap-3">
            {[0, 1, 2].map((i) => <Skeleton key={i} className="h-10" />)}
          </div>
        ) : (
          <ul className="grid gap-x-8 gap-y-1 sm:grid-cols-2 xl:grid-cols-4">
            {(providers.data ?? []).map((p) => (
              <li key={p.id} className="flex items-center gap-3 py-2">
                <span className="flex size-9 shrink-0 items-center justify-center rounded-md bg-muted">
                  <ProviderLogo id={p.id} className="size-4" />
                </span>
                <span className="flex-1 text-sm font-medium">{PROVIDERS[p.id]?.name ?? p.id}</span>
                <span className={cn("text-xs", p.linked ? "text-success" : "text-muted-foreground")}>
                  {p.linked ? t("overview.linked.linked") : t("overview.linked.notLinked")}
                </span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

/* ---------- page ---------- */

export default function OverviewPage() {
  const { t } = useI18n()
  const { user } = useAccount()
  const [days, setDays] = useState(7)
  const [now] = useState(() => Date.now())
  const [params] = useSearchParams()
  const tab = TABS.includes(params.get("tab")) ? params.get("tab") : TABS[0]

  const sessions = useAsync(getSessions)
  const providers = useAsync(getProviders)
  const activity = useAsync(getActivity)
  // Fetch twice the visible range so the chart can compare with the previous period.
  const history = useAsync(() => loadHistory(Math.max(days * 2, 30)), [days])
  const month = useAsync(() => loadHistory(30))

  const monthEntries = month.data ?? []
  const failed7 = monthEntries.filter((e) => e.result !== "success" && now - new Date(e.at) < 7 * DAY).length
  const linkedCount = (providers.data ?? []).filter((p) => p.linked).length

  const lastPasswordChange = (activity.data ?? [])
    .filter((e) => e.type === "password_changed" || e.type === "account_created")
    .map((e) => new Date(e.at).getTime())
    .sort((a, b) => b - a)[0]

  const checks = [
    { key: "phone", ok: !!user?.phone, to: "/profile", label: t("overview.checkup.phone"), hint: t("overview.checkup.phoneHint") },
    { key: "linked", ok: linkedCount > 0, to: "/linked-accounts", label: t("overview.checkup.linked"), hint: t("overview.checkup.linkedHint") },
    { key: "failed", ok: failed7 === 0, to: "/login-history", label: t("overview.checkup.failed"), hint: t("overview.checkup.failedHint", { n: failed7 }) },
    {
      key: "password",
      ok: !!lastPasswordChange && now - lastPasswordChange < SIX_MONTHS,
      to: "/security",
      label: t("overview.checkup.password"),
      hint: t("overview.checkup.passwordHint"),
    },
  ]
  const checksLoading = !user || providers.status === "loading" || month.status === "loading" || activity.status === "loading"


  const badges = {
    checkup: checksLoading ? 0 : checks.filter((c) => !c.ok).length,
    signIns: failed7,
    devices: (sessions.data ?? []).filter((s) => !s.current).length,
  }

  // One section per tab; the tab bar itself lives in the top header.
  const sections = {
    checkup: <Checkup checks={checks} loading={checksLoading} />,
    signIns: <SignInChart history={history} days={days} setDays={setDays} />,
    devices: <Devices sessions={sessions} />,
    activity: <RecentActivity activity={activity} />,
    linked: <LinkedAccounts providers={providers} />,
  }

  return (
    <div className="flex flex-col gap-6">
      <TopbarTabs
        label={t("overview.title")}
        items={TABS.map((key) => ({
          key,
          to: { search: `?tab=${key}` },
          label: t(`overview.tabs.${key}`),
          active: key === tab,
          badge: badges[key] ?? 0,
        }))}
      />

      <h1 className="motion-rise text-3xl leading-tight font-medium tracking-tight">{t("overview.title")}</h1>

      {/* Flex, not grid: each section's lg:col-span-* is ignored so it fills the width. */}
      <div key={tab} className="flex flex-col gap-6">
        {sections[tab]}
      </div>
    </div>
  )
}
