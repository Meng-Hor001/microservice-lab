import { clearSession, getAccessToken, IAM_URL } from "@/lib/auth"

export class ApiError extends Error {
  constructor(code, field = null, status = 0, detail = "") {
    super(detail || code)
    this.code = code
    this.field = field
    this.status = status
    this.detail = detail
  }
}

const API_BASE = import.meta.env.VITE_API_BASE ?? "/api"
const PROVIDERS = ["google", "github", "facebook", "telegram"]
const FIELD_MAP = {
  givenName: "name",
  familyName: "name",
  username: "username",
  email: "email",
  phoneNumber: "phone",
  pictureUrl: "avatar",
  currentPassword: "current",
  newPassword: "next",
  confirmPassword: "confirm",
}

function problemCode(type = "") {
  if (type.endsWith("duplicate-resource")) return "duplicate"
  if (type.endsWith("invalid-request")) return "invalid"
  if (type.endsWith("resource-not-found")) return "not_found"
  if (type.endsWith("version-conflict")) return "conflict"
  return "server"
}

export async function http(method, path, body, { auth = true } = {}) {
  const token = auth ? getAccessToken() : null
  if (auth && !token) {
    window.dispatchEvent(new Event("iam:unauthorized"))
    throw new ApiError("unauthorized", null, 401)
  }

  let response
  try {
    response = await fetch(API_BASE + path, {
      method,
      headers: {
        Accept: "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(body ? { "Content-Type": "application/json" } : {}),
      },
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError("network")
  }

  if (auth && response.status === 401) {
    clearSession()
    window.dispatchEvent(new Event("iam:unauthorized"))
    throw new ApiError("unauthorized", null, 401)
  }

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    const fieldError = problem?.errors?.[0]
    if (fieldError) {
      throw new ApiError("serverInvalid", FIELD_MAP[fieldError.field] ?? null, response.status, fieldError.message)
    }
    throw new ApiError(problemCode(problem?.type), null, response.status, problem?.detail ?? "")
  }

  if (response.status === 204) return null
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

const toProfile = (user, fallback = {}) => ({
  name: (user.name ?? "").replace(/\bnull\b/g, "").trim(),
  username: user.username ?? fallback.username ?? "",
  email: user.email,
  phone: user.phoneNumber ?? "",
  avatarUrl: user.pictureUrl ?? null,
})

export const getProfile = async () => toProfile(await http("GET", "/me"))

export async function updateProfile(profile) {
  const [givenName, ...rest] = profile.name.trim().split(/\s+/)
  const body = {
    username: profile.username.trim(),
    givenName,
    email: profile.email.trim(),
    phoneNumber: profile.phone.replace(/[\s-]/g, ""),
    pictureUrl: profile.avatarUrl ?? "",
  }
  if (rest.length) body.familyName = rest.join(" ")
  try {
    return toProfile(await http("PATCH", "/me", body), profile)
  } catch (error) {
    if (error.code === "duplicate" && /username/i.test(error.detail)) {
      throw new ApiError("username_taken", "username", error.status)
    }
    if (error.code === "duplicate") throw new ApiError("emailTaken", "email", error.status)
    throw error
  }
}

export async function changePassword({ current, next }) {
  try {
    await http("PUT", "/me/password", { currentPassword: current, newPassword: next, confirmPassword: next })
    return { ok: true }
  } catch (error) {
    if (error.code === "invalid" && /current password/i.test(error.detail)) {
      throw new ApiError("wrong_password", "current", error.status)
    }
    if (error.code === "invalid") throw new ApiError("passwordWeak", "next", error.status)
    throw error
  }
}

export const requestPasswordReset = () => http("POST", "/me/password/reset")

export async function confirmPasswordReset(token, newPassword) {
  try {
    await http(
      "POST",
      "/auth/password/reset/confirm",
      { token, newPassword, confirmPassword: newPassword },
      { auth: false }
    )
  } catch (error) {
    if (error.status === 400 || error.status === 404) throw new ApiError("resetLinkInvalid", null, error.status, error.detail)
    throw error
  }
}

export async function getProviders() {
  const identities = (await http("GET", "/me/identities")) ?? []
  return PROVIDERS.map((id) => {
    const identity = identities.find((item) => item.provider?.toLowerCase() === id)
    return identity
      ? {
          id,
          linked: true,
          identityId: identity.uuid,
          account: identity.email ?? identity.displayName ?? "",
          linkedAt: identity.linkedAt,
        }
      : { id, linked: false }
  })
}

export function linkProvider() {
  window.location.assign(`${IAM_URL}/profile/accounts`)
  return new Promise(() => {})
}

export const unlinkProvider = (provider) => http("DELETE", `/me/identities/${provider.identityId}`)

export const getSessions = async () => (await http("GET", "/me/sessions")) ?? []
export const terminateSession = (id) => http("DELETE", `/me/sessions/${id}`)
export const terminateOtherSessions = () => http("DELETE", "/me/sessions")
// Public, so the reset-by-link page can show the rules before anyone signs in.
export const getPasswordPolicy = () => http("GET", "/auth/password-policy", undefined, { auth: false })
// Every session including this one; the caller signs out locally afterwards.
export const terminateAllSessions = () => http("DELETE", "/me/sessions/all")

export async function getLoginHistory({ result = "all", range = "30", page = 1, pageSize = 10 } = {}) {
  const query = new URLSearchParams({ page: String(page - 1), size: String(pageSize) })
  if (result !== "all") query.set("result", result)
  if (range !== "all") query.set("days", range)
  const data = await http("GET", `/me/login-history?${query}`)
  return { items: data?.content ?? [], total: data?.page?.totalElements ?? 0 }
}

export const getActivity = async () => (await http("GET", "/me/activity")) ?? []

export async function getDeactivation() {
  try {
    return await http("GET", "/me/deactivation")
  } catch (error) {
    if (error.status === 404) return null
    throw error
  }
}

export const requestDeactivation = (reason) => http("POST", "/me/deactivation", { reason })
export const cancelDeactivation = async () => {
  await http("DELETE", "/me/deactivation")
  return null
}
