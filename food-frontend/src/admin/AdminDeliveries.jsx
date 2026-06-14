import { useEffect, useState } from 'react'
import { deliveryApi, orderApi, DELIVERY_STATUSES } from '../api/services'

const EMPTY_FORM = {
  orderId: '',
  deliveryPerson: '',
  deliveryPhone: '',
  deliveryAddress: '',
  estimatedDelivery: '',
}

export default function AdminDeliveries() {
  const [deliveries, setDeliveries] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [savingId, setSavingId] = useState(null)

  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState(null)

  const load = () => {
    setLoading(true)
    Promise.all([deliveryApi.getAll(), orderApi.getAll()])
      .then(([d, o]) => {
        setDeliveries(d.data)
        setOrders(o.data)
      })
      .catch(() => setError('Could not load deliveries. Is the backend running?'))
      .finally(() => setLoading(false))
  }

  // eslint-disable-next-line react-hooks/set-state-in-effect -- initial data load toggles the loading flag
  useEffect(load, [])

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleStatusChange = async (delivery, status) => {
    setSavingId(delivery.deliveryId)
    try {
      const res = await deliveryApi.update(delivery.deliveryId, {
        ...delivery,
        deliveryStatus: status,
      })
      setDeliveries((prev) =>
        prev.map((d) => (d.deliveryId === delivery.deliveryId ? res.data : d)),
      )
    } catch {
      alert('Could not update the delivery status.')
    } finally {
      setSavingId(null)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.orderId) {
      setFormError('Please select an order.')
      return
    }
    setSaving(true)
    setFormError(null)
    try {
      await deliveryApi.create({
        orderId: Number(form.orderId),
        deliveryPerson: form.deliveryPerson.trim(),
        deliveryPhone: form.deliveryPhone.trim(),
        deliveryAddress: form.deliveryAddress.trim(),
        deliveryStatus: 'Preparing',
        estimatedDelivery: form.estimatedDelivery || null,
      })
      setModalOpen(false)
      setForm(EMPTY_FORM)
      load()
    } catch {
      setFormError('Could not create the delivery. Check the fields and try again.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div className="admin-page-header">
        <h1 className="admin-title">Deliveries</h1>
        <button
          className="btn btn-primary"
          onClick={() => {
            setForm(EMPTY_FORM)
            setFormError(null)
            setModalOpen(true)
          }}
        >
          + Add delivery
        </button>
      </div>

      {loading ? (
        <p className="muted">Loading…</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : deliveries.length === 0 ? (
        <p className="muted">No deliveries yet.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Order</th>
                <th>Courier</th>
                <th>Phone</th>
                <th>Address</th>
                <th>Est. delivery</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {deliveries.map((d) => (
                <tr key={d.deliveryId}>
                  <td>#{d.deliveryId}</td>
                  <td>#{d.orderId}</td>
                  <td className="cell-strong">{d.deliveryPerson}</td>
                  <td>{d.deliveryPhone}</td>
                  <td>{d.deliveryAddress}</td>
                  <td>
                    {d.estimatedDelivery
                      ? new Date(d.estimatedDelivery).toLocaleString()
                      : '—'}
                  </td>
                  <td>
                    <select
                      className={`status-select status-${d.deliveryStatus?.toLowerCase()}`}
                      value={d.deliveryStatus}
                      disabled={savingId === d.deliveryId}
                      onChange={(e) => handleStatusChange(d, e.target.value)}
                    >
                      {DELIVERY_STATUSES.map((s) => (
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

      {modalOpen && (
        <div className="modal-overlay" onClick={() => setModalOpen(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Add delivery</h2>
            <form onSubmit={handleSubmit} className="form-grid">
              <label className="form-full">
                Order
                <select name="orderId" required value={form.orderId} onChange={handleChange}>
                  <option value="">Select an order…</option>
                  {orders.map((o) => (
                    <option key={o.orderId} value={o.orderId}>
                      #{o.orderId} — ${Number(o.totalAmount).toFixed(2)} ({o.orderStatus})
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Delivery person
                <input
                  name="deliveryPerson"
                  required
                  value={form.deliveryPerson}
                  onChange={handleChange}
                />
              </label>

              <label>
                Phone
                <input
                  name="deliveryPhone"
                  required
                  value={form.deliveryPhone}
                  onChange={handleChange}
                />
              </label>

              <label className="form-full">
                Address
                <input
                  name="deliveryAddress"
                  required
                  value={form.deliveryAddress}
                  onChange={handleChange}
                />
              </label>

              <label className="form-full">
                Estimated delivery
                <input
                  name="estimatedDelivery"
                  type="datetime-local"
                  value={form.estimatedDelivery}
                  onChange={handleChange}
                />
              </label>

              {formError && <p className="error-text form-full">{formError}</p>}

              <div className="modal-actions form-full">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setModalOpen(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : 'Create delivery'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
