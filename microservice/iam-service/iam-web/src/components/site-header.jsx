import { KeyRoundIcon, LogOutIcon, ShieldCheckIcon, UserRoundIcon } from "lucide-react"
import { NavLink, useNavigate } from "react-router-dom"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ModeToggle } from "@/components/mode-toggle"
import { LanguageSwitcher } from "@/components/language-switcher"
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip"
import { useAccount } from "@/lib/account"
import { getSessions } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { initials } from "@/lib/providers"
import { useAsync } from "@/lib/use-async"

function LiveSessions() {
  const { t } = useI18n()
  const { data } = useAsync(getSessions)
  if (!data?.length) return null
  const count = data.length
  const current = data.find((s) => s.current)

  return (
    <Tooltip>
      <TooltipTrigger asChild>
        {/* Same height as the language and theme buttons next to it; the device detail lives in the tooltip. */}
        <Button asChild variant="outline" size="sm" className="hidden gap-2 sm:inline-flex">
          <NavLink to="/sessions">
            <span aria-hidden className="relative flex size-2">
              <span className="motion-halo absolute inset-0 rounded-full bg-primary" />
              <span className="relative size-2 rounded-full bg-primary" />
            </span>
            <span className="tabular">
              {count === 1 ? t("header.sessionsOne") : t("header.sessionsMany", { n: count })}
            </span>
          </NavLink>
        </Button>
      </TooltipTrigger>
      {current && <TooltipContent>{t("header.thisDevice", { browser: current.browser })}</TooltipContent>}
    </Tooltip>
  )
}

/** `slotRef` receives an empty spot for page tabs: inline on wide screens, its own row below on smaller ones. */
export function SiteHeader({ onSignOut, label, home = "/overview", slotRef }) {
  const { t } = useI18n()
  const { user } = useAccount()
  const navigate = useNavigate()

  return (
    <header className="relative flex min-h-20 shrink-0 flex-wrap items-center gap-3 px-4 py-4 md:px-6">
      <NavLink
        to={home}
        className="flex min-w-0 flex-1 items-center gap-3 rounded-md outline-none focus-visible:ring-2 focus-visible:ring-ring"
      >
        <span className="flex size-10 shrink-0 items-center justify-center rounded-md bg-primary text-primary-foreground [&_svg]:size-5">
          <ShieldCheckIcon />
        </span>
        <span className="truncate text-base font-semibold">{label ?? t("app.name")}</span>
      </NavLink>

      {slotRef && (
        <div ref={slotRef} className="order-last flex w-full min-w-0 justify-center empty:hidden xl:absolute xl:top-1/2 xl:left-1/2 xl:w-auto xl:-translate-x-1/2 xl:-translate-y-1/2" />
      )}

      <LiveSessions />

      <LanguageSwitcher />

      <ModeToggle />

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="icon-lg" className="rounded-md" aria-label={t("header.accountMenu")}>
            <Avatar>
              {user?.avatarUrl && <AvatarImage src={user.avatarUrl} alt="" />}
              <AvatarFallback>{initials(user?.name)}</AvatarFallback>
            </Avatar>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" sideOffset={8} className="w-72">
          <DropdownMenuLabel className="flex flex-col gap-0.5">
            <span className="truncate text-sm font-medium text-foreground">{user?.name}</span>
            <span className="truncate text-xs font-normal text-muted-foreground">{user?.email}</span>
            <span className="pt-2 text-xs leading-relaxed font-normal text-muted-foreground">{t("app.footer")}</span>
          </DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem onSelect={() => navigate("/profile")}>
              <UserRoundIcon />
              {t("nav.profile")}
            </DropdownMenuItem>
            <DropdownMenuItem onSelect={() => navigate("/security")}>
              <KeyRoundIcon />
              {t("nav.security")}
            </DropdownMenuItem>
          </DropdownMenuGroup>
          <DropdownMenuSeparator />
          <DropdownMenuGroup>
            <DropdownMenuItem variant="destructive" onSelect={onSignOut}>
              <LogOutIcon />
              {t("header.signOut")}
            </DropdownMenuItem>
          </DropdownMenuGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  )
}
