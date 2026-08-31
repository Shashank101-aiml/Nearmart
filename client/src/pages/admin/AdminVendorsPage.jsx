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
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/admin">&larr; Back to dashboard</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Vendors</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading vendors...</p>}
      {!loading && vendors.length === 0 && <p>No vendors found.</p>}

      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
        {vendors.map((vendor) => (
          <div className="flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4" key={vendor.id}>
            <h3 className="m-0 text-lg text-text-h">{vendor.storeName}</h3>
            <p className="text-sm text-text">{vendor.location}</p>
            <p>
              {vendor.username} &middot; {vendor.email}
            </p>
            <p
              className={`self-start rounded-full px-2 py-0.5 text-xs ${
                vendor.enabled ? 'bg-accent-bg text-accent' : 'bg-code-bg text-text'
              }`}
            >
              {vendor.enabled ? 'Enabled' : 'Disabled'}
            </p>
          </div>
        ))}
      </div>
    </div>
  )
}
