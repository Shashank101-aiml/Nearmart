import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import * as cartService from '../../services/cartService'
import * as orderService from '../../services/orderService'
import { openRazorpayCheckout } from '../../utils/razorpayCheckout'

export default function CartPage() {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [itemErrors, setItemErrors] = useState({})
  const [busyProductId, setBusyProductId] = useState(null)
  const [checkingOut, setCheckingOut] = useState(false)

  useEffect(() => {
    cartService
      .getCart()
      .then(setCart)
      .catch((err) => setError(err.message || 'Failed to load cart'))
      .finally(() => setLoading(false))
  }, [])

  const runMutation = async (productId, action) => {
    setBusyProductId(productId)
    setItemErrors((current) => ({ ...current, [productId]: null }))
    try {
      const updated = await action()
      setCart(updated)
    } catch (err) {
      setItemErrors((current) => ({ ...current, [productId]: err.message || 'Failed to update cart' }))
    } finally {
      setBusyProductId(null)
    }
  }

  const increment = (item) => runMutation(item.productId, () => cartService.updateItem(item.productId, item.quantity + 1))

  const decrement = (item) =>
    item.quantity <= 1
      ? runMutation(item.productId, () => cartService.removeItem(item.productId))
      : runMutation(item.productId, () => cartService.updateItem(item.productId, item.quantity - 1))

  const remove = (item) => runMutation(item.productId, () => cartService.removeItem(item.productId))

  const handleCheckout = async () => {
    setError('')
    setCheckingOut(true)
    try {
      const order = await orderService.placeOrder()
      openRazorpayCheckout(order, { onSettled: () => navigate('/customer/orders') })
    } catch (err) {
      setError(err.message || 'Checkout failed')
    } finally {
      setCheckingOut(false)
    }
  }

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          <h1>Your cart</h1>
        </div>
        <div className="header-actions">
          <Link to="/customer/orders">Orders</Link>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading cart...</p>}
      {!loading && !error && cart?.items.length === 0 && <p>Your cart is empty.</p>}

      {!loading && !error && cart?.items.length > 0 && (
        <>
          <div className="cart-items">
            {cart.items.map((item) => (
              <div className="cart-item" key={item.productId}>
                <div className="cart-item-info">
                  <h3>{item.productTitle}</h3>
                  <p className="price">${item.price.toFixed(2)} each</p>
                </div>
                <div className="cart-item-controls">
                  <button type="button" onClick={() => decrement(item)} disabled={busyProductId === item.productId}>
                    -
                  </button>
                  <span>{item.quantity}</span>
                  <button type="button" onClick={() => increment(item)} disabled={busyProductId === item.productId}>
                    +
                  </button>
                </div>
                <p className="line-total">${item.lineTotal.toFixed(2)}</p>
                <button type="button" onClick={() => remove(item)} disabled={busyProductId === item.productId}>
                  Remove
                </button>
                {itemErrors[item.productId] && <p className="field-error">{itemErrors[item.productId]}</p>}
              </div>
            ))}
          </div>
          <div className="cart-summary">
            <p>
              Total: <strong>${cart.total.toFixed(2)}</strong>
            </p>
            <button type="button" onClick={handleCheckout} disabled={checkingOut}>
              {checkingOut ? 'Placing order...' : 'Checkout'}
            </button>
          </div>
        </>
      )}
    </div>
  )
}
