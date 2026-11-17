import { useMemo, useState } from "react"
import { Link } from "react-router-dom"
import { CircleAlertIcon, CircleCheckIcon, LogInIcon, ShieldAlertIcon } from "lucide-react"
import { FilterChip, FilterChips } from "@/components/filter-chips"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { CountUp } from "@/hooks/use-count-up"
import { useI18n } from "@/lib/i18n-context"
import { cn } from "@/lib/utils"

/*
 * The two showcase cards shared by the account overview and the Management overview:
 * a check-up ring with fixable checks, and a sign-in chart. Sign-in entries are { at, result: "success" | "failed" }.
 */

const DAY = 86400000

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

export function Checkup({ checks, loading, title, description, passedLabel }) {
  const { t } = useI18n()
  const passed = checks.filter((c) => c.ok).length

  return (
    <Card className="motion-rise lg:col-span-5" style={{ "--i": 1 }}>
      <CardHeader>
        <CardTitle>{title ?? t("overview.checkup.title")}</CardTitle>
        <CardDescription>{description ?? t("overview.checkup.body")}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-6">
        <div className="relative mx-auto">
          {loading ? <Skeleton className="size-52 rounded-full" /> : <Ring passed={passed} total={checks.length} />}
          {!loading && (
            // Inset to the clear space inside the ticks (they end at 34/200 of the ring), so the label never runs over them.
            <div className="absolute inset-[18%] flex flex-col items-center justify-center text-center">
              <span className="tabular text-5xl font-medium tracking-tight"><CountUp value={passed} /></span>
              <span className="text-xs leading-tight text-balance text-muted-foreground">
                {passedLabel ?? t("overview.checkup.passed", { n: checks.length })}
              </span>
            </div>
          )}
        </div>

        <ul className="grid gap-x-8 gap-y-1 md:grid-cols-2">
          {checks.map((c) => (
            <li key={c.key} className="flex items-center gap-3 rounded-md py-2">
              <span
                className={cn(
                  "flex size-9 shrink-0 items-center justify-center rounded-md [&_svg]:size-4",
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

export function SignInChart({ history, days, setDays, title, description }) {
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
        <CardTitle>{title ?? t("overview.chart.title")}</CardTitle>
        <CardDescription>{description ?? t("overview.chart.body")}</CardDescription>
        <CardAction>
          <FilterChips
            value={String(days)}
            onValueChange={(v) => v && setDays(Number(v))}
            aria-label={t("overview.chart.range")}
          >
            <FilterChip value="7">{t("overview.chart.days7")}</FilterChip>
            <FilterChip value="30">{t("overview.chart.days30")}</FilterChip>
          </FilterChips>
        </CardAction>
      </CardHeader>

      <CardContent className="flex flex-col gap-6">
        <FilterChips
          value={show}
          onValueChange={(v) => v && setShow(v)}
          aria-label={t("overview.chart.filter")}
          className="flex-wrap justify-start"
        >
          <FilterChip value="all">{t("history.all")}</FilterChip>
          <FilterChip value="success">
            <LogInIcon data-icon="inline-start" />
            {t("overview.chart.successful")}
          </FilterChip>
          <FilterChip value="failed">
            <ShieldAlertIcon data-icon="inline-start" />
            {t("overview.chart.failed")}
          </FilterChip>
        </FilterChips>

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
            <span className="size-2.5 rounded-md bg-primary" />
            {t("overview.chart.today")}
          </span>
          <span className="flex items-center gap-2">
            <span className="size-2.5 rounded-md bg-foreground/20" />
            {t("overview.chart.successful")}
          </span>
          <span className="flex items-center gap-2">
            <span className="size-2.5 rounded-md bg-destructive/75" />
            {t("overview.chart.failed")}
          </span>
        </div>
      </CardContent>
    </Card>
  )
}
