import { useEffect, useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import * as cartService from '../services/cartService'
import { CartContext } from './cart-context'

export function CartProvider({ children }) {
  const { isAuthenticated, user } = useAuth()
  const { showToast } = useToast()
  const [cart, setCart] = useState(null)
  const [isOpen, setIsOpen] = useState(false)
  const [busyProductId, setBusyProductId] = useState(null)
  const [itemErrors, setItemErrors] = useState({})

  useEffect(() => {
    if (isAuthenticated && user?.role === 'CUSTOMER') {
      cartService.getCart().then(setCart).catch(() => {})
    } else {
      Promise.resolve().then(() => setCart(null))
    }
  }, [isAuthenticated, user?.role])

  const runMutation = async (productId, action) => {
    setBusyProductId(productId)
    setItemErrors((current) => ({ ...current, [productId]: null }))
    try {
      const updated = await action()
      setCart(updated)
    } catch (err) {
      const message = err.message || 'Failed to update cart'
      setItemErrors((current) => ({ ...current, [productId]: message }))
      showToast(message, 'error')
    } finally {
      setBusyProductId(null)
    }
  }

  const quantityFor = (productId) => cart?.items.find((item) => item.productId === productId)?.quantity || 0

  const addItem = (productId) => runMutation(productId, () => cartService.addItem(productId, 1))

  const incrementItem = (productId) => {
    const nextQuantity = quantityFor(productId) + 1
    return runMutation(productId, () => cartService.updateItem(productId, nextQuantity))
  }

  const decrementItem = (productId) => {
    const currentQuantity = quantityFor(productId)
    if (currentQuantity <= 1) {
      return runMutation(productId, () => cartService.removeItem(productId))
    }
    return runMutation(productId, () => cartService.updateItem(productId, currentQuantity - 1))
  }

  const removeItem = (productId) => runMutation(productId, () => cartService.removeItem(productId))

  const itemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) || 0

  const value = {
    cart,
    itemCount,
    isOpen,
    openCart: () => setIsOpen(true),
    closeCart: () => setIsOpen(false),
    busyProductId,
    itemErrors,
    addItem,
    incrementItem,
    decrementItem,
    removeItem,
  }

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
