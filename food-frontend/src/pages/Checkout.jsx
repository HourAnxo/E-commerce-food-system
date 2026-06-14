import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { orderApi } from '../api/services'

export default function Checkout() {
  const { items, totalPrice, clearCart } = useCart()
  const { customer } = useAuth()
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  // Must be logged in to place an order (order requires a customerId).
  if (!customer) {
    return (
      <div className="empty-state">
        <h2>Please log in to checkout</h2>
        <p>You need an account to place an order.</p>
        <Link to="/login" className="btn btn-primary">
          Go to login
        </Link>
      </div>
    )
  }

  if (items.length === 0) {
    return (
      <div className="empty-state">
        <h2>Nothing to checkout</h2>
        <Link to="/products" className="btn btn-primary">
          Browse products
        </Link>
      </div>
    )
  }

  const handlePlaceOrder = async () => {
    setSubmitting(true)
    setError(null)
    try {
      await orderApi.create({
        customerId: customer.customerId,
        orderDate: new Date().toISOString(),
        totalAmount: Number(totalPrice.toFixed(2)),
        orderStatus: 'Pending',
      })
      clearCart()
      navigate('/orders', { state: { justOrdered: true } })
    } catch {
      setError('Failed to place order. Please try again.')
      setSubmitting(false)
    }
  }

  return (
    <div className="checkout">
      <h1 className="section-title">Checkout</h1>

      <section className="checkout-section">
        <h2>Delivery details</h2>
        <p>
          <strong>{customer.fullName}</strong>
        </p>
        <p className="muted">{customer.email}</p>
        {customer.phone && <p className="muted">{customer.phone}</p>}
        <p className="muted">{customer.address || 'No address on file'}</p>
      </section>

      <section className="checkout-section">
        <h2>Order summary</h2>
        <div className="cart-table">
          {items.map((item) => (
            <div key={item.productId} className="cart-row simple">
              <span className="cart-row-info">
                {item.productName} × {item.quantity}
              </span>
              <span className="cart-row-total">
                ${(item.price * item.quantity).toFixed(2)}
              </span>
            </div>
          ))}
        </div>
        <div className="cart-total">
          <span>Total</span>
          <strong>${totalPrice.toFixed(2)}</strong>
        </div>
      </section>

      {error && <p className="error-text">{error}</p>}

      <button
        className="btn btn-primary btn-lg"
        onClick={handlePlaceOrder}
        disabled={submitting}
      >
        {submitting ? 'Placing order…' : 'Place order'}
      </button>
    </div>
  )
}
