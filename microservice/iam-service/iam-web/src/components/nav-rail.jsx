import { Fragment } from "react"
import { NavLink, useLocation } from "react-router-dom"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useIsMobile } from "@/hooks/use-mobile"
import { useAccess } from "@/lib/access"
import { useI18n } from "@/lib/i18n-context"
import { ADMIN_NAV, NAV } from "@/lib/nav"
import { cn } from "@/lib/utils"

/**
 * The floating black icon rail: left edge on desktop, bottom of the screen on mobile.
 * `sections` is a list of item lists; a hairline divider separates sections.
 * Each item: { path, label, icon, active, danger }.
 */
export function Rail({ label, sections }) {
  const isMobile = useIsMobile()
  const visible = sections.filter((s) => s.length > 0)

  return (
    <nav
      aria-label={label}
      className="fixed inset-x-0 bottom-4 z-20 mx-auto w-fit md:inset-x-auto md:top-1/2 md:bottom-auto md:left-6 md:mx-0 md:-translate-y-1/2"
    >
      <ul className="flex items-center gap-2 rounded-full bg-sidebar p-2.5 shadow-[0_8px_24px_rgba(0,0,0,0.12)] dark:ring-1 dark:ring-sidebar-border md:flex-col">
        {visible.map((items, i) => (
          <Fragment key={i}>
            {i > 0 && <li aria-hidden className="mx-1.5 h-6 w-px bg-sidebar-border md:mx-0 md:my-1.5 md:h-px md:w-6" />}
            {items.map((n) => (
              <li key={n.path}>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <NavLink
                      to={n.path}
                      aria-label={n.label}
                      aria-current={n.active ? "page" : undefined}
                      className={cn(
                        "flex size-11 items-center justify-center rounded-full text-sidebar-foreground outline-none transition-[color,background-color,transform] duration-150 ease-[cubic-bezier(0.22,1,0.36,1)] focus-visible:ring-2 focus-visible:ring-sidebar-ring active:scale-[0.97] [&_svg]:size-5 [&_svg]:stroke-[1.75]",
                        n.danger
                          ? "hover:bg-rail-danger/15 hover:text-rail-danger"
                          : "hover:bg-sidebar-accent hover:text-sidebar-primary",
                        n.active &&
                          (n.danger
                            ? "bg-rail-danger/20 text-rail-danger hover:bg-rail-danger/20"
                            : "bg-primary text-primary-foreground shadow-[inset_0_1px_0_rgba(255,255,255,0.18),0_4px_12px_rgba(18,97,90,0.45)] hover:bg-primary hover:text-primary-foreground")
                      )}
                    >
                      <n.icon />
                    </NavLink>
                  </TooltipTrigger>
                  <TooltipContent side={isMobile ? "top" : "right"} sideOffset={12}>
                    {n.label}
                  </TooltipContent>
                </Tooltip>
              </li>
            ))}
          </Fragment>
        ))}
      </ul>
    </nav>
  )
}

const GROUPS = ["home", "account", "activity", "admin", "danger"]

/** Rail for the account pages. */
export function NavRail() {
  const { t } = useI18n()
  const { pathname } = useLocation()
  const { isAdmin } = useAccess()
  const items = isAdmin ? [...NAV, ADMIN_NAV] : NAV

  const sections = GROUPS.map((g) =>
    items
      .filter((n) => n.group === g)
      .map((n) => ({ path: n.path, label: t(`nav.${n.key}`), icon: n.icon, danger: n.danger, active: pathname === n.path }))
  )

  return <Rail label={t("nav.group")} sections={sections} />
}
