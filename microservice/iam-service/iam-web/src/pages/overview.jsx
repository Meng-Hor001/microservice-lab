import { useMemo, useState } from "react"
import { Link } from "react-router-dom"
import {
  ActivityIcon,
  ArrowRightIcon,
  CircleAlertIcon,
  CircleCheckIcon,
  LaptopIcon,
  LinkIcon,
  LogInIcon,
  MonitorSmartphoneIcon,
  ShieldAlertIcon,
  SmartphoneIcon,
  TabletIcon,
} from "lucide-react"
import { ProviderLogo } from "@/components/provider-logo"
import { CountUp } from "@/hooks/use-count-up"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useAccount } from "@/lib/account"
import { getActivity, getLoginHistory, getProviders, getSessions } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { PROVIDERS } from "@/lib/providers"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const DAY = 86400000
const DEVICE_ICON = { desktop: LaptopIcon, laptop: LaptopIcon, mobile: SmartphoneIcon, phone: SmartphoneIcon, tablet: TabletIcon }
const SIX_MONTHS = 183 * DAY

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

const dayKey = (d) => {
  const x = new Date(d)
  return `${x.getFullYear()}-${x.getMonth()}-${x.getDate()}`
}

function buildDays(entries, days, offset = 0) {
  const start = new Date()
  start.setHours(0, 0, 0, 0)
  const list = Array.from({ length: days }, (_, i) => {
    const date = new Date(start.getTime() - (days - 1 - i + offset) * DAY)
    return { key: dayKey(date), date, ok: 0, bad: 0 }
  })
  const index = new Map(list.map((d) => [d.key, d]))
  for (const e of entries) {
    const d = index.get(dayKey(e.at))
    if (!d) continue
    if (e.result === "success") d.ok++
    else d.bad++
  }
  return list
}

function niceMax(n) {
  if (n <= 4) return 4
  const step = 10 ** Math.floor(Math.log10(n))
  return Math.ceil(n / step) * step
}

/* ---------- stat strip ---------- */

function Stat({ icon: Icon, value, unit, label, strong, to }) {
  return (
    <Link
      to={to}
      className="group flex items-center gap-3 rounded-xl py-1 pr-2 outline-none focus-visible:ring-2 focus-visible:ring-ring"
    >
      <span
        className={cn(
          "flex size-11 shrink-0 items-center justify-center rounded-full [&_svg]:size-5",
          strong
            ? "bg-primary text-primary-foreground shadow-[0_4px_12px_rgba(18,97,90,0.3)]"
            : "bg-card text-muted-foreground ring-1 ring-foreground/8"
        )}
      >
        <Icon />
      </span>
      <span className="flex flex-col">
        <span className="flex items-baseline gap-1.5">
          <span className="tabular text-xl font-medium"><CountUp value={value} /></span>
          {unit && <span className="text-xs text-muted-foreground">{unit}</span>}
        </span>
        <span className="text-xs text-muted-foreground transition-colors group-hover:text-foreground">{label}</span>
      </span>
    </Link>
  )
}

/* ---------- checkup ring ---------- */

const TICKS = 56

function Ring({ passed, total }) {
  const lit = Math.round((passed / total) * TICKS)
  return (
    <svg viewBox="0 0 200 200" className="size-52" role="presentation">
      {Array.from({ length: TICKS }, (_, i) => {
        return (
          <rect
            key={i}
            x="96"
            y="8"
            width="8"
            height="26"
            rx="4"
            transform={`rotate(${(i / TICKS) * 360} 100 100)`}
            className={i < lit ? "motion-tick fill-primary" : "fill-foreground/8"}
            style={i < lit ? { "--i": i } : undefined}
          />
        )
      })}
    </svg>
  )
}

