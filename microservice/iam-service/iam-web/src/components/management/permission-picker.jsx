import { useMemo, useState } from "react"
import { Checkbox } from "@/components/ui/checkbox"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Skeleton } from "@/components/ui/skeleton"
import { useI18n } from "@/lib/i18n-context"

/**
 * Checkbox list of items grouped by `groupOf`. Used for picking permissions (grouped by resource)
 * and roles. `selected` is a Set of uuids.
 */
export function ItemPicker({ items, loading, selected, onChange, label, sublabel, groupOf, exclude = [] }) {
  const { t } = useI18n()
  const [q, setQ] = useState("")
  const hidden = useMemo(() => new Set(exclude), [exclude])

  const groups = useMemo(() => {
    const needle = q.trim().toLowerCase()
    const map = new Map()
    for (const item of items ?? []) {
      if (hidden.has(item.uuid)) continue
      if (needle && !`${label(item)} ${sublabel?.(item) ?? ""}`.toLowerCase().includes(needle)) continue
      const g = groupOf ? groupOf(item) : ""
      if (!map.has(g)) map.set(g, [])
      map.get(g).push(item)
    }
    return [...map.entries()]
  }, [items, q, hidden, label, sublabel, groupOf])

  const toggle = (uuid, on) => {
    const next = new Set(selected)
    if (on) next.add(uuid)
    else next.delete(uuid)
    onChange(next)
  }

  return (
    <div className="flex flex-col gap-3">
      <Input
        type="search"
        aria-label={t("mgmt.common.search")}
        placeholder={t("mgmt.common.search")}
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />
      <ScrollArea className="h-64 rounded-lg border">
        <div className="flex flex-col gap-4 p-3">
          {loading &&
            [0, 1, 2, 3].map((i) => <Skeleton key={i} className="h-8" />)}
          {!loading && groups.length === 0 && (
            <p className="py-6 text-center text-sm text-muted-foreground">{t("mgmt.common.noResults")}</p>
          )}
          {groups.map(([group, list]) => (
            <div key={group} className="flex flex-col gap-1">
              {group && <p className="px-1 pb-1 text-xs font-medium text-muted-foreground">{group}</p>}
              {list.map((item) => {
                const id = `pick-${item.uuid}`
                return (
                  <Label
                    key={item.uuid}
                    htmlFor={id}
                    className="flex cursor-pointer items-center gap-3 rounded-md px-2 py-2 font-normal hover:bg-muted"
                  >
                    <Checkbox
                      id={id}
                      checked={selected.has(item.uuid)}
                      onCheckedChange={(v) => toggle(item.uuid, v === true)}
                    />
                    <span className="flex min-w-0 flex-col">
                      <span className="truncate font-mono text-sm">{label(item)}</span>
                      {sublabel?.(item) && (
                        <span className="truncate text-xs text-muted-foreground">{sublabel(item)}</span>
                      )}
                    </span>
                  </Label>
                )
              })}
            </div>
          ))}
        </div>
      </ScrollArea>
      <p className="tabular text-xs text-muted-foreground">
        {selected.size > 0 ? t("mgmt.common.selected", { n: selected.size }) : t("mgmt.users.pick")}
      </p>
    </div>
  )
}
