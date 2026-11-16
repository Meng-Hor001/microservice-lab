import { useState } from "react"
import { EyeIcon, EyeOffIcon } from "lucide-react"
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from "@/components/ui/input-group"
import { useI18n } from "@/lib/i18n-context"

export function PasswordInput({ id, value, onChange, invalid, autoComplete, describedBy }) {
  const { t } = useI18n()
  const [visible, setVisible] = useState(false)
  return (
    <InputGroup>
      <InputGroupInput
        id={id}
        type={visible ? "text" : "password"}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
        aria-invalid={invalid}
        aria-describedby={describedBy}
      />
      <InputGroupAddon align="inline-end">
        <InputGroupButton
          size="icon-xs"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? t("security.hide") : t("security.show")}
          aria-pressed={visible}
        >
          {visible ? <EyeOffIcon /> : <EyeIcon />}
        </InputGroupButton>
      </InputGroupAddon>
    </InputGroup>
  )
}
