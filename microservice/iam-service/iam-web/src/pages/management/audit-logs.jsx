import { useCallback } from "react"
import { Link, useSearchParams } from "react-router-dom"
import { ScrollTextIcon } from "lucide-react"
import { DataBody, DataHead, DataRow, DataTable, Td, Th } from "@/components/management/data-table"
import { ListPanel, PageHeader, Pager, PanelError, SearchBox, TableSkeleton } from "@/components/management/states"
import { EmptyState } from "@/components/page-states"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Spinner } from "@/components/ui/spinner"
import { ActionTag } from "@/components/management/action-tag"
import { listAuditLogs } from "@/lib/admin-api"
import { ACTION_GROUPS, actionKey } from "@/lib/audit"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

const PAGE_SIZE = 20
const RANGES = ["1", "7", "30", "all"]
const TARGETS = ["USER", "ROLE", "PERMISSION", "CLIENT", "SETTINGS", "AUTH"]

function Target({ entry }) {
  const { t } = useI18n()
  const label = t(`mgmt.audit.targets.${entry.targetType}`)
  const id = entry.targetId
  // Users are the only targets with a detail page keyed by the same id; AUTH targets are user ids too.
  const userId = (entry.targetType === "USER" || entry.targetType === "AUTH") && id && /^[0-9a-f-]{36}$/i.test(id) ? id : null
  return (
    <div className="flex min-w-0 flex-col leading-snug">
      <span>{label}</span>
      {id &&
        (userId ? (
          <Link
            to={`/management/users/${userId}`}
            className="truncate font-mono text-sm text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
            title={t("mgmt.audit.openUser")}
          >
            {id.slice(0, 8)}
          </Link>
        ) : (
          <span className="truncate font-mono text-sm text-muted-foreground" title={id}>
            {id.length > 12 ? `${id.slice(0, 8)}…` : id}
          </span>
        ))}
    </div>
  )
}

