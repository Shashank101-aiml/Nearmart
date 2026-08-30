import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as adminService from '../../services/adminService'

export default function AdminVendorsPage() {
  const [vendors, setVendors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    adminService
      .listVendors()
      .then(setVendors)
      .catch((err) => setError(err.message || 'Failed to load vendors'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/admin">&larr; Back to dashboard</Link>
          <h1>Vendors</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading vendors...</p>}
      {!loading && vendors.length === 0 && <p>No vendors found.</p>}

      <div className="product-grid">
        {vendors.map((vendor) => (
          <div className="product-card" key={vendor.id}>
            <h3>{vendor.storeName}</h3>
            <p className="description">{vendor.location}</p>
            <p>
              {vendor.username} &middot; {vendor.email}
            </p>
            <p className={vendor.enabled ? 'badge badge-available' : 'badge badge-hidden'}>
              {vendor.enabled ? 'Enabled' : 'Disabled'}
            </p>
          </div>
        ))}
      </div>
    </div>
  )
}
