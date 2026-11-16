import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import { ArrowLeftIcon, ChevronLeftIcon, ChevronRightIcon, CloudOffIcon, LockKeyholeIcon, RotateCwIcon, SearchIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { InputGroup, InputGroupAddon, InputGroupInput } from "@/components/ui/input-group"
import { Skeleton } from "@/components/ui/skeleton"
import { useI18n } from "@/lib/i18n-context"
import { cn } from "@/lib/utils"

export function PageHeader({ title, intro, children }) {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <h1 className="min-w-0 text-3xl leading-tight font-medium tracking-tight">{title}</h1>
        {children && <div className="flex shrink-0 flex-wrap gap-2">{children}</div>}
      </div>
      {intro && <p className="max-w-[62ch] text-muted-foreground">{intro}</p>}
    </div>
  )
}

/** Quiet link back to the list a detail page belongs to. */
export function BackLink({ to, children }) {
  return (
    <Link
      to={to}
      className="-ml-1 inline-flex items-center gap-2 self-start rounded-md px-1 text-muted-foreground transition-colors hover:text-foreground focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none [&_svg]:size-4"
    >
      <ArrowLeftIcon />
      {children}
    </Link>
  )
}

/**
 * The signature detail of Management: permission and role names set as small key tags.
 * `user:read` renders the resource muted and the action in full ink, so the eye lands on the verb.
 */
export function KeyTag({ name, className, tone = "default", bare = false }) {
  const i = name?.indexOf(":") ?? -1
  return (
    <code
      className={cn(
        "inline-flex max-w-full items-center truncate font-mono text-sm leading-6",
        !bare && "rounded-md border px-1.5 py-px",
        tone === "deny"
          ? cn("text-destructive", !bare && "border-destructive/30 bg-destructive/5 line-through decoration-destructive/40")
          : cn("text-foreground", !bare && "bg-muted/60"),
        className
      )}
    >
      {i > 0 ? (
        <>
          <span className={tone === "deny" ? "" : "text-muted-foreground"}>{name.slice(0, i + 1)}</span>
          {name.slice(i + 1)}
        </>
      ) : (
        name
      )}
    </code>
  )
}

/** Status as a dot and a word, not a colored pill. */
export function StatusDot({ on, label, tone, className }) {
  const danger = tone === "danger"
  return (
    <span
      className={cn("inline-flex items-center gap-2 whitespace-nowrap", on || danger ? "text-foreground" : "text-muted-foreground", className)}
    >
      <span
        aria-hidden
        className={cn("size-2 shrink-0 rounded-full", danger ? "bg-destructive" : on ? "bg-success" : "bg-muted-foreground/35")}
      />
      {label}
    </span>
  )
}

/** A list page's single panel: toolbar on top, the table or state below, the pager at the foot. */
export function ListPanel({ toolbar, children, footer }) {
  return (
    <Card className="gap-0 py-0">
      {toolbar && <div className="flex flex-col gap-3 border-b px-5 py-4 sm:flex-row sm:items-center">{toolbar}</div>}
      {children}
      {footer}
    </Card>
  )
}

export function TableSkeleton({ rows = 6 }) {
  return (
    <div aria-busy="true" className="flex flex-col">
      {Array.from({ length: rows }, (_, i) => (
        <div key={i} className="flex h-15 items-center gap-4 border-b border-border/70 px-5 last:border-b-0">
          <Skeleton className="size-8 rounded-md" />
          <Skeleton className="h-4 w-48" />
          <Skeleton className="ml-auto h-4 w-20" />
        </div>
      ))}
    </div>
  )
}

/** Error state for use inside a panel (no card of its own). */
export function PanelError({ onRetry }) {
  const { t } = useI18n()
  return (
    <Empty className="py-12">
      <EmptyHeader>
        <EmptyMedia variant="icon">
          <CloudOffIcon />
        </EmptyMedia>
        <EmptyTitle>{t("states.errorTitle")}</EmptyTitle>
        <EmptyDescription>{t("states.errorBody")}</EmptyDescription>
      </EmptyHeader>
      {onRetry && (
        <EmptyContent>
          <Button variant="outline" onClick={onRetry}>
            <RotateCwIcon data-icon="inline-start" />
            {t("common.retry")}
          </Button>
        </EmptyContent>
      )}
    </Empty>
  )
}

export function AccessDenied() {
  const { t } = useI18n()
  return (
    <div className="flex min-h-[60svh] items-center justify-center px-4">
      <Empty>
        <EmptyHeader>
          <EmptyMedia variant="icon">
            <LockKeyholeIcon />
          </EmptyMedia>
          <EmptyTitle>{t("mgmt.denied.title")}</EmptyTitle>
          <EmptyDescription>{t("mgmt.denied.body")}</EmptyDescription>
        </EmptyHeader>
        <EmptyContent>
          <Button asChild variant="outline">
            <Link to="/overview">
              <ArrowLeftIcon data-icon="inline-start" />
              {t("mgmt.denied.back")}
            </Link>
          </Button>
        </EmptyContent>
      </Empty>
    </div>
  )
}

export function Pager({ page, pages, total, size, onPage }) {
  const { t } = useI18n()
  const from = size ? (page - 1) * size + 1 : null
  const to = size ? Math.min(page * size, total) : null
  return (
    <div className="flex items-center justify-between gap-4 border-t px-5 py-3">
      <span className="tabular text-muted-foreground">
        {size ? t("mgmt.common.range", { from, to, total }) : t("mgmt.common.results", { n: total })}
      </span>
      {pages > 1 && (
        <nav aria-label={t("history.pagination")} className="flex items-center gap-3">
          <span className="tabular hidden text-muted-foreground sm:inline">{t("mgmt.common.pageOf", { page, pages })}</span>
          <div className="flex gap-1">
            <Button
              variant="outline"
              size="icon-sm"
              aria-label={t("mgmt.common.prev")}
              disabled={page <= 1}
              onClick={() => onPage(page - 1)}
            >
              <ChevronLeftIcon />
            </Button>
            <Button
              variant="outline"
              size="icon-sm"
              aria-label={t("mgmt.common.next")}
              disabled={page >= pages}
              onClick={() => onPage(page + 1)}
            >
              <ChevronRightIcon />
            </Button>
          </div>
        </nav>
      )}
    </div>
  )
}

/** Search box that reports its value 300ms after typing stops. */
export function SearchBox({ value, onChange, placeholder, className }) {
  const { t } = useI18n()
  const [text, setText] = useState(value)
  useEffect(() => {
    if (text === value) return
    const id = setTimeout(() => onChange(text), 300)
    return () => clearTimeout(id)
  }, [text, value, onChange])

  return (
    <InputGroup className={className}>
      <InputGroupAddon>
        <SearchIcon />
      </InputGroupAddon>
      <InputGroupInput
        type="search"
        aria-label={t("mgmt.common.search")}
        placeholder={placeholder ?? t("mgmt.common.search")}
        value={text}
        onChange={(e) => setText(e.target.value)}
      />
    </InputGroup>
  )
}
