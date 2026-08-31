import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function AdminHome() {
  const { user, logout } = useAuth()

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="m-0 mb-1 text-[28px] text-left">Admin Dashboard</h1>
          <p>
            Signed in as <strong>{user.username}</strong> ({user.role})
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/admin/users" className="text-sm text-text-h underline">
            Users
          </Link>
          <Link to="/admin/vendors" className="text-sm text-text-h underline">
            Vendors
          </Link>
          <Link to="/admin/orders" className="text-sm text-text-h underline">
            Orders
          </Link>
          <button
            type="button"
            onClick={logout}
            className="cursor-pointer whitespace-nowrap rounded-md border border-border bg-bg px-3.5 py-2 text-text-h"
          >
            Log out
          </button>
        </div>
      </header>
    </div>
  )
}
