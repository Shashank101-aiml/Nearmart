import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import * as adminService from '../../services/adminService'

export default function AdminUsersPage() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  useEffect(() => {
    adminService
      .listUsers()
      .then(setUsers)
      .catch((err) => setError(err.message || 'Failed to load users'))
      .finally(() => setLoading(false))
  }, [])

  const handleToggle = async (targetUser) => {
    const nextEnabled = !targetUser.enabled
    if (!nextEnabled && !window.confirm(`Disable ${targetUser.username}'s account?`)) {
      return
    }

    setError('')
    setBusyId(targetUser.id)
    try {
      const updated = await adminService.setUserEnabled(targetUser.id, nextEnabled)
      setUsers(users.map((u) => (u.id === updated.id ? updated : u)))
    } catch (err) {
      setError(err.message || 'Failed to update user')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/admin">&larr; Back to dashboard</Link>
          <h1>Users</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading users...</p>}
      {!loading && users.length === 0 && <p>No users found.</p>}

      <div className="product-grid">
        {users.map((u) => {
          const isSelf = u.id === currentUser.userId
          return (
            <div className="product-card" key={u.id}>
              <h3>{u.username}</h3>
              <p className="description">{u.email}</p>
              <p>Role: {u.role}</p>
              <p className={u.enabled ? 'badge badge-available' : 'badge badge-hidden'}>
                {u.enabled ? 'Enabled' : 'Disabled'}
              </p>
              <button
                type="button"
                onClick={() => handleToggle(u)}
                disabled={busyId === u.id || isSelf}
                title={isSelf ? 'You cannot disable your own account' : undefined}
              >
                {busyId === u.id ? 'Saving...' : u.enabled ? 'Disable' : 'Enable'}
              </button>
            </div>
          )
        })}
      </div>
    </div>
  )
}
