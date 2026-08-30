import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as orderService from '../../services/orderService'
import * as paymentService from '../../services/paymentService'
import { openRazorpayCheckout } from '../../utils/razorpayCheckout'
import { badgeClassFor, fulfillmentBadgeClassFor } from '../../utils/badges'

export default function OrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [retryingId, setRetryingId] = useState(null)

  const refetchOrders = () => {
    orderService
      .listOrders()
      .then(setOrders)
      .catch((err) => setError(err.message || 'Failed to load orders'))
  }

  useEffect(() => {
    orderService
      .listOrders()
      .then(setOrders)
      .catch((err) => setError(err.message || 'Failed to load orders'))
      .finally(() => setLoading(false))
  }, [])

  const toggleExpand = (id) => {
    setExpandedId((current) => (current === id ? null : id))
  }

  const handleRetry = async (order) => {
    setError('')
    setRetryingId(order.id)
    try {
      const updated = await paymentService.retryPayment(order.id)
      openRazorpayCheckout(updated, { onSettled: refetchOrders })
    } catch (err) {
      setError(err.message || 'Retry failed')
    } finally {
      setRetryingId(null)
    }
  }

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          <h1>Your orders</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading orders...</p>}
      {!loading && !error && orders.length === 0 && <p>You haven't placed any orders yet.</p>}

      <div className="order-list">
        {orders.map((order) => (
          <div className="order-card" key={order.id}>
            <div className="order-card-header" onClick={() => toggleExpand(order.id)}>
              <div>
                <strong>Order #{order.id}</strong>
                <span className={`badge ${badgeClassFor(order.status)}`}>{order.status}</span>
              </div>
              <p>{new Date(order.createdAt).toLocaleString()}</p>
              <p className="price">${order.total.toFixed(2)}</p>
            </div>
            <button type="button" onClick={() => toggleExpand(order.id)}>
              {expandedId === order.id ? 'Hide items' : 'Show items'}
            </button>
            {(order.status === 'PENDING_PAYMENT' || order.status === 'PAYMENT_FAILED') && (
              <button type="button" onClick={() => handleRetry(order)} disabled={retryingId === order.id}>
                {retryingId === order.id ? 'Opening payment...' : 'Retry payment'}
              </button>
            )}
            {expandedId === order.id && (
              <div className="order-items">
                {order.items.map((item) => (
                  <div className="order-item" key={item.id}>
                    <span>{item.productTitle}</span>
                    <span>
                      {item.quantity} &times; ${item.unitPrice.toFixed(2)}
                    </span>
                    <span>${item.lineTotal.toFixed(2)}</span>
                    <span className={`badge ${fulfillmentBadgeClassFor(item.fulfillmentStatus)}`}>
                      {item.fulfillmentStatus}
                    </span>
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
