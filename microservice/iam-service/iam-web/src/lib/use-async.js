import { useCallback, useEffect, useRef, useState } from "react"

export function useAsync(loader, deps = []) {
  const loaderRef = useRef(loader)
  const [nonce, setNonce] = useState(0)
  const key = JSON.stringify([...deps, nonce])
  const [state, setState] = useState({ key: null, ok: false, data: null, error: null })

  useEffect(() => {
    loaderRef.current = loader
  })

  useEffect(() => {
    let alive = true
    loaderRef.current().then(
      (data) => alive && setState({ key, ok: true, data, error: null }),
      (error) => alive && setState((s) => ({ key, ok: false, data: s.data, error }))
    )
    return () => {
      alive = false
    }
  }, [key])

  const pending = state.key !== key
  const status = pending ? (state.data ? "refreshing" : "loading") : state.ok ? "success" : "error"

  const reload = useCallback(() => setNonce((n) => n + 1), [])
  const setData = useCallback((data) => setState((s) => ({ ...s, data })), [])
  return { status, data: state.data, error: state.error, reload, setData }
}
