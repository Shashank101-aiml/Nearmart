import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as adminService from '../../services/adminService'

export default function AdminDeliveryPartnersPage() {
  const [partners, setPartners] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  useEffect(() => {
    adminService
      .listDeliveryPartners()
      .then(setPartners)
      .catch((err) => setError(err.message || 'Failed to load delivery partners'))
      .finally(() => setLoading(false))
  }, [])

  const handleToggleEnabled = async (partner) => {
    const nextEnabled = !partner.enabled
    if (!nextEnabled && !window.confirm(`Disable ${partner.name}? They will no longer be able to log in.`)) {
      return
    }

    setError('')
    setBusyId(partner.id)
    try {
      const updated = await adminService.setUserEnabled(partner.id, nextEnabled)
      setPartners(partners.map((p) => (p.id === partner.id ? { ...p, enabled: updated.enabled } : p)))
    } catch (err) {
      setError(err.message || 'Failed to update delivery partner status')
    } finally {
      setBusyId(null)
    }
  }

  const handleDelete = async (partner) => {
    if (!window.confirm(`Delete ${partner.name}? This cannot be undone.`)) {
      return
    }

    setError('')
    setBusyId(partner.id)
    try {
      await adminService.deleteUser(partner.id)
      setPartners(partners.filter((p) => p.id !== partner.id))
    } catch (err) {
      setError(err.message || 'Failed to delete delivery partner')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <Link to="/admin">&larr; Back to dashboard</Link>
        <h1 className="m-0 mb-1 text-[28px] text-left">Delivery Partners</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading delivery partners...</p>}
      {!loading && partners.length === 0 && <p>No delivery partners found.</p>}

      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
        {partners.map((partner) => (
          <div className="flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4" key={partner.id}>
            <h3 className="m-0 text-lg text-text-h">{partner.name}</h3>
            {partner.vehicleNumber && <p className="text-sm text-text">{partner.vehicleNumber}</p>}
            <p>
              {partner.username} &middot; {partner.email}
            </p>
            {partner.phoneNumber && <p className="text-sm text-text">{partner.phoneNumber}</p>}
            <div className="flex flex-wrap gap-1.5">
              <p
                className={`self-start rounded-full px-2 py-0.5 text-xs ${
                  partner.enabled ? 'bg-accent-bg text-accent' : 'bg-code-bg text-text'
                }`}
              >
                {partner.enabled ? 'Enabled' : 'Disabled'}
              </p>
              <p
                className={`self-start rounded-full px-2 py-0.5 text-xs ${
                  partner.available ? 'bg-accent-bg text-accent' : 'bg-code-bg text-text'
                }`}
              >
                {partner.available ? 'Online' : 'Offline'}
              </p>
            </div>
            <div className="mt-1 flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => handleToggleEnabled(partner)}
                disabled={busyId === partner.id}
                className="cursor-pointer self-start rounded-md border border-border bg-bg px-2.5 py-1.5 text-xs text-text-h disabled:cursor-not-allowed disabled:opacity-60"
              >
                {busyId === partner.id ? 'Saving...' : partner.enabled ? 'Disable' : 'Enable'}
              </button>
              <button
                type="button"
                onClick={() => handleDelete(partner)}
                disabled={busyId === partner.id}
                className="cursor-pointer self-start rounded-md border border-border bg-bg px-2.5 py-1.5 text-xs text-text-h disabled:cursor-not-allowed disabled:opacity-60"
              >
                {busyId === partner.id ? 'Saving...' : 'Delete'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
