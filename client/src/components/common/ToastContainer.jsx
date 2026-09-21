import { useToast } from '../../hooks/useToast'

export default function ToastContainer() {
  const { toasts, dismissToast } = useToast()

  if (toasts.length === 0) {
    return null
  }

  return (
    <div className="fixed right-5 bottom-5 z-50 flex flex-col gap-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          onClick={() => dismissToast(toast.id)}
          className={`cursor-pointer rounded-lg px-4 py-3 text-sm font-semibold text-white shadow-lg ${
            toast.type === 'error' ? 'bg-red-600' : 'bg-accent-dark'
          }`}
        >
          {toast.message}
        </div>
      ))}
    </div>
  )
}
