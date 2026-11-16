import { useState } from "react"
import { CircleCheckIcon, CircleXIcon, HistoryIcon } from "lucide-react"
import { CardSkeleton, EmptyState, ErrorState, PageIntro } from "@/components/page-states"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { getLoginHistory } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

const PAGE_SIZE = 10

function ResultBadge({ entry }) {
  const { t } = useI18n()
  return entry.result === "success" ? (
    <Badge variant="secondary" className="bg-success-soft text-success">
      <CircleCheckIcon data-icon="inline-start" />
      {t("history.success")}
    </Badge>
  ) : (
    <Badge variant="destructive">
      <CircleXIcon data-icon="inline-start" />
      {t("history.failed")}
    </Badge>
  )
}

export default function LoginHistoryPage() {
  const { t, fmt } = useI18n()
  const [result, setResult] = useState("all")
  const [range, setRange] = useState("30")
  const [page, setPage] = useState(1)
  const history = useAsync(() => getLoginHistory({ result, range, page, pageSize: PAGE_SIZE }), [result, range, page])

  const data = history.data
  const pages = data ? Math.max(1, Math.ceil(data.total / PAGE_SIZE)) : 1
  const method = (m) => t(`history.methods.${m}`)
  const detail = (e) => (e.result === "failed" ? t(`history.reasons.${e.reason}`) : method(e.method))

  const filters = (
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <ToggleGroup
        type="single"
        variant="outline"
        value={result}
        onValueChange={(v) => {
          if (!v) return
          setResult(v)
          setPage(1)
        }}
        aria-label={t("history.resultFilter")}
      >
        <ToggleGroupItem value="all">{t("history.all")}</ToggleGroupItem>
        <ToggleGroupItem value="success">{t("history.success")}</ToggleGroupItem>
        <ToggleGroupItem value="failed">{t("history.failed")}</ToggleGroupItem>
      </ToggleGroup>
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
              <SelectItem key={r} value={r}>{t(`history.ranges.${r}`)}</SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  )

  const body = () => {
    if (history.status === "loading") return <CardSkeleton rows={6} />
    if (history.status === "error") return <ErrorState onRetry={history.reload} />
    const items = data.items
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            {t("history.title")}
            {history.status === "refreshing" && <Spinner className="text-muted-foreground" />}
          </CardTitle>
          <CardDescription>{t("history.count", { n: data.total })}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          {filters}
          {items.length === 0 ? (
            <EmptyState icon={HistoryIcon} title={t("history.emptyTitle")} description={t("history.emptyBody")}>
              {(result !== "all" || range !== "all") && (
                <Button variant="outline" onClick={() => { setResult("all"); setRange("all"); setPage(1) }}>
                  {t("history.clearFilters")}
                </Button>
              )}
            </EmptyState>
          ) : (
            <>
              <div className="hidden md:block">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>{t("history.when")}</TableHead>
                      <TableHead>{t("history.result")}</TableHead>
                      <TableHead>{t("history.device")}</TableHead>
                      <TableHead>{t("history.location")}</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {items.map((e) => (
                      <TableRow key={e.id}>
                        <TableCell className="tabular whitespace-nowrap">{fmt.dateTime(e.at)}</TableCell>
                        <TableCell>
                          <div className="flex flex-col items-start gap-1">
                            <ResultBadge entry={e} />
                            <span className="text-xs text-muted-foreground">{detail(e)}</span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span>{e.device}</span>
                            <span className="text-xs text-muted-foreground">{e.browser}, {e.os}</span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex flex-col">
                            <span>{e.location}</span>
                            <span className="tabular text-xs text-muted-foreground">{e.ip}</span>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
              <ul className="flex flex-col divide-y md:hidden">
                {items.map((e) => (
                  <li key={e.id} className="flex flex-col gap-1.5 py-3 first:pt-0">
                    <div className="flex items-center justify-between gap-2">
                      <ResultBadge entry={e} />
                      <span className="tabular text-xs text-muted-foreground">{fmt.dateTime(e.at)}</span>
                    </div>
                    <span className="text-sm">{e.device}, {e.browser}</span>
                    <span className="text-xs text-muted-foreground">
                      {e.location} ({e.ip}). {detail(e)}
                    </span>
                  </li>
                ))}
              </ul>
            </>
          )}
        </CardContent>
        {pages > 1 && (
          <CardFooter className="flex-col gap-3 border-t sm:flex-row sm:justify-between">
            <span className="tabular text-sm text-muted-foreground">
              {t("history.pageOf", { page, pages })}
            </span>
            <nav aria-label={t("history.pagination")} className="flex gap-2">
              <Button variant="outline" size="sm" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
                {t("history.prev")}
              </Button>
              <Button variant="outline" size="sm" disabled={page >= pages} onClick={() => setPage((p) => p + 1)}>
                {t("history.next")}
              </Button>
            </nav>
          </CardFooter>
        )}
      </Card>
    )
  }

  return (
    <>
      <PageIntro>{t("history.intro")}</PageIntro>
      {body()}
    </>
  )
}
