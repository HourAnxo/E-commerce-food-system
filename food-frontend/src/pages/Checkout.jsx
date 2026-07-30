import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import QRCode from 'qrcode'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { orderApi, paymentApi, bakongApi, PAYMENT_METHODS } from '../api/services'

// Banks that use the in-app "account ID or QR" payment screen. Keys are the
// backend Payment.PaymentMethod enum values (note the ACELEDA spelling).
const BANKS = {
    ABA: { label: 'ABA Bank', short: 'ABA', className: 'aba', app: 'ABA Mobile' },
    ACELEDA: { label: 'ACLEDA Bank', short: 'ACLEDA', className: 'aceleda', app: 'ACLEDA Mobile' },
}

// Chip look for every payment method (real brand colors live in CSS).
const METHOD_CHIPS = {
    ABA: { short: 'ABA', className: 'aba' },
    ACELEDA: { short: 'ACLEDA', className: 'aceleda' },
    Bakong: { short: 'KHQR', className: 'bakong' },
    Wing: { short: 'Wing', className: 'wing' },
    'Credit Card': { short: 'CARD', className: 'credit' },
    CreditCard: { short: 'CARD', className: 'credit' },
    Cash: { short: '$', className: 'cash' },
}

const METHOD_LABELS = { ABA: 'ABA Bank', ACELEDA: 'ACLEDA Bank' }

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
    // ABA/ACLEDA bank payment state: { bank, orderId, amount } once the order exists.
    const [bankPay, setBankPay] = useState(null)
    const [payMode, setPayMode] = useState('account') // 'account' | 'qr'
    const [accountId, setAccountId] = useState('')
    const [accountName, setAccountName] = useState('')
    const [bankQr, setBankQr] = useState(null)
    const [confirming, setConfirming] = useState(false)

    // Render the KHQR string into a QR image whenever a new one is issued.
    useEffect(() => {
        if (!bakong?.qr) return
        QRCode.toDataURL(bakong.qr, { width: 260, margin: 1 })
            .then(setQrImage)
            .catch(() => setError('Could not render the QR code.'))
    }, [bakong])

    // Render a demo QR for the ABA/ACLEDA screen (fake payload — nothing is charged).
    useEffect(() => {
        if (!bankPay) return
        const payload = `KHQR|DEMO|CHIN MINGHOUR|${bankPay.bank}|ORDER:${bankPay.orderId}|USD:${bankPay.amount}`
        QRCode.toDataURL(payload, { width: 260, margin: 1 })
            .then(setBankQr)
            .catch(() => setError('Could not render the QR code.'))
    }, [bankPay])

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

    if (items.length === 0 && !bakong && !bankPay) {
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
            // Every method starts by creating the order. The line items are what let
            // the backend deduct stock — the price is re-read server-side, so only the
            // product and quantity are sent.
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

            if (paymentMethod === 'Bakong') {
                // Generate a KHQR; the backend records a Pending payment and we poll for it.
                try {
                    const { data: qr } = await bakongApi.generateQr(order.orderId)
                    setBakong(qr)
                } catch (err) {
                    setError(err.response?.data?.message || 'Bakong payment is unavailable right now.')
                    setSubmitting(false)
                }
                return
            }

            if (BANKS[paymentMethod]) {
                // ABA/ACLEDA: switch to the bank payment screen; the payment is recorded
                // against the backend once the user confirms there.
                setBankPay({
                    bank: paymentMethod,
                    orderId: order.orderId,
                    amount: Number(totalPrice.toFixed(2)),
                })
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
        } catch (err) {
            // A 409 means an item ran out of stock — show which one rather than a
            // generic failure the customer cannot act on.
            setError(err.response?.data?.message || 'Failed to place order. Please try again.')
            setSubmitting(false)
        }
    }

    const cancelBankPay = () => {
        setBankPay(null)
        setBankQr(null)
        setPayMode('account')
        setAccountId('')
        setAccountName('')
        setError(null)
        setSubmitting(false)
    }

    const handleConfirmBankPay = async () => {
        setError(null)
        if (payMode === 'account') {
            if (!/^\d{6,20}$/.test(accountId.trim())) {
                setError('Enter a valid account ID (6–20 digits).')
                return
            }
            if (!accountName.trim()) {
                setError('Enter the account holder name.')
                return
            }
        }
        setConfirming(true)
        try {
            // Simulated bank processing delay (demo — no real charge happens).
            await new Promise((resolve) => setTimeout(resolve, 1500))
            await paymentApi.create({
                orderId: bankPay.orderId,
                paymentMethod: bankPay.bank,
                paymentStatus: 'Paid',
            })
            clearCart()
            navigate('/orders', { state: { justOrdered: true } })
        } catch {
            setError('Payment failed. Please try again.')
            setConfirming(false)
        }
    }

    // ABA/ACLEDA payment screen: pay by account ID or by (demo) QR.
    if (bankPay) {
        const bank = BANKS[bankPay.bank]
        const amount = Number(bankPay.amount).toFixed(2)
        return (
            <div className="checkout">
                <h1 className="section-title">Pay with {bank.label}</h1>
                <section className="checkout-section bank-pay">
                    <div className={`bank-pay-header ${bank.className}`}>
                        <span className="bank-logo">{bank.short}</span>
                        <div className="bank-pay-meta">
                            <strong>{bank.label}</strong>
                            <p className="muted">Order #{bankPay.orderId}</p>
                        </div>
                        <span className="bank-pay-amount">${amount}</span>
                    </div>

                    <div className="pay-tabs">
                        <button
                            type="button"
                            className={payMode === 'account' ? 'pay-tab active' : 'pay-tab'}
                            onClick={() => setPayMode('account')}
                        >
                            Account ID
                        </button>
                        <button
                            type="button"
                            className={payMode === 'qr' ? 'pay-tab active' : 'pay-tab'}
                            onClick={() => setPayMode('qr')}
                        >
                            Scan QR
                        </button>
                    </div>

                    {payMode === 'account' ? (
                        <div className="bank-form">
                            <label>
                                {bank.label} account ID
                                <input
                                    type="text"
                                    inputMode="numeric"
                                    placeholder="e.g. 000123456"
                                    value={accountId}
                                    onChange={(e) => setAccountId(e.target.value)}
                                />
                            </label>
                            <label>
                                Account holder name
                                <input
                                    type="text"
                                    placeholder="Name on the account"
                                    value={accountName}
                                    onChange={(e) => setAccountName(e.target.value)}
                                />
                            </label>
                        </div>
                    ) : (
                        <div className="bank-qr-wrap">
                            {bankQr ? (
                                <img src={bankQr} alt={`${bank.label} QR`} className="bakong-qr" />
                            ) : (
                                <p className="muted">Generating QR…</p>
                            )}
                            <p className="muted">
                                Scan with the {bank.app} app, then tap “I’ve paid”. (Demo QR — no real
                                charge.)
                            </p>
                        </div>
                    )}

                    {error && <p className="error-text">{error}</p>}

                    <div className="bank-pay-actions">
                        <button className="btn btn-secondary" onClick={cancelBankPay} disabled={confirming}>
                            Back
                        </button>
                        <button
                            className="btn btn-primary"
                            onClick={handleConfirmBankPay}
                            disabled={confirming}
                        >
                            {confirming
                                ? 'Processing…'
                                : payMode === 'qr'
                                    ? 'I’ve paid'
                                    : `Pay $${amount}`}
                        </button>
                    </div>
                </section>
            </div>
        )
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
                    {error ? (
                        <p className="error-text">{error}</p>
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
                    {PAYMENT_METHODS.map((method) => (
                        <label key={method} className="payment-option">
                            <input
                                type="radio"
                                name="paymentMethod"
                                value={method}
                                checked={paymentMethod === method}
                                onChange={() => setPaymentMethod(method)}
                            />
                            {METHOD_CHIPS[method] ? (
                                <span className={`bank-chip ${METHOD_CHIPS[method].className}`}>
                  {METHOD_CHIPS[method].short}
                </span>
                            ) : null}
                            <span>{METHOD_LABELS[method] ?? method}</span>
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