import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as vendorOrderService from '../../services/vendorOrderService'

export default function VendorOrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedId, setExpandedId] = useState(null)

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

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/vendor">&larr; Back to dashboard</Link>
          <h1>Orders</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading orders...</p>}
      {!loading && !error && orders.length === 0 && <p>No orders for your products yet.</p>}

      <div className="order-list">
        {orders.map((order) => (
          <div className="order-card" key={order.id}>
            <div className="order-card-header" onClick={() => toggleExpand(order.id)}>
              <div>
                <strong>Order #{order.id}</strong>
                <span className="badge badge-available">{order.status}</span>
              </div>
              <p>{new Date(order.createdAt).toLocaleString()}</p>
              <p className="price">${order.vendorSubtotal.toFixed(2)}</p>
            </div>
            <p className="order-card-customer">Customer: {order.customerName}</p>
            <button type="button" onClick={() => toggleExpand(order.id)}>
              {expandedId === order.id ? 'Hide items' : 'Show items'}
            </button>
            {expandedId === order.id && (
              <div className="order-items">
                {order.items.map((item, index) => (
                  <div className="order-item" key={index}>
                    <span>{item.productTitle}</span>
                    <span>
                      {item.quantity} &times; ${item.unitPrice.toFixed(2)}
                    </span>
                    <span>${item.lineTotal.toFixed(2)}</span>
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
