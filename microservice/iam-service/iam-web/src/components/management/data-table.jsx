import { ChevronRightIcon } from "lucide-react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { cn } from "@/lib/utils"

/*
 * Management tables. One look everywhere:
 * - header is a quiet band with muted labels, so it reads apart from data rows
 * - rows are 60px with a hairline between them; hover is a small surface step
 * - keyboard focus on the row's link lights the whole row, so focus is visible without a ring on every cell
 * - row actions sit quiet until the row is hovered or focused (always shown on touch screens)
 */

export function DataTable({ children, className }) {
  return (
    <Table
      className={cn(
        "[&_td:first-child]:pl-5 [&_td:last-child]:pr-5 [&_th:first-child]:pl-5 [&_th:last-child]:pr-5",
        className
      )}
    >
      {children}
    </Table>
  )
}

export function DataHead({ children }) {
  return (
    <TableHeader className="bg-muted/45">
      <TableRow className="border-b hover:bg-transparent">{children}</TableRow>
    </TableHeader>
  )
}

export function Th({ className, children, ...props }) {
  return (
    <TableHead className={cn("h-10 px-3 font-normal text-muted-foreground", className)} {...props}>
      {children}
    </TableHead>
  )
}

export const DataBody = TableBody

export function DataRow({ onOpen, className, children }) {
  return (
    <TableRow
      onClick={onOpen}
      className={cn(
        "group/row border-border/70 transition-colors hover:bg-muted/35 has-[a:focus-visible]:bg-muted/50 has-[[data-state=open]]:bg-muted/35",
        onOpen && "cursor-pointer",
        className
      )}
    >
      {children}
    </TableRow>
  )
}

export function Td({ className, children, ...props }) {
  return (
    <TableCell className={cn("h-15 px-3 py-2", className)} {...props}>
      {children}
    </TableCell>
  )
}

/** Hidden until the row is hovered or focused, on devices that can hover. */
export const revealOnRow =
  "transition-opacity [@media(hover:hover)]:opacity-0 group-hover/row:opacity-100 group-has-[:focus-visible]/row:opacity-100 group-has-[[data-state=open]]/row:opacity-100"

export function RowChevron() {
  return (
    <ChevronRightIcon
      aria-hidden
      className="ml-auto size-4 text-muted-foreground/50 transition-[color,transform] duration-150 group-hover/row:translate-x-0.5 group-hover/row:text-foreground"
    />
  )
}

/** Link styling for the main cell of a clickable row: no underline, focus lights the row instead. */
export const rowLink = "rounded-sm outline-none focus-visible:underline focus-visible:underline-offset-4"
