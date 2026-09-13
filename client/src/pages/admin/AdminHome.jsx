import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as adminService from '../../services/adminService'
import { badgeClassFor } from '../../utils/badges'

export default function AdminHome() {
  const [users, setUsers] = useState([])
  const [vendors, setVendors] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([adminService.listUsers(), adminService.listVendors(), adminService.listOrders()])
      .then(([userData, vendorData, orderData]) => {
        setUsers(userData)
        setVendors(vendorData)
        setOrders(orderData)
      })
      .catch((err) => setError(err.message || 'Failed to load dashboard'))
      .finally(() => setLoading(false))
  }, [])

  const statusCounts = orders.reduce((counts, order) => {
    counts[order.status] = (counts[order.status] || 0) + 1
    return counts
  }, {})

  const statCardClasses = 'flex flex-1 flex-col gap-1 rounded-lg border border-border bg-bg p-5 no-underline'

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <h1 className="m-0 mb-1 text-[28px] text-left">Admin Dashboard</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading dashboard...</p>}

      {!loading && !error && (
        <div className="flex flex-wrap gap-4">
          <Link to="/admin/users" className={statCardClasses}>
            <span className="text-3xl font-bold text-accent">{users.length}</span>
            <span className="text-sm text-text-h">Users</span>
          </Link>
          <Link to="/admin/vendors" className={statCardClasses}>
            <span className="text-3xl font-bold text-accent">{vendors.length}</span>
            <span className="text-sm text-text-h">Vendors</span>
          </Link>
          <Link to="/admin/orders" className={statCardClasses}>
            <span className="text-3xl font-bold text-accent">{orders.length}</span>
            <span className="text-sm text-text-h">Orders</span>
            <div className="mt-1 flex flex-wrap gap-1.5">
              {Object.entries(statusCounts).map(([status, count]) => (
                <span key={status} className={badgeClassFor(status)}>
                  {status}: {count}
                </span>
              ))}
            </div>
          </Link>
        </div>
      )}
    </div>
  )
}
