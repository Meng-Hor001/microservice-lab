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
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
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
    <NavLink
      to="/sessions"
      className="hidden items-center gap-3 rounded-full bg-card py-1.5 pr-4 pl-3 shadow-[0_1px_2px_rgba(0,0,0,0.05)] ring-1 ring-foreground/8 transition-colors duration-150 hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring sm:flex"
    >
      <span aria-hidden className="relative flex size-2">
        <span className="motion-halo absolute inset-0 rounded-full bg-primary" />
        <span className="relative size-2 rounded-full bg-primary" />
      </span>
      <span className="flex flex-col leading-tight">
        <span className="tabular text-sm font-medium">
          {count === 1 ? t("header.sessionsOne") : t("header.sessionsMany", { n: count })}
        </span>
        {current && (
          <span className="text-xs text-muted-foreground">
            {t("header.thisDevice", { browser: current.browser })}
          </span>
        )}
      </span>
    </NavLink>
  )
}

export function SiteHeader({ onSignOut }) {
  const { t, locale, setLocale } = useI18n()
  const { user } = useAccount()
  const navigate = useNavigate()

  return (
    <header className="flex h-20 shrink-0 items-center gap-3 px-4 md:px-6">
      <NavLink
        to="/overview"
        className="flex min-w-0 flex-1 items-center gap-3 rounded-lg outline-none focus-visible:ring-2 focus-visible:ring-ring"
      >
        <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary text-primary-foreground [&_svg]:size-5">
          <ShieldCheckIcon />
        </span>
        <span className="truncate text-base font-semibold">{t("app.name")}</span>
      </NavLink>

      <LiveSessions />

      <ToggleGroup
        type="single"
        variant="outline"
        size="sm"
        spacing={0}
        value={locale}
        onValueChange={(v) => v && setLocale(v)}
        aria-label={t("header.language")}
      >
        <ToggleGroupItem value="en" aria-label="English">EN</ToggleGroupItem>
        <ToggleGroupItem value="km" aria-label="ភាសាខ្មែរ">ខ្មែរ</ToggleGroupItem>
      </ToggleGroup>

      <ModeToggle />

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="icon-lg" className="rounded-full" aria-label={t("header.accountMenu")}>
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
