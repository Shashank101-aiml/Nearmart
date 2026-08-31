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
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Notifications</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading notifications...</p>}
      {!loading && !error && notifications.length === 0 && <p>You have no notifications yet.</p>}

      <div className="mt-4 flex flex-col gap-3">
        {notifications.map((notification) => (
          <div
            className="rounded-lg border border-border p-4"
            key={notification.id}
            onClick={() => handleMarkRead(notification)}
            style={{ opacity: notification.read ? 0.6 : 1, cursor: notification.read ? 'default' : 'pointer' }}
          >
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <strong className="mr-2 text-text-h">{notification.message}</strong>
                {!notification.read && (
                  <span className="rounded-full bg-code-bg px-2 py-0.5 text-xs text-text">New</span>
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
