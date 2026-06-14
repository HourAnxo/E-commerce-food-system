import { useEffect, useState } from 'react'
import { productApi, categoryApi } from '../api/services'
import ProductImage from '../components/ProductImage.jsx'

const EMPTY_FORM = {
  productName: '',
  price: '',
  description: '',
  image_url: '',
  categoryId: '',
  stockQuantity: '',
}

export default function AdminProducts() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [modalOpen, setModalOpen] = useState(false)
  const [editingId, setEditingId] = useState(null) // null = creating
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState(null)

  const load = () => {
    setLoading(true)
    Promise.all([productApi.getAll(), categoryApi.getAll()])
      .then(([p, c]) => {
        setProducts(p.data)
        setCategories(c.data)
      })
      .catch(() => setError('Could not load products. Is the backend running?'))
      .finally(() => setLoading(false))
  }

  // eslint-disable-next-line react-hooks/set-state-in-effect -- initial data load toggles the loading flag
  useEffect(load, [])

  const categoryName = (id) =>
    categories.find((c) => c.categoryId === id)?.categoryName || '—'

  const openCreate = () => {
    setEditingId(null)
    setForm(EMPTY_FORM)
    setFormError(null)
    setModalOpen(true)
  }

  const openEdit = (product) => {
    setEditingId(product.productId)
    setForm({
      productName: product.productName ?? '',
      price: product.price ?? '',
      description: product.description ?? '',
      image_url: product.image_url ?? '',
      categoryId: product.categoryId ?? '',
      stockQuantity: product.stockQuantity ?? '',
    })
    setFormError(null)
    setModalOpen(true)
  }

  const handleChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.categoryId) {
      setFormError('Please select a category.')
      return
    }
    setSaving(true)
    setFormError(null)
    const payload = {
      productName: form.productName.trim(),
      description: form.description.trim() || null,
      price: Number(form.price),
      stockQuantity: form.stockQuantity === '' ? null : Number(form.stockQuantity),
      categoryId: Number(form.categoryId),
      image_url: form.image_url.trim() || null,
    }
    try {
      if (editingId) {
        await productApi.update(editingId, payload)
      } else {
        await productApi.create(payload)
      }
      setModalOpen(false)
      load()
    } catch {
      setFormError('Could not save the product. Check the fields and try again.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (product) => {
    if (!window.confirm(`Delete "${product.productName}"? This cannot be undone.`)) return
    try {
      await productApi.remove(product.productId)
      setProducts((prev) => prev.filter((p) => p.productId !== product.productId))
    } catch {
      alert('Could not delete the product.')
    }
  }

  return (
    <div>
      <div className="admin-page-header">
        <h1 className="admin-title">Products</h1>
        <button className="btn btn-primary" onClick={openCreate}>
          + Add product
        </button>
      </div>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : products.length === 0 ? (
        <p className="muted">No products yet.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Image</th>
                <th>Name</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Category</th>
                <th className="col-actions">Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.productId}>
                  <td>
                    <div className="table-thumb">
                      <ProductImage src={p.image_url} alt={p.productName} />
                    </div>
                  </td>
                  <td className="cell-strong">{p.productName}</td>
                  <td>${Number(p.price).toFixed(2)}</td>
                  <td>{p.stockQuantity ?? '—'}</td>
                  <td>{categoryName(p.categoryId)}</td>
                  <td className="col-actions">
                    <button className="link-button" onClick={() => openEdit(p)}>
                      Edit
                    </button>
                    <button className="link-button danger" onClick={() => handleDelete(p)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {modalOpen && (
        <div className="modal-overlay" onClick={() => setModalOpen(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{editingId ? 'Edit product' : 'Add product'}</h2>
            <form onSubmit={handleSubmit} className="form-grid">
              <label className="form-full">
                Name
                <input name="productName" required value={form.productName} onChange={handleChange} />
              </label>

              <label>
                Price
                <input
                  name="price"
                  type="number"
                  step="0.01"
                  min="0"
                  required
                  value={form.price}
                  onChange={handleChange}
                />
              </label>

              <label>
                Stock quantity
                <input
                  name="stockQuantity"
                  type="number"
                  min="0"
                  value={form.stockQuantity}
                  onChange={handleChange}
                  placeholder="(blank = unlimited)"
                />
              </label>

              <label>
                Category
                <select name="categoryId" required value={form.categoryId} onChange={handleChange}>
                  <option value="">Select…</option>
                  {categories.map((c) => (
                    <option key={c.categoryId} value={c.categoryId}>
                      {c.categoryName}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Image URL
                <input
                  name="image_url"
                  type="url"
                  value={form.image_url}
                  onChange={handleChange}
                  placeholder="https://…"
                />
              </label>

              <label className="form-full">
                Description
                <textarea
                  name="description"
                  rows="3"
                  value={form.description}
                  onChange={handleChange}
                />
              </label>

              {formError && <p className="error-text form-full">{formError}</p>}

              <div className="modal-actions form-full">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setModalOpen(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save changes' : 'Create product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
