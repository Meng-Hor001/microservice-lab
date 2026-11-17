import { ActivityIcon, Settings2Icon, LayoutDashboardIcon, HistoryIcon, KeyRoundIcon, LinkIcon, MonitorSmartphoneIcon, PowerIcon, UserRoundIcon } from "lucide-react"

export const NAV = [
  { path: "/overview", key: "overview", group: "home", icon: LayoutDashboardIcon, wide: true },
  { path: "/profile", key: "profile", group: "account", icon: UserRoundIcon },
  { path: "/security", key: "security", group: "account", icon: KeyRoundIcon },
  { path: "/linked-accounts", key: "linked", group: "account", icon: LinkIcon },
  { path: "/sessions", key: "sessions", group: "activity", icon: MonitorSmartphoneIcon },
  { path: "/login-history", key: "history", group: "activity", icon: HistoryIcon },
  { path: "/activity", key: "activity", group: "activity", icon: ActivityIcon },
  { path: "/deactivate", key: "deactivate", group: "danger", icon: PowerIcon, danger: true, narrow: true },
]

// Shown in the rail only for accounts with the ADMIN role.
export const ADMIN_NAV = { path: "/management", key: "management", group: "admin", icon: Settings2Icon }
