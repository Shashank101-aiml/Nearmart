import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import * as adminService from '../../services/adminService'

const ROLE_OPTIONS = ['ALL', 'CUSTOMER', 'VENDOR', 'ADMIN']

export default function AdminUsersPage() {
  const { user: currentUser } = useAuth()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')

  useEffect(() => {
    adminService
      .listUsers()
      .then(setUsers)
      .catch((err) => setError(err.message || 'Failed to load users'))
      .finally(() => setLoading(false))
  }, [])

  const query = search.trim().toLowerCase()
  const visibleUsers = users.filter((u) => {
    const matchesRole = roleFilter === 'ALL' || u.role === roleFilter
    const matchesQuery =
      !query || u.username.toLowerCase().includes(query) || u.email.toLowerCase().includes(query)
    return matchesRole && matchesQuery
  })

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

  const handleDelete = async (targetUser) => {
    if (!window.confirm(`Delete ${targetUser.username}? This cannot be undone.`)) {
      return
    }

    setError('')
    setBusyId(targetUser.id)
    try {
      await adminService.deleteUser(targetUser.id)
      setUsers(users.filter((u) => u.id !== targetUser.id))
    } catch (err) {
      setError(err.message || 'Failed to delete user')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <Link to="/admin">&larr; Back to dashboard</Link>
        <h1 className="m-0 mb-1 text-[28px] text-left">Users</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading users...</p>}
      {!loading && users.length === 0 && <p>No users found.</p>}

      {!loading && users.length > 0 && (
        <div className="mt-4 flex flex-wrap items-center gap-3">
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search username or email..."
            className="rounded-md border border-border bg-bg px-2.5 py-2 text-text-h"
          />
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="rounded-md border border-border bg-bg px-2.5 py-2 text-text-h"
          >
            {ROLE_OPTIONS.map((role) => (
              <option key={role} value={role}>
                {role === 'ALL' ? 'All roles' : role}
              </option>
            ))}
          </select>
          <span className="text-sm text-text">
            {visibleUsers.length} of {users.length}
          </span>
        </div>
      )}

      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
        {!loading && users.length > 0 && visibleUsers.length === 0 && <p>No users match your search.</p>}
        {visibleUsers.map((u) => {
          const isSelf = u.id === currentUser.userId
          return (
            <div className="flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4" key={u.id}>
              <h3 className="m-0 text-lg text-text-h">{u.username}</h3>
              <p className="text-sm text-text">{u.email}</p>
              {u.phoneNumber && <p className="text-sm text-text">{u.phoneNumber}</p>}
              <p>Role: {u.role}</p>
              <p
                className={`self-start rounded-full px-2 py-0.5 text-xs ${
                  u.enabled ? 'bg-accent-bg text-accent' : 'bg-code-bg text-text'
                }`}
              >
                {u.enabled ? 'Enabled' : 'Disabled'}
              </p>
              <div className="mt-1 flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => handleToggle(u)}
                  disabled={busyId === u.id || isSelf}
                  title={isSelf ? 'You cannot disable your own account' : undefined}
                  className="cursor-pointer self-start rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {busyId === u.id ? 'Saving...' : u.enabled ? 'Disable' : 'Enable'}
                </button>
                {u.role !== 'ADMIN' && (
                  <button
                    type="button"
                    onClick={() => handleDelete(u)}
                    disabled={busyId === u.id || isSelf}
                    title={isSelf ? 'You cannot delete your own account' : undefined}
                    className="cursor-pointer self-start rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {busyId === u.id ? 'Saving...' : 'Delete'}
                  </button>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
