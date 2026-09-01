import { Link } from 'react-router-dom'
import DiscountBadge from './DiscountBadge'
import PriceDisplay from './PriceDisplay'
import AddToCartControl from './AddToCartControl'

export default function ProductCardV2({ product, quantityInCart = 0, onAdd, onIncrement, onDecrement, showStoreLink = false }) {
  const outOfStock = !product.stockQuantity || product.stockQuantity <= 0
  const discountPercent = product.mrp && product.mrp > product.price
    ? ((product.mrp - product.price) / product.mrp) * 100
    : 0

  return (
    <div className="flex flex-col gap-2 rounded-lg border border-border bg-bg p-3">
      <div className="relative aspect-square w-full overflow-hidden rounded-md bg-code-bg">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.title} className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-xs text-text">No image</div>
        )}
        {discountPercent > 0 && (
          <div className="absolute top-1.5 left-1.5">
            <DiscountBadge percent={discountPercent} />
          </div>
        )}
      </div>

      <h3 className="m-0 line-clamp-2 text-sm text-text-h">{product.title}</h3>
      {product.unit && <p className="text-xs text-text">{product.unit}</p>}

      {showStoreLink && product.vendorId && (
        <Link to={`/customer/vendors/${product.vendorId}`} className="text-xs text-text-h underline">
          {product.storeName}
        </Link>
      )}

      <div className="mt-auto flex items-center justify-between gap-2">
        <PriceDisplay price={product.price} mrp={product.mrp} />
        {outOfStock ? (
          <span className="text-xs text-text">Out of stock</span>
        ) : (
          <AddToCartControl
            quantity={quantityInCart}
            onAdd={() => onAdd?.(product)}
            onIncrement={() => onIncrement?.(product)}
            onDecrement={() => onDecrement?.(product)}
          />
        )}
      </div>
    </div>
  )
}
