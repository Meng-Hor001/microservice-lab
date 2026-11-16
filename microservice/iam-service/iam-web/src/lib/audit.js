// Every action the backend records (see @Audited in the REST controllers and AccountLoginRecorder).
export const ACTION_GROUPS = {
  AUTH: ["LOGIN_SUCCESS", "LOGIN_FAILED"],
  USER: [
    "USER_CREATED",
    "USER_UPDATED",
    "USER_DELETED",
    "USER_ENABLED",
    "USER_DISABLED",
    "USER_LOCKED",
    "USER_UNLOCKED",
    "USER_PASSWORD_RESET",
    "ROLE_ASSIGNED",
    "ROLE_REVOKED",
    "PERMISSION_GRANTED",
    "PERMISSION_REVOKED",
    "IDENTITY_UNLINKED",
    "SESSION_REVOKED",
    "SESSIONS_REVOKED_ALL",
  ],
  ROLE: [
    "ROLE_CREATED",
    "ROLE_UPDATED",
    "ROLE_DELETED",
    "ROLE_ACTIVATED",
    "ROLE_DEACTIVATED",
    "ROLE_PERMISSION_ASSIGNED",
    "ROLE_PERMISSION_REVOKED",
  ],
  PERMISSION: ["PERMISSION_CREATED", "PERMISSION_UPDATED", "PERMISSION_DELETED", "PERMISSION_ACTIVATED", "PERMISSION_DEACTIVATED"],
  CLIENT: ["CLIENT_CREATED", "CLIENT_UPDATED", "CLIENT_SECRET_ROTATED", "CLIENT_DELETED"],
  SETTINGS: ["SETTINGS_UPDATED"],
}

/** USER_LOCKED → user:locked, so actions read like the permission key tags used across Management. */
export const actionKey = (action) => {
  const i = action.indexOf("_")
  return (i > 0 ? `${action.slice(0, i)}:${action.slice(i + 1)}` : action).toLowerCase()
}
