import {
  ActivityIcon,
  AtSignIcon,
  CircleUserRoundIcon,
  ImageIcon,
  KeyRoundIcon,
  LinkIcon,
  LogInIcon,
  LogOutIcon,
  MailQuestionIcon,
  PowerIcon,
  UndoIcon,
  UnlinkIcon,
  UserPlusIcon,
} from "lucide-react"
import { CardSkeleton, EmptyState, ErrorState, PageIntro } from "@/components/page-states"
import { PROVIDERS } from "@/lib/providers"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getActivity } from "@/lib/api"
import { useI18n } from "@/lib/i18n-context"
import { useAsync } from "@/lib/use-async"
import { cn } from "@/lib/utils"

const EVENTS = {
  login_new_device: { icon: LogInIcon },
  profile_updated: { icon: CircleUserRoundIcon },
  avatar_updated: { icon: ImageIcon },
  email_changed: { icon: AtSignIcon, sensitive: true },
  password_changed: { icon: KeyRoundIcon, sensitive: true },
  password_reset_requested: { icon: MailQuestionIcon, sensitive: true },
  provider_linked: { icon: LinkIcon },
  provider_unlinked: { icon: UnlinkIcon, sensitive: true },
  session_terminated: { icon: LogOutIcon },
  sessions_terminated_all: { icon: LogOutIcon, sensitive: true },
  deactivation_requested: { icon: PowerIcon, sensitive: true },
  deactivation_cancelled: { icon: UndoIcon },
  account_created: { icon: UserPlusIcon },
}

export default function ActivityPage() {
  const { t, fmt } = useI18n()
  const activity = useAsync(getActivity)

  const detail = (e) => {
    if (!e.detail) return null
    switch (e.type) {
      case "provider_linked":
      case "provider_unlinked":
        return PROVIDERS[e.detail]?.name ?? e.detail
      case "profile_updated":
        return t("activity.fields", { fields: e.detail.split(", ").map((f) => t(`profile.${f}`)).join(", ") })
      case "sessions_terminated_all":
        return t("activity.devicesCount", { n: e.detail })
      default:
        return e.detail
    }
  }

  const body = () => {
    if (activity.status === "loading") return <CardSkeleton rows={6} />
    if (activity.status === "error") return <ErrorState onRetry={activity.reload} />
    const items = activity.data
    if (!items.length) {
      return (
        <Card>
          <CardContent>
            <EmptyState icon={ActivityIcon} title={t("activity.emptyTitle")} description={t("activity.emptyBody")} />
          </CardContent>
        </Card>
      )
    }

    const groups = []
    for (const e of items) {
      const label = fmt.date(e.at)
      const last = groups.at(-1)
      if (last?.label === label) last.items.push(e)
      else groups.push({ label, items: [e] })
    }

    return (
      <Card>
        <CardHeader>
          <CardTitle>{t("activity.title")}</CardTitle>
          <CardDescription>{t("activity.hint")}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          {groups.map((g) => (
            <section key={g.label} className="flex flex-col gap-3">
              <h2 className="text-sm font-medium text-muted-foreground">{g.label}</h2>
              <ol className="relative flex flex-col">
                {g.items.map((e, i) => {
                  const meta = EVENTS[e.type] ?? { icon: ActivityIcon }
                  const Icon = meta.icon
                  const d = detail(e)
                  const isLast = i === g.items.length - 1
                  return (
                    <li key={e.id} className="relative flex gap-4 pb-5 last:pb-0">
                      {!isLast && <span aria-hidden="true" className="absolute top-9 bottom-0 left-[17px] w-px bg-border" />}
                      <span
                        className={cn(
                          "relative flex size-9 shrink-0 items-center justify-center rounded-full border bg-card",
                          meta.sensitive ? "border-warning/30 bg-warning-soft text-warning" : "text-muted-foreground"
                        )}
                      >
                        <Icon className="size-4" />
                      </span>
                      <div className="flex min-w-0 flex-1 flex-col gap-0.5 pt-1.5 sm:flex-row sm:items-baseline sm:justify-between sm:gap-4">
                        <div className="flex min-w-0 flex-col gap-0.5">
                          <span className="text-sm font-medium">{t(`activity.events.${e.type}`)}</span>
                          {d && <span className="truncate text-sm text-muted-foreground">{d}</span>}
                        </div>
                        <time dateTime={e.at} title={fmt.dateTime(e.at)} className="tabular shrink-0 text-xs text-muted-foreground">
                          {fmt.time(e.at)}
                        </time>
                      </div>
                    </li>
                  )
                })}
              </ol>
            </section>
          ))}
        </CardContent>
      </Card>
    )
  }

  return (
    <>
      <PageIntro>{t("activity.intro")}</PageIntro>
      {body()}
    </>
  )
}
