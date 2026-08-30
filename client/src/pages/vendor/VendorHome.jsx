import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import * as productService from '../../services/productService'

const emptyForm = { title: '', description: '', price: '', stockQuantity: '', available: true }

export default function VendorHome() {
  const { user, logout } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [createForm, setCreateForm] = useState(emptyForm)
  const [creating, setCreating] = useState(false)

  const [editingId, setEditingId] = useState(null)
  const [editForm, setEditForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    productService
      .listMine()
      .then(setProducts)
      .catch((err) => setError(err.message || 'Failed to load products'))
      .finally(() => setLoading(false))
  }, [])

  const handleCreateChange = (e) => {
    const { name, value, type, checked } = e.target
    setCreateForm({ ...createForm, [name]: type === 'checkbox' ? checked : value })
  }

  const handleCreateSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setCreating(true)
    try {
      const created = await productService.createProduct({
        title: createForm.title,
        description: createForm.description,
        price: Number(createForm.price),
        stockQuantity: Number(createForm.stockQuantity),
        available: createForm.available,
      })
      setProducts([created, ...products])
      setCreateForm(emptyForm)
    } catch (err) {
      setError(err.message || 'Failed to create product')
    } finally {
      setCreating(false)
    }
  }

  const startEdit = (product) => {
    setEditingId(product.id)
    setEditForm({
      title: product.title,
      description: product.description || '',
      price: String(product.price),
      stockQuantity: String(product.stockQuantity ?? 0),
      available: product.available,
    })
  }

  const cancelEdit = () => {
    setEditingId(null)
    setEditForm(emptyForm)
  }

  const handleEditChange = (e) => {
    const { name, value, type, checked } = e.target
    setEditForm({ ...editForm, [name]: type === 'checkbox' ? checked : value })
  }

  const handleEditSubmit = async (e, productId) => {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const updated = await productService.updateProduct(productId, {
        title: editForm.title,
        description: editForm.description,
        price: Number(editForm.price),
        stockQuantity: Number(editForm.stockQuantity),
        available: editForm.available,
      })
      setProducts(products.map((p) => (p.id === productId ? updated : p)))
      cancelEdit()
    } catch (err) {
      setError(err.message || 'Failed to update product')
    } finally {
      setSaving(false)
    }
  }

  const handleToggleAvailable = async (product) => {
    setError('')
    try {
      const updated = await productService.updateProduct(product.id, {
        title: product.title,
        description: product.description,
        price: product.price,
        stockQuantity: product.stockQuantity,
        available: !product.available,
      })
      setProducts(products.map((p) => (p.id === product.id ? updated : p)))
    } catch (err) {
      setError(err.message || 'Failed to update availability')
    }
  }

  const handleDelete = async (productId) => {
    if (!window.confirm('Delete this product?')) return
    setError('')
    try {
      await productService.deleteProduct(productId)
      setProducts(products.filter((p) => p.id !== productId))
    } catch (err) {
      setError(err.message || 'Failed to delete product')
    }
  }

  return (
    <div className="catalog-page">
      <header className="catalog-header">
        <div>
          <h1>Vendor Dashboard</h1>
          <p>
            Signed in as <strong>{user.username}</strong> ({user.role})
          </p>
        </div>
        <div className="header-actions">
          <Link to="/vendor/orders">Orders</Link>
          <button type="button" onClick={logout}>
            Log out
          </button>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}

      <section className="product-form-section">
        <h2>Add a product</h2>
        <form onSubmit={handleCreateSubmit} className="product-form">
          <label>
            Title
            <input name="title" value={createForm.title} onChange={handleCreateChange} required />
          </label>
          <label>
            Description
            <textarea name="description" value={createForm.description} onChange={handleCreateChange} />
          </label>
          <label>
            Price
            <input
              name="price"
              type="number"
              step="0.01"
              min="0.01"
              value={createForm.price}
              onChange={handleCreateChange}
              required
            />
          </label>
          <label>
            Stock
            <input
              name="stockQuantity"
              type="number"
              step="1"
              min="0"
              value={createForm.stockQuantity}
              onChange={handleCreateChange}
              required
            />
          </label>
          <label className="checkbox-label">
            <input
              name="available"
              type="checkbox"
              checked={createForm.available}
              onChange={handleCreateChange}
            />
            Available
          </label>
          <button type="submit" disabled={creating}>
            {creating ? 'Adding...' : 'Add product'}
          </button>
        </form>
      </section>

      <section>
        <h2>My products</h2>
        {loading && <p>Loading products...</p>}
        {!loading && products.length === 0 && <p>You haven't added any products yet.</p>}

        <div className="product-grid">
          {products.map((product) =>
            editingId === product.id ? (
              <form
                key={product.id}
                className="product-card product-form"
                onSubmit={(e) => handleEditSubmit(e, product.id)}
              >
                <label>
                  Title
                  <input name="title" value={editForm.title} onChange={handleEditChange} required />
                </label>
                <label>
                  Description
                  <textarea name="description" value={editForm.description} onChange={handleEditChange} />
                </label>
                <label>
                  Price
                  <input
                    name="price"
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={editForm.price}
                    onChange={handleEditChange}
                    required
                  />
                </label>
                <label>
                  Stock
                  <input
                    name="stockQuantity"
                    type="number"
                    step="1"
                    min="0"
                    value={editForm.stockQuantity}
                    onChange={handleEditChange}
                    required
                  />
                </label>
                <label className="checkbox-label">
                  <input
                    name="available"
                    type="checkbox"
                    checked={editForm.available}
                    onChange={handleEditChange}
                  />
                  Available
                </label>
                <div className="product-actions">
                  <button type="submit" disabled={saving}>
                    {saving ? 'Saving...' : 'Save'}
                  </button>
                  <button type="button" onClick={cancelEdit}>
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <div className="product-card" key={product.id}>
                <h3>{product.title}</h3>
                <p className="price">${product.price.toFixed(2)}</p>
                {product.description && <p className="description">{product.description}</p>}
                <p>Stock: {product.stockQuantity}</p>
                <p className={product.available ? 'badge badge-available' : 'badge badge-hidden'}>
                  {product.available ? 'Available' : 'Hidden'}
                </p>
                <div className="product-actions">
                  <button type="button" onClick={() => startEdit(product)}>
                    Edit
                  </button>
                  <button type="button" onClick={() => handleToggleAvailable(product)}>
                    {product.available ? 'Hide' : 'Unhide'}
                  </button>
                  <button type="button" onClick={() => handleDelete(product.id)}>
                    Delete
                  </button>
                </div>
              </div>
            )
          )}
        </div>
      </section>
    </div>
  )
}
