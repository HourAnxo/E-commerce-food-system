import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import ProductImage from './ProductImage.jsx'

export default function ProductCard({ product }) {
  const { addItem } = useCart()
  const outOfStock = product.stockQuantity != null && product.stockQuantity <= 0

  return (
    <div className="product-card">
      <Link to={`/products/${product.productId}`} className="product-card-image">
        <ProductImage src={product.image_url} alt={product.productName} />
      </Link>
      <div className="product-card-body">
        <Link to={`/products/${product.productId}`} className="product-name">
          {product.productName}
        </Link>
        {product.description && (
          <p className="product-card-desc">{product.description}</p>
        )}
        <p className="product-price">${Number(product.price).toFixed(2)}</p>
        <div className="product-card-footer">
          <button
            className="btn btn-primary btn-block"
            disabled={outOfStock}
            onClick={() => addItem(product)}
          >
            {outOfStock ? 'Out of stock' : 'Add to cart'}
          </button>
        </div>
      </div>
    </div>
  )
}
