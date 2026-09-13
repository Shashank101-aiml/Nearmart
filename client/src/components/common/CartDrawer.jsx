import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useCart } from '../../hooks/useCart'
import QuantityStepper from './QuantityStepper'
import * as orderService from '../../services/orderService'
import { openRazorpayCheckout } from '../../utils/razorpayCheckout'

export default function CartDrawer() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const {
    cart,
    isOpen,
    closeCart,
    busyProductId,
    itemErrors,
    incrementItem,
    decrementItem,
    removeItem,
  } = useCart()
  const [error, setError] = useState('')
  const [checkingOut, setCheckingOut] = useState(false)

  if (user.role !== 'CUSTOMER' || !isOpen) {
    return null
  }

  const handleCheckout = async () => {
    setError('')
    setCheckingOut(true)
    try {
      const order = await orderService.placeOrder()
      openRazorpayCheckout(order, {
        onSettled: () => {
          closeCart()
          navigate('/customer/orders')
        },
      })
    } catch (err) {
      setError(err.message || 'Checkout failed')
    } finally {
      setCheckingOut(false)
    }
  }

  return (
    <>
      <div className="fixed inset-0 z-20 bg-black/40" onClick={closeCart} />
      <div className="fixed top-0 right-0 z-30 flex h-full w-[380px] max-w-full flex-col border-l border-border bg-bg p-5 text-left">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="m-0 text-lg text-text-h">Your cart</h2>
          <button
            type="button"
            onClick={closeCart}
            aria-label="Close cart"
            className="cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1 text-text-h"
          >
            &times;
          </button>
        </div>

        {error && <p className="auth-error">{error}</p>}

        {!cart || cart.items.length === 0 ? (
          <p>Your cart is empty.</p>
        ) : (
          <>
            <div className="flex-1 overflow-y-auto">
              <div className="flex flex-col gap-3">
                {cart.items.map((item) => (
                  <div key={item.productId} className="rounded-lg border border-border p-3">
                    <h3 className="m-0 mb-1 text-sm text-text-h">{item.productTitle}</h3>
                    <p className="text-xs text-text">${item.price.toFixed(2)} each</p>
                    <div className="mt-2 flex items-center justify-between gap-2">
                      <QuantityStepper
                        quantity={item.quantity}
                        onIncrement={() => incrementItem(item.productId)}
                        onDecrement={() => decrementItem(item.productId)}
                        disabled={busyProductId === item.productId}
                      />
                      <p className="font-semibold text-accent">${item.lineTotal.toFixed(2)}</p>
                    </div>
                    <button
                      type="button"
                      onClick={() => removeItem(item.productId)}
                      disabled={busyProductId === item.productId}
                      className="mt-2 cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1 text-xs text-text-h"
                    >
                      Remove
                    </button>
                    {itemErrors[item.productId] && <p className="field-error">{itemErrors[item.productId]}</p>}
                  </div>
                ))}
              </div>
            </div>

            <div className="mt-4 flex items-center justify-between border-t border-border pt-4">
              <p>
                Total: <strong>${cart.total.toFixed(2)}</strong>
              </p>
              <button
                type="button"
                onClick={handleCheckout}
                disabled={checkingOut}
                className="cursor-pointer rounded-md border-none bg-accent px-4 py-2 text-white disabled:cursor-not-allowed disabled:opacity-60"
              >
                {checkingOut ? 'Placing order...' : 'Checkout'}
              </button>
            </div>
          </>
        )}
      </div>
    </>
  )
}
