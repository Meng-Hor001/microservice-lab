import { useRef, useState } from "react"
import { ImageUpIcon, Trash2Icon } from "lucide-react"
import { toast } from "sonner"
import { CardSkeleton, ErrorState, PageIntro } from "@/components/page-states"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Field, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { deleteAvatar, getProfile, updateProfile, uploadAvatar } from "@/lib/api"
import { useAccount } from "@/lib/account"
import { useI18n } from "@/lib/i18n-context"
import { initials } from "@/lib/providers"
import { useAsync } from "@/lib/use-async"

const FIELDS = ["name", "username", "email", "phone"]
const MAX_AVATAR_BYTES = 2 * 1024 * 1024

function validate(v) {
  const e = {}
  if (!v.name.trim()) e.name = "required"
  else if (v.name.trim().length > 80) e.name = "nameTooLong"
  if (!v.username.trim()) e.username = "required"
  else if (!/^[a-z0-9._-]{3,50}$/i.test(v.username.trim())) e.username = "usernameFormat"
  if (!v.email.trim()) e.email = "required"
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.email)) e.email = "emailFormat"
  if (v.phone.trim() && !/^\+[1-9]\d{1,14}$/.test(v.phone.replace(/[\s-]/g, ""))) e.phone = "phoneFormat"
  return e
}

export default function ProfilePage() {
  const { t } = useI18n()
  const profile = useAsync(getProfile)

  if (profile.status === "loading") {
    return (
      <>
        <PageIntro>{t("profile.intro")}</PageIntro>
        <CardSkeleton rows={1} />
        <CardSkeleton rows={4} />
      </>
    )
  }
  if (profile.status === "error" && !profile.data) return <ErrorState onRetry={profile.reload} />
  return <ProfileForm saved={profile.data} onSaved={profile.setData} />
}

