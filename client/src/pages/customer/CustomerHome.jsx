import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import * as productService from '../../services/productService'
import * as cartService from '../../services/cartService'
import ProductCard from '../../components/common/ProductCard'
import ProductFilters from '../../components/common/ProductFilters'
import { useInventorySync } from '../../hooks/useInventorySync'

export default function CustomerHome() {
  const { user, logout } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [addingId, setAddingId] = useState(null)
  const [addErrors, setAddErrors] = useState({})

  const [searchTerm, setSearchTerm] = useState('')
  const [selectedVendorId, setSelectedVendorId] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [inStockOnly, setInStockOnly] = useState(false)

  useInventorySync(setProducts)

  useEffect(() => {
    productService
      .listAvailable()
      .then(setProducts)
      .catch((err) => setError(err.message || 'Failed to load products'))
      .finally(() => setLoading(false))
  }, [])

  const vendorOptions = useMemo(() => {
    const seen = new Map()
    for (const product of products) {
      if (product.vendorId && !seen.has(product.vendorId)) {
        seen.set(product.vendorId, product.storeName)
      }
    }
    return Array.from(seen.entries()).map(([id, name]) => ({ id, name }))
  }, [products])

  const hasActiveFilters = Boolean(searchTerm || selectedVendorId || minPrice || maxPrice || inStockOnly)

  const filteredProducts = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    const min = minPrice === '' ? null : Number(minPrice)
    const max = maxPrice === '' ? null : Number(maxPrice)

    return products.filter((product) => {
      if (term) {
        const haystack = `${product.title} ${product.description || ''}`.toLowerCase()
        if (!haystack.includes(term)) return false
      }
      if (selectedVendorId && String(product.vendorId) !== String(selectedVendorId)) return false
      if (min !== null && product.price < min) return false
      if (max !== null && product.price > max) return false
      if (inStockOnly && !(product.stockQuantity > 0)) return false
      return true
    })
  }, [products, searchTerm, selectedVendorId, minPrice, maxPrice, inStockOnly])

  const clearFilters = () => {
    setSearchTerm('')
    setSelectedVendorId('')
    setMinPrice('')
    setMaxPrice('')
    setInStockOnly(false)
  }

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

      {!loading && !error && products.length > 0 && (
        <ProductFilters
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
          vendorOptions={vendorOptions}
          selectedVendorId={selectedVendorId}
          onVendorChange={setSelectedVendorId}
          minPrice={minPrice}
          maxPrice={maxPrice}
          onMinPriceChange={setMinPrice}
          onMaxPriceChange={setMaxPrice}
          inStockOnly={inStockOnly}
          onInStockOnlyChange={setInStockOnly}
          onClear={clearFilters}
          hasActiveFilters={hasActiveFilters}
        />
      )}

      {!loading && !error && products.length > 0 && filteredProducts.length === 0 && (
        <p>No products match your filters.</p>
      )}

      <div className="product-grid">
        {filteredProducts.map((product) => (
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
