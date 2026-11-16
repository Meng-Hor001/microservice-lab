import { useMemo, useState } from "react"
import { Link } from "react-router-dom"
import { AppWindowIcon, ChevronRightIcon, KeyRoundIcon, MonitorSmartphoneIcon, PlusIcon, ServerIcon, ShieldCheckIcon, UsersIcon } from "lucide-react"
import { Stat } from "@/components/stat"
import { KeyTag, StatusDot } from "@/components/management/states"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useAccess } from "@/lib/access"
import { allPermissions, allRoles, countUsers, getSettings, listAuditLogs, listClients, listSuspiciousLogins, listUsers } from "@/lib/admin-api"
import { Checkup, SignInChart } from "@/components/overview-cards"
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ClientType } from "@/pages/management/clients"
import { ActionTag } from "@/components/management/action-tag"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const denied = () => Promise.reject(new Error("denied"))

// Common verbs first, in the order people read them; anything else follows alphabetically.
const ACTION_ORDER = ["read", "create", "update", "delete", "assign", "manage"]
const byActionOrder = (a, b) => {
  const ia = ACTION_ORDER.indexOf(a)
  const ib = ACTION_ORDER.indexOf(b)
  if (ia !== -1 || ib !== -1) return (ia === -1 ? 99 : ia) - (ib === -1 ? 99 : ib)
  return a.localeCompare(b)
}

/* ---------- the access map: resources × actions ---------- */

function AccessCell({ permission, resource, action, dim }) {
  const { t } = useI18n()
  if (!permission) {
    return (
      <span className={cn("flex size-9 items-center justify-center transition-opacity", dim && "opacity-25")} aria-label={`${resource}:${action} ${t("mgmt.overview.legendNone")}`}>
        {/* Same size as a defined cell, so the empty slot reads as "nothing here" rather than vanishing. */}
        <span aria-hidden className="size-6 rounded-[4px] border-2 border-dashed border-foreground/20" />
      </span>
    )
  }
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Link
          to={`/management/permissions?resource=${encodeURIComponent(resource)}`}
          aria-label={`${permission.name}, ${permission.active ? t("mgmt.overview.legendActive") : t("mgmt.overview.legendInactive")}`}
          className={cn(
            "group/cell flex size-9 items-center justify-center rounded-md transition-opacity focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none",
            // Filtered out: fades back but keeps its place, so rows and columns still line up.
            dim && "opacity-15"
          )}
        >
          <span
            className={cn(
              "flex size-6 items-center justify-center rounded-[4px] ring-primary/30 transition-shadow duration-150 group-hover/cell:ring-4 group-focus-visible/cell:ring-4",
              permission.active ? "bg-primary" : "border-2 border-primary/60 bg-background"
            )}
          >
            {permission.system && (
              <span aria-hidden className={cn("size-1.5 rounded-full", permission.active ? "bg-primary-foreground" : "bg-primary/60")} />
            )}
          </span>
        </Link>
      </TooltipTrigger>
      <TooltipContent side="top" className="flex max-w-64 flex-col items-start gap-1">
        <span className="font-mono">{permission.name}</span>
        {permission.description && <span className="opacity-70">{permission.description}</span>}
      </TooltipContent>
    </Tooltip>
  )
}

function Legend() {
  const { t } = useI18n()
  const items = [
    { key: "legendActive", swatch: <span className="size-4 rounded-[4px] bg-primary" /> },
    { key: "legendInactive", swatch: <span className="size-4 rounded-[4px] border-2 border-primary/60" /> },
    {
      key: "legendSystem",
      swatch: (
        <span className="flex size-4 items-center justify-center rounded-[4px] bg-primary">
          <span className="size-1 rounded-full bg-primary-foreground" />
        </span>
      ),
    },
    {
      key: "legendNone",
      swatch: <span className="size-4 rounded-[4px] border-2 border-dashed border-foreground/25" />,
    },
  ]
  return (
    <ul className="flex flex-col gap-2.5 text-muted-foreground">
      {items.map((i) => (
        <li key={i.key} className="flex items-center gap-2">
          <span aria-hidden className="flex">{i.swatch}</span>
          {t(`mgmt.overview.${i.key}`)}
        </li>
      ))}
    </ul>
  )
}

