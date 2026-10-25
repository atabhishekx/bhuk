import { useCallback, useEffect, useRef, useState } from 'react'
import api from '@/api/axios'
import { Alert, EmptyState, Loading, PageHeader } from '@/components/Common'
import Drawer from '@/components/Drawer'
import { useToastHelpers } from '@/components/Toast'

type OrderItem = {
  id: number
  itemId?: number
  itemNameSnapshot: string
  quantity: number
  unitPriceMinor: number
  lineTotalMinor: number
  instructions?: string | null
  modifiers: { id: number; nameSnapshot: string; priceDeltaMinor: number; quantity: number }[]
}

type OrderStatusEvent = {
  id: number
  oldStatus?: string | null
  newStatus: string
  actorEmail?: string | null
  actorRole?: string | null
  reason?: string | null
  createdAt: string
}

type Order = {
  id: number
  orderNumber: string
  customerId: number
  customerEmail: string
  outletId?: number | null
  outletName?: string | null
  status: string
  subtotalMinor: number
  taxMinor: number
  deliveryFeeMinor: number
  grandTotalMinor: number
  deliveryAddressJson: string
  createdAt: string
  updatedAt: string
  items: OrderItem[]
  statusEvents: OrderStatusEvent[]
}

const POLL_INTERVAL_MS = 5000

const statusBadges: Record<string, string> = {
  PLACED: 'badge-info',
  ACCEPTED: 'badge-info',
  PREPARING: 'badge-warning',
  READY_FOR_PICKUP: 'badge-warning',
  PICKED_UP: 'badge-info',
  OUT_FOR_DELIVERY: 'badge-info',
  DELIVERED: 'badge-success',
  REJECTED: 'badge-error',
  CANCELLED: 'badge-neutral',
  DELIVERY_FAILED: 'badge-error',
}

const dotColors: Record<string, string> = {
  PLACED: 'bg-blue-500',
  ACCEPTED: 'bg-blue-500',
  PREPARING: 'bg-orange-500',
  READY_FOR_PICKUP: 'bg-orange-500',
  PICKED_UP: 'bg-purple-500',
  OUT_FOR_DELIVERY: 'bg-indigo-500',
  DELIVERED: 'bg-food-green-500',
  REJECTED: 'bg-food-red-500',
  CANCELLED: 'bg-gray-500',
  DELIVERY_FAILED: 'bg-food-red-500',
}

const terminalStates = new Set(['DELIVERED', 'REJECTED', 'CANCELLED', 'DELIVERY_FAILED'])

const nextStatuses: Record<string, string[]> = {
  PLACED: ['ACCEPTED', 'REJECTED', 'CANCELLED'],
  ACCEPTED: ['PREPARING', 'REJECTED', 'CANCELLED'],
  PREPARING: ['READY_FOR_PICKUP', 'CANCELLED'],
  READY_FOR_PICKUP: ['PICKED_UP', 'OUT_FOR_DELIVERY', 'CANCELLED'],
  PICKED_UP: ['OUT_FOR_DELIVERY'],
  OUT_FOR_DELIVERY: ['DELIVERED', 'DELIVERY_FAILED', 'CANCELLED'],
}

const labelFor = (s: string) => s.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
const money = (minor: number | null | undefined) => (minor == null ? '₹0.00' : `₹${(minor / 100).toFixed(2)}`)

