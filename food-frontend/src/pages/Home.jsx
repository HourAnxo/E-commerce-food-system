import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { productApi } from '../api/services'
import ProductCard from '../components/ProductCard.jsx'

export default function Home() {
  const [featured, setFeatured] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    productApi
      .getAll()
      .then((res) => setFeatured(res.data.slice(0, 4)))
      .catch(() => setFeatured([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <section className="hero-banner">
        <h1>
          Fresh food, <span className="accent">delivered fast</span>
        </h1>
        <p>Browse our menu and get your favourites delivered to your door.</p>
        <Link to="/products" className="btn btn-primary btn-lg">
          Shop now
        </Link>
      </section>

      <section>
        <h2 className="section-title">Featured products</h2>
        {loading ? (
          <p className="muted">Loading…</p>
        ) : featured.length === 0 ? (
          <p className="muted">No products available yet.</p>
        ) : (
          <div className="product-grid">
            {featured.map((p) => (
              <ProductCard key={p.productId} product={p} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
