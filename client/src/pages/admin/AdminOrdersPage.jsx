import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as adminService from '../../services/adminService'
import { connectAdminOrders } from '../../services/adminOrderSocket'
import { badgeClassFor, fulfillmentBadgeClassFor } from '../../utils/badges'
import { useAuth } from '../../hooks/useAuth'

function groupByVendor(items) {
  const groups = new Map()
  for (const item of items) {
    const key = item.vendorStoreName || 'Unknown vendor'
    if (!groups.has(key)) {
      groups.set(key, [])
    }
    groups.get(key).push(item)
  }
  return groups
}

export default function AdminOrdersPage() {
  const { token } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [detailsById, setDetailsById] = useState({})
  const [detailError, setDetailError] = useState('')

  useEffect(() => {
    adminService
      .listOrders()
      .then(setOrders)
      .catch((err) => setError(err.message || 'Failed to load orders'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!token) {
      return undefined
    }
    const socket = connectAdminOrders(token, ({ orderId, status }) => {
      setOrders((current) => current.map((order) => (order.id !== orderId ? order : { ...order, status })))
    })
    return () => socket.close()
  }, [token])

  const toggleExpand = (order) => {
    const nextId = expandedId === order.id ? null : order.id
    setExpandedId(nextId)
    setDetailError('')

    if (nextId !== null && !detailsById[order.id]) {
      adminService
        .getOrder(order.id)
        .then((detail) => setDetailsById((current) => ({ ...current, [order.id]: detail })))
        .catch((err) => setDetailError(err.message || 'Failed to load order detail'))
    }
  }

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/admin">&larr; Back to dashboard</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Orders</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading orders...</p>}
      {!loading && orders.length === 0 && <p>No orders yet.</p>}

      <div className="mt-4 flex flex-col gap-3">
        {orders.map((order) => {
          const detail = detailsById[order.id]
          const vendorGroups = detail ? groupByVendor(detail.items) : null

          return (
            <div className="rounded-lg border border-border p-4" key={order.id}>
              <div
                className="flex flex-wrap items-center justify-between gap-4 cursor-pointer"
                onClick={() => toggleExpand(order)}
              >
                <div>
                  <strong className="mr-2 text-text-h">Order #{order.id}</strong>
                  <span className={badgeClassFor(order.status)}>{order.status}</span>
                </div>
                <p>{new Date(order.createdAt).toLocaleString()}</p>
                <p>${order.total.toFixed(2)}</p>
              </div>
              <p className="mt-1.5 text-sm text-text">
                Customer: {order.customerName} ({order.customerUsername})
              </p>
              <button
                type="button"
                onClick={() => toggleExpand(order)}
                className="mt-2.5 cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
              >
                {expandedId === order.id ? 'Hide items' : 'Show items'}
              </button>
              {expandedId === order.id && (
                <div className="mt-3 flex flex-col gap-2 border-t border-border pt-3">
                  {!detail && !detailError && <p>Loading order detail...</p>}
                  {detailError && <p className="auth-error">{detailError}</p>}
                  {vendorGroups &&
                    Array.from(vendorGroups.entries()).map(([vendorStoreName, items]) => (
                      <div key={vendorStoreName}>
                        <h4>{vendorStoreName}</h4>
                        {items.map((item, index) => (
                          <div className="flex justify-between gap-3 text-sm text-text" key={index}>
                            <span>{item.productTitle}</span>
                            <span>
                              {item.quantity} &times; ${item.unitPrice.toFixed(2)}
                            </span>
                            <span>${item.lineTotal.toFixed(2)}</span>
                            <span className={fulfillmentBadgeClassFor(item.fulfillmentStatus)}>
                              {item.fulfillmentStatus}
                            </span>
                          </div>
                        ))}
                      </div>
                    ))}
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
