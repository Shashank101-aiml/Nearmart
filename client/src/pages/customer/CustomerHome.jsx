import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import * as productService from '../../services/productService'

export default function CustomerHome() {
  const { user, logout } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [expandedId, setExpandedId] = useState(null)

  useEffect(() => {
    productService
      .listAvailable()
      .then(setProducts)
      .catch((err) => setError(err.message || 'Failed to load products'))
      .finally(() => setLoading(false))
  }, [])

  const toggleExpand = (id) => {
    setExpandedId((current) => (current === id ? null : id))
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
        <button type="button" onClick={logout}>
          Log out
        </button>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading products...</p>}
      {!loading && !error && products.length === 0 && <p>No products available yet.</p>}

      <div className="product-grid">
        {products.map((product) => (
          <div className="product-card" key={product.id}>
            <h3>{product.title}</h3>
            <p className="price">${product.price.toFixed(2)}</p>
            <p className="store-name">
              <Link to={`/customer/vendors/${product.vendorId}`}>{product.storeName}</Link>
            </p>
            {expandedId === product.id ? (
              <p className="description">{product.description || 'No description provided.'}</p>
            ) : (
              product.description && (
                <p className="description truncated">{product.description}</p>
              )
            )}
            <button type="button" onClick={() => toggleExpand(product.id)}>
              {expandedId === product.id ? 'Show less' : 'Show more'}
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
