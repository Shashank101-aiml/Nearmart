import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as adminService from '../../services/adminService'
import { badgeClassFor } from '../../utils/badges'

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
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/admin">&larr; Back to dashboard</Link>
          <h1>Orders</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading orders...</p>}
      {!loading && orders.length === 0 && <p>No orders yet.</p>}

      <div className="order-list">
        {orders.map((order) => {
          const detail = detailsById[order.id]
          const vendorGroups = detail ? groupByVendor(detail.items) : null

          return (
            <div className="order-card" key={order.id}>
              <div className="order-card-header" onClick={() => toggleExpand(order)}>
                <div>
                  <strong>Order #{order.id}</strong>
                  <span className={`badge ${badgeClassFor(order.status)}`}>{order.status}</span>
                </div>
                <p>{new Date(order.createdAt).toLocaleString()}</p>
                <p className="price">${order.total.toFixed(2)}</p>
              </div>
              <p className="order-card-customer">
                Customer: {order.customerName} ({order.customerUsername})
              </p>
              <button type="button" onClick={() => toggleExpand(order)}>
                {expandedId === order.id ? 'Hide items' : 'Show items'}
              </button>
              {expandedId === order.id && (
                <div className="order-items">
                  {!detail && !detailError && <p>Loading order detail...</p>}
                  {detailError && <p className="auth-error">{detailError}</p>}
                  {vendorGroups &&
                    Array.from(vendorGroups.entries()).map(([vendorStoreName, items]) => (
                      <div key={vendorStoreName}>
                        <h4>{vendorStoreName}</h4>
                        {items.map((item, index) => (
                          <div className="order-item" key={index}>
                            <span>{item.productTitle}</span>
                            <span>
                              {item.quantity} &times; ${item.unitPrice.toFixed(2)}
                            </span>
                            <span>${item.lineTotal.toFixed(2)}</span>
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
