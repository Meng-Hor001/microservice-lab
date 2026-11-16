import { AppWindowIcon, KeyRoundIcon, LayoutDashboardIcon, ScrollTextIcon, SettingsIcon, ShieldCheckIcon, UsersIcon } from "lucide-react"

// Paths are relative to the router basename (/me/), so /management/users lives at /me/management/users.
export const MGMT_NAV = [
  { group: null, items: [{ path: "/management", key: "overview", icon: LayoutDashboardIcon, end: true }] },
  {
    group: "access",
    items: [
      { path: "/management/users", key: "users", icon: UsersIcon, permission: "user:read" },
      { path: "/management/roles", key: "roles", icon: ShieldCheckIcon, permission: "role:read" },
      { path: "/management/permissions", key: "permissions", icon: KeyRoundIcon, permission: "permission:read" },
    ],
  },
  {
    group: "system",
    items: [
      { path: "/management/clients", key: "clients", icon: AppWindowIcon, permission: "client:read" },
      { path: "/management/audit-logs", key: "audit", icon: ScrollTextIcon, permission: "audit:read" },
      { path: "/management/settings", key: "settings", icon: SettingsIcon, permission: "settings:read" },
    ],
  },
]

export function findNav(pathname) {
  let best = null
  for (const g of MGMT_NAV) {
    for (const item of g.items) {
      const match = item.end ? pathname === item.path : pathname === item.path || pathname.startsWith(`${item.path}/`)
      if (match && (!best || item.path.length > best.item.path.length)) best = { group: g.group, item }
    }
  }
  return best
}
