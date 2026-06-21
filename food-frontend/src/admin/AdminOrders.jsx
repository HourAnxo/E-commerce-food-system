import { useEffect, useState, useCallback } from 'react'
import { orderApi, customerApi, paymentApi, ORDER_STATUSES } from '../api/services'

// How often to re-poll payments so Bakong "Pending → Paid" updates on its own.
const PAYMENT_POLL_MS = 5000

export default function AdminOrders() {
  const [orders, setOrders] = useState([])
  const [customerNames, setCustomerNames] = useState({})
  // Map of orderId -> payment record, so each order shows method + status.
  const [payments, setPayments] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [savingId, setSavingId] = useState(null)

  // Fetch the latest payment for each order and key it by orderId.
  const refreshPayments = useCallback(async (orderList) => {
    const results = await Promise.all(
      orderList.map((order) =>
        paymentApi
          .getByOrder(order.orderId)
          .then((r) => [order.orderId, r.data[r.data.length - 1]])
          .catch(() => [order.orderId, null]),
      ),
    )
    setPayments(Object.fromEntries(results.filter(([, p]) => p)))
  }, [])

  useEffect(() => {
    Promise.all([orderApi.getAll(), customerApi.getAll()])
      .then(([o, c]) => {
        const sorted = [...o.data].sort(
          (a, b) => new Date(b.orderDate || 0) - new Date(a.orderDate || 0),
        )
        setOrders(sorted)
        setCustomerNames(
          Object.fromEntries(c.data.map((cust) => [cust.customerId, cust.fullName])),
        )
        return refreshPayments(sorted)
      })
      .catch(() => setError('Could not load orders. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [refreshPayments])

  // Re-poll just the payment column so Bakong payments flip to Paid on their own.
  useEffect(() => {
    if (orders.length === 0) return
    const timer = setInterval(() => refreshPayments(orders), PAYMENT_POLL_MS)
    return () => clearInterval(timer)
  }, [orders, refreshPayments])

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
                <th>Payment</th>
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
                    {payments[o.orderId] ? (
                      <span
                        className={`status status-${payments[o.orderId].paymentStatus?.toLowerCase()}`}
                      >
                        {payments[o.orderId].paymentMethod} ·{' '}
                        {payments[o.orderId].paymentStatus}
                      </span>
                    ) : (
                      <span className="muted">—</span>
                    )}
                  </td>
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
