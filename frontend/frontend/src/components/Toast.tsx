import { useState, useCallback, useEffect } from 'react'

type ToastType = 'success' | 'error' | 'warning' | 'info'

interface Toast {
  id: number
  type: ToastType
  message: string
}

let toastId = 0
const listeners: Set<() => void> = new Set()
const toasts: Toast[] = []

function notify() { listeners.forEach(l => l()) }

export function useToast() {
  const [, setTick] = useState(0)

  useEffect(() => {
    listeners.add(forceUpdate)
    return () => { listeners.delete(forceUpdate) }
  }, [])

  function forceUpdate() { setTick(t => t + 1) }

  const show = useCallback((type: ToastType, message: string) => {
    const id = ++toastId
    toasts.push({ id, type, message })
    notify()
    setTimeout(() => dismiss(id), 4500)
    return id
  }, [])

  const dismiss = useCallback((id: number) => {
    const idx = toasts.findIndex(t => t.id === id)
    if (idx >= 0) {
      toasts.splice(idx, 1)
      notify()
    }
  }, [])

  return { toasts, show, dismiss }
}

export function ToastContainer() {
  const { toasts, dismiss } = useToast()

  return (
    <div className="toast-container" aria-live="polite" aria-atomic="true">
      {toasts.map(t => (
        <div key={t.id} className={`toast toast-${t.type}`} role="alert">
          <span className="flex-1">{t.message}</span>
          <button
            onClick={() => dismiss(t.id)}
            className="p-1 text-current opacity-50 hover:opacity-100 transition-opacity"
            aria-label="Dismiss"
          >
            ✕
          </button>
        </div>
      ))}
    </div>
  )
}

export function useToastHelpers() {
  const { show } = useToast()
  return {
    success: (msg: string) => show('success', msg),
    error: (msg: string) => show('error', msg),
    warning: (msg: string) => show('warning', msg),
    info: (msg: string) => show('info', msg),
  }
}