function Checkup({ checks, loading }) {
  const { t } = useI18n()
  const passed = checks.filter((c) => c.ok).length

  return (
    <Card className="motion-rise lg:col-span-5" style={{ "--i": 1 }}>
      <CardHeader>
        <CardTitle className="text-base">{t("overview.checkup.title")}</CardTitle>
        <CardDescription>{t("overview.checkup.body")}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        <div className="relative mx-auto">
          {loading ? <Skeleton className="size-52 rounded-full" /> : <Ring passed={passed} total={checks.length} />}
          {!loading && (
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="tabular text-5xl font-medium tracking-tight"><CountUp value={passed} /></span>
              <span className="text-xs text-muted-foreground">{t("overview.checkup.passed", { n: checks.length })}</span>
            </div>
          )}
        </div>

        <ul className="flex flex-col gap-1">
          {checks.map((c) => (
            <li key={c.key} className="flex items-center gap-3 rounded-lg py-2">
              <span
                className={cn(
                  "flex size-9 shrink-0 items-center justify-center rounded-full [&_svg]:size-4",
                  c.ok ? "bg-success-soft text-success" : "bg-warning-soft text-warning"
                )}
              >
                {c.ok ? <CircleCheckIcon /> : <CircleAlertIcon />}
              </span>
              <span className="flex min-w-0 flex-1 flex-col">
                <span className="text-sm font-medium">{c.label}</span>
                {!c.ok && <span className="text-xs text-muted-foreground">{c.hint}</span>}
              </span>
              {c.ok ? (
                <span className="text-xs text-muted-foreground">{t("overview.checkup.ok")}</span>
              ) : (
                <Button asChild variant="outline" size="sm">
                  <Link to={c.to}>{t("overview.checkup.fix")}</Link>
                </Button>
              )}
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}

/* ---------- sign-in chart ---------- */

function SignInChart({ history, days, setDays }) {
  const { t, fmt, locale } = useI18n()
  const [show, setShow] = useState("all")
  const loading = history.status === "loading"

  const { current, previous } = useMemo(() => {
    const entries = history.data ?? []
    return { current: buildDays(entries, days), previous: buildDays(entries, days, days) }
  }, [history.data, days])

  const value = (d) => (show === "success" ? d.ok : show === "failed" ? d.bad : d.ok + d.bad)
  const sum = (list) => list.reduce((n, d) => n + value(d), 0)
  const total = sum(current)
  const prevTotal = sum(previous)
  const today = current.at(-1)
  const busiest = current.reduce((best, d) => (value(d) > value(best) ? d : best), current[0])
  const max = niceMax(Math.max(...current.map(value), 1))
  const change = prevTotal === 0 ? null : Math.round(((total - prevTotal) / prevTotal) * 100)

  const weekday = new Intl.DateTimeFormat(locale === "km" ? "km" : "en-GB", { weekday: "short" })
  const dayNum = new Intl.DateTimeFormat(locale === "km" ? "km" : "en-GB", { day: "numeric", month: "short" })
  const label = (d, i) => {
    if (days === 7) return weekday.format(d.date)
    return i % 5 === 4 || i === days - 1 ? dayNum.format(d.date) : ""
  }

  const figures = [
    { label: t("overview.chart.today"), value: value(today ?? { ok: 0, bad: 0 }), accent: true },
    { label: t("overview.chart.average"), value: total / days, decimals: 1 },
    { label: t("overview.chart.busiest"), value: busiest ? value(busiest) : 0, meta: busiest && value(busiest) ? dayNum.format(busiest.date) : null },
    {
      label: t("overview.chart.change", { n: days }),
      value: change === null ? "–" : change,
      suffix: "%",
      sign: change !== null && change > 0 ? "+" : "",
    },
  ]

  return (
    <Card className="motion-rise lg:col-span-7" style={{ "--i": 2 }}>
      <CardHeader>
        <CardTitle className="text-base">{t("overview.chart.title")}</CardTitle>
        <CardDescription>{t("overview.chart.body")}</CardDescription>
        <CardAction>
          <ToggleGroup
            type="single"
            variant="outline"
            size="sm"
            spacing={0}
            value={String(days)}
            onValueChange={(v) => v && setDays(Number(v))}
            aria-label={t("overview.chart.range")}
          >
            <ToggleGroupItem value="7">{t("overview.chart.days7")}</ToggleGroupItem>
            <ToggleGroupItem value="30">{t("overview.chart.days30")}</ToggleGroupItem>
          </ToggleGroup>
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-6">
        <ToggleGroup
          type="single"
          size="sm"
          spacing={2}
          value={show}
          onValueChange={(v) => v && setShow(v)}
          aria-label={t("overview.chart.filter")}
          className="flex-wrap justify-start"
        >
          <ToggleGroupItem value="all" className="rounded-full px-3">{t("history.all")}</ToggleGroupItem>
          <ToggleGroupItem value="success" className="rounded-full px-3">
            <LogInIcon data-icon="inline-start" />
            {t("overview.chart.successful")}
          </ToggleGroupItem>
          <ToggleGroupItem value="failed" className="rounded-full px-3">
            <ShieldAlertIcon data-icon="inline-start" />
            {t("overview.chart.failed")}
          </ToggleGroupItem>
        </ToggleGroup>

        <dl className="grid grid-cols-2 gap-x-6 gap-y-4 sm:grid-cols-4">
          {figures.map((f) => (
            <div key={f.label} className="flex flex-col gap-1">
              <dt className="text-xs text-muted-foreground">{f.label}</dt>
              <dd className="flex items-baseline gap-1.5">
                {loading ? (
                  <Skeleton className="h-7 w-12" />
                ) : (
                  <span className={cn("tabular text-2xl font-medium", f.accent && "text-primary")}>
                    {f.sign}
                    <CountUp value={f.value} decimals={f.decimals} suffix={typeof f.value === "number" ? f.suffix : ""} />
                  </span>
                )}
                {f.meta && <span className="text-xs text-muted-foreground">{f.meta}</span>}
              </dd>
            </div>
          ))}
        </dl>

        {loading ? (
          <Skeleton className="h-64 w-full" />
        ) : (
          <div className="flex h-64 gap-3">
            <div className="tabular flex flex-col justify-between pb-6 text-right text-xs text-muted-foreground">
              {[max, max * 0.75, max * 0.5, max * 0.25, 0].map((v) => (
                <span key={v}>{Number.isInteger(v) ? v : v.toFixed(1)}</span>
              ))}
            </div>
            <div className="relative flex flex-1 flex-col">
              <div aria-hidden className="absolute inset-x-0 top-0 bottom-6 flex flex-col justify-between">
                {[0, 1, 2, 3, 4].map((i) => (
                  <span key={i} className="h-px bg-foreground/6" />
                ))}
              </div>
              {total === 0 && (
                <p className="absolute inset-x-0 top-1/3 text-center text-sm text-muted-foreground">{t("overview.chart.empty")}</p>
              )}
              <div key={`${days}-${show}`} className={cn("relative flex flex-1 items-end", days === 7 ? "gap-4" : "gap-1")}>
                {current.map((d, i) => {
                  const isToday = i === current.length - 1
                  const ok = show === "failed" ? 0 : d.ok
                  const bad = show === "success" ? 0 : d.bad
                  return (
                    <Tooltip key={d.key}>
                      <TooltipTrigger asChild>
                        <div className="flex h-full flex-1 flex-col items-center justify-end">
                          <div
                            className="motion-bar flex w-full max-w-12 flex-col justify-end"
                            style={{ height: `${((ok + bad) / max) * 100}%`, "--i": i, "--step": days === 7 ? "50ms" : "14ms" }}
                          >
                            {bad > 0 && (
                              <span
                                className={cn("w-full bg-destructive/75", ok === 0 ? "rounded-md" : "rounded-t-md")}
                                style={{ height: `${(bad / (ok + bad)) * 100}%` }}
                              />
                            )}
                            {ok > 0 && (
                              <span
                                className={cn(
                                  "w-full",
                                  bad === 0 ? "rounded-md" : "rounded-b-md",
                                  isToday
                                    ? "bg-primary"
                                    : "bg-[repeating-linear-gradient(135deg,var(--color-foreground)_0_1px,transparent_1px_6px)] bg-foreground/8 opacity-60"
                                )}
                                style={{ height: `${(ok / (ok + bad)) * 100}%` }}
                              />
                            )}
                          </div>
                        </div>
                      </TooltipTrigger>
                      <TooltipContent>
                        {fmt.date(d.date)}: {t("overview.chart.tooltip", { ok: d.ok, bad: d.bad })}
                      </TooltipContent>
                    </Tooltip>
                  )
                })}
              </div>
              <div className={cn("flex h-6 items-end", days === 7 ? "gap-4" : "gap-1")}>
                {current.map((d, i) => (
                  <span
                    key={d.key}
                    className={cn(
                      "flex-1 truncate text-center text-xs text-muted-foreground",
                      i === current.length - 1 && "font-medium text-foreground"
                    )}
                  >
                    {label(d, i)}
                  </span>
                ))}
              </div>
            </div>
          </div>
        )}

        <div className="flex flex-wrap items-center gap-x-5 gap-y-2 text-xs text-muted-foreground">
          <span className="flex items-center gap-2">
            <span className="size-2.5 rounded-sm bg-primary" />
            {t("overview.chart.today")}
          </span>
          <span className="flex items-center gap-2">
            <span className="size-2.5 rounded-sm bg-foreground/20" />
            {t("overview.chart.successful")}
          </span>
          <span className="flex items-center gap-2">
            <span className="size-2.5 rounded-sm bg-destructive/75" />
            {t("overview.chart.failed")}
          </span>
        </div>
      </CardContent>
    </Card>
  )
}

/* ---------- bottom row ---------- */

function Devices({ sessions }) {
  const { t, fmt } = useI18n()
  const list = [...(sessions.data ?? [])].sort((a, b) => Number(b.current) - Number(a.current)).slice(0, 4)

  return (
    <section className="motion-rise flex flex-col gap-5 rounded-xl bg-sidebar p-6 text-white lg:col-span-5 dark:ring-1 dark:ring-sidebar-border" style={{ "--i": 3 }}>
      <div className="flex items-center justify-between gap-3">
        <h2 className="text-lg font-medium">{t("overview.devices.title")}</h2>
        <span className="tabular rounded-full px-3 py-1 text-xs text-sidebar-foreground ring-1 ring-sidebar-border">
          {sessions.data?.length ?? "–"}
        </span>
      </div>
      <ul className="flex flex-col gap-1">
        {sessions.status === "loading" &&
          [0, 1, 2].map((i) => <Skeleton key={i} className="h-14 bg-sidebar-accent" />)}
        {list.map((s) => {
          const Icon = DEVICE_ICON[s.type] ?? LaptopIcon
          return (
            <li key={s.id} className="flex items-center gap-3 rounded-lg py-2">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-sidebar-accent text-sidebar-foreground [&_svg]:size-4.5">
                <Icon />
              </span>
              <span className="flex min-w-0 flex-1 flex-col">
                <span className="truncate text-sm font-medium">{s.device}</span>
                <span className="truncate text-xs text-sidebar-foreground">
                  {s.location} · {fmt.relative(s.lastActive)}
                </span>
              </span>
              {s.current && (
                <span className="flex shrink-0 items-center gap-2 text-xs text-sidebar-primary">
                  <span className="size-2 rounded-full bg-sidebar-primary" />
                  {t("overview.devices.thisDevice")}
                </span>
              )}
            </li>
          )
        })}
      </ul>
      <Button asChild variant="secondary" className="mt-auto self-start">
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
        <CardTitle className="text-base">{t("overview.activity.title")}</CardTitle>
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
                <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground [&_svg]:size-4">
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
        <CardTitle className="text-base">{t("overview.linked.title")}</CardTitle>
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
          <ul className="flex flex-col gap-1">
            {(providers.data ?? []).map((p) => (
              <li key={p.id} className="flex items-center gap-3 py-2">
                <span className="flex size-9 shrink-0 items-center justify-center rounded-full bg-muted">
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

  const sessions = useAsync(getSessions)
  const providers = useAsync(getProviders)
  const activity = useAsync(getActivity)
  // Fetch twice the visible range so the chart can compare with the previous period.
  const history = useAsync(() => loadHistory(Math.max(days * 2, 30)), [days])
  const month = useAsync(() => loadHistory(30))

  const monthEntries = month.data ?? []
  const signIns = monthEntries.filter((e) => e.result === "success").length
  const failed30 = monthEntries.filter((e) => e.result !== "success").length
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

  const stats = [
    { icon: MonitorSmartphoneIcon, value: sessions.data?.length ?? "–", unit: t("overview.stats.live"), label: t("overview.stats.devices"), strong: true, to: "/sessions" },
    { icon: LogInIcon, value: month.data ? signIns : "–", label: t("overview.stats.signIns"), to: "/login-history" },
    { icon: ShieldAlertIcon, value: month.data ? failed30 : "–", label: t("overview.stats.failed"), to: "/login-history" },
    {
      icon: LinkIcon,
      value: providers.data ? linkedCount : "–",
      unit: providers.data ? t("overview.stats.of", { n: providers.data.length }) : null,
      label: t("overview.stats.linked"),
      to: "/linked-accounts",
    },
  ]

  return (
    <div className="flex flex-col gap-6">
      <div className="motion-rise flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
        <h1 className="text-3xl font-medium tracking-tight">{t("overview.title")}</h1>
        <div className="grid grid-cols-2 gap-x-6 gap-y-4 sm:flex sm:flex-wrap sm:gap-x-8">
          {stats.map((s) => (
            <Stat key={s.label} {...s} />
          ))}
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-12">
        <Checkup checks={checks} loading={checksLoading} />
        <SignInChart history={history} days={days} setDays={setDays} />
      </div>

      <div className="grid gap-6 lg:grid-cols-12">
        <Devices sessions={sessions} />
        <RecentActivity activity={activity} />
        <LinkedAccounts providers={providers} />
      </div>
    </div>
  )
}
