import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import ProductImage from '../components/ProductImage.jsx'

export default function Cart() {
  const { items, updateQuantity, removeItem, totalPrice, clearCart } = useCart()
  const navigate = useNavigate()

  if (items.length === 0) {
    return (
      <div className="empty-state">
        <h2>Your cart is empty</h2>
        <p>Add some products to get started.</p>
        <Link to="/products" className="btn btn-primary">
          Browse products
        </Link>
      </div>
    )
  }

  return (
    <div>
      <h1 className="section-title">Your cart</h1>

      <div className="cart-table">
        {items.map((item) => (
          <div key={item.productId} className="cart-row">
            <div className="cart-thumb">
              <ProductImage src={item.image_url} alt={item.productName} />
            </div>
            <div className="cart-row-info">
              <span className="product-name">{item.productName}</span>
              <span className="muted">${item.price.toFixed(2)} each</span>
            </div>
            <input
              type="number"
              min="1"
              className="qty-input"
              value={item.quantity}
              onChange={(e) => updateQuantity(item.productId, Number(e.target.value))}
            />
            <span className="cart-row-total">
              ${(item.price * item.quantity).toFixed(2)}
            </span>
            <button
              className="link-button danger"
              onClick={() => removeItem(item.productId)}
            >
              Remove
            </button>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <button className="link-button" onClick={clearCart}>
          Clear cart
        </button>
        <div className="cart-total">
          <span>Total</span>
          <strong>${totalPrice.toFixed(2)}</strong>
        </div>
        <button className="btn btn-primary btn-lg" onClick={() => navigate('/checkout')}>
          Proceed to checkout
        </button>
      </div>
    </div>
  )
}
