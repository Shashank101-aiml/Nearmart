import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import * as productService from '../../services/productService'
import * as cartService from '../../services/cartService'
import ProductCard from '../../components/common/ProductCard'
import { useInventorySync } from '../../hooks/useInventorySync'

export default function CustomerHome() {
  const { user, logout } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [addingId, setAddingId] = useState(null)
  const [addErrors, setAddErrors] = useState({})

  useInventorySync(setProducts)

  useEffect(() => {
    productService
      .listAvailable()
      .then(setProducts)
      .catch((err) => setError(err.message || 'Failed to load products'))
      .finally(() => setLoading(false))
  }, [])

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
          <h1>Browse products</h1>
          <p>
            Signed in as <strong>{user.username}</strong> ({user.role})
          </p>
        </div>
        <div className="header-actions">
          <Link to="/customer/orders">Orders</Link>
          <Link to="/customer/notifications">Notifications</Link>
          <Link to="/customer/cart">Cart</Link>
          <button type="button" onClick={logout}>
            Log out
          </button>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading products...</p>}
      {!loading && !error && products.length === 0 && <p>No products available yet.</p>}

      <div className="product-grid">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            showStoreLink
            onAddToCart={handleAddToCart}
            adding={addingId === product.id}
            addError={addErrors[product.id]}
          />
        ))}
      </div>
    </div>
  )
}
