import { useCallback, useEffect, useMemo, useState } from "react"
import { I18nContext } from "@/lib/i18n-context"
import { messages } from "@/lib/messages"

const STORAGE_KEY = "iam.locale"

function readStoredLocale() {
  const fromUrl = new URLSearchParams(window.location.search).get("lang")
  if (fromUrl === "km" || fromUrl === "en") return fromUrl
  try {
    const v = localStorage.getItem(STORAGE_KEY)
    return v === "km" || v === "en" ? v : "en"
  } catch {
    return "en"
  }
}

function lookup(dict, key) {
  return key.split(".").reduce((node, part) => (node == null ? node : node[part]), dict)
}

export function I18nProvider({ children }) {
  const [locale, setLocaleState] = useState(readStoredLocale)

  useEffect(() => {
    document.documentElement.lang = locale
  }, [locale])

  const setLocale = useCallback((next) => {
    setLocaleState(next)
    try {
      localStorage.setItem(STORAGE_KEY, next)
    } catch {
      return
    }
  }, [])

  const value = useMemo(() => {
    const km = locale === "km"
    const num = (n) => (km ? toKhmerDigits(n) : String(n))
    const t = (key, vars) => {
      let str = lookup(messages[locale], key) ?? lookup(messages.en, key) ?? key
      if (vars) for (const [k, v] of Object.entries(vars)) str = str.replaceAll(`{${k}}`, typeof v === "number" ? num(v) : v)
      return str
    }
    const fmt = km ? khmerFormatter(t) : englishFormatter(t)
    return { locale, setLocale, t, fmt }
  }, [locale, setLocale])

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}


const KM_DIGITS = "០១២៣៤៥៦៧៨៩"
const KM_MONTHS = ["មករា", "កុម្ភៈ", "មីនា", "មេសា", "ឧសភា", "មិថុនា", "កក្កដា", "សីហា", "កញ្ញា", "តុលា", "វិច្ឆិកា", "ធ្នូ"]
const toKhmerDigits = (v) => String(v).replace(/[0-9]/g, (d) => KM_DIGITS[d])
const pad = (n) => String(n).padStart(2, "0")

function relativeParts(d) {
  const diff = (Date.now() - new Date(d).getTime()) / 1000
  if (diff < 60) return null
  if (diff < 3600) return ["minute", Math.round(diff / 60)]
  if (diff < 86400) return ["hour", Math.round(diff / 3600)]
  if (diff < 86400 * 30) return ["day", Math.round(diff / 86400)]
  return ["date"]
}

function khmerFormatter(t) {
  const date = (d) => {
    const x = new Date(d)
    return toKhmerDigits(`${x.getDate()} `) + KM_MONTHS[x.getMonth()] + toKhmerDigits(` ${x.getFullYear()}`)
  }
  const time = (d) => {
    const x = new Date(d)
    return toKhmerDigits(`${pad(x.getHours())}:${pad(x.getMinutes())}`)
  }
  const units = { minute: "នាទីមុន", hour: "ម៉ោងមុន", day: "ថ្ងៃមុន" }
  return {
    date,
    time,
    dateTime: (d) => `${date(d)} ${time(d)}`,
    relative: (d) => {
      const r = relativeParts(d)
      if (!r) return t("common.justNow")
      if (r[0] === "date") return date(d)
      if (r[0] === "day" && r[1] === 1) return "ម្សិលមិញ"
      return `${toKhmerDigits(r[1])} ${units[r[0]]}`
    },
  }
}

function englishFormatter(t) {
  const dateTime = new Intl.DateTimeFormat("en-GB", { dateStyle: "medium", timeStyle: "short" })
  const dateOnly = new Intl.DateTimeFormat("en-GB", { dateStyle: "long" })
  const timeOnly = new Intl.DateTimeFormat("en-GB", { timeStyle: "short" })
  const rtf = new Intl.RelativeTimeFormat("en", { numeric: "auto" })
  return {
    dateTime: (d) => dateTime.format(new Date(d)),
    date: (d) => dateOnly.format(new Date(d)),
    time: (d) => timeOnly.format(new Date(d)),
    relative: (d) => {
      const r = relativeParts(d)
      if (!r) return t("common.justNow")
      if (r[0] === "date") return dateOnly.format(new Date(d))
      return rtf.format(-r[1], r[0])
    },
  }
}