export default function AdminOrders() {
  const [orders, setOrders] = useState<Order[]>([])
  const [selected, setSelected] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState<number | null>(null)
  const [statusFilter, setStatusFilter] = useState<string | null>(null)
  const [query, setQuery] = useState('')
  const [liveEnabled, setLiveEnabled] = useState(true)
  const [lastRefreshed, setLastRefreshed] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [availablePartners, setAvailablePartners] = useState<any[]>([])
  const [assignLoading, setAssignLoading] = useState<number | null>(null)
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const { error: toastError, success: toastSuccess } = useToastHelpers()

  const fetchOrders = useCallback(async () => {
    try {
      setLoading(true)
      const params: Record<string, string> = {}
      if (statusFilter) params.status = statusFilter
      if (query.trim()) params.q = query.trim()
      const res = await api.get<{ data: Order[] }>('/admin/orders', { params })
      const data = res.data.data ?? []
      setOrders(data)
      setLastRefreshed(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }))
      if (selected) {
        const updated = data.find(o => o.id === selected.id)
        if (updated) setSelected(updated)
      }
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to load orders')
    } finally {
      setLoading(false)
    }
  }, [statusFilter, query, selected])

  const fetchAvailablePartners = useCallback(async () => {
    try {
      const res = await api.get<{ data: any[] }>('/admin/delivery-partners/available')
      setAvailablePartners(res.data.data ?? [])
    } catch (e) {
      console.error('Failed to load available delivery partners', e)
    }
  }, [])

  useEffect(() => {
    fetchOrders()
    fetchAvailablePartners()
  }, [fetchOrders, fetchAvailablePartners])

  useEffect(() => {
    if (!liveEnabled) {
      if (pollRef.current) clearInterval(pollRef.current)
      pollRef.current = null
      return
    }
    pollRef.current = setInterval(fetchOrders, POLL_INTERVAL_MS)
    return () => {
      if (pollRef.current) clearInterval(pollRef.current)
      pollRef.current = null
    }
  }, [liveEnabled, fetchOrders])

  const updateStatus = async (orderId: number, newStatus: string, reason?: string) => {
    setError(null)
    setSaving(orderId)
    try {
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus, updatedAt: new Date().toISOString() } : o))
      if (selected?.id === orderId) {
        setSelected(prev => prev ? { ...prev, status: newStatus, updatedAt: new Date().toISOString() } : null)
      }
      const res = await api.post(`/staff/orders/${orderId}/status`, null, { params: { newStatus, reason } })
      const updated = res.data.data as Order
      setOrders(prev => prev.map(o => o.id === orderId ? updated : o))
      if (selected?.id === orderId) setSelected(updated)
      toastSuccess(`Order status updated to ${labelFor(newStatus)}`)
    } catch (e: any) {
      const msg = e.response?.data?.error?.message || 'Failed to update status'
      setError(msg)
      toastError(msg)
      await fetchOrders()
    } finally {
      setSaving(null)
    }
  }

  const assignDelivery = async (orderId: number, partnerId: number, reason: string) => {
    setError(null)
    setAssignLoading(orderId)
    try {
      await api.post(`/admin/deliveries/${orderId}/assign`, null, { params: { partnerId, reason } })
      await fetchOrders()
      await fetchAvailablePartners()
      toastSuccess('Delivery partner assigned')
    } catch (e: any) {
      const msg = e.response?.data?.error?.message || 'Failed to assign delivery partner'
      setError(msg)
      toastError(msg)
    } finally {
      setAssignLoading(null)
    }
  }

  const openDetail = (order: Order) => setSelected(order)
  const closeDetail = () => setSelected(null)

  if (loading) return <Loading />

  return (
    <div className="min-h-screen bg-gray-50">
      <main className="page-content">
        <PageHeader
          title="Order Management"
          subtitle={`Admin oversight of all chain orders · Last refreshed ${lastRefreshed || '—'}`}
          action={
            <div className="flex items-center gap-3">
              <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={liveEnabled}
                  onChange={e => setLiveEnabled(e.target.checked)}
                  className="accent-bhuk-600"
                />
                Live updates ({POLL_INTERVAL_MS / 1000}s)
              </label>
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-gray-200 bg-white text-sm">
                <span className="text-gray-400">🔓</span>
                <span className="text-gray-500 font-medium">Live</span>
              </div>
            </div>
          }
        />

        {error && <Alert type="error" message={error} onClose={() => setError(null)} />}

        <div className="flex flex-col lg:flex-row gap-4 mb-6">
          <aside className="flex-1 lg:flex-none lg:w-80">
            <div className="card p-5 space-y-4">
              <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Filters</h3>
              <div className="space-y-2">
                <input
                  type="text"
                  placeholder="Search order # or customer email"
                  value={query}
                  onChange={e => setQuery(e.target.value)}
                  className="input-field"
                />
                <div className="flex gap-2">
                  <button
                    onClick={() => setStatusFilter(null)}
                    className={`btn ${!statusFilter ? 'btn-primary' : 'btn-outline'} btn-sm flex-1`}
                  >
                    All
                  </button>
                  {['PLACED', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY'].map(s => (
                    <button
                      key={s}
                      onClick={() => setStatusFilter(s)}
                      className={`btn ${statusFilter === s ? 'btn-primary' : 'btn-outline'} btn-sm flex-1`}
                    >
                      {labelFor(s)}
                    </button>
                  ))}
                </div>
              </div>
              <div className="pt-3 border-t border-gray-100">
                <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Counts</h3>
                <div className="space-y-2 mt-3 text-sm">
                  {[...new Set(orders.map(o => o.status))].sort().map(s => (
                    <div key={s} className="flex justify-between">
                      <span className="text-gray-600">{labelFor(s)}</span>
                      <span className={`font-medium badge ${statusBadges[s] || 'badge-neutral'}`}>
                        {orders.filter(o => o.status === s).length}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </aside>

          <section className="flex-1">
            {orders.length === 0 ? (
              <EmptyState
                title="No orders"
                description={query || statusFilter ? "No orders match your filters." : "No orders have been placed yet."}
                icon="📦"
              />
            ) : (
              <div className="space-y-3">
                {orders.map(order => (
                  <article
                    key={order.id}
                    className="card p-5 hover:shadow-md transition-shadow cursor-pointer border"
                    onClick={() => openDetail(order)}
                  >
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <h3 className="font-bold text-gray-900">Order #{order.orderNumber}</h3>
                        <p className="text-sm text-gray-500">
                          {order.outletName ? `Outlet: ${order.outletName} · ` : ''}{order.customerEmail}
                        </p>
                      </div>
                      <span className={`badge ${statusBadges[order.status] || 'badge-neutral'}`}>
                        {labelFor(order.status)}
                      </span>
                    </div>
                    <div className="flex justify-between items-center mb-3">
                      <span className="text-gray-600 text-sm">{order.items.length} item(s) · {new Date(order.createdAt).toLocaleString()}</span>
                      <span className="font-bold text-bhuk-600">{money(order.grandTotalMinor)}</span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-gray-400">
                      <span>Updated {new Date(order.updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                      <span className="text-bhuk-600">View details →</span>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        </div>

        <Drawer
          isOpen={!!selected}
          onClose={closeDetail}
          title={`Order #${selected?.orderNumber}`}
          maxWidth="lg"
        >
          {selected && <OrderDetailDrawer
            order={selected}
            onStatusChange={(newStatus, reason) => updateStatus(selected.id, newStatus, reason)}
            availablePartners={availablePartners}
            onAssignDelivery={(partnerId, reason) => assignDelivery(selected.id, partnerId, reason)}
            assignLoading={assignLoading}
            saving={saving}
          />}
        </Drawer>
      </main>
    </div>
  )
}

function OrderDetailDrawer({
  order,
  onStatusChange,
  availablePartners,
  onAssignDelivery,
  assignLoading,
  saving,
}: {
  order: Order
  onStatusChange: (newStatus: string, reason?: string) => void
  availablePartners: any[]
  onAssignDelivery: (partnerId: number, reason: string) => void
  assignLoading: number | null
  saving: number | null
}) {
  const [reasonPrompt, setReasonPrompt] = useState<string | null>(null)
  const [reasonText, setReasonText] = useState('')

  const address = (() => { try { return JSON.parse(order.deliveryAddressJson || '{}') } catch { return {} } })()
  const allowed = nextStatuses[order.status]?.filter(s => !terminalStates.has(s)) || []

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-2">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Status</p>
          <span className={`badge ${statusBadges[order.status] || 'badge-neutral'}`}>
            {labelFor(order.status)}
          </span>
        </div>
        <div className="space-y-2">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Placed</p>
          <p className="text-sm text-gray-700">{new Date(order.createdAt).toLocaleString()}</p>
        </div>
        <div className="space-y-2">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Last updated</p>
          <p className="text-sm text-gray-700">{new Date(order.updatedAt).toLocaleString()}</p>
        </div>
        <div className="space-y-2">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Customer</p>
          <p className="text-sm text-gray-700">{order.customerEmail}</p>
        </div>
        <div className="space-y-2">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Outlet</p>
          <p className="text-sm text-gray-700">{order.outletName || 'Not set'}</p>
        </div>
      </div>

      <div>
        <div className="flex justify-between items-center mb-3">
          <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Items</h3>
          <span className="text-xs font-medium text-bhuk-600">{order.items.length} item(s)</span>
        </div>
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr><th>Item</th><th className="text-right">Qty</th><th className="text-right">Unit</th><th className="text-right">Total</th></tr>
            </thead>
            <tbody>
              {order.items.map(item => (
                <tr key={item.id}>
                  <td>
                    <div className="text-sm text-gray-900">{item.itemNameSnapshot}</div>
                    {item.instructions && <div className="text-xs text-gray-500 italic">{item.instructions}</div>}
                    {item.modifiers.length > 0 && (
                      <div className="text-xs text-gray-600 mt-1">
                        {item.modifiers.map(m => `+ ${m.nameSnapshot} ×${m.quantity} (₹${((m.priceDeltaMinor || 0) / 100).toFixed(2)})`).join(' · ')}
                      </div>
                    )}
                  </td>
                  <td className="text-right text-sm text-gray-700">{item.quantity}</td>
                  <td className="text-right text-sm text-gray-700">{money(item.unitPriceMinor)}</td>
                  <td className="text-right text-sm font-bold text-gray-900">{money(item.lineTotalMinor)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card-elevated p-4">
        <div className="flex justify-between items-center mb-3">
          <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Pricing</h3>
          <span className="text-xs font-medium text-bhuk-600">Grand total</span>
        </div>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between text-gray-600"><span>Subtotal</span><span>{money(order.subtotalMinor)}</span></div>
          <div className="flex justify-between text-gray-600"><span>Tax</span><span>{money(order.taxMinor)}</span></div>
          <div className="flex justify-between text-gray-600"><span>Delivery fee</span><span>{money(order.deliveryFeeMinor)}</span></div>
          <div className="flex justify-between border-t border-gray-200 pt-2 font-bold text-gray-900"><span>Total</span><span>{money(order.grandTotalMinor)}</span></div>
        </div>
      </div>

      <div>
        <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider mb-3">Delivery address</h3>
        <div className="text-sm text-gray-700 space-y-1 bg-gray-50 p-4 rounded-xl">
          {address.fullAddress ? <p>{address.fullAddress}</p> : null}
          {address.city || address.state || address.pincode ? <p>{[address.city, address.state, address.pincode].filter(Boolean).join(', ')}</p> : null}
          {address.phone ? <p className="text-gray-500">{address.phone}</p> : null}
          {!address.fullAddress && <p className="text-gray-400 text-xs">No address recorded</p>}
        </div>
      </div>

      <div>
        <div className="flex justify-between items-center mb-3">
          <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Status history</h3>
          <span className="text-xs font-medium text-gray-500">{order.statusEvents.length} event(s)</span>
        </div>
        <div className="space-y-3">
          {order.statusEvents.length === 0 ? (
            <p className="text-sm text-gray-500">No status changes yet.</p>
          ) : (
            [...order.statusEvents].reverse().map(event => (
              <div key={event.id} className="flex gap-3">
                <span className={`w-2 h-2 rounded-full ${dotColors[event.newStatus || 'PLACED']} mt-2 flex-shrink-0`}></span>
                <div className="flex-1">
                  <div className="flex items-center gap-2 text-sm">
                    <span className={`font-medium badge ${statusBadges[event.newStatus] || 'badge-neutral'}`}>
                      {labelFor(event.newStatus || 'PLACED')}
                    </span>
                    {event.actorEmail && <span className="text-xs text-gray-500">· {event.actorEmail}</span>}
                    {event.actorRole && <span className="text-xs text-gray-500 capitalize">({event.actorRole})</span>}
                  </div>
                  <p className="text-xs text-gray-500">{event.reason || '-'}</p>
                  <p className="text-xs text-gray-400">{new Date(event.createdAt).toLocaleString()}</p>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="pt-6 border-t border-gray-100">
        <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider mb-3">Change status</h3>
        <p className="text-xs text-gray-500 mb-3 text-bhuk-600">Server-enforced state transitions only.</p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          {terminalStates.has(order.status) ? (
            <button disabled className="btn btn-outline btn-sm" style={{ gridColumn: '1 / -1' }}>
              Terminal state
            </button>
          ) : (
            allowed.map(s => (
              <button
                key={s}
                disabled={reasonPrompt !== null || saving === order.id}
                onClick={() => {
                  if (s === 'REJECTED' || s === 'CANCELLED') {
                    setReasonPrompt(s)
                    setReasonText('')
                    return
                  }
                  onStatusChange(s)
                }}
                className={`btn ${reasonPrompt !== null || saving === order.id ? 'btn-outline' : 'btn-outline'} btn-sm`}
              >
                {saving === order.id ? 'Saving...' : labelFor(s)}
              </button>
            ))
          )}
        </div>
        {reasonPrompt && (
          <div className="mt-4 space-y-3 animate-in">
            <p className="text-sm text-gray-700">Reason for {labelFor(reasonPrompt)}:</p>
            <input
              type="text"
              autoFocus
              value={reasonText}
              onChange={e => setReasonText(e.target.value)}
              className="input-field"
              onKeyDown={e => {
                if (e.key === 'Enter' && reasonText.trim()) {
                  onStatusChange(reasonPrompt, reasonText.trim())
                  setReasonPrompt(null)
                  setReasonText('')
                } else if (e.key === 'Escape') {
                  setReasonPrompt(null)
                  setReasonText('')
                }
              }}
            />
            <div className="flex gap-2 justify-end">
              <button onClick={() => { setReasonPrompt(null); setReasonText('') }} className="btn btn-outline btn-sm">Cancel</button>
              <button
                onClick={() => {
                  if (!reasonText.trim()) return
                  onStatusChange(reasonPrompt, reasonText.trim())
                  setReasonPrompt(null)
                  setReasonText('')
                }}
                className="btn btn-primary btn-sm"
              >
                Confirm
              </button>
            </div>
          </div>
        )}
      </div>

      <div className="pt-6 border-t border-gray-100">
        <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider mb-3">Assign delivery person</h3>
        <p className="text-xs text-gray-500 mb-3 text-bhuk-600">Assign an available delivery partner to this order.</p>
        {availablePartners.length === 0 ? (
          <p className="text-sm text-gray-500">No delivery partners registered.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2">
            {availablePartners.map((p) => {
              const isOnline = p.status === 'ONLINE'
              const isBusy = p.status === 'BUSY'
              const canAssign = isOnline
              return (
                <button
                  key={p.id}
                  disabled={assignLoading === order.id || !canAssign}
                  onClick={() => onAssignDelivery(p.id, 'Assigned by admin')}
                  className={`flex flex-col items-center gap-1.5 px-3 py-2.5 rounded-lg text-sm transition-colors ${assignLoading === order.id ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : !canAssign ? 'bg-gray-50 border border-gray-200 text-gray-500 cursor-not-allowed' : 'card-elevated'}`}
                >
                  <span className={`badge ${isOnline ? 'badge-success' : isBusy ? 'badge-warning' : 'badge-neutral'}`}>
                    {isOnline ? 'Online' : isBusy ? 'Busy' : 'Offline'}
                  </span>
                  <span className={`font-medium ${canAssign ? 'text-gray-900' : 'text-gray-500'}`} style={{ lineClamp: 1 }}>
                    {p.userName || 'Delivery Partner'}
                  </span>
                  <span className="text-[10px] text-gray-400">{isOnline ? 'Available to accept' : isBusy ? 'Currently busy' : 'Not available'}</span>
                </button>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}