export const IAM_URL = import.meta.env.VITE_IAM_URL ?? "http://localhost:9999"
const CLIENT_ID = import.meta.env.VITE_IAM_CLIENT_ID ?? "iam-web"
const TOKEN_URL = import.meta.env.VITE_IAM_TOKEN_URL ?? "/oauth2/token"
const SCOPES = "openid profile email phone"
const TOKEN_KEY = "iam.token"
const PKCE_KEY = "iam.pkce"
const ID_TOKEN_KEY = "iam.idToken"

const redirectUri = () => `${window.location.origin}${import.meta.env.BASE_URL}callback`

const base64Url = (buffer) =>
  btoa(String.fromCharCode(...new Uint8Array(buffer)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "")

const randomString = (size) => base64Url(crypto.getRandomValues(new Uint8Array(size)))

function save(key, value) {
  try {
    if (value == null) sessionStorage.removeItem(key)
    else sessionStorage.setItem(key, JSON.stringify(value))
  } catch {
    return
  }
}

function read(key) {
  try {
    return JSON.parse(sessionStorage.getItem(key))
  } catch {
    return null
  }
}

export function getAccessToken() {
  const token = read(TOKEN_KEY)
  if (!token || Date.now() >= token.expiresAt) return null
  return token.accessToken
}

export function clearSession() {
  save(TOKEN_KEY, null)
}

// Ends the session on the IAM server too (OIDC RP-initiated logout), so the next visit shows the
// login form again instead of silently signing back in. The server sends the browser back to the app.
export function logout() {
  const idToken = read(ID_TOKEN_KEY)
  save(TOKEN_KEY, null)
  save(ID_TOKEN_KEY, null)
  if (!idToken) {
    window.location.assign(`${IAM_URL}/login`)
    return
  }
  const query = new URLSearchParams({
    id_token_hint: idToken,
    client_id: CLIENT_ID,
    post_logout_redirect_uri: `${window.location.origin}${import.meta.env.BASE_URL}`,
  })
  window.location.assign(`${IAM_URL}/connect/logout?${query}`)
}

export async function login() {
  const verifier = randomString(48)
  const state = randomString(16)
  const challenge = base64Url(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier)))
  save(PKCE_KEY, { verifier, state })
  const query = new URLSearchParams({
    response_type: "code",
    client_id: CLIENT_ID,
    redirect_uri: redirectUri(),
    scope: SCOPES,
    state,
    code_challenge: challenge,
    code_challenge_method: "S256",
  })
  window.location.assign(`${IAM_URL}/oauth2/authorize?${query}`)
}

async function exchangeCode(search) {
  const params = new URLSearchParams(search)
  const saved = read(PKCE_KEY)
  save(PKCE_KEY, null)
  if (params.get("error")) throw new Error(params.get("error"))
  if (!saved || !params.get("code") || params.get("state") !== saved.state) throw new Error("invalid_state")

  const response = await fetch(TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      code: params.get("code"),
      redirect_uri: redirectUri(),
      client_id: CLIENT_ID,
      code_verifier: saved.verifier,
    }),
  })
  if (!response.ok) throw new Error("token_exchange_failed")
  const token = await response.json()
  save(ID_TOKEN_KEY, token.id_token ?? null)
  save(TOKEN_KEY, {
    accessToken: token.access_token,
    expiresAt: Date.now() + Math.max(0, token.expires_in - 30) * 1000,
  })
}

let pendingExchange = null
export function completeLogin(search) {
  pendingExchange ??= exchangeCode(search)
  return pendingExchange
}

// Reads the roles and permissions the IAM server puts into the access token.
// This only decides what the UI shows; the API still checks every request.
export function getClaims() {
  const token = getAccessToken()
  if (!token) return { roles: [], permissions: [] }
  try {
    const part = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")
    const json = decodeURIComponent(
      atob(part.padEnd(part.length + ((4 - (part.length % 4)) % 4), "="))
        .split("")
        .map((c) => `%${c.charCodeAt(0).toString(16).padStart(2, "0")}`)
        .join("")
    )
    const claims = JSON.parse(json)
    return { roles: claims.roles ?? [], permissions: claims.permissions ?? [] }
  } catch {
    return { roles: [], permissions: [] }
  }
}
