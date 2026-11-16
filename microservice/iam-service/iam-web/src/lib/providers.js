export const PROVIDERS = {
  google: { name: "Google" },
  github: { name: "GitHub" },
  facebook: { name: "Facebook" },
  telegram: { name: "Telegram" },
  microsoft: { name: "Microsoft" },
  apple: { name: "Apple" },
}

export function initials(name = "") {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("")
}
