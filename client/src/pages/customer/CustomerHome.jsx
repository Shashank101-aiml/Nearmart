import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import * as productService from '../../services/productService'
import ProductCardV2 from '../../components/common/ProductCardV2'
import ProductFilters from '../../components/common/ProductFilters'
import CategoryGrid from '../../components/common/CategoryGrid'
import { useInventorySync } from '../../hooks/useInventorySync'
import { useCart } from '../../hooks/useCart'

export default function CustomerHome() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const { cart, busyProductId, itemErrors, addItem, incrementItem, decrementItem } = useCart()
  const [searchParams] = useSearchParams()

  const [searchTerm, setSearchTerm] = useState(() => searchParams.get('q') || '')
  const [selectedVendorId, setSelectedVendorId] = useState('')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [inStockOnly, setInStockOnly] = useState(false)
  const [selectedCategory, setSelectedCategory] = useState(() => searchParams.get('category') || '')

  useInventorySync(setProducts)

  useEffect(() => {
    const q = searchParams.get('q')
    if (q) Promise.resolve().then(() => setSearchTerm(q))
    const category = searchParams.get('category')
    if (category) Promise.resolve().then(() => setSelectedCategory(category))
  }, [searchParams])

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

  const hasActiveFilters = Boolean(
    searchTerm || selectedVendorId || minPrice || maxPrice || inStockOnly || selectedCategory
  )

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
      if (selectedCategory && product.category !== selectedCategory) return false
      return true
    })
  }, [products, searchTerm, selectedVendorId, minPrice, maxPrice, inStockOnly, selectedCategory])

  const clearFilters = () => {
    setSearchTerm('')
    setSelectedVendorId('')
    setMinPrice('')
    setMaxPrice('')
    setInStockOnly(false)
    setSelectedCategory('')
  }

  const quantityFor = (productId) => cart?.items.find((item) => item.productId === productId)?.quantity || 0

  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <div className="mb-6">
        <h1 className="m-0 mb-1 text-[28px] text-left">Browse products</h1>
      </div>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading products...</p>}
      {!loading && !error && products.length === 0 && <p>No products available yet.</p>}

      {!loading && !error && products.length > 0 && (
        <CategoryGrid selectedCategory={selectedCategory} onSelect={setSelectedCategory} />
      )}

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

      <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(200px,1fr))] gap-4">
        {filteredProducts.map((product) => (
          <ProductCardV2
            key={product.id}
            product={product}
            description={product.description}
            quantityInCart={quantityFor(product.id)}
            onAdd={(p) => addItem(p.id)}
            onIncrement={(p) => incrementItem(p.id)}
            onDecrement={(p) => decrementItem(p.id)}
            showStoreLink
            disabled={busyProductId === product.id}
            error={itemErrors[product.id]}
          />
        ))}
      </div>
    </div>
  )
}
