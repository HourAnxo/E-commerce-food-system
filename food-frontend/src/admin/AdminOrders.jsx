import { useEffect, useState } from 'react'
import { orderApi, customerApi, ORDER_STATUSES } from '../api/services'

export default function AdminOrders() {
  const [orders, setOrders] = useState([])
  const [customerNames, setCustomerNames] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [savingId, setSavingId] = useState(null)

  useEffect(() => {
    Promise.all([orderApi.getAll(), customerApi.getAll()])
      .then(([o, c]) => {
        setOrders(
          [...o.data].sort(
            (a, b) => new Date(b.orderDate || 0) - new Date(a.orderDate || 0),
          ),
        )
        setCustomerNames(
          Object.fromEntries(c.data.map((cust) => [cust.customerId, cust.fullName])),
        )
      })
      .catch(() => setError('Could not load orders. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  const handleStatusChange = async (order, status) => {
    setSavingId(order.orderId)
    try {
      // PUT overwrites the whole order — send the full DTO with the new status.
      const res = await orderApi.update(order.orderId, { ...order, orderStatus: status })
      setOrders((prev) =>
        prev.map((o) => (o.orderId === order.orderId ? res.data : o)),
      )
    } catch {
      alert('Could not update the order status.')
    } finally {
      setSavingId(null)
    }
  }

  return (
    <div>
      <h1 className="admin-title">Orders</h1>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : orders.length === 0 ? (
        <p className="muted">No orders yet.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Order</th>
                <th>Customer</th>
                <th>Date</th>
                <th>Total</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.orderId}>
                  <td>#{o.orderId}</td>
                  <td>{customerNames[o.customerId] || `Customer ${o.customerId}`}</td>
                  <td>{o.orderDate ? new Date(o.orderDate).toLocaleString() : '—'}</td>
                  <td>${Number(o.totalAmount).toFixed(2)}</td>
                  <td>
                    <select
                      className={`status-select status-${o.orderStatus?.toLowerCase()}`}
                      value={o.orderStatus}
                      disabled={savingId === o.orderId}
                      onChange={(e) => handleStatusChange(o, e.target.value)}
                    >
                      {ORDER_STATUSES.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
