import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { deliveryApi } from '../api/services'

// The driver's page. Reached by opening the single-use link the admin sends
// (/delivery/respond/{acceptToken}) — there is no driver login, the token in
// the URL is the only thing identifying them.
//
// Accepting starts the delivery (-> Shipped) and returns the 6-digit code the
// customer will read out; entering that code finishes the job (-> Delivered).
const seenKey = (token) => `foodapp.driver.${token}`

export default function DeliveryRespond() {
    const { token } = useParams()
    const [delivery, setDelivery] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    const [acting, setActing] = useState(false)
    const [actionError, setActionError] = useState(null)
    const [code, setCode] = useState('')

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect -- show spinner while resolving the token
        setLoading(true)
        setError(null)
        deliveryApi
            .getByToken(token)
            .then((res) => {
                // Accepting clears the token, so remember which delivery this
                // link pointed at — otherwise reloading the page after accepting
                // would lock the driver out before they can enter the code.
                localStorage.setItem(seenKey(token), String(res.data.deliveryId))
                setDelivery(res.data)
            })
            .catch((err) => {
                if (err.response?.status !== 404) {
                    setError('Could not load this delivery. Please try again.')
                    return null
                }
                const knownId = localStorage.getItem(seenKey(token))
                if (!knownId) {
                    setError('This link is no longer valid. It may have already been used.')
                    return null
                }
                return deliveryApi
                    .getById(knownId)
                    .then((res) => setDelivery(res.data))
                    .catch(() => setError('This link is no longer valid.'))
            })
            .finally(() => setLoading(false))
    }, [token])

    // Wraps the accept/decline/complete calls: they all replace the delivery and
    // surface the backend's reason (409 when someone already answered the offer).
    const run = async (call) => {
        setActing(true)
        setActionError(null)
        try {
            const { data } = await call()
            setDelivery(data)
        } catch (err) {
            setActionError(err.response?.data?.message || 'Something went wrong. Please try again.')
        } finally {
            setActing(false)
        }
    }

    const handleAccept = () => run(() => deliveryApi.accept(delivery.deliveryId))
    const handleDecline = () => run(() => deliveryApi.decline(delivery.deliveryId))

    const handleComplete = (e) => {
        e.preventDefault()
        return run(() => deliveryApi.complete(delivery.deliveryId, code.trim()))
    }

    if (loading) {
        return (
            <div className="driver-page">
                <p className="muted">Loading…</p>
            </div>
        )
    }

    if (error) {
        return (
            <div className="driver-page">
                <div className="auth-card">
                    <h1>Delivery offer</h1>
                    <p className="error-text">{error}</p>
                </div>
            </div>
        )
    }

    return (
        <div className="driver-page">
            <div className="auth-card">
                <div className="tracking-header">
                    <span className="order-id">Delivery #{delivery.deliveryId}</span>
                    <span className={`status status-${delivery.deliveryStatus?.toLowerCase()}`}>
                        {delivery.deliveryStatus}
                    </span>
                </div>

                <dl className="tracking-details">
                    <div>
                        <dt>Order</dt>
                        <dd>#{delivery.orderId}</dd>
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
                    <div>
                        <dt>Offered to</dt>
                        <dd>{delivery.deliveryPerson || '—'}</dd>
                    </div>
                </dl>

                {actionError && <p className="error-text">{actionError}</p>}

                {delivery.deliveryStatus === 'Assigned' && (
                    <div className="delivery-confirm-box">
                        <p className="muted">Can you take this delivery?</p>
                        <div className="delivery-confirm-actions">
                            <button className="btn btn-primary" onClick={handleAccept} disabled={acting}>
                                {acting ? 'Please wait…' : 'Accept'}
                            </button>
                            <button className="btn btn-secondary" onClick={handleDecline} disabled={acting}>
                                Decline
                            </button>
                        </div>
                    </div>
                )}

                {delivery.deliveryStatus === 'Shipped' && (
                    <div className="delivery-confirm-box">
                        <p className="muted">
                            You are on the way. Ask the customer for their 6-digit code to finish.
                        </p>
                        <form className="complete-delivery" onSubmit={handleComplete}>
                            <input
                                className="code-input"
                                inputMode="numeric"
                                maxLength={6}
                                placeholder="000000"
                                value={code}
                                onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
                            />
                            <button
                                type="submit"
                                className="btn btn-primary"
                                disabled={acting || code.trim().length !== 6}
                            >
                                {acting ? 'Please wait…' : 'Confirm delivery'}
                            </button>
                        </form>
                    </div>
                )}

                {delivery.deliveryStatus === 'Preparing' && (
                    <p className="muted delivery-final-note">
                        You turned this delivery down. It has gone back to the pool.
                    </p>
                )}

                {['Delivered', 'Completed'].includes(delivery.deliveryStatus) && (
                    <p className="muted delivery-final-note">
                        Delivered
                        {delivery.deliveredAt
                            ? ` on ${new Date(delivery.deliveredAt).toLocaleString()}`
                            : ''}
                        . Thanks!
                    </p>
                )}
            </div>
        </div>
    )
}
