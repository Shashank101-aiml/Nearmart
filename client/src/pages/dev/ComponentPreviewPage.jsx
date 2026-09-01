import { useState } from 'react'
import ProductCardV2 from '../../components/common/ProductCardV2'
import DiscountBadge from '../../components/common/DiscountBadge'
import PriceDisplay from '../../components/common/PriceDisplay'
import QuantityStepper from '../../components/common/QuantityStepper'
import AddToCartControl from '../../components/common/AddToCartControl'

const mockProducts = [
  {
    id: 1,
    title: 'Onion (Pyaz)',
    imageUrl: '',
    price: 55,
    mrp: 66,
    unit: '1 kg',
    stockQuantity: 20,
    storeName: 'Fresh Vendor Store',
    vendorId: 1,
  },
  {
    id: 2,
    title: 'Basmati Rice Premium Long Grain',
    imageUrl: '',
    price: 180,
    mrp: null,
    unit: '5 kg',
    stockQuantity: 12,
    storeName: 'Grain Vendor Store',
    vendorId: 2,
  },
  {
    id: 3,
    title: 'Whole Milk',
    imageUrl: '',
    price: 32,
    mrp: 34,
    unit: '500 ml',
    stockQuantity: 0,
    storeName: 'Dairy Vendor Store',
    vendorId: 3,
  },
  {
    id: 4,
    title: 'Green Chilli (Hari Mirch)',
    imageUrl: '',
    price: 12,
    mrp: 14,
    unit: '100 g',
    stockQuantity: 30,
    storeName: 'Fresh Vendor Store',
    vendorId: 1,
  },
]

export default function ComponentPreviewPage() {
  const [cart, setCart] = useState({})
  const [standaloneQty, setStandaloneQty] = useState(2)

  const handleAdd = (product) => setCart((current) => ({ ...current, [product.id]: 1 }))
  const handleIncrement = (product) =>
    setCart((current) => ({ ...current, [product.id]: (current[product.id] || 0) + 1 }))
  const handleDecrement = (product) =>
    setCart((current) => {
      const next = (current[product.id] || 0) - 1
      const updated = { ...current }
      if (next <= 0) {
        delete updated[product.id]
      } else {
        updated[product.id] = next
      }
      return updated
    })

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <h1 className="m-0 mb-1 text-[28px] text-left">Component preview (temporary — dev only)</h1>
      <p className="text-sm text-text">Not linked from any nav. Deleted once slice 4 wires real pages to these components.</p>

      <h2 className="mt-8">ProductCardV2</h2>
      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(200px,1fr))] gap-4">
        {mockProducts.map((product) => (
          <ProductCardV2
            key={product.id}
            product={product}
            quantityInCart={cart[product.id] || 0}
            onAdd={handleAdd}
            onIncrement={handleIncrement}
            onDecrement={handleDecrement}
            showStoreLink
          />
        ))}
      </div>

      <h2 className="mt-8">DiscountBadge</h2>
      <div className="mt-2 flex items-center gap-3">
        <DiscountBadge percent={16} />
        <DiscountBadge percent={0} />
        <span className="text-xs text-text">(second one renders nothing — no discount)</span>
      </div>

      <h2 className="mt-8">PriceDisplay</h2>
      <div className="mt-2 flex items-center gap-6">
        <PriceDisplay price={55} mrp={66} />
        <PriceDisplay price={180} />
      </div>

      <h2 className="mt-8">QuantityStepper (standalone, controlled here)</h2>
      <div className="mt-2">
        <QuantityStepper
          quantity={standaloneQty}
          onIncrement={() => setStandaloneQty((q) => q + 1)}
          onDecrement={() => setStandaloneQty((q) => Math.max(0, q - 1))}
        />
      </div>

      <h2 className="mt-8">AddToCartControl (standalone)</h2>
      <div className="mt-2">
        <AddToCartControl
          quantity={standaloneQty}
          onAdd={() => setStandaloneQty(1)}
          onIncrement={() => setStandaloneQty((q) => q + 1)}
          onDecrement={() => setStandaloneQty((q) => Math.max(0, q - 1))}
        />
      </div>
    </div>
  )
}
