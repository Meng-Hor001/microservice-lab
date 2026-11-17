import { useEffect, useMemo, useState } from "react"
import { BrowserRouter, Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom"
import { LogInIcon, ShieldCheckIcon } from "lucide-react"
import { NavRail } from "@/components/nav-rail"
import { SiteHeader } from "@/components/site-header"
import { TopbarSlotContext } from "@/lib/topbar"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Toaster } from "@/components/ui/sonner"
import { Spinner } from "@/components/ui/spinner"
import { TooltipProvider } from "@/components/ui/tooltip"
import { getProfile } from "@/lib/api"
import { AccountContext } from "@/lib/account"
import { completeLogin, getAccessToken, login, logout } from "@/lib/auth"
import { I18nProvider } from "@/lib/i18n"
import { useI18n } from "@/lib/i18n-context"
import { NAV } from "@/lib/nav"
import { cn } from "@/lib/utils"
import OverviewPage from "@/pages/overview"
import ProfilePage from "@/pages/profile"
import SecurityPage from "@/pages/security"
import LinkedAccountsPage from "@/pages/linked-accounts"
import SessionsPage from "@/pages/sessions"
import LoginHistoryPage from "@/pages/login-history"
import ActivityPage from "@/pages/activity"
import DeactivatePage from "@/pages/deactivate"
import ResetPasswordPage from "@/pages/reset-password"
import { MgmtShell } from "@/components/management/mgmt-shell"
import { AccessDenied } from "@/components/management/states"
import { homePath, useAccess } from "@/lib/access"
import ManagementOverview from "@/pages/management/overview"
import UsersPage from "@/pages/management/users"
import UserDetailPage from "@/pages/management/user-detail"
import RolesPage from "@/pages/management/roles"
import RoleDetailPage from "@/pages/management/role-detail"
import PermissionsPage from "@/pages/management/permissions"
import ClientsPage from "@/pages/management/clients"
import AuditLogsPage from "@/pages/management/audit-logs"
import SettingsPage from "@/pages/management/settings"

function RequireAdmin({ children }) {
  const { isAdmin } = useAccess()
  return isAdmin ? children : <AccessDenied />
}

function RequirePermission({ permission, children }) {
  const { can } = useAccess()
  return can(permission) ? children : <AccessDenied />
}

function Shell({ onSignOut }) {
  const { t } = useI18n()
  const { pathname } = useLocation()
  const current = NAV.find((n) => n.path === pathname)
  const title = current ? t(`nav.${current.key}`) : t("nav.group")
  const [tabSlot, setTabSlot] = useState(null)

  useEffect(() => {
    document.title = `${title} | ${t("app.name")}`
  }, [title, t])

  return (
    <TopbarSlotContext.Provider value={tabSlot}>
    <div className="flex min-h-svh flex-col bg-background">
      <SiteHeader onSignOut={onSignOut} slotRef={setTabSlot} />
      <NavRail />
      <main className="flex flex-1 flex-col px-4 pt-4 pb-28 md:pr-6 md:pb-12 md:pl-28">
        <div
          key={pathname}
          className={cn("mx-auto flex w-full flex-col gap-6", !current?.wide && "motion-rise", current?.narrow ? "max-w-5xl" : "max-w-none")}
        >
          {!current?.wide && <h1 className="text-3xl leading-tight font-medium tracking-tight">{title}</h1>}
          <Routes>
            <Route path="/" element={<Navigate to={homePath()} replace />} />
            <Route path="/overview" element={<OverviewPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/security" element={<SecurityPage />} />
            <Route path="/linked-accounts" element={<LinkedAccountsPage />} />
            <Route path="/sessions" element={<SessionsPage />} />
            <Route path="/login-history" element={<LoginHistoryPage />} />
            <Route path="/activity" element={<ActivityPage />} />
            <Route path="/deactivate" element={<DeactivatePage />} />
            <Route path="*" element={<Navigate to={homePath()} replace />} />
          </Routes>
        </div>
      </main>
    </div>
    </TopbarSlotContext.Provider>
  )
}

function SignedOut({ error }) {
  const { t } = useI18n()

  // Like Keycloak: go straight to the IAM server's login form. Stay here only if sign-in just failed,
  // so a broken login can't loop.
  useEffect(() => {
    if (!error) login()
  }, [error])

  if (!error) {
    return (
      <div className="flex min-h-svh items-center justify-center gap-3 text-sm text-muted-foreground">
        <Spinner />
        {t("signedOut.redirecting")}
      </div>
    )
  }

  return (
    <div className="flex min-h-svh flex-col items-center justify-center gap-4 px-4 text-center">
      <span className="flex size-12 items-center justify-center rounded-md bg-primary text-primary-foreground">
        <ShieldCheckIcon />
      </span>
      <div className="flex flex-col gap-1">
        <h1 className="text-xl font-medium">{t("signedOut.title")}</h1>
        <p className="text-sm text-muted-foreground">{t("signedOut.body")}</p>
      </div>
      {error && (
        <Alert variant="destructive" className="max-w-sm text-left">
          <AlertDescription>{t("signedOut.failed")}</AlertDescription>
        </Alert>
      )}
      <Button onClick={login}>
        <LogInIcon data-icon="inline-start" />
        {t("signedOut.signIn")}
      </Button>
    </div>
  )
}

function Callback({ onSignedIn }) {
  const { t } = useI18n()
  const navigate = useNavigate()
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    completeLogin(window.location.search).then(
      () => {
        onSignedIn()
        navigate(homePath(), { replace: true })
      },
      () => setFailed(true)
    )
  }, [navigate, onSignedIn])

  if (failed) return <SignedOut error />
  return (
    <div className="flex min-h-svh items-center justify-center gap-3 text-sm text-muted-foreground">
      <Spinner />
      {t("signedOut.signingIn")}
    </div>
  )
}

