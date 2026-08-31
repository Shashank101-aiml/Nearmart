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
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          <h1 className="m-0 mb-1 text-[28px] text-left">Your cart</h1>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/customer/orders" className="text-sm text-text-h underline">
            Orders
          </Link>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading cart...</p>}
      {!loading && !error && cart?.items.length === 0 && <p>Your cart is empty.</p>}

      {!loading && !error && cart?.items.length > 0 && (
        <>
          <div className="mt-4 flex flex-col gap-3">
            {cart.items.map((item) => (
              <div
                className="flex flex-wrap items-center gap-4 rounded-lg border border-border px-4 py-3"
                key={item.productId}
              >
                <div className="min-w-40 flex-1">
                  <h3 className="m-0 mb-1 text-base text-text-h">{item.productTitle}</h3>
                  <p>${item.price.toFixed(2)} each</p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => decrement(item)}
                    disabled={busyProductId === item.productId}
                    className="size-7 cursor-pointer rounded-md border border-border bg-bg text-text-h"
                  >
                    -
                  </button>
                  <span>{item.quantity}</span>
                  <button
                    type="button"
                    onClick={() => increment(item)}
                    disabled={busyProductId === item.productId}
                    className="size-7 cursor-pointer rounded-md border border-border bg-bg text-text-h"
                  >
                    +
                  </button>
                </div>
                <p className="min-w-[70px] text-right font-semibold text-accent">${item.lineTotal.toFixed(2)}</p>
                <button
                  type="button"
                  onClick={() => remove(item)}
                  disabled={busyProductId === item.productId}
                  className="cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
                >
                  Remove
                </button>
                {itemErrors[item.productId] && <p className="field-error">{itemErrors[item.productId]}</p>}
              </div>
            ))}
          </div>
          <div className="mt-5 flex items-center justify-end gap-4 border-t border-border pt-4 text-right text-lg">
            <p>
              Total: <strong>${cart.total.toFixed(2)}</strong>
            </p>
            <button
              type="button"
              onClick={handleCheckout}
              disabled={checkingOut}
              className="cursor-pointer rounded-md border-none bg-accent px-5 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-60"
            >
              {checkingOut ? 'Placing order...' : 'Checkout'}
            </button>
          </div>
        </>
      )}
    </div>
  )
}
