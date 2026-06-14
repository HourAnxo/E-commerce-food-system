import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { orderApi, deliveryApi } from '../api/services'

// Human-readable label for the backend enum (Preparing, Shipped, Delivered).
function statusLabel(status) {
  return status ? status.replace(/_/g, ' ') : 'No delivery info yet'
}

const STEPS = ['Preparing', 'Shipped', 'Delivered']

export default function DeliveryTracking() {
  const { customer } = useAuth()
  const location = useLocation()
  const [orders, setOrders] = useState([])
  const [deliveriesByOrder, setDeliveriesByOrder] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!customer) return
    // eslint-disable-next-line react-hooks/set-state-in-effect -- show spinner while loading tracking data
    setLoading(true)
    Promise.all([
      orderApi.getByCustomer(customer.customerId),
      deliveryApi.getAll(),
    ])
      .then(([orderRes, deliveryRes]) => {
        setOrders(orderRes.data)
        // Index deliveries by orderId for quick lookup per order.
        const byOrder = {}
        for (const d of deliveryRes.data) {
          byOrder[d.orderId] = d
        }
        setDeliveriesByOrder(byOrder)
      })
      .catch(() => setError('Could not load delivery information.'))
      .finally(() => setLoading(false))
  }, [customer])

  // Scroll to the deep-linked order (e.g. #order-5) once data has rendered.
  useEffect(() => {
    if (loading || !location.hash) return
    const el = document.querySelector(location.hash)
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [loading, location.hash])

  if (!customer) {
    return (
      <div className="empty-state">
        <h2>Please log in</h2>
        <p>Log in to track your deliveries.</p>
        <Link to="/login" className="btn btn-primary">
          Go to login
        </Link>
      </div>
    )
  }

  return (
    <div>
      <Link to="/orders" className="back-link">
        ← Back to orders
      </Link>
      <h1 className="section-title">Delivery tracking</h1>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : orders.length === 0 ? (
        <div className="empty-state">
          <p>You have no orders to track.</p>
          <Link to="/products" className="btn btn-primary">
            Start shopping
          </Link>
        </div>
      ) : (
        <div className="tracking-list">
          {orders.map((order) => {
            const delivery = deliveriesByOrder[order.orderId]
            const currentStep = delivery
              ? STEPS.indexOf(delivery.deliveryStatus)
              : -1
            return (
              <div key={order.orderId} id={`order-${order.orderId}`} className="tracking-card">
                <div className="tracking-header">
                  <span className="order-id">Order #{order.orderId}</span>
                  <span
                    className={`status status-${(delivery?.deliveryStatus || 'pending').toLowerCase()}`}
                  >
                    {statusLabel(delivery?.deliveryStatus)}
                  </span>
                </div>

                {delivery ? (
                  <>
                    <div className="tracking-steps">
                      {STEPS.map((step, idx) => (
                        <div
                          key={step}
                          className={`tracking-step ${idx <= currentStep ? 'done' : ''}`}
                        >
                          <span className="dot" />
                          <span className="step-label">{step.replace(/_/g, ' ')}</span>
                        </div>
                      ))}
                    </div>
                    <dl className="tracking-details">
                      <div>
                        <dt>Courier</dt>
                        <dd>{delivery.deliveryPerson || '—'}</dd>
                      </div>
                      <div>
                        <dt>Contact</dt>
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
                  </>
                ) : (
                  <p className="muted">
                    No delivery has been scheduled for this order yet.
                  </p>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
