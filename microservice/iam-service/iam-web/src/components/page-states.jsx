import { CloudOffIcon, RotateCwIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from "@/components/ui/empty"
import { Skeleton } from "@/components/ui/skeleton"
import { useI18n } from "@/lib/i18n-context"

export function CardSkeleton({ rows = 3 }) {
  return (
    <Card aria-busy="true">
      <CardHeader className="flex flex-col gap-2">
        <Skeleton className="h-5 w-40" />
        <Skeleton className="h-4 w-72 max-w-full" />
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {Array.from({ length: rows }, (_, i) => (
          <div key={i} className="flex items-center gap-3">
            <Skeleton className="size-9 rounded-full" />
            <div className="flex flex-1 flex-col gap-2">
              <Skeleton className="h-4 w-1/3" />
              <Skeleton className="h-3 w-1/2" />
            </div>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}

export function ErrorState({ onRetry }) {
  const { t } = useI18n()
  return (
    <Card>
      <CardContent>
        <Empty>
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <CloudOffIcon />
            </EmptyMedia>
            <EmptyTitle>{t("states.errorTitle")}</EmptyTitle>
            <EmptyDescription>{t("states.errorBody")}</EmptyDescription>
          </EmptyHeader>
          {onRetry && (
            <EmptyContent>
              <Button variant="outline" onClick={onRetry}>
                <RotateCwIcon data-icon="inline-start" />
                {t("common.retry")}
              </Button>
            </EmptyContent>
          )}
        </Empty>
      </CardContent>
    </Card>
  )
}

export function EmptyState({ icon: Icon, title, description, children }) {
  return (
    <Empty>
      <EmptyHeader>
        {Icon && (
          <EmptyMedia variant="icon">
            <Icon />
          </EmptyMedia>
        )}
        <EmptyTitle>{title}</EmptyTitle>
        {description && <EmptyDescription>{description}</EmptyDescription>}
      </EmptyHeader>
      {children && <EmptyContent>{children}</EmptyContent>}
    </Empty>
  )
}

export function PageIntro({ children }) {
  return <p className="max-w-[62ch] text-sm text-muted-foreground">{children}</p>
}
