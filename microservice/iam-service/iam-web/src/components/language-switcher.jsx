import { useId } from "react"
import { ChevronDownIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useI18n } from "@/lib/i18n-context"
import { cn } from "@/lib/utils"

/* Flat flags drawn in SVG (no emoji, so they look the same on every OS). Colors are the official ones. */

function FlagUK({ className }) {
  const id = useId()
  return (
    <svg viewBox="0 0 60 30" aria-hidden className={className} preserveAspectRatio="xMidYMid slice">
      <clipPath id={`${id}-s`}>
        <path d="M0,0 v30 h60 v-30 z" />
      </clipPath>
      <clipPath id={`${id}-t`}>
        <path d="M30,15 h30 v15 z v15 h-30 z h-30 v-15 z v-15 h30 z" />
      </clipPath>
      <g clipPath={`url(#${id}-s)`}>
        <path d="M0,0 v30 h60 v-30 z" fill="#012169" />
        <path d="M0,0 L60,30 M60,0 L0,30" stroke="#fff" strokeWidth="6" />
        <path d="M0,0 L60,30 M60,0 L0,30" clipPath={`url(#${id}-t)`} stroke="#C8102E" strokeWidth="4" />
        <path d="M30,0 v30 M0,15 h60" stroke="#fff" strokeWidth="10" />
        <path d="M30,0 v30 M0,15 h60" stroke="#C8102E" strokeWidth="6" />
      </g>
    </svg>
  )
}

function FlagKH({ className }) {
  return (
    <svg viewBox="0 0 25 16" aria-hidden className={className} preserveAspectRatio="xMidYMid slice">
      <rect width="25" height="16" fill="#032EA1" />
      <rect y="4" width="25" height="8" fill="#E00025" />
      {/* Angkor Wat, simplified so it still reads at icon size */}
      <g fill="#fff">
        <rect x="7.4" y="10.4" width="10.2" height="0.8" />
        <rect x="8.2" y="8.8" width="8.6" height="1.6" />
        <path d="M11.7 8.8 L12.5 5.2 L13.3 8.8 Z" />
        <path d="M9.6 8.8 L10.15 6.6 L10.7 8.8 Z" />
        <path d="M14.3 8.8 L14.85 6.6 L15.4 8.8 Z" />
        <path d="M8.2 8.8 L8.6 7.6 L9 8.8 Z" />
        <path d="M16 8.8 L16.4 7.6 L16.8 8.8 Z" />
      </g>
    </svg>
  )
}

const LANGUAGES = [
  { code: "en", short: "EN", name: "English", Flag: FlagUK },
  { code: "km", short: "ខ្មែរ", name: "ភាសាខ្មែរ", Flag: FlagKH },
]

function Flag({ Flag: F }) {
  return (
    <span className="inline-flex h-3.5 w-5 shrink-0 overflow-hidden rounded-[2px] ring-1 ring-foreground/10">
      <F className="size-full" />
    </span>
  )
}

/** English ⇄ Khmer as a dropdown with flags. Each name is written in its own language. */
export function LanguageSwitcher({ className, align = "end" }) {
  const { t, locale, setLocale } = useI18n()
  const current = LANGUAGES.find((l) => l.code === locale) ?? LANGUAGES[0]

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="sm" className={cn("gap-2", className)} aria-label={t("header.language")}>
          <Flag Flag={current.Flag} />
          {current.short}
          <ChevronDownIcon data-icon="inline-end" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align={align} className="w-48">
        <DropdownMenuLabel className="text-muted-foreground">{t("header.language")}</DropdownMenuLabel>
        <DropdownMenuSeparator />
        <DropdownMenuRadioGroup value={locale} onValueChange={setLocale}>
          {LANGUAGES.map((l) => (
            <DropdownMenuRadioItem key={l.code} value={l.code} lang={l.code} className="gap-3">
              <Flag Flag={l.Flag} />
              {l.name}
            </DropdownMenuRadioItem>
          ))}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
