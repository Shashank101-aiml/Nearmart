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
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          <h1>Notifications</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading notifications...</p>}
      {!loading && !error && notifications.length === 0 && <p>You have no notifications yet.</p>}

      <div className="order-list">
        {notifications.map((notification) => (
          <div
            className="order-card"
            key={notification.id}
            onClick={() => handleMarkRead(notification)}
            style={{ opacity: notification.read ? 0.6 : 1, cursor: notification.read ? 'default' : 'pointer' }}
          >
            <div className="order-card-header">
              <div>
                <strong>{notification.message}</strong>
                {!notification.read && <span className="badge badge-pending">New</span>}
              </div>
              <p>{new Date(notification.createdAt).toLocaleString()}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