/** Beside the matrix: what the marks mean, and the three numbers that summarize it. */
function MapFacts({ permissions }) {
  const { t } = useI18n()
  const list = permissions ?? []
  const facts = [
    { key: "legendActive", n: list.filter((p) => p.active).length },
    { key: "legendInactive", n: list.filter((p) => !p.active).length },
    { key: "legendSystem", n: list.filter((p) => p.system).length },
  ]
  return (
    <aside className="flex shrink-0 flex-col gap-6 xl:w-64 xl:border-l xl:pl-8">
      <dl className="grid grid-cols-3 gap-4 xl:grid-cols-1">
        {facts.map((f) => (
          <div key={f.key} className="flex flex-col gap-1">
            <dt className="text-muted-foreground">{t(`mgmt.overview.${f.key}`)}</dt>
            <dd className="tabular text-2xl leading-none font-medium">{f.n}</dd>
          </div>
        ))}
      </dl>
      <Legend />
    </aside>
  )
}

// Which cells a status filter keeps; anything else (including undefined slots) fades back.
const MAP_FILTERS = {
  all: () => true,
  active: (p) => !!p?.active,
  inactive: (p) => !!p && !p.active,
  system: (p) => !!p?.system,
}

function AccessMap({ state }) {
  const { t } = useI18n()
  const [status, setStatus] = useState("all")
  const keep = MAP_FILTERS[status]
  const { resources, actions, cells } = useMemo(() => {
    const list = state.data ?? []
    const cells = new Map(list.map((p) => [`${p.resource}:${p.action}`, p]))
    return {
      resources: [...new Set(list.map((p) => p.resource))].sort(),
      actions: [...new Set(list.map((p) => p.action))].sort(byActionOrder),
      cells,
    }
  }, [state.data])

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("mgmt.overview.accessMap")}</CardTitle>
        <CardDescription className="tabular">
          {state.data
            ? t("mgmt.overview.accessMapBody", { n: state.data.length, r: resources.length })
            : " "}
        </CardDescription>
        <CardAction className="flex items-center gap-2">
          {state.status === "success" && resources.length > 0 && (
            <Select value={status} onValueChange={setStatus}>
              <SelectTrigger className="w-36" aria-label={t("mgmt.common.status")}>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectGroup>
                  <SelectItem value="all">{t("mgmt.common.all")}</SelectItem>
                  <SelectItem value="active">{t("mgmt.overview.legendActive")}</SelectItem>
                  <SelectItem value="inactive">{t("mgmt.overview.legendInactive")}</SelectItem>
                  <SelectItem value="system">{t("mgmt.overview.legendSystem")}</SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
          )}
          <Button asChild variant="ghost" size="sm">
            <Link to="/management/permissions">
              {t("mgmt.overview.viewAll")}
              <ChevronRightIcon data-icon="inline-end" />
            </Link>
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent className="flex flex-1 flex-col gap-5">
        {state.status === "loading" ? (
          <div className="flex flex-col gap-3">
            {[0, 1, 2, 3, 4].map((i) => (
              <Skeleton key={i} className="h-9" />
            ))}
          </div>
        ) : state.status === "error" ? (
          <p className="py-8 text-muted-foreground">{t("mgmt.common.failed")}</p>
        ) : resources.length === 0 ? (
          <p className="py-8 text-muted-foreground">{t("mgmt.overview.noPermissions")}</p>
        ) : (
          <div className="flex flex-col gap-8 xl:flex-row xl:items-start">
          <div className="-mx-4 min-w-0 flex-1 overflow-x-auto px-4">
            <div
              role="table"
              aria-label={t("mgmt.overview.accessMap")}
              className="grid min-w-max items-center"
              style={{ gridTemplateColumns: `minmax(9rem, 14rem) repeat(${actions.length}, minmax(5rem, 1fr))` }}
            >
              <div role="row" className="contents">
                <span role="columnheader" className="pb-3 text-muted-foreground">
                  {t("mgmt.overview.resource")}
                </span>
                {actions.map((a) => (
                  <span role="columnheader" key={a} className="pb-3 text-center font-mono text-muted-foreground">
                    {a}
                  </span>
                ))}
              </div>
              {resources.map((r) => (
                <div role="row" key={r} className="group/row contents">
                  <span
                    role="rowheader"
                    className="flex h-12 items-center border-t pr-6 font-medium capitalize"
                  >
                    {r}
                  </span>
                  {actions.map((a) => (
                    <span role="cell" key={a} className="flex h-12 items-center justify-center border-t">
                      <AccessCell
                        permission={cells.get(`${r}:${a}`)}
                        resource={r}
                        action={a}
                        dim={status !== "all" && !keep(cells.get(`${r}:${a}`))}
                      />
                    </span>
                  ))}
                </div>
              ))}
            </div>
          </div>
          <MapFacts permissions={state.data} />
          </div>
        )}
      </CardContent>
    </Card>
  )
}

