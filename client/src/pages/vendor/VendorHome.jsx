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

  const labelClasses = 'flex flex-col gap-1.5 text-sm text-text-h'
  const fieldClasses = 'resize-y rounded-md border border-border bg-bg px-3 py-2.5 text-text-h'
  const checkboxLabelClasses = 'flex flex-row items-center gap-2 text-sm text-text-h'
  const submitButtonClasses =
    'self-start cursor-pointer rounded-md border-none bg-accent px-4 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-60'
  const cardButtonClasses = 'cursor-pointer rounded-md border border-border bg-bg px-2.5 py-1.5 text-text-h'

  return (
    <div className="flex-1 px-8 pt-6 pb-12 text-left">
      <header className="mb-6 flex items-start justify-between gap-4">
        <div>
          <h1 className="m-0 mb-1 text-[28px] text-left">Vendor Dashboard</h1>
          <p>
            Signed in as <strong>{user.username}</strong> ({user.role})
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/vendor/orders" className="text-sm text-text-h underline">
            Orders
          </Link>
          <button
            type="button"
            onClick={logout}
            className="cursor-pointer whitespace-nowrap rounded-md border border-border bg-bg px-3.5 py-2 text-text-h"
          >
            Log out
          </button>
        </div>
      </header>

      {error && <p className="auth-error">{error}</p>}

      <section className="mb-8">
        <h2>Add a product</h2>
        <form onSubmit={handleCreateSubmit} className="flex max-w-[420px] flex-col gap-3 text-left">
          <label className={labelClasses}>
            Title
            <input
              name="title"
              value={createForm.title}
              onChange={handleCreateChange}
              required
              className={fieldClasses}
            />
          </label>
          <label className={labelClasses}>
            Description
            <textarea
              name="description"
              value={createForm.description}
              onChange={handleCreateChange}
              className={fieldClasses}
            />
          </label>
          <label className={labelClasses}>
            Price
            <input
              name="price"
              type="number"
              step="0.01"
              min="0.01"
              value={createForm.price}
              onChange={handleCreateChange}
              required
              className={fieldClasses}
            />
          </label>
          <label className={labelClasses}>
            Stock
            <input
              name="stockQuantity"
              type="number"
              step="1"
              min="0"
              value={createForm.stockQuantity}
              onChange={handleCreateChange}
              required
              className={fieldClasses}
            />
          </label>
          <label className={checkboxLabelClasses}>
            <input
              name="available"
              type="checkbox"
              checked={createForm.available}
              onChange={handleCreateChange}
            />
            Available
          </label>
          <button type="submit" disabled={creating} className={submitButtonClasses}>
            {creating ? 'Adding...' : 'Add product'}
          </button>
        </form>
      </section>

      <section>
        <h2>My products</h2>
        {loading && <p>Loading products...</p>}
        {!loading && products.length === 0 && <p>You haven't added any products yet.</p>}

        <div className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
          {products.map((product) =>
            editingId === product.id ? (
              <form
                key={product.id}
                className="flex flex-col gap-3 rounded-lg border border-border bg-bg p-4 text-left"
                onSubmit={(e) => handleEditSubmit(e, product.id)}
              >
                <label className={labelClasses}>
                  Title
                  <input
                    name="title"
                    value={editForm.title}
                    onChange={handleEditChange}
                    required
                    className={fieldClasses}
                  />
                </label>
                <label className={labelClasses}>
                  Description
                  <textarea
                    name="description"
                    value={editForm.description}
                    onChange={handleEditChange}
                    className={fieldClasses}
                  />
                </label>
                <label className={labelClasses}>
                  Price
                  <input
                    name="price"
                    type="number"
                    step="0.01"
                    min="0.01"
                    value={editForm.price}
                    onChange={handleEditChange}
                    required
                    className={fieldClasses}
                  />
                </label>
                <label className={labelClasses}>
                  Stock
                  <input
                    name="stockQuantity"
                    type="number"
                    step="1"
                    min="0"
                    value={editForm.stockQuantity}
                    onChange={handleEditChange}
                    required
                    className={fieldClasses}
                  />
                </label>
                <label className={checkboxLabelClasses}>
                  <input
                    name="available"
                    type="checkbox"
                    checked={editForm.available}
                    onChange={handleEditChange}
                  />
                  Available
                </label>
                <div className="flex flex-wrap gap-2">
                  <button type="submit" disabled={saving} className={submitButtonClasses}>
                    {saving ? 'Saving...' : 'Save'}
                  </button>
                  <button type="button" onClick={cancelEdit}>
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <div className="flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4" key={product.id}>
                <h3 className="m-0 text-lg text-text-h">{product.title}</h3>
                <p className="font-semibold text-accent">${product.price.toFixed(2)}</p>
                {product.description && <p className="text-sm text-text">{product.description}</p>}
                <p>Stock: {product.stockQuantity}</p>
                <p
                  className={`self-start rounded-full px-2 py-0.5 text-xs ${
                    product.available ? 'bg-accent-bg text-accent' : 'bg-code-bg text-text'
                  }`}
                >
                  {product.available ? 'Available' : 'Hidden'}
                </p>
                <div className="flex flex-wrap gap-2">
                  <button type="button" onClick={() => startEdit(product)} className={cardButtonClasses}>
                    Edit
                  </button>
                  <button
                    type="button"
                    onClick={() => handleToggleAvailable(product)}
                    className={cardButtonClasses}
                  >
                    {product.available ? 'Hide' : 'Unhide'}
                  </button>
                  <button type="button" onClick={() => handleDelete(product.id)} className={cardButtonClasses}>
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
