import { useMemo } from "react"
import { Link } from "react-router-dom"
import { ChevronRightIcon, ShieldCheckIcon, UserPlusIcon } from "lucide-react"
import { KeyTag, PageHeader, StatusDot } from "@/components/management/states"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import { Card, CardAction, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useAccess } from "@/lib/access"
import { allPermissions, allRoles, countUsers, listUsers } from "@/lib/admin-api"
import { useI18n } from "@/lib/i18n-context"
import { initials } from "@/lib/providers"
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

/* ---------- stat strip: one panel, four figures ---------- */

function Stat({ label, value, note, to, loading, failed }) {
  const { t } = useI18n()
  const body = (
    <>
      <span className="text-muted-foreground">{label}</span>
      {loading ? (
        <Skeleton className="h-9 w-20" />
      ) : failed ? (
        <span className="text-muted-foreground">{t("mgmt.common.failed")}</span>
      ) : (
        <span className="tabular text-4xl leading-none font-medium tracking-tight">{value}</span>
      )}
      <span className="min-h-6 text-muted-foreground">{!loading && !failed ? note : null}</span>
    </>
  )
  const cls = "flex flex-col gap-3 px-5 py-5"
  return to ? (
    <Link
      to={to}
      className={cn(cls, "transition-colors hover:bg-muted/50 focus-visible:bg-muted/50 focus-visible:outline-none")}
    >
      {body}
    </Link>
  ) : (
    <div className={cls}>{body}</div>
  )
}

/* ---------- the access map: resources × actions ---------- */

function AccessCell({ permission, resource, action }) {
  const { t } = useI18n()
  if (!permission) {
    return (
      <span className="flex size-9 items-center justify-center" aria-label={`${resource}:${action} ${t("mgmt.overview.legendNone")}`}>
        <span aria-hidden className="size-1 rounded-full bg-foreground/15" />
      </span>
    )
  }
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Link
          to={`/management/permissions?resource=${encodeURIComponent(resource)}`}
          aria-label={`${permission.name}, ${permission.active ? t("mgmt.overview.legendActive") : t("mgmt.overview.legendInactive")}`}
          className="group/cell flex size-9 items-center justify-center rounded-md focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
        >
          <span
            className={cn(
              "flex size-6 items-center justify-center rounded-[5px] transition-transform duration-150 group-hover/cell:scale-110",
              permission.active ? "bg-primary" : "border-[1.5px] border-primary/60 bg-background"
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
    { key: "legendInactive", swatch: <span className="size-4 rounded-[4px] border-[1.5px] border-primary/60" /> },
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
      swatch: (
        <span className="flex size-4 items-center justify-center">
          <span className="size-1 rounded-full bg-foreground/25" />
        </span>
      ),
    },
  ]
  return (
    <ul className="flex flex-wrap gap-x-6 gap-y-2 text-muted-foreground">
      {items.map((i) => (
        <li key={i.key} className="flex items-center gap-2">
          <span aria-hidden className="flex">{i.swatch}</span>
          {t(`mgmt.overview.${i.key}`)}
        </li>
      ))}
    </ul>
  )
}