/* ---------- side column ---------- */

function RolesCard({ state }) {
  const { t } = useI18n()
  const canCreate = useAccess().can("role:create")
  const roles = (state.data ?? []).slice(0, 6)
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("mgmt.overview.rolesTitle")}</CardTitle>
        <CardAction className="flex gap-1">
          {canCreate && (
            <Button asChild variant="ghost" size="sm">
              <Link to="/management/roles?new=1">
                <PlusIcon data-icon="inline-start" />
                {t("mgmt.overview.createRole")}
              </Link>
            </Button>
          )}
          <Button asChild variant="ghost" size="sm">
            <Link to="/management/roles">
              {t("mgmt.overview.viewAll")}
              <ChevronRightIcon data-icon="inline-end" />
            </Link>
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? (
          <div className="flex flex-col gap-3">
            {[0, 1, 2].map((i) => (
              <Skeleton key={i} className="h-7" />
            ))}
          </div>
        ) : state.status === "error" ? (
          <p className="text-muted-foreground">{t("mgmt.common.failed")}</p>
        ) : roles.length === 0 ? (
          <p className="text-muted-foreground">{t("mgmt.overview.noRoles")}</p>
        ) : (
          <ul className="-mx-2 flex flex-col">
            {roles.map((r) => (
              <li key={r.uuid}>
                <Link
                  to={`/management/roles/${r.uuid}`}
                  className="flex items-center gap-3 rounded-md px-2 py-2 transition-colors hover:bg-muted/60 focus-visible:bg-muted/60 focus-visible:outline-none"
                >
                  <span className="flex min-w-0 flex-1 flex-col items-start gap-1">
                    <span className="flex items-center gap-2">
                      <KeyTag name={r.name} />
                      {r.system && <span className="text-muted-foreground">{t("mgmt.common.system")}</span>}
                    </span>
                    {r.description && <span className="w-full truncate text-muted-foreground">{r.description}</span>}
                  </span>
                  <StatusDot
                    className="ml-auto"
                    on={r.active}
                    label={r.active ? t("mgmt.common.active") : t("mgmt.common.inactive")}
                  />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

function ViewAll({ to }) {
  const { t } = useI18n()
  return (
    <Button asChild variant="ghost" size="sm">
      <Link to={to}>
        {t("mgmt.overview.viewAll")}
        <ChevronRightIcon data-icon="inline-end" />
      </Link>
    </Button>
  )
}

function ListSkeleton({ rows = 3, className = "h-9" }) {
  return (
    <div className="flex flex-col gap-3">
      {Array.from({ length: rows }, (_, i) => (
        <Skeleton key={i} className={className} />
      ))}
    </div>
  )
}

/** Latest audit entries: who did what, newest first. */
function RecentActivityCard({ state }) {
  const { t, fmt } = useI18n()
  const items = state.data?.items ?? []
  return (
    <Card className="lg:col-span-7">
      <CardHeader>
        <CardTitle>{t("mgmt.overview.recent")}</CardTitle>
        <CardAction>
          <ViewAll to="/management/audit-logs" />
        </CardAction>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? (
          <ListSkeleton rows={7} />
        ) : state.status === "error" ? (
          <p className="text-muted-foreground">{t("mgmt.common.failed")}</p>
        ) : items.length === 0 ? (
          <p className="text-muted-foreground">{t("mgmt.overview.recentEmpty")}</p>
        ) : (
          <ul className="flex flex-col gap-1">
            {items.map((e) => (
              <li key={e.id} className="flex items-center gap-3 py-1.5">
                <span className="flex min-w-0 flex-1 flex-col">
                  <ActionTag action={e.action} bare />
                  <span className="truncate text-muted-foreground">{e.actor}</span>
                </span>
                <span className="tabular shrink-0 text-muted-foreground">{fmt.relative(e.occurredAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

/** Registered OAuth clients, like the account side's linked accounts. */
function ClientsCard({ state }) {
  const { t } = useI18n()
  const items = state.data?.items ?? []
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("mgmt.overview.clientsTitle")}</CardTitle>
        <CardAction>
          <ViewAll to="/management/clients" />
        </CardAction>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? (
          <ListSkeleton rows={3} />
        ) : state.status === "error" ? (
          <p className="text-muted-foreground">{t("mgmt.common.failed")}</p>
        ) : items.length === 0 ? (
          <p className="text-muted-foreground">{t("mgmt.overview.noClients")}</p>
        ) : (
          <ul className="flex flex-col gap-1">
            {items.map((c) => (
              <li key={c.clientId} className="flex items-center gap-3 py-1.5">
                {/* Public clients run in a browser or on a phone; confidential ones on a server that keeps a secret. */}
                <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground [&_svg]:size-4">
                  {c.authenticationMethod === "none" ? <MonitorSmartphoneIcon /> : <ServerIcon />}
                </span>
                <span className="flex min-w-0 flex-1 flex-col">
                  <span className="truncate font-medium">{c.clientName}</span>
                  <ClientType method={c.authenticationMethod} />
                </span>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

const DAY = 86400000

// Sign-in events from the audit log, as the chart's { at, result } entries (up to 5 pages of 100).
async function loadSignIns(days) {
  const from = new Date(Date.now() - days * DAY).toISOString()
  const items = []
  for (let page = 1; page <= 5; page++) {
    const { items: rows, total } = await listAuditLogs({ targetType: "AUTH", from, page, size: 100 })
    items.push(...rows)
    if (items.length >= total || rows.length === 0) break
  }
  return items
    .filter((e) => e.action === "LOGIN_SUCCESS" || e.action === "LOGIN_FAILED")
    .map((e) => ({ at: e.occurredAt, result: e.action === "LOGIN_SUCCESS" ? "success" : "failed" }))
}

/* ---------- page ---------- */

export default function ManagementOverview() {
  const { t } = useI18n()
  const { can } = useAccess()
  const canUsers = can("user:read")
  const canAudit = can("audit:read")
  const [days, setDays] = useState(7)

  const users = useAsync(() => (canUsers ? listUsers({ size: 100 }) : denied()))
  const active = useAsync(() => (canUsers ? countUsers({ enabled: true }) : denied()))
  const roles = useAsync(() => (can("role:read") ? allRoles() : denied()))
  const permissions = useAsync(() => (can("permission:read") ? allPermissions() : denied()))
  const clients = useAsync(() => (can("client:read") ? listClients({ size: 4 }) : denied()))
  const recent = useAsync(() => (canAudit ? listAuditLogs({ size: 7 }) : denied()))
  const suspicious = useAsync(() => (canUsers ? listSuspiciousLogins({ days: 1, threshold: 5 }) : denied()))
  const settings = useAsync(() => (can("settings:read") ? getSettings() : denied()))
  // Twice the visible range, so the chart can compare with the previous period.
  const signIns = useAsync(() => (canAudit ? loadSignIns(Math.max(days * 2, 30)) : denied()), [days])

  const userList = users.data?.items ?? []
  const locked = userList.filter((u) => u.locked).length
  const inactive = userList.filter((u) => !u.enabled).length
  const offPermissions = (permissions.data ?? []).filter((p) => !p.active).length
  const firstSuspect = suspicious.data?.[0]
  const s = settings.data
  const strongPolicy =
    !!s && s.passwordMinLength >= 8 && s.passwordRequireUppercase && s.passwordRequireLowercase && s.passwordRequireDigit

  // Platform health: each check passes or links to the page that fixes it.
  const checks = [
    canUsers && {
      key: "locked",
      ok: locked === 0,
      to: "/management/users",
      label: t("mgmt.overview.checks.locked"),
      hint: t("mgmt.overview.checks.lockedHint", { n: locked }),
    },
    canUsers && {
      key: "suspicious",
      ok: !firstSuspect,
      to: firstSuspect ? `/management/users/${firstSuspect.userId}?tab=history` : "/management/users",
      label: t("mgmt.overview.checks.suspicious"),
      hint: firstSuspect
        ? t("mgmt.overview.checks.suspiciousHint", { name: firstSuspect.username, n: firstSuspect.failedCount })
        : "",
    },
    canUsers && {
      key: "inactive",
      ok: inactive === 0,
      to: "/management/users?status=inactive",
      label: t("mgmt.overview.checks.inactive"),
      hint: t("mgmt.overview.checks.inactiveHint", { n: inactive }),
    },
    can("permission:read") && {
      key: "permissions",
      ok: offPermissions === 0,
      to: "/management/permissions",
      label: t("mgmt.overview.checks.permissions"),
      hint: t("mgmt.overview.checks.permissionsHint", { n: offPermissions }),
    },
    can("settings:read") && {
      key: "policy",
      ok: strongPolicy,
      to: "/management/settings",
      label: t("mgmt.overview.checks.policy"),
      hint: t("mgmt.overview.checks.policyHint"),
    },
  ].filter(Boolean)
  const checksLoading = [users, suspicious, permissions, settings].some((x) => x.status === "loading")

  const count = (state, value) => (state.status === "success" ? value : "–")
  const stats = [
    {
      icon: UsersIcon,
      value: count(active, active.data),
      unit: users.data ? t("mgmt.overview.ofTotal", { n: users.data.total }) : null,
      label: t("mgmt.overview.activeUsers"),
      to: "/management/users",
      strong: true,
      show: canUsers,
    },
    { icon: ShieldCheckIcon, value: count(roles, roles.data?.length), label: t("mgmt.overview.roles"), to: "/management/roles", show: can("role:read") },
    {
      icon: KeyRoundIcon,
      value: count(permissions, permissions.data?.length),
      label: t("mgmt.overview.permissions"),
      to: "/management/permissions",
      show: can("permission:read"),
    },
    { icon: AppWindowIcon, value: count(clients, clients.data?.total), label: t("mgmt.overview.clients"), to: "/management/clients", show: can("client:read") },
  ].filter((x) => x.show)

  return (
    <div className="flex flex-col gap-6">
      <div className="motion-rise flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
        <h1 className="text-3xl leading-tight font-medium tracking-tight">{t("mgmt.overview.title")}</h1>
        <div className="grid grid-cols-2 gap-x-6 gap-y-4 sm:flex sm:flex-wrap sm:gap-x-8">
          {stats.map((st) => (
            <Stat key={st.label} icon={st.icon} value={st.value} unit={st.unit} label={st.label} to={st.to} strong={st.strong} />
          ))}
        </div>
      </div>

      {(checks.length > 0 || canAudit) && (
        <div className="grid gap-6 lg:grid-cols-12">
          {checks.length > 0 && (
            <Checkup
              checks={checks}
              loading={checksLoading}
              title={t("mgmt.overview.healthTitle")}
              description={t("mgmt.overview.healthBody")}
              passedLabel={t("mgmt.overview.healthPassed", { n: checks.length })}
            />
          )}
          {canAudit && (
            <SignInChart
              history={signIns}
              days={days}
              setDays={setDays}
              title={t("mgmt.overview.chartTitle")}
              description={t("mgmt.overview.chartBody")}
            />
          )}
        </div>
      )}

      {/* Activity on the left; roles and clients stacked on the right so both columns end level. */}
      <div className="grid gap-6 lg:grid-cols-12">
        {canAudit && <RecentActivityCard state={recent} />}
        {(can("role:read") || can("client:read")) && (
          <div className={cn("flex flex-col gap-6", canAudit ? "lg:col-span-5" : "lg:col-span-12")}>
            {can("role:read") && <RolesCard state={roles} />}
            {can("client:read") && <ClientsCard state={clients} />}
          </div>
        )}
      </div>

      {can("permission:read") && <AccessMap state={permissions} />}
    </div>
  )
}
