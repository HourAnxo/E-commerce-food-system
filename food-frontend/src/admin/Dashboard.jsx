import { useEffect, useState } from 'react'
import { productApi, orderApi, customerApi } from '../api/services'

function formatMoney(n) {
  return `$${Number(n || 0).toFixed(2)}`
}

export default function Dashboard() {
  const [stats, setStats] = useState({ products: 0, orders: 0, customers: 0, revenue: 0 })
  const [recent, setRecent] = useState([])
  const [customerNames, setCustomerNames] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([productApi.getAll(), orderApi.getAll(), customerApi.getAll()])
      .then(([products, orders, customers]) => {
        const names = Object.fromEntries(
          customers.data.map((c) => [c.customerId, c.fullName]),
        )
        setCustomerNames(names)
        const revenue = orders.data.reduce((sum, o) => sum + Number(o.totalAmount || 0), 0)
        setStats({
          products: products.data.length,
          orders: orders.data.length,
          customers: customers.data.length,
          revenue,
        })
        // Most recent orders first.
        const sorted = [...orders.data].sort(
          (a, b) => new Date(b.orderDate || 0) - new Date(a.orderDate || 0),
        )
        setRecent(sorted.slice(0, 8))
      })
      .catch(() => setError('Could not load dashboard data. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <p className="muted">Loading…</p>
  if (error) return <p className="error-text">{error}</p>

  return (
    <div>
      <h1 className="admin-title">Dashboard</h1>

      <div className="stat-grid">
        <StatCard label="Products" value={stats.products} />
        <StatCard label="Orders" value={stats.orders} />
        <StatCard label="Customers" value={stats.customers} />
        <StatCard label="Revenue" value={formatMoney(stats.revenue)} accent />
      </div>

      <h2 className="admin-subtitle">Recent orders</h2>
      {recent.length === 0 ? (
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
              {recent.map((o) => (
                <tr key={o.orderId}>
                  <td>#{o.orderId}</td>
                  <td>{customerNames[o.customerId] || `Customer ${o.customerId}`}</td>
                  <td>{o.orderDate ? new Date(o.orderDate).toLocaleDateString() : '—'}</td>
                  <td>{formatMoney(o.totalAmount)}</td>
                  <td>
                    <span className={`status status-${o.orderStatus?.toLowerCase()}`}>
                      {o.orderStatus}
                    </span>
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

function StatCard({ label, value, accent }) {
  return (
    <div className={`stat-card ${accent ? 'stat-card-accent' : ''}`}>
      <span className="stat-value">{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  )
}
