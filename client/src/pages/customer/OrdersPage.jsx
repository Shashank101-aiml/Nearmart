import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as orderService from '../../services/orderService'
import * as paymentService from '../../services/paymentService'
import { openRazorpayCheckout } from '../../utils/razorpayCheckout'
import { connectTracking } from '../../services/trackingSocket'
import { badgeClassFor, fulfillmentBadgeClassFor } from '../../utils/badges'
import { useAuth } from '../../hooks/useAuth'

export default function OrdersPage() {
  const { token } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [retryingId, setRetryingId] = useState(null)

  useEffect(() => {
    if (!token) {
      return undefined
    }
    const socket = connectTracking(token, ({ orderId, itemId, fulfillmentStatus }) => {
      setOrders((current) =>
        current.map((order) =>
          order.id !== orderId
            ? order
            : {
                ...order,
                items: order.items.map((item) =>
                  item.id !== itemId ? item : { ...item, fulfillmentStatus }
                ),
              }
        )
      )
    })
    return () => socket.close()
  }, [token])

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
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Your orders</h1>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading orders...</p>}
      {!loading && !error && orders.length === 0 && <p>You haven't placed any orders yet.</p>}

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
              <p>${order.total.toFixed(2)}</p>
            </div>
            <button
              type="button"
              onClick={() => toggleExpand(order.id)}
              className="mt-2.5 cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
            >
              {expandedId === order.id ? 'Hide items' : 'Show items'}
            </button>
            {(order.status === 'PENDING_PAYMENT' || order.status === 'PAYMENT_FAILED') && (
              <button
                type="button"
                onClick={() => handleRetry(order)}
                disabled={retryingId === order.id}
                className="mt-2.5 cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
              >
                {retryingId === order.id ? 'Opening payment...' : 'Retry payment'}
              </button>
            )}
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
