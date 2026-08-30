import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import * as productService from '../../services/productService'
import * as cartService from '../../services/cartService'
import ProductCard from '../../components/common/ProductCard'

export default function VendorStorefront() {
  const { vendorId } = useParams()
  const [vendor, setVendor] = useState(null)
  const [products, setProducts] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [addingId, setAddingId] = useState(null)
  const [addErrors, setAddErrors] = useState({})

  useEffect(() => {
    Promise.all([productService.getVendor(vendorId), productService.listByVendor(vendorId)])
      .then(([vendorData, productData]) => {
        setVendor(vendorData)
        setProducts(productData)
      })
      .catch((err) => setError(err.message || 'Failed to load storefront'))
      .finally(() => setLoading(false))
  }, [vendorId])

  const handleAddToCart = async (product) => {
    setAddingId(product.id)
    setAddErrors((current) => ({ ...current, [product.id]: null }))
    try {
      await cartService.addItem(product.id, 1)
    } catch (err) {
      setAddErrors((current) => ({ ...current, [product.id]: err.message || 'Failed to add to cart' }))
    } finally {
      setAddingId(null)
    }
  }

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          {vendor && <h1>{vendor.storeName}</h1>}
          {vendor?.location && <p>{vendor.location}</p>}
        </div>
        <div className="header-actions">
          <Link to="/customer/cart">Cart</Link>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading storefront...</p>}
      {!loading && !error && products.length === 0 && <p>This store has no products available right now.</p>}

      <div className="product-grid">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            onAddToCart={handleAddToCart}
            adding={addingId === product.id}
            addError={addErrors[product.id]}
          />
        ))}
      </div>
    </div>
  )
}
