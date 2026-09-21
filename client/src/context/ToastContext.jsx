import { useCallback, useRef, useState } from 'react'
import { ToastContext } from './toast-context'

let nextId = 1

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const timers = useRef({})

  const dismissToast = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
    clearTimeout(timers.current[id])
    delete timers.current[id]
  }, [])

  const showToast = useCallback((message, type = 'info') => {
    const id = nextId++
    setToasts((current) => [...current, { id, message, type }])
    timers.current[id] = setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
      delete timers.current[id]
    }, 3000)
  }, [])

  return (
    <ToastContext.Provider value={{ toasts, showToast, dismissToast }}>{children}</ToastContext.Provider>
  )
}