export default function App() {
  const [user, setUser] = useState(null)
  const [signedIn, setSignedIn] = useState(() => !!getAccessToken())

  useEffect(() => {
    const onUnauthorized = () => {
      setUser(null)
      setSignedIn(false)
    }
    window.addEventListener("iam:unauthorized", onUnauthorized)
    return () => window.removeEventListener("iam:unauthorized", onUnauthorized)
  }, [])

  useEffect(() => {
    if (signedIn) getProfile().then(setUser, () => {})
  }, [signedIn])

  const account = useMemo(() => ({ user, setUser }), [user])
  const markSignedIn = useMemo(() => () => setSignedIn(true), [])

  const signOut = () => logout()

  return (
    <I18nProvider>
      <TooltipProvider>
        <AccountContext.Provider value={account}>
          <BrowserRouter basename={import.meta.env.BASE_URL}>
            <Routes>
              <Route path="/callback" element={<Callback onSignedIn={markSignedIn} />} />
              <Route path="/reset-password" element={<ResetPasswordPage />} />
              {signedIn && (
                <Route
                  path="/management"
                  element={
                    <RequireAdmin>
                      <MgmtShell onSignOut={signOut} />
                    </RequireAdmin>
                  }
                >
                  <Route index element={<ManagementOverview />} />
                  <Route path="users" element={<RequirePermission permission="user:read"><UsersPage /></RequirePermission>} />
                  <Route path="users/:id" element={<RequirePermission permission="user:read"><UserDetailPage /></RequirePermission>} />
                  <Route path="roles" element={<RequirePermission permission="role:read"><RolesPage /></RequirePermission>} />
                  <Route path="roles/:id" element={<RequirePermission permission="role:read"><RoleDetailPage /></RequirePermission>} />
                  <Route
                    path="permissions"
                    element={<RequirePermission permission="permission:read"><PermissionsPage /></RequirePermission>}
                  />
                  <Route path="clients" element={<RequirePermission permission="client:read"><ClientsPage /></RequirePermission>} />
                  <Route path="audit-logs" element={<RequirePermission permission="audit:read"><AuditLogsPage /></RequirePermission>} />
                  <Route path="settings" element={<RequirePermission permission="settings:read"><SettingsPage /></RequirePermission>} />
                  <Route path="*" element={<Navigate to="/management" replace />} />
                </Route>
              )}
              <Route path="*" element={signedIn ? <Shell onSignOut={signOut} /> : <SignedOut />} />
            </Routes>
          </BrowserRouter>
          <Toaster position="top-center" />
        </AccountContext.Provider>
      </TooltipProvider>
    </I18nProvider>
  )
}
