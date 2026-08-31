import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import * as productService from '../../services/productService'
import * as cartService from '../../services/cartService'
import ProductCard from '../../components/common/ProductCard'
import ProductFilters from '../../components/common/ProductFilters'
import { useInventorySync } from '../../hooks/useInventorySync'

export default function VendorStorefront() {
  const { vendorId } = useParams()
  const [vendor, setVendor] = useState(null)
  const [products, setProducts] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [addingId, setAddingId] = useState(null)
  const [addErrors, setAddErrors] = useState({})

  const [searchTerm, setSearchTerm] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [inStockOnly, setInStockOnly] = useState(false)

  useInventorySync(setProducts)

  useEffect(() => {
    Promise.all([productService.getVendor(vendorId), productService.listByVendor(vendorId)])
      .then(([vendorData, productData]) => {
        setVendor(vendorData)
        setProducts(productData)
      })
      .catch((err) => setError(err.message || 'Failed to load storefront'))
      .finally(() => setLoading(false))
  }, [vendorId])

  const hasActiveFilters = Boolean(searchTerm || minPrice || maxPrice || inStockOnly)

  const filteredProducts = useMemo(() => {
    const term = searchTerm.trim().toLowerCase()
    const min = minPrice === '' ? null : Number(minPrice)
    const max = maxPrice === '' ? null : Number(maxPrice)

    return products.filter((product) => {
      if (term) {
        const haystack = `${product.title} ${product.description || ''}`.toLowerCase()
        if (!haystack.includes(term)) return false
      }
      if (min !== null && product.price < min) return false
      if (max !== null && product.price > max) return false
      if (inStockOnly && !(product.stockQuantity > 0)) return false
      return true
    })
  }, [products, searchTerm, minPrice, maxPrice, inStockOnly])

  const clearFilters = () => {
    setSearchTerm('')
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
          <Link to="/customer">&larr; Back to catalog</Link>
          {vendor && <h1>{vendor.storeName}</h1>}
          {vendor?.location && <p>{vendor.location}</p>}
        </div>
        <div className="header-actions">
          <Link to="/customer/orders">Orders</Link>
          <Link to="/customer/cart">Cart</Link>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading storefront...</p>}
      {!loading && !error && products.length === 0 && <p>This store has no products available right now.</p>}

      {!loading && !error && products.length > 0 && (
        <ProductFilters
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
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
            onAddToCart={handleAddToCart}
            adding={addingId === product.id}
            addError={addErrors[product.id]}
          />
        ))}
      </div>
    </div>
  )
}
