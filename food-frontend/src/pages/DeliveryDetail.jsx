import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { deliveryApi } from '../api/services'

// Backend enum is Preparing / Shipped / Delivered.
const STEPS = ['Preparing', 'Shipped', 'Delivered']

function statusLabel(status) {
  return status ? status.replace(/_/g, ' ') : '—'
}

export default function DeliveryDetail() {
  const { orderId } = useParams()
  const [delivery, setDelivery] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

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
