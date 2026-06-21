import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import QRCode from 'qrcode'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { orderApi, paymentApi, bakongApi, PAYMENT_METHODS } from '../api/services'

export default function Checkout() {
  const { items, totalPrice, clearCart } = useCart()
  const { customer } = useAuth()
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [paymentMethod, setPaymentMethod] = useState(PAYMENT_METHODS[0])
  // Bakong KHQR state: the pending QR ({ qr, md5, orderId }) and its rendered image.
  const [bakong, setBakong] = useState(null)
  const [qrImage, setQrImage] = useState(null)

  // Render the KHQR string into a QR image whenever a new one is issued.
  useEffect(() => {
    if (!bakong?.qr) return
    QRCode.toDataURL(bakong.qr, { width: 260, margin: 1 })
      .then(setQrImage)
      .catch(() => setError('Could not render the QR code.'))
  }, [bakong])

  // While a Bakong QR is showing, poll the backend until the payment is confirmed.
  useEffect(() => {
    if (!bakong) return
    const timer = setInterval(async () => {
      try {
        const { data } = await bakongApi.checkStatus(bakong.md5, bakong.orderId)
        if (data.status === 'Paid') {
          clearInterval(timer)
          clearCart()
          navigate('/orders', { state: { justOrdered: true } })
        }
      } catch {
        // transient error — keep polling
      }
    }, 3000)
    return () => clearInterval(timer)
  }, [bakong, clearCart, navigate])

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

  if (items.length === 0 && !bakong) {
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
      // Every method starts by creating the order.
      const { data: order } = await orderApi.create({
        customerId: customer.customerId,
        orderDate: new Date().toISOString(),
        totalAmount: Number(totalPrice.toFixed(2)),
        orderStatus: 'Pending',
      })

      if (paymentMethod === 'Bakong') {
        // Generate a KHQR; the backend records a Pending payment and we poll for it.
        const { data: qr } = await bakongApi.generateQr(order.orderId)
        setBakong(qr)
        return
      }

      // Other methods: record the payment immediately.
      await paymentApi.create({
        orderId: order.orderId,
        paymentMethod,
        // Cash on delivery stays Pending; everything else is treated as Paid.
        paymentStatus: paymentMethod === 'Cash' ? 'Pending' : 'Paid',
      })
      clearCart()
      navigate('/orders', { state: { justOrdered: true } })
    } catch {
      setError('Failed to place order. Please try again.')
      setSubmitting(false)
    }
  }

  // Once a Bakong QR is issued, show only the QR + polling state.
  if (bakong) {
    return (
      <div className="checkout">
        <h1 className="section-title">Scan to pay with Bakong</h1>
        <section className="checkout-section bakong-pay">
          {qrImage ? (
            <img src={qrImage} alt="Bakong KHQR" className="bakong-qr" />
          ) : (
            <p className="muted">Generating QR…</p>
          )}
          <p className="bakong-amount">
            <strong>${Number(bakong.amount).toFixed(2)}</strong>
          </p>
          <p className="muted">
            Open any Bakong-supported banking app, scan this code, and confirm.
          </p>
          <p className="muted bakong-waiting">Waiting for payment…</p>
        </section>
      </div>
    )
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

      <section className="checkout-section">
        <h2>Payment method</h2>
        <div className="payment-methods">
          {PAYMENT_METHODS.map((method) => (
            <label key={method} className="payment-option">
              <input
                type="radio"
                name="paymentMethod"
                value={method}
                checked={paymentMethod === method}
                onChange={() => setPaymentMethod(method)}
              />
              <span>{method}</span>
            </label>
          ))}
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