function ProfileForm({ saved, onSaved }) {
  const { t, fmt } = useI18n()
  const { setUser } = useAccount()
  const [values, setValues] = useState(saved)
  const [errors, setErrors] = useState({})
  const [submitted, setSubmitted] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState(false)
  const fileRef = useRef(null)
  // The picked photo waits here until Save; values.avatarUrl only holds a local preview of it.
  const [avatarFile, setAvatarFile] = useState(null)

  const dirty = [...FIELDS, "avatarUrl"].some((k) => values[k] !== saved[k])

  const set = (key) => (ev) => {
    const next = { ...values, [key]: ev.target.value }
    setValues(next)
    setSaveError(false)
    if (submitted) setErrors(validate(next))
  }

  const pickAvatar = (ev) => {
    const file = ev.target.files?.[0]
    ev.target.value = ""
    if (!file) return
    if (!/^image\/(png|jpe?g|webp)$/.test(file.type)) {
      setErrors((e) => ({ ...e, avatar: "avatarType" }))
      return
    }
    if (file.size > MAX_AVATAR_BYTES) {
      setErrors((e) => ({ ...e, avatar: "avatarSize" }))
      return
    }
    setAvatarFile(file)
    setValues((v) => ({ ...v, avatarUrl: URL.createObjectURL(file) }))
    setErrors((e) => ({ ...e, avatar: undefined }))
  }

  const removeAvatar = () => {
    setAvatarFile(null)
    setValues((v) => ({ ...v, avatarUrl: null }))
  }

  const cancel = () => {
    setAvatarFile(null)
    setValues(saved)
    setErrors({})
    setSubmitted(false)
    setSaveError(false)
  }

  const submit = async (ev) => {
    ev.preventDefault()
    setSubmitted(true)
    const found = validate(values)
    setErrors(found)
    if (Object.keys(found).length) {
      document.getElementById(`profile-${Object.keys(found)[0]}`)?.focus()
      return
    }
    setSaving(true)
    setSaveError(false)
    try {
      // Upload the new photo first, then save its URL with the rest of the profile.
      const avatarUrl = avatarFile ? await uploadAvatar(avatarFile) : values.avatarUrl
      const next = await updateProfile({ ...values, avatarUrl })
      // Removing an uploaded photo also deletes the stored image.
      if (!avatarUrl && saved.avatarUrl?.includes("/api/avatars/")) await deleteAvatar().catch(() => {})
      setAvatarFile(null)
      onSaved(next)
      setValues(next)
      setUser(next)
      setSubmitted(false)
      toast.success(t("profile.saved"))
    } catch (err) {
      if (err.field) setErrors({ [err.field]: err.code })
      else setSaveError(true) // keep everything the person typed
    } finally {
      setSaving(false)
    }
  }

  const err = (key) => errors[key] && <FieldError>{t(`validation.${errors[key]}`)}</FieldError>

  return (
    <form onSubmit={submit} noValidate className="flex flex-col gap-6">
      <PageIntro>{t("profile.intro")}</PageIntro>

      <Card>
        <CardHeader>
          <CardTitle>{t("profile.detailsTitle")}</CardTitle>
          <CardDescription>{t("profile.detailsHint")}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-5">
          <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center">
            <Avatar className="size-20 text-xl">
              {values.avatarUrl && <AvatarImage src={values.avatarUrl} alt={t("profile.photoAlt")} className="object-cover" />}
              <AvatarFallback className="bg-accent text-accent-foreground">{initials(values.name)}</AvatarFallback>
            </Avatar>
            <div className="flex flex-col gap-2">
              <span className="font-medium">{t("profile.photoTitle")}</span>
              <div className="flex flex-wrap gap-2">
                <input ref={fileRef} type="file" accept="image/png,image/jpeg,image/webp" className="sr-only" onChange={pickAvatar} tabIndex={-1} aria-hidden="true" />
                <Button type="button" variant="outline" onClick={() => fileRef.current?.click()}>
                  <ImageUpIcon data-icon="inline-start" />
                  {t("profile.upload")}
                </Button>
                {values.avatarUrl && (
                  <Button type="button" variant="ghost" onClick={removeAvatar}>
                    <Trash2Icon data-icon="inline-start" />
                    {t("profile.removePhoto")}
                  </Button>
                )}
              </div>
              {errors.avatar ? (
                <p role="alert" className="text-sm text-destructive">{t(`validation.${errors.avatar}`)}</p>
              ) : (
                values.avatarUrl !== saved.avatarUrl && <p className="text-sm text-muted-foreground">{t("profile.photoPending")}</p>
              )}
            </div>
          </div>
          <dl className="grid gap-x-10 gap-y-4 text-sm sm:grid-cols-3 lg:text-right">
            <div className="flex flex-col gap-1.5">
              <dt className="text-muted-foreground">{t("profile.roles")}</dt>
              <dd className="flex flex-wrap gap-1.5 lg:justify-end">
                {saved.roles.length ? (
                  saved.roles.map((r) => (
                    <Badge key={r} variant="secondary" className="font-mono">
                      {r}
                    </Badge>
                  ))
                ) : (
                  <span className="text-muted-foreground">{t("profile.noRoles")}</span>
                )}
              </dd>
            </div>
            <div className="flex flex-col gap-1.5">
              <dt className="text-muted-foreground">{t("profile.createdAt")}</dt>
              <dd className="font-medium">{saved.createdAt ? fmt.date(saved.createdAt) : "–"}</dd>
            </div>
            <div className="flex flex-col gap-1.5">
              <dt className="text-muted-foreground">{t("profile.updatedAt")}</dt>
              <dd className="font-medium" title={saved.updatedAt ? fmt.dateTime(saved.updatedAt) : undefined}>
                {saved.updatedAt ? fmt.relative(saved.updatedAt) : "–"}
              </dd>
            </div>
          </dl>
          </div>
          <Separator />
          {saveError && (
            <Alert variant="destructive">
              <AlertTitle>{t("profile.saveFailedTitle")}</AlertTitle>
              <AlertDescription>{t("profile.saveFailedBody")}</AlertDescription>
            </Alert>
          )}
          <FieldGroup className="grid gap-5 md:grid-cols-2 2xl:grid-cols-4">
            <Field data-invalid={!!errors.name || undefined}>
              <FieldLabel htmlFor="profile-name">{t("profile.name")}</FieldLabel>
              <Input id="profile-name" autoComplete="name" value={values.name} onChange={set("name")} aria-invalid={!!errors.name} />
              {err("name")}
            </Field>
            <Field data-invalid={!!errors.username || undefined}>
              <FieldLabel htmlFor="profile-username">{t("profile.username")}</FieldLabel>
              <Input id="profile-username" autoComplete="username" value={values.username} onChange={set("username")} aria-invalid={!!errors.username} />
              {err("username")}
            </Field>
            <Field data-invalid={!!errors.email || undefined}>
              <FieldLabel htmlFor="profile-email">{t("profile.email")}</FieldLabel>
              <Input id="profile-email" type="email" autoComplete="email" value={values.email} onChange={set("email")} aria-invalid={!!errors.email} />
              {err("email")}
            </Field>
            <Field data-invalid={!!errors.phone || undefined}>
              <FieldLabel htmlFor="profile-phone">
                {t("profile.phone")} <span className="font-normal text-muted-foreground">{t("common.optional")}</span>
              </FieldLabel>
              <Input id="profile-phone" type="tel" autoComplete="tel" placeholder="+855 12 345 678" value={values.phone} onChange={set("phone")} aria-invalid={!!errors.phone} />
              {err("phone")}
            </Field>
          </FieldGroup>
        </CardContent>
        <CardFooter className="justify-end gap-2 border-t">
          <Button type="button" variant="outline" onClick={cancel} disabled={!dirty || saving}>
            {t("common.cancel")}
          </Button>
          <Button type="submit" disabled={!dirty || saving}>
            {saving && <Spinner data-icon="inline-start" />}
            {t("profile.save")}
          </Button>
        </CardFooter>
      </Card>
    </form>
  )
}
