import { useState } from 'react'
import { Link } from 'react-router-dom'

export default function ProductCard({ product, showStoreLink = false, onAddToCart, adding, addError }) {
  const [expanded, setExpanded] = useState(false)

  const outOfStock = !product.stockQuantity || product.stockQuantity <= 0
  const canAddToCart = Boolean(onAddToCart) && product.available !== false && !outOfStock

  return (
    <div className="flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4">
      <h3 className="m-0 text-lg text-text-h">{product.title}</h3>
      <p className="font-semibold text-accent">${product.price.toFixed(2)}</p>
      {showStoreLink && product.vendorId && (
        <p>
          <Link to={`/customer/vendors/${product.vendorId}`} className="text-text-h underline">
            {product.storeName}
          </Link>
        </p>
      )}

      {product.description && (
        <p className={`text-sm text-text ${expanded ? '' : 'line-clamp-2'}`}>{product.description}</p>
      )}
      {product.description && (
        <button
          type="button"
          onClick={() => setExpanded((current) => !current)}
          className="self-start cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
        >
          {expanded ? 'Show less' : 'Show more'}
        </button>
      )}

      <p
        className={`self-start rounded-full px-2 py-0.5 text-xs ${
          outOfStock ? 'bg-code-bg text-text' : 'bg-accent-bg text-accent'
        }`}
      >
        {outOfStock ? 'Out of stock' : `${product.stockQuantity} in stock`}
      </p>

      {onAddToCart && (
        <button
          type="button"
          onClick={() => onAddToCart(product)}
          disabled={!canAddToCart || adding}
          className="self-start cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h"
        >
          {adding ? 'Adding...' : 'Add to cart'}
        </button>
      )}
      {addError && <p className="field-error">{addError}</p>}
    </div>
  )
}
