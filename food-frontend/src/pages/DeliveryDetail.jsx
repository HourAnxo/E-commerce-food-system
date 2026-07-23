import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { deliveryApi } from '../api/services'

// Backend enum is Preparing / Shipped / Delivered / Completed / Disputed.
// Disputed is shown as a badge, not a step.
const STEPS = ['Preparing', 'Shipped', 'Delivered', 'Completed']

function statusLabel(status) {
    return status ? status.replace(/_/g, ' ') : '—'
}

export default function DeliveryDetail() {
    const { orderId } = useParams()
    const [delivery, setDelivery] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)
    // ===== NEW =====
    const [acting, setActing] = useState(false)
    const [actionError, setActionError] = useState(null)
    // ===============

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- show spinner while fetching delivery
        setLoading(true)
        setError(null)
        deliveryApi
            .getByOrder(orderId)
            .then((res) => setDelivery(res.data))
            .catch((err) => {
                if (err.response?.status === 404) {
                    setError('No delivery has been scheduled for this order yet.')
                } else {
                    setError('Could not load delivery information.')
                }
            })
            .finally(() => setLoading(false))
    }, [orderId])

    // ===== NEW: customer confirms they received the order =====
    const handleConfirm = async () => {
        setActing(true)
        setActionError(null)
        try {
            const { data } = await deliveryApi.confirm(delivery.deliveryId)
            setDelivery(data)
        } catch (err) {
            setActionError(err.response?.data?.message || 'Could not confirm. Please try again.')
        } finally {
            setActing(false)
        }
    }

    // ===== NEW: customer reports a problem =====
    const handleProblem = async () => {
        setActing(true)
        setActionError(null)
        try {
            const { data } = await deliveryApi.reportProblem(delivery.deliveryId)
            setDelivery(data)
        } catch (err) {
            setActionError(err.response?.data?.message || 'Could not report the problem. Please try again.')
        } finally {
            setActing(false)
        }
    }
    // ===============

    const currentStep = delivery ? STEPS.indexOf(delivery.deliveryStatus) : -1

    return (
        <div>
            <Link to="/orders" className="back-link">
                ← Back to orders
            </Link>
            <h1 className="section-title">Tracking order #{orderId}</h1>

            {loading ? (
                <p className="muted">Loading…</p>
            ) : error ? (
                <div className="empty-state">
                    <p>{error}</p>
                    <Link to="/orders" className="btn btn-primary">
                        Back to orders
                    </Link>
                </div>
            ) : (
                <div className="tracking-card">
                    <div className="tracking-header">
                        <span className="order-id">Delivery #{delivery.deliveryId}</span>
                        <span className={`status status-${delivery.deliveryStatus?.toLowerCase()}`}>
              {statusLabel(delivery.deliveryStatus)}
            </span>
                    </div>

                    <div className="tracking-steps">
                        {STEPS.map((step, idx) => (
                            <div key={step} className={`tracking-step ${idx <= currentStep ? 'done' : ''}`}>
                                <span className="dot" />
                                <span className="step-label">{step.replace(/_/g, ' ')}</span>
                            </div>
                        ))}
                    </div>

                    {/* ===== NEW: delivery code while on the way ===== */}
                    {delivery.deliveryStatus === 'Shipped' && delivery.deliveryCode && (
                        <div className="delivery-code-box">
                            <p className="muted">Give this code to your driver to receive your order:</p>
                            <div className="delivery-code">
                                {delivery.deliveryCode.split('').map((digit, i) => (
                                    <span key={i} className="code-digit">
                    {digit}
                  </span>
                                ))}
                            </div>
                        </div>
                    )}
                    {/* =============== */}

                    {/* ===== NEW: confirm / report once delivered ===== */}
                    {delivery.deliveryStatus === 'Delivered' && (
                        <div className="delivery-confirm-box">
                            <p className="muted">Did you receive your order in good condition?</p>
                            {actionError && <p className="error-text">{actionError}</p>}
                            <div className="delivery-confirm-actions">
                                <button className="btn btn-primary" onClick={handleConfirm} disabled={acting}>
                                    {acting ? 'Please wait…' : 'Yes, I received it'}
                                </button>
                                <button className="btn btn-secondary" onClick={handleProblem} disabled={acting}>
                                    Report a problem
                                </button>
                            </div>
                        </div>
                    )}
                    {/* =============== */}

                    {/* ===== NEW: final state notes ===== */}
                    {delivery.deliveryStatus === 'Completed' && (
                        <p className="muted delivery-final-note">
                            Order completed
                            {delivery.confirmedAt
                                ? ` on ${new Date(delivery.confirmedAt).toLocaleString()}`
                                : ''}
                            . Thank you!
                        </p>
                    )}
                    {delivery.deliveryStatus === 'Disputed' && (
                        <p className="muted delivery-final-note">
                            We received your report. Our team will contact you soon.
                        </p>
                    )}
                    {/* =============== */}

                    <dl className="tracking-details">
                        <div>
                            <dt>Delivery person</dt>
                            <dd>{delivery.deliveryPerson || '—'}</dd>
                        </div>
                        <div>
                            <dt>Phone</dt>
                            <dd>{delivery.deliveryPhone || '—'}</dd>
                        </div>
                        <div>
                            <dt>Address</dt>
                            <dd>{delivery.deliveryAddress || '—'}</dd>
                        </div>
                        <div>
                            <dt>Estimated delivery</dt>
                            <dd>
                                {delivery.estimatedDelivery
                                    ? new Date(delivery.estimatedDelivery).toLocaleString()
                                    : '—'}
                            </dd>
                        </div>
                    </dl>
                </div>
            )}
        </div>
    )
}