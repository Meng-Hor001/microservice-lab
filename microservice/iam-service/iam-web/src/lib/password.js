import { useEffect, useMemo, useState } from "react"
import { getPasswordPolicy } from "@/lib/api"

// Used until the server's policy arrives, or when it can't be loaded. The server always enforces its own.
export const DEFAULT_POLICY = {
  minLength: 8,
  maxLength: 64,
  requireUppercase: true,
  requireLowercase: true,
  requireDigit: true,
  requireSymbol: true,
}

/** Turns the password policy from system settings into checklist rules: { key, n?, test }. */
export function rulesFor(policy) {
  const p = { ...DEFAULT_POLICY, ...policy }
  const rules = [{ key: "length", n: p.minLength, test: (v) => v.length >= p.minLength && v.length <= p.maxLength }]
  if (p.requireUppercase) rules.push({ key: "upper", test: (v) => /[A-Z]/.test(v) })
  if (p.requireLowercase) rules.push({ key: "lower", test: (v) => /[a-z]/.test(v) })
  if (p.requireDigit) rules.push({ key: "number", test: (v) => /\d/.test(v) })
  if (p.requireSymbol) rules.push({ key: "symbol", test: (v) => /[^A-Za-z0-9]/.test(v) })
  return rules
}

export const PASSWORD_RULES = rulesFor(DEFAULT_POLICY)

let cachedPolicy = null

/** The live password rules an administrator configured; one request per page load. */
export function usePasswordRules() {
  const [policy, setPolicy] = useState(cachedPolicy ?? DEFAULT_POLICY)
  useEffect(() => {
    if (cachedPolicy) return
    let alive = true
    getPasswordPolicy().then(
      (p) => {
        cachedPolicy = p
        if (alive) setPolicy(p)
      },
      () => {}
    )
    return () => {
      alive = false
    }
  }, [])
  return useMemo(() => rulesFor(policy), [policy])
}
