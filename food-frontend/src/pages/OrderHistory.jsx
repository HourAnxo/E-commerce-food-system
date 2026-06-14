import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { orderApi } from '../api/services'

export default function OrderHistory() {
  const { customer } = useAuth()
  const location = useLocation()
  const justOrdered = location.state?.justOrdered
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!customer) return
    // eslint-disable-next-line react-hooks/set-state-in-effect -- show spinner while fetching orders
    setLoading(true)
    orderApi
      .getByCustomer(customer.customerId)
      .then((res) => setOrders(res.data))
      .catch(() => setError('Could not load your orders.'))
      .finally(() => setLoading(false))
  }, [customer])

  if (!customer) {
    return (
      <div className="empty-state">
        <h2>Please log in</h2>
        <p>Log in to view your order history.</p>
        <Link to="/login" className="btn btn-primary">
          Go to login
        </Link>
      </div>
    )
  }

  return (
    <div>
      <div className="page-header">
        <h1 className="section-title">Order history</h1>
        <Link to="/deliveries" className="btn btn-primary">
          Track deliveries
        </Link>
      </div>

      {justOrdered && (
        <div className="success-banner">Your order was placed successfully! 🎉</div>
      )}

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : orders.length === 0 ? (
        <div className="empty-state">
          <p>You haven’t placed any orders yet.</p>
          <Link to="/products" className="btn btn-primary">
            Start shopping
          </Link>
        </div>
      ) : (
        <div className="order-list">
          {orders.map((order) => (
            <div key={order.orderId} className="order-card">
              <div>
                <span className="order-id">Order #{order.orderId}</span>
                <span className="muted">
                  {order.orderDate
                    ? new Date(order.orderDate).toLocaleString()
                    : ''}
                </span>
              </div>
              <span className={`status status-${order.orderStatus?.toLowerCase()}`}>
                {order.orderStatus}
              </span>
              <strong>${Number(order.totalAmount).toFixed(2)}</strong>
              <Link to={`/deliveries/${order.orderId}`} className="track-link">
                Track
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
