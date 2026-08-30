import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function AdminHome() {
  const { user, logout } = useAuth()

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <h1>Admin Dashboard</h1>
          <p>
            Signed in as <strong>{user.username}</strong> ({user.role})
          </p>
        </div>
        <div className="header-actions">
          <Link to="/admin/users">Users</Link>
          <Link to="/admin/vendors">Vendors</Link>
          <Link to="/admin/orders">Orders</Link>
          <button type="button" onClick={logout}>
            Log out
          </button>
        </div>
      </header>
    </div>
  )
}
