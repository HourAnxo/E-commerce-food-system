import { useEffect, useState } from 'react'
import { customerApi } from '../api/services'

export default function AdminCustomers() {
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    customerApi
      .getAll()
      .then((res) => setCustomers(res.data))
      .catch(() => setError('Could not load customers. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <h1 className="admin-title">Customers</h1>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : customers.length === 0 ? (
        <p className="muted">No customers yet.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Address</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((c) => (
                <tr key={c.customerId}>
                  <td>#{c.customerId}</td>
                  <td className="cell-strong">{c.fullName}</td>
                  <td>{c.email}</td>
                  <td>{c.phone || '—'}</td>
                  <td>{c.address || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
