import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as vendorOrderService from '../../services/vendorOrderService'
import { badgeClassFor, fulfillmentBadgeClassFor } from '../../utils/badges'
import { formatPrice } from '../../utils/currency'

export default function VendorOrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [updatingItemId, setUpdatingItemId] = useState(null)

  useEffect(() => {
    vendorOrderService
      .listOrders()
      .then(setOrders)
      .catch((err) => setError(err.message || 'Failed to load orders'))
      .finally(() => setLoading(false))
  }, [])

  const toggleExpand = (id) => {
    setExpandedId((current) => (current === id ? null : id))
  }

  const handleAdvance = async (orderId, item) => {
    setError('')
    setUpdatingItemId(item.id)
    try {
      const updated = await vendorOrderService.updateItemFulfillment(orderId, item.id, 'SHIPPED')
      setOrders((current) => current.map((o) => (o.id === updated.id ? updated : o)))
    } catch (err) {
      setError(err.message || 'Failed to update item status')
    } finally {
      setUpdatingItemId(null)
    }
  }

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <Link to="/vendor">&larr; Back to dashboard</Link>
        <h1 className="m-0 mb-1 text-[28px] text-left">Orders</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading orders...</p>}
      {!loading && !error && orders.length === 0 && <p>No orders for your products yet.</p>}

      <div className="mt-4 flex flex-col gap-3">
        {orders.map((order) => (
          <div className="rounded-lg border border-border p-4" key={order.id}>
            <div
              className="flex flex-wrap items-center justify-between gap-4 cursor-pointer"
              onClick={() => toggleExpand(order.id)}
            >
              <div>
                <strong className="mr-2 text-text-h">Order #{order.id}</strong>
                <span className={badgeClassFor(order.status)}>{order.status}</span>
              </div>
              <p>{new Date(order.createdAt).toLocaleString()}</p>
              <p>{formatPrice(order.vendorSubtotal)}</p>
            </div>
            <p className="mt-1.5 text-sm text-text">Customer: {order.customerName}</p>
            <button
              type="button"
              onClick={() => toggleExpand(order.id)}
              className="mt-2.5 cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
            >
              {expandedId === order.id ? 'Hide items' : 'Show items'}
            </button>
            {expandedId === order.id && (
              <div className="mt-3 flex flex-col gap-2 border-t border-border pt-3">
                {order.items.map((item) => (
                  <div className="flex items-center justify-between gap-3 text-sm text-text" key={item.id}>
                    <div className="flex items-center gap-2">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-md bg-code-bg">
                        {item.imageUrl ? (
                          <img src={item.imageUrl} alt={item.productTitle} className="h-full w-full object-cover" />
                        ) : (
                          <span className="text-[9px] text-text">No image</span>
                        )}
                      </div>
                      <span>{item.productTitle}</span>
                    </div>
                    <span>
                      {item.quantity} &times; {formatPrice(item.unitPrice)}
                    </span>
                    <span>{formatPrice(item.lineTotal)}</span>
                    <span className={fulfillmentBadgeClassFor(item.fulfillmentStatus)}>
                      {item.fulfillmentStatus}
                    </span>
                    {item.fulfillmentStatus === 'PROCESSING' && (
                      <button
                        type="button"
                        disabled={updatingItemId === item.id}
                        onClick={() => handleAdvance(order.id, item)}
                      >
                        {updatingItemId === item.id ? 'Updating...' : 'Mark shipped'}
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
