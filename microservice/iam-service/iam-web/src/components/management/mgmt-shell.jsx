import { useEffect } from "react"
import { Outlet, useLocation } from "react-router-dom"
import { ArrowLeftIcon } from "lucide-react"
import { Rail } from "@/components/nav-rail"
import { SiteHeader } from "@/components/site-header"
import { useAccess } from "@/lib/access"
import { useI18n } from "@/lib/i18n-context"
import { findNav, MGMT_NAV } from "@/components/management/nav"
import { cn } from "@/lib/utils"

/** Management uses the same floating icon rail and header as the account pages. */
export function MgmtShell({ onSignOut }) {
  const { t } = useI18n()
  const { pathname } = useLocation()
  const { can } = useAccess()
  const found = findNav(pathname)
  const title = found ? t(`mgmt.nav.${found.item.key}`) : t("mgmt.title")

  useEffect(() => {
    document.title = `${title} | ${t("mgmt.title")}`
  }, [title, t])

  const sections = [
    ...MGMT_NAV.map((g) =>
      g.items
        .filter((item) => !item.permission || can(item.permission))
        .map((item) => ({
          path: item.path,
          label: t(`mgmt.nav.${item.key}`),
          icon: item.icon,
          active: found?.item.path === item.path,
        }))
    ),
    [{ path: "/overview", label: t("mgmt.backToAccount"), icon: ArrowLeftIcon }],
  ]

  return (
    <div className="flex min-h-svh flex-col bg-background">
      <SiteHeader onSignOut={onSignOut} label={t("mgmt.title")} home="/management" />
      <Rail label={t("mgmt.title")} sections={sections} />
      <main className="flex flex-1 flex-col px-4 pt-4 pb-28 md:pr-6 md:pb-12 md:pl-28">
        <div
          key={pathname}
          className={cn("motion-rise mx-auto flex w-full flex-col gap-6", pathname === "/management" ? "max-w-7xl" : "max-w-5xl")}
        >
          <Outlet />
        </div>
      </main>
    </div>
  )
}
