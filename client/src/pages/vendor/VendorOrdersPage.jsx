import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as vendorOrderService from '../../services/vendorOrderService'
import { badgeClassFor, fulfillmentBadgeClassFor } from '../../utils/badges'

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
    const nextStatus = item.fulfillmentStatus === 'PROCESSING' ? 'SHIPPED' : 'DELIVERED'
    setError('')
    setUpdatingItemId(item.id)
    try {
      const updated = await vendorOrderService.updateItemFulfillment(orderId, item.id, nextStatus)
      setOrders((current) => current.map((o) => (o.id === updated.id ? updated : o)))
    } catch (err) {
      setError(err.message || 'Failed to update item status')
    } finally {
      setUpdatingItemId(null)
    }
  }

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/vendor">&larr; Back to dashboard</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Orders</h1>
        </div>
      </header>

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
              <p>${order.vendorSubtotal.toFixed(2)}</p>
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
                  <div className="flex justify-between gap-3 text-sm text-text" key={item.id}>
                    <span>{item.productTitle}</span>
                    <span>
                      {item.quantity} &times; ${item.unitPrice.toFixed(2)}
                    </span>
                    <span>${item.lineTotal.toFixed(2)}</span>
                    <span className={fulfillmentBadgeClassFor(item.fulfillmentStatus)}>
                      {item.fulfillmentStatus}
                    </span>
                    {item.fulfillmentStatus !== 'DELIVERED' && (
                      <button
                        type="button"
                        disabled={updatingItemId === item.id}
                        onClick={() => handleAdvance(order.id, item)}
                      >
                        {updatingItemId === item.id
                          ? 'Updating...'
                          : item.fulfillmentStatus === 'PROCESSING'
                            ? 'Mark shipped'
                            : 'Mark delivered'}
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
