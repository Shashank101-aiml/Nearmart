import { useState } from 'react'
import { Link } from 'react-router-dom'

export default function ProductCard({ product, showStoreLink = false, onAddToCart, adding, addError }) {
  const [expanded, setExpanded] = useState(false)

  const outOfStock = !product.stockQuantity || product.stockQuantity <= 0
  const canAddToCart = Boolean(onAddToCart) && product.available !== false && !outOfStock

  return (
    <div className="product-card">
      <h3>{product.title}</h3>
      <p className="price">${product.price.toFixed(2)}</p>
      {showStoreLink && product.vendorId && (
        <p className="store-name">
          <Link to={`/customer/vendors/${product.vendorId}`}>{product.storeName}</Link>
        </p>
      )}

      {product.description && (
        <p className={expanded ? 'description' : 'description truncated'}>{product.description}</p>
      )}
      {product.description && (
        <button type="button" onClick={() => setExpanded((current) => !current)}>
          {expanded ? 'Show less' : 'Show more'}
        </button>
      )}

      <p className={outOfStock ? 'badge badge-hidden' : 'badge badge-available'}>
        {outOfStock ? 'Out of stock' : `${product.stockQuantity} in stock`}
      </p>

      {onAddToCart && (
        <button type="button" onClick={() => onAddToCart(product)} disabled={!canAddToCart || adding}>
          {adding ? 'Adding...' : 'Add to cart'}
        </button>
      )}
      {addError && <p className="field-error">{addError}</p>}
    </div>
  )
}
