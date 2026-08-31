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
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/admin">&larr; Back to dashboard</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Users</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading users...</p>}
      {!loading && users.length === 0 && <p>No users found.</p>}

      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
        {users.map((u) => {
          const isSelf = u.id === currentUser.userId
          return (
            <div className="flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4" key={u.id}>
              <h3 className="m-0 text-lg text-text-h">{u.username}</h3>
              <p className="text-sm text-text">{u.email}</p>
              <p>Role: {u.role}</p>
              <p
                className={`self-start rounded-full px-2 py-0.5 text-xs ${
                  u.enabled ? 'bg-accent-bg text-accent' : 'bg-code-bg text-text'
                }`}
              >
                {u.enabled ? 'Enabled' : 'Disabled'}
              </p>
              <button
                type="button"
                onClick={() => handleToggle(u)}
                disabled={busyId === u.id || isSelf}
                title={isSelf ? 'You cannot disable your own account' : undefined}
                className="self-start cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
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
