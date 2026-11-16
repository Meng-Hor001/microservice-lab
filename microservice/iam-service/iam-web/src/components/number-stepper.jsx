import { MinusIcon, PlusIcon } from "lucide-react"
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from "@/components/ui/input-group"
import { useI18n } from "@/lib/i18n-context"
import { cn } from "@/lib/utils"

/**
 * Whole-number input with − / + buttons that keep the value between `min` and `max`.
 * `value` is a string (so the field can be empty while typing); `onChange` receives a string too.
 * Arrow up/down step the value, and leaving the field pulls an out-of-range number back into range.
 */
export function NumberStepper({ id, value, onChange, min, max, step = 1, disabled, className, ...props }) {
  const { t } = useI18n()
  const parsed = /^\d+$/.test(String(value)) ? Number(value) : null
  const clamp = (n) => Math.min(max, Math.max(min, n))
  const bump = (delta) => onChange(String(clamp((parsed ?? min) + delta)))

  return (
    <InputGroup
      className={cn(
        "sm:max-w-40 has-disabled:bg-transparent has-disabled:opacity-100 dark:has-disabled:bg-input/30",
        "has-[input:disabled]:bg-input/50 has-[input:disabled]:opacity-50 dark:has-[input:disabled]:bg-input/80",
        className
      )}
      data-disabled={disabled || undefined}
    >
      <InputGroupAddon>
        <InputGroupButton
          size="icon-xs"
          aria-label={t("mgmt.common.decrease")}
          disabled={disabled || (parsed !== null && parsed <= min)}
          onClick={() => bump(-step)}
        >
          <MinusIcon />
        </InputGroupButton>
      </InputGroupAddon>
      <InputGroupInput
        id={id}
        inputMode="numeric"
        className="tabular text-center"
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value.replace(/\D/g, ""))}
        onBlur={() => parsed !== null && parsed !== clamp(parsed) && onChange(String(clamp(parsed)))}
        onKeyDown={(e) => {
          if (e.key === "ArrowUp" || e.key === "ArrowDown") {
            e.preventDefault()
            bump(e.key === "ArrowUp" ? step : -step)
          }
        }}
        {...props}
      />
      <InputGroupAddon align="inline-end">
        <InputGroupButton
          size="icon-xs"
          aria-label={t("mgmt.common.increase")}
          disabled={disabled || (parsed !== null && parsed >= max)}
          onClick={() => bump(step)}
        >
          <PlusIcon />
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  )
}
