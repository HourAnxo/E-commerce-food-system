import { useState, useEffect, useCallback } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import QRCode from 'qrcode'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { orderApi, bakongApi } from '../api/services'

// Must match KhqrGenerator.QR_LIFETIME_MS on the backend: bank apps refuse an expired KHQR.
const QR_LIFETIME_MS = 15 * 60 * 1000

// Bakong KHQR is the only payment method. The backend builds a real KHQR for the
// order total; any Bakong-member bank app (ABA, ACLEDA, Wing, ...) can scan it, and
// we poll the backend, which asks Bakong whether that exact QR (by MD5) was paid.
export default function Checkout() {
    const { items, totalPrice, clearCart } = useCart()
    const { customer } = useAuth()
    const navigate = useNavigate()
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState(null)
    // Order created on the first attempt. Reused if QR generation fails and the customer
    // retries, so a retry doesn't create (and take stock for) a second order.
    const [orderId, setOrderId] = useState(null)
    // The pending QR ({ qr, md5, orderId, amount }) and its rendered image.
    const [bakong, setBakong] = useState(null)
    const [qrImage, setQrImage] = useState(null)
    const [expiresAt, setExpiresAt] = useState(null)
    const [expired, setExpired] = useState(false)

    const issueQr = useCallback(async (id) => {
        const { data: qr } = await bakongApi.generateQr(id)
        setBakong(qr)
        setExpiresAt(Date.now() + QR_LIFETIME_MS)
        setExpired(false)
        setError(null)
    }, [])

    // Render the KHQR string into a QR image whenever a new one is issued.
    useEffect(() => {
        if (!bakong?.qr) return
        QRCode.toDataURL(bakong.qr, { width: 260, margin: 1 })
            .then(setQrImage)
            .catch(() => setError('Could not render the QR code.'))
    }, [bakong])

    // Mark the QR expired when its lifetime runs out.
    useEffect(() => {
        if (!expiresAt) return
        const timer = setTimeout(() => setExpired(true), Math.max(0, expiresAt - Date.now()))
        return () => clearTimeout(timer)
    }, [expiresAt])

    // While a live QR is showing, poll the backend until Bakong confirms the payment.
    useEffect(() => {
        if (!bakong || expired) return
        const timer = setInterval(async () => {
            try {
                const { data } = await bakongApi.checkStatus(bakong.md5, bakong.orderId)
                if (data.status === 'Paid') {
                    clearInterval(timer)
                    clearCart()
                    navigate('/orders', { state: { justOrdered: true } })
                }
            } catch (err) {
                // A 5xx from our backend means Bakong is misconfigured/unreachable — stop
                // polling and show why. Anything else is transient, so keep polling.
                if (err.response && err.response.status >= 500) {
                    clearInterval(timer)
                    setError(err.response.data?.message || 'Could not verify the Bakong payment.')
                }
            }
        }, 3000)
        return () => clearInterval(timer)
    }, [bakong, expired, clearCart, navigate])

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
        let id = orderId
        try {
            if (!id) {
                // The line items let the backend deduct stock; price is re-read
                // server-side, so only product and quantity are sent.
                const { data: order } = await orderApi.create({
                    customerId: customer.customerId,
                    orderDate: new Date().toISOString(),
                    totalAmount: Number(totalPrice.toFixed(2)),
                    orderStatus: 'Pending',
                    items: items.map((i) => ({
                        productId: i.productId,
                        quantity: i.quantity,
                    })),
                })
                id = order.orderId
                setOrderId(id)
            }
            await issueQr(id)
        } catch (err) {
            // 409 = an item ran out of stock; 503 = Bakong not configured on the server.
            setError(err.response?.data?.message || 'Could not start the Bakong payment. Please try again.')
        } finally {
            setSubmitting(false)
        }
    }

    const handleRegenerate = async () => {
        setSubmitting(true)
        setQrImage(null)
        try {
            await issueQr(bakong.orderId)
        } catch (err) {
            setError(err.response?.data?.message || 'Could not generate a new QR.')
        } finally {
            setSubmitting(false)
        }
    }

    // Once a QR is issued, show only the QR + polling state.
    if (bakong) {
        return (
            <div className="checkout">
                <h1 className="section-title">Scan to pay with Bakong</h1>
                <section className="checkout-section bakong-pay">
                    {expired ? (
                        <p className="muted">This QR has expired.</p>
                    ) : qrImage ? (
                        <img src={qrImage} alt="Bakong KHQR" className="bakong-qr" />
                    ) : (
                        <p className="muted">Generating QR…</p>
                    )}
                    <p className="bakong-amount">
                        <strong>${Number(bakong.amount).toFixed(2)}</strong>
                    </p>
                    <p className="muted">Order #{bakong.orderId}</p>
                    <p className="muted">
                        Open any Bakong-supported banking app (ABA, ACLEDA, Wing, …), scan this code,
                        and confirm.
                    </p>
                    {error ? (
                        <p className="error-text">{error}</p>
                    ) : expired ? (
                        <button className="btn btn-primary" onClick={handleRegenerate} disabled={submitting}>
                            {submitting ? 'Generating…' : 'Generate new QR'}
                        </button>
                    ) : (
                        <p className="muted bakong-waiting">Waiting for payment…</p>
                    )}
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
                    <label className="payment-option">
                        <input type="radio" name="paymentMethod" value="Bakong" checked readOnly />
                        <span className="bank-chip bakong">KHQR</span>
                        <span>Bakong KHQR</span>
                    </label>
                </div>
            </section>

            {error && <p className="error-text">{error}</p>}

            <button
                className="btn btn-primary btn-lg"
                onClick={handlePlaceOrder}
                disabled={submitting}
            >
                {submitting ? 'Preparing payment…' : orderId ? 'Retry payment' : 'Place order & pay'}
            </button>
        </div>
    )
}