function AccessMap({ state }) {
  const { t } = useI18n()
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
    <Card className="lg:col-span-2">
      <CardHeader>
        <CardTitle>{t("mgmt.overview.accessMap")}</CardTitle>
        <CardDescription className="tabular">
          {state.data
            ? t("mgmt.overview.accessMapBody", { n: state.data.length, r: resources.length })
            : " "}
        </CardDescription>
        <CardAction>
          <Button asChild variant="ghost" size="sm">
            <Link to="/management/permissions">
              {t("mgmt.overview.viewAll")}
              <ChevronRightIcon data-icon="inline-end" />
            </Link>
          </Button>
        </CardAction>
      </CardHeader>
      <CardContent className="flex-1">
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
          <div className="-mx-4 overflow-x-auto px-4">
            <div
              role="table"
              aria-label={t("mgmt.overview.accessMap")}
              className="grid w-max min-w-full items-center gap-x-1"
              style={{ gridTemplateColumns: `minmax(8rem, 1fr) repeat(${actions.length}, 4.5rem)` }}
            >
              <div role="row" className="contents">
                <span role="columnheader" className="pb-2 text-muted-foreground">
                  {t("mgmt.overview.resource")}
                </span>
                {actions.map((a) => (
                  <span role="columnheader" key={a} className="pb-2 text-center font-mono text-muted-foreground">
                    {a}
                  </span>
                ))}
              </div>
              {resources.map((r) => (
                <div role="row" key={r} className="group/row contents">
                  <span
                    role="rowheader"
                    className="flex h-11 items-center border-t font-mono transition-colors group-hover/row:text-foreground"
                  >
                    {r}
                  </span>
                  {actions.map((a) => (
                    <span role="cell" key={a} className="flex h-11 items-center justify-center border-t">
                      <AccessCell permission={cells.get(`${r}:${a}`)} resource={r} action={a} />
                    </span>
                  ))}
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
      {state.status === "success" && resources.length > 0 && (
        <CardFooter className="border-t">
          <Legend />
        </CardFooter>
      )}
    </Card>
  )
}

/* ---------- side column ---------- */

function RolesCard({ state }) {
  const { t } = useI18n()
  const roles = (state.data ?? []).slice(0, 6)
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("mgmt.overview.rolesTitle")}</CardTitle>
        <CardAction>
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
                  className="flex h-11 items-center gap-3 rounded-md px-2 transition-colors hover:bg-muted/60 focus-visible:bg-muted/60 focus-visible:outline-none"
                >
                  <KeyTag name={r.name} />
                  {r.system && <span className="text-muted-foreground">{t("mgmt.common.system")}</span>}
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

function InactiveCard({ state }) {
  const { t } = useI18n()
  const items = state.data?.items ?? []
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("mgmt.overview.inactiveTitle")}</CardTitle>
        <CardDescription>{t("mgmt.overview.inactiveBody")}</CardDescription>
      </CardHeader>
      <CardContent>
        {state.status === "loading" ? (
          <div className="flex flex-col gap-3">
            {[0, 1].map((i) => (
              <Skeleton key={i} className="h-9" />
            ))}
          </div>
        ) : state.status === "error" ? (
          <p className="text-muted-foreground">{t("mgmt.common.failed")}</p>
        ) : items.length === 0 ? (
          <StatusDot on label={t("mgmt.overview.inactiveNone")} />
        ) : (
          <ul className="-mx-2 flex flex-col">
            {items.map((u) => (
              <li key={u.uuid}>
                <Link
                  to={`/management/users/${u.uuid}`}
                  className="flex h-14 items-center gap-3 rounded-md px-2 transition-colors hover:bg-muted/60 focus-visible:bg-muted/60 focus-visible:outline-none"
                >
                  <Avatar className="rounded-lg">
                    <AvatarFallback className="rounded-lg">{initials(u.name)}</AvatarFallback>
                  </Avatar>
                  <span className="flex min-w-0 flex-col leading-tight">
                    <span className="truncate font-medium">{u.name}</span>
                    <span className="truncate font-mono text-muted-foreground">{u.username}</span>
                  </span>
                  <ChevronRightIcon className="ml-auto size-4 shrink-0 text-muted-foreground" />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
      {state.data?.total > items.length && (
        <CardFooter className="border-t">
          <Button asChild variant="ghost" size="sm" className="-ml-2">
            <Link to="/management/users?status=inactive">
              {t("mgmt.overview.viewAll")}
              <ChevronRightIcon data-icon="inline-end" />
            </Link>
          </Button>
        </CardFooter>
      )}
    </Card>
  )
}

/* ---------- page ---------- */

export default function ManagementOverview() {
  const { t } = useI18n()
  const { can } = useAccess()
  const canUsers = can("user:read")
  const users = useAsync(() => (canUsers ? countUsers() : denied()))
  const active = useAsync(() => (canUsers ? countUsers({ enabled: true }) : denied()))
  const inactive = useAsync(() => (canUsers ? listUsers({ enabled: false, size: 4 }) : denied()))
  const roles = useAsync(() => (can("role:read") ? allRoles() : denied()))
  const permissions = useAsync(() => (can("permission:read") ? allPermissions() : denied()))

  const share = users.data ? Math.round(((active.data ?? 0) / users.data) * 100) : null
  const activeRoles = roles.data?.filter((r) => r.active).length ?? 0
  const resourceCount = new Set((permissions.data ?? []).map((p) => p.resource)).size

  const stats = [
    {
      label: t("mgmt.overview.totalUsers"),
      state: users,
      value: users.data,
      note: users.data != null && active.data != null ? t("mgmt.overview.inactiveNote", { n: users.data - active.data }) : null,
      to: "/management/users",
      show: canUsers,
    },
    {
      label: t("mgmt.overview.activeUsers"),
      state: active,
      value: active.data,
      note: share !== null ? t("mgmt.overview.activeShare", { n: share }) : null,
      to: "/management/users?status=active",
      show: canUsers,
    },
    {
      label: t("mgmt.overview.roles"),
      state: roles,
      value: roles.data?.length,
      note: t("mgmt.overview.rolesNote", { n: activeRoles }),
      to: "/management/roles",
      show: can("role:read"),
    },
    {
      label: t("mgmt.overview.permissions"),
      state: permissions,
      value: permissions.data?.length,
      note: t("mgmt.overview.resourcesNote", { n: resourceCount }),
      to: "/management/permissions",
      show: can("permission:read"),
    },
  ].filter((s) => s.show)

  return (
    <>
      <PageHeader title={t("mgmt.overview.title")} intro={t("mgmt.overview.intro")}>
        {can("role:create") && (
          <Button asChild variant="outline">
            <Link to="/management/roles?new=1">
              <ShieldCheckIcon data-icon="inline-start" />
              {t("mgmt.overview.createRole")}
            </Link>
          </Button>
        )}
        {can("user:create") && (
          <Button asChild>
            <Link to="/management/users?new=1">
              <UserPlusIcon data-icon="inline-start" />
              {t("mgmt.overview.createUser")}
            </Link>
          </Button>
        )}
      </PageHeader>

      {stats.length > 0 && (
        <Card className="gap-0 py-0">
          <div className="grid grid-cols-2 lg:grid-cols-4">
            {stats.map((s, i) => (
                <div
                  key={s.label}
                  className={cn(
                    "border-border",
                    i % 2 === 1 && "border-l",
                    i >= 2 && "border-t lg:border-t-0",
                    i > 0 && "lg:border-l"
                  )}
                >
                  <Stat
                    label={s.label}
                    value={s.value}
                    note={s.note}
                    to={s.to}
                    loading={s.state.status === "loading"}
                    failed={s.state.status === "error"}
                  />
                </div>
            ))}
          </div>
        </Card>
      )}

      <div className="grid items-start gap-6 lg:grid-cols-3">
        {can("permission:read") && <AccessMap state={permissions} />}
        <div className="flex flex-col gap-6">
          {can("role:read") && <RolesCard state={roles} />}
          {canUsers && <InactiveCard state={inactive} />}
        </div>
      </div>
    </>
  )
}
