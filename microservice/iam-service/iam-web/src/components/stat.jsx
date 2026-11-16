import { Link } from "react-router-dom"
import { CountUp } from "@/hooks/use-count-up"
import { cn } from "@/lib/utils"

/** A figure in a page header: icon chip, number and what it counts. Shared by both overviews. */
export function Stat({ icon: Icon, value, unit, label, strong, to }) {
  return (
    <Link
      to={to}
      className="group flex items-center gap-3 rounded-md py-1 pr-2 outline-none focus-visible:ring-2 focus-visible:ring-ring"
    >
      <span
        className={cn(
          "flex size-11 shrink-0 items-center justify-center rounded-md [&_svg]:size-5",
          strong
            ? "bg-primary text-primary-foreground"
            : "bg-card text-muted-foreground ring-1 ring-foreground/8"
        )}
      >
        <Icon />
      </span>
      <span className="flex flex-col">
        <span className="flex items-baseline gap-1.5">
          <span className="tabular text-xl font-medium"><CountUp value={value} /></span>
          {unit && <span className="text-xs text-muted-foreground">{unit}</span>}
        </span>
        <span className="text-xs text-muted-foreground transition-colors group-hover:text-foreground">{label}</span>
      </span>
    </Link>
  )
}
