import { http } from "@/lib/api"

const toPage = (data) => ({
  items: data?.content ?? [],
  total: data?.page?.totalElements ?? 0,
  pages: Math.max(1, data?.page?.totalPages ?? 1),
})

function listQuery({ page = 1, size = 10, sort, ...filters }) {
  const q = new URLSearchParams({ page: String(page - 1), size: String(size) })
  if (sort) q.set("sort", sort)
  for (const [k, v] of Object.entries(filters)) {
    if (v !== undefined && v !== null && v !== "" && v !== "all") q.set(k, String(v))
  }
  return q
}

const clean = (body) => Object.fromEntries(Object.entries(body).filter(([, v]) => v !== "" && v !== undefined && v !== null))

/* users */
export const listUsers = async (params) => toPage(await http("GET", `/users?${listQuery({ sort: "username,asc", ...params })}`))
export const countUsers = async (filters = {}) => (await listUsers({ ...filters, size: 1 })).total
export const getUser = (uuid) => http("GET", `/users/${uuid}`)
export const getUserAuthorities = (uuid) => http("GET", `/users/${uuid}/authorities`)
export const createUser = (body) => http("POST", "/users", clean(body))
export const updateUser = (uuid, body) => http("PATCH", `/users/${uuid}`, clean(body))
export const deleteUser = (uuid) => http("DELETE", `/users/${uuid}`)
export const setUserStatus = (uuid, action) => http("POST", `/users/${uuid}/${action}`) // enable | disable | lock | unlock
export const resetUserPassword = (uuid, newPassword) => http("PUT", `/users/${uuid}/password`, { newPassword })
export const assignUserRoles = (uuid, body) => http("POST", `/users/${uuid}/roles`, clean(body))
export const revokeUserRole = (uuid, roleUuid, reason) =>
  http("DELETE", `/users/${uuid}/roles/${roleUuid}${reason ? `?reason=${encodeURIComponent(reason)}` : ""}`)
export const grantUserPermissions = (uuid, body) => http("POST", `/users/${uuid}/permissions`, clean(body))
export const revokeUserPermission = (uuid, permissionUuid, reason) =>
  http("DELETE", `/users/${uuid}/permissions/${permissionUuid}${reason ? `?reason=${encodeURIComponent(reason)}` : ""}`)
export const getUserIdentities = (uuid) => http("GET", `/users/${uuid}/identities`)

/* roles */
export const listRoles = async (params) => toPage(await http("GET", `/roles?${listQuery({ sort: "name,asc", ...params })}`))
export const countRoles = async () => (await listRoles({ size: 1 })).total
export const getRole = (uuid) => http("GET", `/roles/${uuid}`)
export const createRole = (body) => http("POST", "/roles", clean(body))
export const updateRole = (uuid, body) => http("PATCH", `/roles/${uuid}`, body)
export const deleteRole = (uuid) => http("DELETE", `/roles/${uuid}`)
export const setRoleActive = (uuid, active) => http("POST", `/roles/${uuid}/${active ? "activate" : "deactivate"}`)
export const assignRolePermissions = (uuid, body) => http("POST", `/roles/${uuid}/permissions`, clean(body))
export const revokeRolePermission = (uuid, permissionUuid) => http("DELETE", `/roles/${uuid}/permissions/${permissionUuid}`)

/* permissions */
export const listPermissions = async (params) =>
  toPage(await http("GET", `/permissions?${listQuery({ sort: "name,asc", ...params })}`))
export const countPermissions = async () => (await listPermissions({ size: 1 })).total
export async function allPermissions() {
  const first = await listPermissions({ size: 100 })
  const rest = []
  for (let page = 2; page <= Math.min(first.pages, 10); page++) rest.push(listPermissions({ size: 100, page }))
  return [first, ...(await Promise.all(rest))].flatMap((p) => p.items)
}
export async function allRoles() {
  const first = await listRoles({ size: 100 })
  const rest = []
  for (let page = 2; page <= Math.min(first.pages, 10); page++) rest.push(listRoles({ size: 100, page }))
  return [first, ...(await Promise.all(rest))].flatMap((p) => p.items)
}
export const getPermission = (uuid) => http("GET", `/permissions/${uuid}`)
export const createPermission = (body) => http("POST", "/permissions", clean(body))
export const updatePermission = (uuid, body) => http("PATCH", `/permissions/${uuid}`, body)
export const deletePermission = (uuid) => http("DELETE", `/permissions/${uuid}`)
export const setPermissionActive = (uuid, active) =>
  http("POST", `/permissions/${uuid}/${active ? "activate" : "deactivate"}`)
export const unlinkUserIdentity = (uuid, identityUuid) => http("DELETE", `/users/${uuid}/identities/${identityUuid}`)

/* user sessions & login history (admin) */
export const getUserSessions = async (uuid) => (await http("GET", `/users/${uuid}/sessions`)) ?? []
export const revokeUserSession = (uuid, sessionId) => http("DELETE", `/users/${uuid}/sessions/${sessionId}`)
export const revokeAllUserSessions = (uuid) => http("DELETE", `/users/${uuid}/sessions`)
export const getUserLoginHistory = async (uuid, { result = "all", days, page = 1, size = 10 } = {}) =>
  toPage(await http("GET", `/users/${uuid}/login-history?${listQuery({ page, size, result, days })}`))
export const listSuspiciousLogins = async ({ days = 1, threshold = 5 } = {}) =>
  (await http("GET", `/login-history/suspicious?days=${days}&threshold=${threshold}`)) ?? []

/* OAuth2 clients */
const clientPath = (clientId) => `/clients/${encodeURIComponent(clientId)}`
export const listClients = async (params) => toPage(await http("GET", `/clients?${listQuery(params)}`))
export const countClients = async () => (await listClients({ size: 1 })).total
export const getClient = (clientId) => http("GET", clientPath(clientId))
export const createClient = (body) => http("POST", "/clients", body)
export const updateClient = (clientId, body) => http("PUT", clientPath(clientId), body)
export const rotateClientSecret = (clientId) => http("POST", `${clientPath(clientId)}/secret`)
export const deleteClient = (clientId) => http("DELETE", clientPath(clientId))

/* audit logs */
export const listAuditLogs = async (params) => toPage(await http("GET", `/audit-logs?${listQuery(params)}`))

/* system settings */
export const getSettings = () => http("GET", "/settings")
export const updateSettings = (body) => http("PUT", "/settings", body)
