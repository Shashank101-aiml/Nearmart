import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import * as productService from '../../services/productService'
import DiscountBadge from '../../components/common/DiscountBadge'
import PriceDisplay from '../../components/common/PriceDisplay'
import AddToCartControl from '../../components/common/AddToCartControl'
import ProductCardV2 from '../../components/common/ProductCardV2'
import { useCart } from '../../hooks/useCart'

export default function ProductDetailPage() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [relatedProducts, setRelatedProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { cart, busyProductId, itemErrors, addItem, incrementItem, decrementItem } = useCart()

  useEffect(() => {
    productService
      .getById(id)
      .then((data) => {
        setProduct(data)
        return productService.listByVendor(data.vendorId)
      })
      .then((vendorProducts) => setRelatedProducts(vendorProducts.filter((p) => String(p.id) !== id)))
      .catch(() => setError('Product not found'))
      .finally(() => setLoading(false))
  }, [id])

  const quantityFor = (productId) => cart?.items.find((item) => item.productId === productId)?.quantity || 0

  if (loading) {
    return (
      <div className="flex-1 px-8 pt-6 pb-12 text-left">
        <p>Loading product...</p>
      </div>
    )
  }

  if (error || !product) {
    return (
      <div className="flex-1 px-8 pt-6 pb-12 text-left">
        <div className="mb-6">
          <Link to="/customer">&larr; Back to catalog</Link>
        </div>
        <p className="auth-error">{error || 'Product not found'}</p>
      </div>
    )
  }

  const outOfStock = !product.stockQuantity || product.stockQuantity <= 0
  const discountPercent = product.mrp && product.mrp > product.price
    ? ((product.mrp - product.price) / product.mrp) * 100
    : 0

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <Link to="/customer">&larr; Back to catalog</Link>
      </div>

      <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
        <div className="relative aspect-square w-full overflow-hidden rounded-lg bg-code-bg">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.title} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-sm text-text">No image</div>
          )}
          {discountPercent > 0 && (
            <div className="absolute top-2 left-2">
              <DiscountBadge percent={discountPercent} />
            </div>
          )}
        </div>

        <div className="flex flex-col gap-3">
          <h1 className="m-0 text-2xl text-text-h">{product.title}</h1>
          {product.unit && <p className="text-sm text-text">{product.unit}</p>}
          {product.vendorId && (
            <Link to={`/customer/vendors/${product.vendorId}`} className="text-sm text-text-h underline">
              {product.storeName}
            </Link>
          )}
          {product.description && <p className="text-text">{product.description}</p>}

          <div className="mt-2 flex items-center gap-4">
            <PriceDisplay price={product.price} mrp={product.mrp} />
            {outOfStock ? (
              <span className="text-sm text-text">Out of stock</span>
            ) : (
              <AddToCartControl
                quantity={quantityFor(product.id)}
                onAdd={() => addItem(product.id)}
                onIncrement={() => incrementItem(product.id)}
                onDecrement={() => decrementItem(product.id)}
                disabled={busyProductId === product.id}
              />
            )}
          </div>
          {itemErrors[product.id] && <p className="field-error">{itemErrors[product.id]}</p>}
        </div>
      </div>

      {relatedProducts.length > 0 && (
        <div className="mt-10">
          <h2 className="mb-4 text-lg text-text-h">More from this store</h2>
          <div className="grid grid-cols-[repeat(auto-fill,minmax(200px,1fr))] gap-4">
            {relatedProducts.map((relatedProduct) => (
              <ProductCardV2
                key={relatedProduct.id}
                product={relatedProduct}
                description={relatedProduct.description}
                quantityInCart={quantityFor(relatedProduct.id)}
                onAdd={(p) => addItem(p.id)}
                onIncrement={(p) => incrementItem(p.id)}
                onDecrement={(p) => decrementItem(p.id)}
                disabled={busyProductId === relatedProduct.id}
                error={itemErrors[relatedProduct.id]}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
