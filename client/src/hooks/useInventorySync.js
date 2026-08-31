import { useEffect } from 'react'
import { connectInventory } from '../services/inventorySocket'

export function useInventorySync(setProducts) {
  useEffect(() => {
    const socket = connectInventory(({ productId, stockQuantity }) => {
      setProducts((current) =>
        current.map((product) => (product.id !== productId ? product : { ...product, stockQuantity }))
      )
    })
    return () => socket.close()
  }, [setProducts])
}
