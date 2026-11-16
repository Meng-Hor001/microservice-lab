import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group"
import { cn } from "@/lib/utils"

/**
 * Single-choice filter as separate chips: quiet outlined chips, the selected one filled in graphite.
 * Built on ToggleGroup, so arrow keys move between chips and screen readers announce the pressed one.
 */
export function FilterChips({ className, children, ...props }) {
  return (
    <ToggleGroup type="single" variant="outline" spacing={2} className={cn("flex-wrap justify-start", className)} {...props}>
      {children}
    </ToggleGroup>
  )
}

// The toggle base styles set `data-[state=on]:bg-muted`; `!` makes the filled selected state win.
export function FilterChip({ className, ...props }) {
  return (
    <ToggleGroupItem
      className={cn(
        "h-9 rounded-md! px-3.5 font-normal text-muted-foreground transition-colors hover:text-foreground",
        "data-[state=on]:border-foreground! data-[state=on]:bg-foreground! data-[state=on]:font-medium data-[state=on]:text-background!",
        className
      )}
      {...props}
    />
  )
}
