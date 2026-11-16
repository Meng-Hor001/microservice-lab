import { useMemo } from "react"
import { getClaims } from "@/lib/auth"

export const ADMIN_ROLE = "ADMIN"

export function useAccess() {
  return useMemo(() => {
    const { roles, permissions } = getClaims()
    const set = new Set(permissions)
    return {
      roles,
      isAdmin: roles.includes(ADMIN_ROLE),
      can: (permission) => set.has(permission),
    }
  }, [])
}

// Where someone lands after login or when opening the app root: admins go straight to Management.
export const homePath = () => (getClaims().roles.includes(ADMIN_ROLE) ? "/management" : "/overview")
