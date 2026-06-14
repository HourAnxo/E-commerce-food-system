import { useEffect, useState } from 'react'
import { productApi, categoryApi } from '../api/services'
import ProductCard from '../components/ProductCard.jsx'

export default function ProductList() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [activeCategory, setActiveCategory] = useState('all')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Load categories once for the filter bar.
  useEffect(() => {
    categoryApi
      .getAll()
      .then((res) => setCategories(res.data))
      .catch(() => setCategories([]))
  }, [])

  // Reload products whenever the active category changes.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- show spinner while (re)fetching on category change
    setLoading(true)
    setError(null)
    const request =
      activeCategory === 'all'
        ? productApi.getAll()
        : productApi.getByCategory(activeCategory)

    request
      .then((res) => setProducts(res.data))
      .catch(() => setError('Could not load products. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [activeCategory])

  const visible = products.filter((p) =>
    p.productName?.toLowerCase().includes(search.toLowerCase()),
  )

  return (
    <div>
      <h1 className="section-title">Products</h1>

      <div className="toolbar">
        <input
          type="text"
          className="search-input"
          placeholder="Search products…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <div className="category-filters">
          <button
            className={`chip ${activeCategory === 'all' ? 'chip-active' : ''}`}
            onClick={() => setActiveCategory('all')}
          >
            All
          </button>
          {categories.map((c) => (
            <button
              key={c.categoryId}
              className={`chip ${activeCategory === c.categoryId ? 'chip-active' : ''}`}
              onClick={() => setActiveCategory(c.categoryId)}
            >
              {c.categoryName}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : visible.length === 0 ? (
        <p className="muted">No products found.</p>
      ) : (
        <div className="product-grid">
          {visible.map((p) => (
            <ProductCard key={p.productId} product={p} />
          ))}
        </div>
      )}
    </div>
  )
}
