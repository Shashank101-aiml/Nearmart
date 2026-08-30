import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import * as productService from '../../services/productService'

export default function VendorStorefront() {
  const { vendorId } = useParams()
  const [vendor, setVendor] = useState(null)
  const [products, setProducts] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([productService.getVendor(vendorId), productService.listByVendor(vendorId)])
      .then(([vendorData, productData]) => {
        setVendor(vendorData)
        setProducts(productData)
      })
      .catch((err) => setError(err.message || 'Failed to load storefront'))
      .finally(() => setLoading(false))
  }, [vendorId])

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <Link to="/customer">&larr; Back to catalog</Link>
          {vendor && <h1>{vendor.storeName}</h1>}
          {vendor?.location && <p>{vendor.location}</p>}
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}
      {loading && <p>Loading storefront...</p>}
      {!loading && !error && products.length === 0 && <p>This store has no products available right now.</p>}

      <div className="product-grid">
        {products.map((product) => (
          <div className="product-card" key={product.id}>
            <h3>{product.title}</h3>
            <p className="price">${product.price.toFixed(2)}</p>
            {product.description && <p className="description">{product.description}</p>}
          </div>
        ))}
      </div>
    </div>
  )
}
