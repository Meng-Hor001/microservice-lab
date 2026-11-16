export const PASSWORD_RULES = [
  { key: "length", test: (p) => p.length >= 8 },
  { key: "case", test: (p) => /[a-z]/.test(p) && /[A-Z]/.test(p) },
  { key: "number", test: (p) => /\d/.test(p) },
  { key: "symbol", test: (p) => /[^A-Za-z0-9]/.test(p) },
]
