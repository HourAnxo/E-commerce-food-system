import { useEffect, useState } from 'react'
import { deliveryApi, orderApi, DELIVERY_STATUSES } from '../api/services'

const EMPTY_FORM = {
  orderId: '',
  deliveryPerson: '',
  deliveryPhone: '',
  deliveryAddress: '',
  estimatedDelivery: '',
}

const EMPTY_ASSIGN = { deliveryPerson: '', deliveryPhone: '' }

// Assigned and Disputed are never set by hand: Assigned has to come from the
// assign endpoint (only that mints the driver's token) and Disputed comes from
// the customer. Rows in those states show a badge instead of the dropdown.
const MANUAL_STATUSES = DELIVERY_STATUSES

function driverLink(delivery) {
  return `${window.location.origin}/delivery/respond/${delivery.acceptToken}`
}

export default function AdminDeliveries() {
  const [deliveries, setDeliveries] = useState([])
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [savingId, setSavingId] = useState(null)
  const [copiedId, setCopiedId] = useState(null)

  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState(null)

  // ===== NEW: assignment flow =====
  const [assignTarget, setAssignTarget] = useState(null)
  const [assignForm, setAssignForm] = useState(EMPTY_ASSIGN)
  const [declined, setDeclined] = useState([])
  const [assigning, setAssigning] = useState(false)
  const [assignError, setAssignError] = useState(null)
  // ===============

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

  const replaceRow = (row) =>
    setDeliveries((prev) => prev.map((d) => (d.deliveryId === row.deliveryId ? row : d)))

  const handleStatusChange = async (delivery, status) => {
    setSavingId(delivery.deliveryId)
    try {
      const res = await deliveryApi.update(delivery.deliveryId, {
        ...delivery,
        deliveryStatus: status,
      })
      replaceRow(res.data)
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
        // Left blank on purpose in the normal flow: the delivery is created
        // into the pool, then offered to a driver with Assign. Filling a
        // courier here sends the offer immediately instead.
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

  // ===== NEW: offer the delivery to a driver =====
  const openAssign = async (delivery) => {
    setAssignTarget(delivery)
    setAssignForm(EMPTY_ASSIGN)
    setAssignError(null)
    setDeclined([])
    try {
      const res = await deliveryApi.declinedBy(delivery.deliveryId)
      setDeclined(res.data)
    } catch {
      // Not fatal — the backend rejects a re-offer anyway, this only warns early.
    }
  }

  const handleAssignChange = (e) =>
    setAssignForm((f) => ({ ...f, [e.target.name]: e.target.value }))

  const handleAssign = async (e) => {
    e.preventDefault()
    const person = assignForm.deliveryPerson.trim()
    if (!person) {
      setAssignError('Please enter the driver’s name.')
      return
    }
    if (declined.includes(person)) {
      setAssignError(`${person} already declined this delivery. Choose someone else.`)
      return
    }
    setAssigning(true)
    setAssignError(null)
    try {
      const res = await deliveryApi.assign(
        assignTarget.deliveryId,
        person,
        assignForm.deliveryPhone.trim(),
      )
      replaceRow(res.data)
      setAssignTarget(null)
    } catch (err) {
      setAssignError(err.response?.data?.message || 'Could not assign this delivery.')
    } finally {
      setAssigning(false)
    }
  }

  const copyLink = async (delivery) => {
    const url = driverLink(delivery)
    try {
      await navigator.clipboard.writeText(url)
      setCopiedId(delivery.deliveryId)
      setTimeout(() => setCopiedId(null), 2000)
    } catch {
      // Clipboard needs a secure context; fall back to showing the link.
      window.prompt('Copy this link and send it to the driver:', url)
    }
  }
  // ===============

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
                <th>Driver</th>
              </tr>
            </thead>
            <tbody>
              {deliveries.map((d) => (
                <tr key={d.deliveryId}>
                  <td>#{d.deliveryId}</td>
                  <td>#{d.orderId}</td>
                  <td className="cell-strong">{d.deliveryPerson || '—'}</td>
                  <td>{d.deliveryPhone || '—'}</td>
                  <td>{d.deliveryAddress}</td>
                  <td>
                    {d.estimatedDelivery
                      ? new Date(d.estimatedDelivery).toLocaleString()
                      : '—'}
                  </td>
                  <td>
                    {MANUAL_STATUSES.includes(d.deliveryStatus) ? (
                      <select
                        className={`status-select status-${d.deliveryStatus?.toLowerCase()}`}
                        value={d.deliveryStatus}
                        disabled={savingId === d.deliveryId}
                        onChange={(e) => handleStatusChange(d, e.target.value)}
                      >
                        {MANUAL_STATUSES.map((s) => (
                          <option key={s} value={s}>
                            {s}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <span className={`status status-${d.deliveryStatus?.toLowerCase()}`}>
                        {d.deliveryStatus}
                      </span>
                    )}
                  </td>
                  {/* ===== NEW: assignment flow ===== */}
                  <td>
                    {d.deliveryStatus === 'Preparing' ? (
                      <button className="btn btn-secondary btn-sm" onClick={() => openAssign(d)}>
                        Assign driver
                      </button>
                    ) : d.deliveryStatus === 'Assigned' ? (
                      <div className="assign-cell">
                        <span className="muted">Awaiting reply</span>
                        <button className="btn btn-secondary btn-sm" onClick={() => copyLink(d)}>
                          {copiedId === d.deliveryId ? 'Copied!' : 'Copy link'}
                        </button>
                      </div>
                    ) : (
                      <span className="muted">—</span>
                    )}
                  </td>
                  {/* =============== */}
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
                Delivery person <span className="muted">(optional)</span>
                <input
                  name="deliveryPerson"
                  value={form.deliveryPerson}
                  onChange={handleChange}
                />
              </label>

              <label>
                Phone <span className="muted">(optional)</span>
                <input
                  name="deliveryPhone"
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

              <p className="muted form-full">
                Leave the courier blank to put this delivery in the pool and assign it later.
                Naming one now sends them an offer straight away.
              </p>

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

      {/* ===== NEW: assign modal ===== */}
      {assignTarget && (
        <div className="modal-overlay" onClick={() => setAssignTarget(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Assign delivery #{assignTarget.deliveryId}</h2>
            <p className="muted">
              This sends the driver an offer. The delivery only starts once they accept.
            </p>

            {declined.length > 0 && (
              <p className="error-text">
                Already declined by: {declined.join(', ')}
              </p>
            )}

            <form onSubmit={handleAssign} className="form-grid">
              <label className="form-full">
                Driver name
                <input
                  name="deliveryPerson"
                  required
                  value={assignForm.deliveryPerson}
                  onChange={handleAssignChange}
                />
              </label>

              <label className="form-full">
                Phone
                <input
                  name="deliveryPhone"
                  value={assignForm.deliveryPhone}
                  onChange={handleAssignChange}
                />
              </label>

              {assignError && <p className="error-text form-full">{assignError}</p>}

              <div className="modal-actions form-full">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setAssignTarget(null)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={assigning}>
                  {assigning ? 'Sending…' : 'Send offer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      {/* =============== */}
    </div>
  )
}
