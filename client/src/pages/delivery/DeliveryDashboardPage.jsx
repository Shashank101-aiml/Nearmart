import { useEffect, useState } from 'react'
import * as deliveryService from '../../services/deliveryService'
import { fulfillmentBadgeClassFor } from '../../utils/badges'
import { formatPrice } from '../../utils/currency'

export default function DeliveryDashboardPage() {
  const [available, setAvailable] = useState(true)
  const [availableItems, setAvailableItems] = useState([])
  const [myItems, setMyItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)
  const [savingAvailability, setSavingAvailability] = useState(false)

  const refetchAll = () => {
    return Promise.all([deliveryService.listAvailable(), deliveryService.listMine()]).then(
      ([availableRes, mineRes]) => {
        setAvailableItems(availableRes)
        setMyItems(mineRes)
      }
    )
  }

  useEffect(() => {
    Promise.all([deliveryService.getProfile(), deliveryService.listAvailable(), deliveryService.listMine()])
      .then(([profile, availableRes, mineRes]) => {
        setAvailable(profile.available)
        setAvailableItems(availableRes)
        setMyItems(mineRes)
      })
      .catch((err) => setError(err.message || 'Failed to load deliveries'))
      .finally(() => setLoading(false))
  }, [])

  const handleToggleAvailable = async () => {
    const next = !available
    setError('')
    setSavingAvailability(true)
    try {
      const profile = await deliveryService.setAvailability(next)
      setAvailable(profile.available)
    } catch (err) {
      setError(err.message || 'Failed to update availability')
    } finally {
      setSavingAvailability(false)
    }
  }

  const handleClaim = async (itemId) => {
    setError('')
    setBusyId(itemId)
    try {
      await deliveryService.claimItem(itemId)
      await refetchAll()
    } catch (err) {
      setError(err.message || 'Failed to claim item')
    } finally {
      setBusyId(null)
    }
  }

  const handleMarkDelivered = async (itemId) => {
    setError('')
    setBusyId(itemId)
    try {
      await deliveryService.updateItemStatus(itemId, 'DELIVERED')
      await refetchAll()
    } catch (err) {
      setError(err.message || 'Failed to update item status')
    } finally {
      setBusyId(null)
    }
  }

  const activeItems = myItems.filter((item) => item.fulfillmentStatus !== 'DELIVERED')
  const completedItems = myItems.filter((item) => item.fulfillmentStatus === 'DELIVERED')

  const itemRow = (item, action) => (
    <div className="flex items-center justify-between gap-3 rounded-lg border border-border p-3 text-sm text-text" key={item.id}>
      <div className="flex items-center gap-2">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-md bg-code-bg">
          {item.imageUrl ? (
            <img src={item.imageUrl} alt={item.productTitle} className="h-full w-full object-cover" />
          ) : (
            <span className="text-[9px] text-text">No image</span>
          )}
        </div>
        <div>
          <p className="m-0 text-text-h">{item.productTitle}</p>
          <p className="m-0 text-xs text-text">Order #{item.orderId} &middot; {item.customerName}</p>
        </div>
      </div>
      <span>
        {item.quantity} &times; {formatPrice(item.unitPrice)}
      </span>
      <span>{formatPrice(item.lineTotal)}</span>
      <span className={fulfillmentBadgeClassFor(item.fulfillmentStatus)}>{item.fulfillmentStatus}</span>
      {action}
    </div>
  )

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <h1 className="m-0 mb-1 text-[28px] text-left">Deliveries</h1>
        <button
          type="button"
          onClick={handleToggleAvailable}
          disabled={savingAvailability}
          className={`cursor-pointer rounded-md border px-3.5 py-1.5 text-sm font-semibold disabled:cursor-not-allowed disabled:opacity-60 ${
            available ? 'border-accent bg-accent-bg text-accent' : 'border-border bg-bg text-text-h'
          }`}
        >
          {savingAvailability ? 'Saving...' : available ? 'Online' : 'Offline'}
        </button>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading deliveries...</p>}

      {!loading && (
        <>
          <section className="mb-8">
            <h2 className="mb-2 text-lg text-text-h">Available for pickup</h2>
            {!available && (
              <p className="text-sm text-text">Go online to see and claim available deliveries.</p>
            )}
            {available && availableItems.length === 0 && <p className="text-sm text-text">Nothing to pick up right now.</p>}
            {available && (
              <div className="flex flex-col gap-2">
                {availableItems.map((item) =>
                  itemRow(
                    item,
                    <button
                      type="button"
                      disabled={busyId === item.id}
                      onClick={() => handleClaim(item.id)}
                      className="cursor-pointer rounded-md border-none bg-accent px-3 py-1.5 text-sm text-white disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {busyId === item.id ? 'Claiming...' : 'Claim'}
                    </button>
                  )
                )}
              </div>
            )}
          </section>

          <section className="mb-8">
            <h2 className="mb-2 text-lg text-text-h">My active deliveries</h2>
            {activeItems.length === 0 && <p className="text-sm text-text">No active deliveries.</p>}
            <div className="flex flex-col gap-2">
              {activeItems.map((item) =>
                itemRow(
                  item,
                  <button
                    type="button"
                    disabled={busyId === item.id}
                    onClick={() => handleMarkDelivered(item.id)}
                    className="cursor-pointer rounded-md border border-border bg-bg px-3 py-1.5 text-sm text-text-h disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {busyId === item.id ? 'Updating...' : 'Mark delivered'}
                  </button>
                )
              )}
            </div>
          </section>

          <section>
            <h2 className="mb-2 text-lg text-text-h">Completed</h2>
            {completedItems.length === 0 && <p className="text-sm text-text">No completed deliveries yet.</p>}
            <div className="flex flex-col gap-2">{completedItems.map((item) => itemRow(item, null))}</div>
          </section>
        </>
      )}
    </div>
  )
}
