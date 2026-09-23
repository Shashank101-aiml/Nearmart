import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as notificationService from '../../services/notificationService'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    notificationService
      .listNotifications()
      .then(setNotifications)
      .catch((err) => setError(err.message || 'Failed to load notifications'))
      .finally(() => setLoading(false))
  }, [])

  const handleMarkRead = async (notification) => {
    if (notification.read) {
      return
    }
    try {
      const updated = await notificationService.markRead(notification.id)
      setNotifications((current) => current.map((n) => (n.id === updated.id ? updated : n)))
    } catch (err) {
      setError(err.message || 'Failed to mark notification as read')
    }
  }

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <Link
          to="/customer"
          className="mb-3 inline-block text-sm font-semibold text-text hover:text-accent"
        >
          &larr; Back to catalog
        </Link>
        <h1 className="m-0 mb-1 text-[28px] text-left">Notifications</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading notifications...</p>}
      {!loading && !error && notifications.length === 0 && <p>You have no notifications yet.</p>}

      <div className="mt-4 flex flex-col gap-3">
        {notifications.map((notification) => (
          <div
            className="rounded-lg border border-border p-4"
            key={notification.id}
            onClick={() => handleMarkRead(notification)}
            style={{ cursor: notification.read ? 'default' : 'pointer' }}
          >
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-2">
                {!notification.read && <span className="h-2 w-2 shrink-0 rounded-full bg-accent" />}
                <strong className={notification.read ? 'mr-2 text-text' : 'mr-2 text-text-h'}>
                  {notification.message}
                </strong>
                {!notification.read && (
                  <span className="rounded-full bg-accent-bg px-2 py-0.5 text-xs text-accent">New</span>
                )}
              </div>
              <p>{new Date(notification.createdAt).toLocaleString()}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
