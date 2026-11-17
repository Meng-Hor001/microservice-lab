import { useContext, useEffect, useRef } from "react"
import { createPortal } from "react-dom"
import { Link } from "react-router-dom"
import { TopbarSlotContext } from "@/lib/topbar"
import { cn } from "@/lib/utils"

/**
 * Pill tab bar: a soft grey track, the active tab is a solid dark pill.
 * Each item: { key, to, label, active, badge? }. A badge shows only when it is a number above 0.
 * Scrolls sideways when it doesn't fit (mobile), keeping the active tab in view.
 */
export function TopTabs({ label, items, className }) {
  const activeRef = useRef(null)
  const activeKey = items.find((n) => n.active)?.key

  useEffect(() => {
    activeRef.current?.scrollIntoView({ block: "nearest", inline: "center" })
  }, [activeKey])

  return (
    <nav aria-label={label} className={cn("min-w-0 overflow-x-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden", className)}>
      <ul className="flex w-max items-center gap-1 rounded-full bg-muted p-1 ring-1 ring-border/60">
        {items.map((n) => (
          <li key={n.key}>
            <Link
              to={n.to}
              replace
              ref={n.active ? activeRef : undefined}
              aria-current={n.active ? "page" : undefined}
              className={cn(
                "flex h-9 items-center gap-2 rounded-full px-4 text-sm font-medium whitespace-nowrap outline-none transition-[color,background-color,transform] duration-150 ease-(--ease-out-soft) focus-visible:ring-2 focus-visible:ring-ring active:scale-[0.97]",
                n.active ? "bg-foreground text-background shadow-sm" : "text-muted-foreground hover:bg-background hover:text-foreground"
              )}
            >
              {n.label}
              {n.badge > 0 && (
                <span
                  className={cn(
                    "tabular flex h-5 min-w-5 items-center justify-center rounded-full px-1.5 text-xs leading-none",
                    n.active ? "bg-background/20 text-background" : "bg-destructive/10 text-destructive"
                  )}
                >
                  {n.badge}
                </span>
              )}
            </Link>
          </li>
        ))}
      </ul>
    </nav>
  )
}

/** Renders a page's TopTabs inside the header's tab slot; renders nothing when the shell has no slot. */
export function TopbarTabs(props) {
  const slot = useContext(TopbarSlotContext)
  return slot ? createPortal(<TopTabs {...props} />, slot) : null
}
