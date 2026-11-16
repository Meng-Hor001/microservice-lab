import { KeyTag } from "@/components/management/states"
import { actionKey } from "@/lib/audit"

/** An audit action as a key tag (USER_LOCKED → user:locked); failed sign-ins use the deny tone. */
export function ActionTag({ action, className, bare }) {
  return <KeyTag name={actionKey(action)} tone={action === "LOGIN_FAILED" ? "deny" : "default"} bare={bare} className={className} />
}