export default function AuditLogsPage() {
  const { t, fmt } = useI18n()
  const [params, setParams] = useSearchParams()
  const actor = params.get("actor") ?? ""
  const action = params.get("action") ?? "all"
  const target = params.get("target") ?? "all"
  const range = params.get("range") ?? "7"
  const page = Number(params.get("page") ?? 1)

  const update = useCallback(
    (patch) =>
      setParams((p) => {
        const next = new URLSearchParams(p)
        for (const [k, v] of Object.entries(patch)) {
          if (v === null || v === "" || v === "all" || (k === "page" && v === 1) || (k === "range" && v === "7")) next.delete(k)
          else next.set(k, String(v))
        }
        return next
      }),
    [setParams]
  )

  const logs = useAsync(
    () =>
      listAuditLogs({
        page,
        size: PAGE_SIZE,
        actor: actor || undefined,
        action: action === "all" ? undefined : action,
        targetType: target === "all" ? undefined : target,
        // Computed when the request runs (not during render), so the window ends "now".
        from: range === "all" ? undefined : new Date(Date.now() - Number(range) * 86_400_000).toISOString(),
      }),
    [page, actor, action, target, range]
  )

  const filtered = !!actor || action !== "all" || target !== "all" || range !== "7"

  const body = () => {
    if (logs.status === "loading") return <TableSkeleton rows={8} />
    if (logs.status === "error") return <PanelError onRetry={logs.reload} />
    const { items } = logs.data
    if (items.length === 0) {
      return (
        <EmptyState
          icon={ScrollTextIcon}
          title={filtered ? t("mgmt.common.noResults") : t("mgmt.audit.empty")}
          description={filtered ? t("mgmt.common.noResultsBody") : t("mgmt.audit.emptyBody")}
        >
          {filtered && (
            <Button variant="outline" onClick={() => update({ actor: null, action: null, target: null, range: "7", page: 1 })}>
              {t("mgmt.common.clearFilters")}
            </Button>
          )}
        </EmptyState>
      )
    }
    return (
      <DataTable>
        <DataHead>
          <Th className="w-48">{t("mgmt.audit.when")}</Th>
          <Th>{t("mgmt.audit.actor")}</Th>
          <Th>{t("mgmt.audit.action")}</Th>
          <Th className="hidden md:table-cell">{t("mgmt.audit.target")}</Th>
          <Th className="hidden xl:table-cell">{t("mgmt.audit.detail")}</Th>
        </DataHead>
        <DataBody>
          {items.map((e) => (
            <DataRow key={e.id}>
              <Td className="tabular whitespace-nowrap text-muted-foreground">{fmt.dateTime(e.occurredAt)}</Td>
              <Td>
                {/* Who did it, and from which IP address underneath (the table fits the page width this way). */}
                <div className="flex min-w-0 flex-col items-start leading-snug">
                  <button
                    type="button"
                    className="max-w-40 truncate rounded-md font-mono outline-none hover:underline focus-visible:underline focus-visible:underline-offset-4"
                    onClick={() => update({ actor: e.actor, page: 1 })}
                    title={e.actor}
                  >
                    {e.actor}
                  </button>
                  {e.ip && (
                    <span className="tabular font-mono text-sm text-muted-foreground" title={t("mgmt.audit.ip")}>
                      {e.ip}
                    </span>
                  )}
                </div>
              </Td>
              <Td>
                <button
                  type="button"
                  className="rounded-md outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  onClick={() => update({ action: e.action, page: 1 })}
                >
                  <ActionTag action={e.action} />
                </button>
              </Td>
              <Td className="hidden md:table-cell">
                <Target entry={e} />
              </Td>
              <Td className="hidden max-w-72 truncate text-muted-foreground xl:table-cell" title={e.detail ?? ""}>
                {e.detail ?? "—"}
              </Td>
            </DataRow>
          ))}
        </DataBody>
      </DataTable>
    )
  }

  return (
    <>
      <PageHeader title={t("mgmt.audit.title")} intro={t("mgmt.audit.intro")} />

      <ListPanel
        toolbar={
          <div className="flex w-full flex-col gap-3 lg:flex-row lg:items-center">
            <SearchBox
              className="lg:min-w-0 lg:flex-1"
              value={actor}
              placeholder={t("mgmt.audit.actorPlaceholder")}
              onChange={(v) => update({ actor: v, page: 1 })}
            />
            <Select value={target} onValueChange={(v) => update({ target: v, action: null, page: 1 })}>
              <SelectTrigger className="w-full lg:w-44" aria-label={t("mgmt.audit.target")}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="all">{t("mgmt.audit.anyTarget")}</SelectItem>
                  {TARGETS.map((x) => (
                    <SelectItem key={x} value={x}>
                      {t(`mgmt.audit.targets.${x}`)}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
            <Select value={action} onValueChange={(v) => update({ action: v, page: 1 })}>
              <SelectTrigger className="w-full lg:w-56" aria-label={t("mgmt.audit.action")}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="all">{t("mgmt.audit.anyAction")}</SelectItem>
                </SelectGroup>
                {Object.entries(ACTION_GROUPS)
                  .filter(([group]) => target === "all" || group === target)
                  .map(([group, actions]) => (
                    <SelectGroup key={group}>
                      <SelectLabel>{t(`mgmt.audit.targets.${group}`)}</SelectLabel>
                      {actions.map((a) => (
                        <SelectItem key={a} value={a} className="font-mono">
                          {actionKey(a)}
                        </SelectItem>
                      ))}
                    </SelectGroup>
                  ))}
              </SelectContent>
            </Select>
            <Select value={range} onValueChange={(v) => update({ range: v, page: 1 })}>
              <SelectTrigger className="w-full lg:w-40" aria-label={t("mgmt.audit.rangeFilter")}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  {RANGES.map((r) => (
                    <SelectItem key={r} value={r}>
                      {t(`mgmt.audit.ranges.${r}`)}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
            {logs.status === "refreshing" && <Spinner className="text-muted-foreground" />}
          </div>
        }
        footer={
          logs.data?.items?.length > 0 && logs.status !== "loading" ? (
            <Pager page={page} pages={logs.data.pages} total={logs.data.total} size={PAGE_SIZE} onPage={(p) => update({ page: p })} />
          ) : null
        }
      >
        {body()}
      </ListPanel>
    </>
  )
}
