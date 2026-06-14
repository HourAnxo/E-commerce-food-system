import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { productApi } from '../api/services'
import { useCart } from '../context/CartContext.jsx'
import ProductImage from '../components/ProductImage.jsx'

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addItem } = useCart()
  const [product, setProduct] = useState(null)
  const [quantity, setQuantity] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- show spinner while (re)fetching on id change
    setLoading(true)
    productApi
      .getById(id)
      .then((res) => setProduct(res.data))
      .catch(() => setError('Product not found.'))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <p className="muted">Loading…</p>
  if (error) return <p className="error-text">{error}</p>
  if (!product) return null

  const outOfStock = product.stockQuantity != null && product.stockQuantity <= 0

  const handleAdd = () => {
    addItem(product, quantity)
    navigate('/cart')
  }

  return (
    <div>
      <Link to="/products" className="back-link">
        ← Back to products
      </Link>

      <div className="product-detail">
        <div className="product-detail-image">
          <ProductImage src={product.image_url} alt={product.productName} />
        </div>

        <div className="product-detail-info">
          <h1>{product.productName}</h1>
          <p className="product-price-lg">${Number(product.price).toFixed(2)}</p>
          <p className="product-description">
            {product.description || 'No description available.'}
          </p>
          <p className={outOfStock ? 'error-text' : 'muted'}>
            {outOfStock
              ? 'Out of stock'
              : `In stock: ${product.stockQuantity ?? 'available'}`}
          </p>

          {!outOfStock && (
            <div className="quantity-row">
              <label htmlFor="qty">Quantity</label>
              <input
                id="qty"
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
              />
            </div>
          )}

          <button
            className="btn btn-primary btn-lg"
            disabled={outOfStock}
            onClick={handleAdd}
          >
            {outOfStock ? 'Out of stock' : 'Add to cart'}
          </button>
        </div>
      </div>
    </div>
  )
}
