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
          <div className="flex flex-1 flex-col items-center justify-center gap-3 text-center text-text">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-accent-bg text-2xl text-accent">
              <i className="fa-solid fa-bag-shopping" />
            </div>
            <p className="m-0 font-bold text-text-h">Your basket is empty</p>
            <p className="m-0 max-w-xs text-xs text-text">
              Explore fresh groceries, medicines &amp; snacks for fast delivery.
            </p>
          </div>
        ) : (
          <>
            <div className="flex-1 overflow-y-auto">
              <div className="flex flex-col gap-3">
                {cart.items.map((item) => (
                  <div key={item.productId} className="flex gap-3 rounded-lg border border-border p-3">
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center overflow-hidden rounded-md bg-code-bg">
                      {item.imageUrl ? (
                        <img src={item.imageUrl} alt={item.productTitle} className="h-full w-full object-cover" />
                      ) : (
                        <span className="text-[9px] text-text">No image</span>
                      )}
                    </div>
                    <div className="flex-1">
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
                  </div>
                ))}
              </div>
            </div>

            <div className="mt-4 flex flex-col gap-2 border-t border-border pt-4 text-xs font-medium text-text">
              {(() => {
                const deliveryFee = cart.total >= 199 ? 0 : 25
                const handling = 4
                const grandTotal = cart.total + deliveryFee + handling
                return (
                  <>
                    <div className="flex justify-between">
                      <span>Item Subtotal</span>
                      <span className="font-bold text-text-h">${cart.total.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Delivery Fee</span>
                      <span className="font-bold text-accent">{deliveryFee === 0 ? 'FREE' : `$${deliveryFee.toFixed(2)}`}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Handling Charge</span>
                      <span className="font-bold text-text-h">${handling.toFixed(2)}</span>
                    </div>
                    <div className="flex items-center justify-between border-t border-border pt-2 text-base font-black text-text-h">
                      <span>To Pay</span>
                      <span className="text-accent">${grandTotal.toFixed(2)}</span>
                    </div>
                    <button
                      type="button"
                      onClick={handleCheckout}
                      disabled={checkingOut}
                      className="mt-2 cursor-pointer rounded-md border-none bg-accent px-4 py-3 font-bold text-white hover:bg-accent-hover disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {checkingOut ? 'Placing order...' : `Place Order · $${grandTotal.toFixed(2)}`}
                    </button>
                  </>
                )
              })()}
            </div>
          </>
        )}
      </div>
    </>
  )
}
