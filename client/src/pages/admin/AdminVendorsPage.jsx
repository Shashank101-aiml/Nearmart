import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as adminService from '../../services/adminService'

export default function AdminVendorsPage() {
  const [vendors, setVendors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [editingId, setEditingId] = useState(null)
  const [editForm, setEditForm] = useState({ storeName: '', location: '', username: '' })
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    adminService
      .listVendors()
      .then(setVendors)
      .catch((err) => setError(err.message || 'Failed to load vendors'))
      .finally(() => setLoading(false))
  }, [])

  const startEdit = (vendor) => {
    setEditingId(vendor.id)
    setEditForm({ storeName: vendor.storeName, location: vendor.location, username: vendor.username })
  }

  const cancelEdit = () => {
    setEditingId(null)
    setEditForm({ storeName: '', location: '', username: '' })
  }

  const handleEditChange = (e) => {
    setEditForm({ ...editForm, [e.target.name]: e.target.value })
  }

  const handleEditSubmit = async (e, vendorId) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const updated = await adminService.updateVendor(vendorId, editForm)
      setVendors(vendors.map((v) => (v.id === vendorId ? { ...v, ...updated } : v)))
      cancelEdit()
    } catch (err) {
      setError(err.message || 'Failed to update vendor')
    } finally {
      setSaving(false)
    }
  }

  const labelClasses = 'flex flex-col gap-1.5 text-sm text-text-h'
  const fieldClasses = 'rounded-md border border-border bg-bg px-2.5 py-2 text-text-h'

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <Link to="/admin">&larr; Back to dashboard</Link>
        <h1 className="m-0 mb-1 text-[28px] text-left">Vendors</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading vendors...</p>}
      {!loading && vendors.length === 0 && <p>No vendors found.</p>}

      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
        {vendors.map((vendor) =>
          editingId === vendor.id ? (
            <form
              key={vendor.id}
              onSubmit={(e) => handleEditSubmit(e, vendor.id)}
              className="flex flex-col gap-2.5 rounded-lg border border-border bg-bg p-4 text-left"
            >
              <label className={labelClasses}>
                Store name
                <input
                  name="storeName"
                  value={editForm.storeName}
                  onChange={handleEditChange}
                  required
                  className={fieldClasses}
                />
              </label>
              <label className={labelClasses}>
                Location
                <input
                  name="location"
                  value={editForm.location}
                  onChange={handleEditChange}
                  required
                  className={fieldClasses}
                />
              </label>
              <label className={labelClasses}>
                Username
                <input
                  name="username"
                  value={editForm.username}
                  onChange={handleEditChange}
                  required
                  minLength={3}
                  maxLength={50}
                  className={fieldClasses}
                />
              </label>
              <div className="flex flex-wrap gap-2">
                <button
                  type="submit"
                  disabled={saving}
                  className="cursor-pointer rounded-md border-none bg-accent px-3.5 py-1.5 text-sm text-white disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {saving ? 'Saving...' : 'Save'}
                </button>
                <button
                  type="button"
                  onClick={cancelEdit}
                  className="cursor-pointer rounded-md border border-border bg-bg px-3.5 py-1.5 text-sm text-text-h"
                >
                  Cancel
                </button>
              </div>
            </form>
          ) : (
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
              <button
                type="button"
                onClick={() => startEdit(vendor)}
                className="mt-1 cursor-pointer self-start rounded-md border border-border bg-bg px-2.5 py-1.5 text-xs text-text-h"
              >
                Edit
              </button>
            </div>
          )
        )}
      </div>
    </div>
  )
}
