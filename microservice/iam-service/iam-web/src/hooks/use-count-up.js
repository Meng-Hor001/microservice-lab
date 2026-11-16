import { useEffect, useRef, useState } from "react"

const reducedMotion = () =>
  typeof window !== "undefined" && window.matchMedia?.("(prefers-reduced-motion: reduce)").matches

const easeOut = (x) => 1 - (1 - x) ** 3

// Tweens a number from its previous value to `target`. Non-numbers pass straight through.
export function useCountUp(target, duration = 700) {
  const [value, setValue] = useState(0)
  const from = useRef(0)

  useEffect(() => {
    if (typeof target !== "number") return
    const start = performance.now()
    const begin = from.current
    const instant = reducedMotion()
    let frame = requestAnimationFrame(function step(now) {
      const p = instant ? 1 : Math.min(1, (now - start) / duration)
      const next = begin + (target - begin) * easeOut(p)
      setValue(next)
      from.current = next
      if (p < 1) frame = requestAnimationFrame(step)
    })
    return () => cancelAnimationFrame(frame)
  }, [target, duration])

  return typeof target === "number" ? value : target
}

export function CountUp({ value, decimals = 0, suffix = "" }) {
  const n = useCountUp(value)
  if (typeof n !== "number") return n
  return `${n.toFixed(decimals)}${suffix}`
}
