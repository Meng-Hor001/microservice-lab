import { Fragment, useState } from "react"
import { LinkIcon, PlugZapIcon, UnlinkIcon } from "lucide-react"
import { toast } from "sonner"
import { ConfirmDialog } from "@/components/confirm-dialog"
import { CardSkeleton, EmptyState, ErrorState, PageIntro } from "@/components/page-states"
import { ProviderLogo } from "@/components/provider-logo"
import { PROVIDERS } from "@/lib/providers"
import { StatusDot } from "@/components/management/states"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Spinner } from "@/components/ui/spinner"
import { getProviders, linkProvider, unlinkProvider } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"

export default function LinkedAccountsPage() {
  const { t, fmt } = useI18n()
  const providers = useAsync(getProviders)
  const [linking, setLinking] = useState(null)
  const [unlinkTarget, setUnlinkTarget] = useState(null)

  const link = async (id) => {
    setLinking(id)
    try {
      await linkProvider(id)
    } catch {
      toast.error(t("states.actionFailed"))
    } finally {
      setLinking(null)
    }
  }

  const body = () => {
    if (providers.status === "loading") return <CardSkeleton rows={5} />
    if (providers.status === "error") return <ErrorState onRetry={providers.reload} />
    const list = providers.data
    const linkedCount = list.filter((p) => p.linked).length
    return (
      <Card>
        <CardHeader>
          <CardTitle>{t("linked.title")}</CardTitle>
          <CardDescription>
            {list.length ? t("linked.count", { n: linkedCount, total: list.length }) : t("linked.hint")}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {list.length === 0 ? (
            <EmptyState icon={PlugZapIcon} title={t("linked.emptyTitle")} description={t("linked.emptyBody")} />
          ) : (
            <ul className="flex flex-col">
              {list.map((p, i) => (
                <Fragment key={p.id}>
                  {i > 0 && <Separator className="my-4" />}
                  <li className="flex flex-col gap-3 sm:flex-row sm:items-center">
                    <div className="flex min-w-0 flex-1 items-center gap-3">
                      <ProviderLogo id={p.id} />
                      <div className="flex min-w-0 flex-col gap-0.5">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="font-medium">{PROVIDERS[p.id]?.name ?? p.id}</span>
                          {p.linked ? (
                            <StatusDot on label={t("linked.connected")} />
                          ) : (
                            <StatusDot label={t("linked.notConnected")} />
                          )}
                        </div>
                        <span className="truncate text-sm text-muted-foreground">
                          {p.linked
                            ? p.linkedAt
                              ? t("linked.identity", { account: p.account, date: fmt.date(p.linkedAt) })
                              : p.account
                            : t("linked.signInWith", { provider: PROVIDERS[p.id]?.name })}
                        </span>
                      </div>
                    </div>
                    {p.linked ? (
                      <Button variant="outline" className="self-start sm:self-auto" onClick={() => setUnlinkTarget(p)}>
                        <UnlinkIcon data-icon="inline-start" />
                        {t("linked.unlink")}
                      </Button>
                    ) : (
                      <Button className="self-start sm:self-auto" onClick={() => link(p.id)} disabled={!!linking}>
                        {linking === p.id ? <Spinner data-icon="inline-start" /> : <LinkIcon data-icon="inline-start" />}
                        {t("linked.link")}
                      </Button>
                    )}
                  </li>
                </Fragment>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    )
  }

  return (
    <>
      <PageIntro>{t("linked.intro")}</PageIntro>
      {body()}
      <ConfirmDialog
        open={!!unlinkTarget}
        onOpenChange={(o) => !o && setUnlinkTarget(null)}
        icon={UnlinkIcon}
        destructive
        title={t("linked.unlinkTitle", { provider: PROVIDERS[unlinkTarget?.id]?.name ?? "" })}
        description={t("linked.unlinkBody", { account: unlinkTarget?.account ?? "", provider: PROVIDERS[unlinkTarget?.id]?.name ?? "" })}
        confirmLabel={t("linked.unlink")}
        onConfirm={async () => {
          await unlinkProvider(unlinkTarget)
          toast.success(t("linked.unlinkedToast", { provider: PROVIDERS[unlinkTarget.id].name }))
          providers.reload()
        }}
      />
    </>
  )
}
