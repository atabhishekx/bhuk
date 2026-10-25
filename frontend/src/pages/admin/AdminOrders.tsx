import { useCallback, useEffect, useRef, useState } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Alert, EmptyState, Loading } from '@/components/Common'

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

const statusColors: Record<string, string> = {
  PLACED: 'bg-blue-100 text-blue-700 border border-blue-200',
  ACCEPTED: 'bg-blue-100 text-blue-700 border border-blue-200',
  PREPARING: 'bg-orange-100 text-orange-700 border border-orange-200',
  READY_FOR_PICKUP: 'bg-orange-100 text-orange-700 border border-orange-200',
  PICKED_UP: 'bg-purple-100 text-purple-700 border border-purple-200',
  OUT_FOR_DELIVERY: 'bg-indigo-100 text-indigo-700 border border-indigo-200',
  DELIVERED: 'bg-food-green-100 text-food-green-700 border border-food-green-200',
  REJECTED: 'bg-food-red-100 text-food-red-700 border border-food-red-200',
  CANCELLED: 'bg-gray-100 text-gray-700 border border-gray-200',
  DELIVERY_FAILED: 'bg-food-red-100 text-food-red-700 border border-food-red-200',
}

const statusDotColors: Record<string, string> = {
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

export default function AdminOrders() {
  const [orders, setOrders] = useState<Order[]>([])
  const [selected, setSelected] = useState<Order | null>(null)
  const [loading, setLoading] = useState(true)
  const [, setSaving] = useState<number | null>(null)
  const [statusFilter, setStatusFilter] = useState<string | null>(null)
  const [query, setQuery] = useState('')
  const [liveEnabled, setLiveEnabled] = useState(true)
  const [lastRefreshed, setLastRefreshed] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [availablePartners, setAvailablePartners] = useState<any[]>([])
  const [assignLoading, setAssignLoading] = useState<number | null>(null)
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null)

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
      // Optimistic update
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus, updatedAt: new Date().toISOString() } : o))
      if (selected?.id === orderId) {
        setSelected(prev => prev ? { ...prev, status: newStatus, updatedAt: new Date().toISOString() } : null)
      }
      const res = await api.post(`/staff/orders/${orderId}/status`, null, { params: { newStatus, reason } })
      const updated = res.data.data as Order
      setOrders(prev => prev.map(o => o.id === orderId ? updated : o))
      if (selected?.id === orderId) setSelected(updated)
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to update status')
      await fetchOrders()
    } finally {
      setSaving(null)
    }
  }

  const assignDelivery = async (orderId: number, partnerId: number, reason: string) => {
    setError(null)
    setAssignLoading(orderId)
    try {
      await api.post(`/admin/deliveries/${orderId}/assign`, null, {
        params: { partnerId, reason },
      })
      await fetchOrders()
      await fetchAvailablePartners()
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to assign delivery partner')
    } finally {
      setAssignLoading(null)
    }
  }

  const openDetail = (order: Order) => setSelected(order)
  const closeDetail = () => setSelected(null)

  const money = (minor: number | null | undefined) => (minor == null ? '₹0.00' : `₹${(minor / 100).toFixed(2)}`)

  if (loading) return <Loading />

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {error && (
          <Alert type="error" message={error} />
        )}
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Order Management</h1>
            <p className="text-gray-500">
              Admin oversight of all chain orders <span className="text-xs text-gray-400">· Last refreshed {lastRefreshed || '—'}</span>
            </p>
          </div>
          <div className="flex items-center gap-3">
            <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={liveEnabled}
                onChange={e => setLiveEnabled(e.target.checked)}
                className="accent-restru-600"
              />
              Live updates ({POLL_INTERVAL_MS / 1000}s)
            </label>
            <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg border border-gray-200 bg-white text-sm">
              <span className="text-gray-400">⟳</span>
              <span className="text-gray-500 font-medium">Live</span>
            </div>
          </div>
        </div>

        <div className="flex flex-col lg:flex-row gap-4 mb-6">
          <div className="flex-1 lg:flex-none lg:w-80">
            <div className="card p-5 space-y-4">
              <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Filters</h3>
              <div className="space-y-2">
                <input
                  type="text"
                  placeholder="Search order # or customer email"
                  value={query}
                  onChange={e => setQuery(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:ring-2 focus:ring-restru-500 focus:border-transparent"
                />
                <div className="flex gap-2">
                  <button
                    onClick={() => setStatusFilter(null)}
                    className={`flex-1 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${!statusFilter ? 'bg-restru-600 text-white' : 'bg-white border border-gray-200 text-gray-700'}`}
                  >
                    All
                  </button>
                  {['PLACED', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'OUT_FOR_DELIVERY'].map(s => (
                    <button
                      key={s}
                      onClick={() => setStatusFilter(s)}
                      className={`flex-1 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${statusFilter === s ? 'bg-restru-600 text-white' : 'bg-white border border-gray-200 text-gray-700'}`}
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
                      <span className={`font-medium px-2 py-0.5 rounded ${statusColors[s]}`}>
                        {orders.filter(o => o.status === s).length}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          <div className="flex-1">
            {orders.length === 0 ? (
              <EmptyState
                title="No orders"
                description={query || statusFilter ? "No orders match your filters." : "No orders have been placed yet."}
                icon="📦"
              />
            ) : (
              <div className="space-y-3">
                {orders.map(order => (
                  <div
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
                      <span className={`px-3 py-1 rounded-full text-xs font-medium border ${statusColors[order.status] || 'bg-gray-100 text-gray-700 border-gray-200'}`}>
                        {labelFor(order.status)}
                      </span>
                    </div>
                    <div className="flex justify-between items-center mb-3">
                      <span className="text-gray-600 text-sm">{order.items.length} item(s) · {new Date(order.createdAt).toLocaleString()}</span>
                      <span className="font-bold text-restru-600">{money(order.grandTotalMinor)}</span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-gray-400">
                      <span>Updated {new Date(order.updatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                      <span className="text-restru-600">View details →</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {selected && (
          <DetailDrawer
            order={selected}
            onClose={closeDetail}
            onStatusChange={(newStatus, reason) => updateStatus(selected.id, newStatus, reason)}
            availablePartners={availablePartners}
            onAssignDelivery={(partnerId: number, reason: string) => assignDelivery(selected.id, partnerId, reason)}
            assignLoading={assignLoading}
          />
        )}
      </div>
    </div>
  )
}

function DetailDrawer({
  order,
  onClose,
  onStatusChange,
  availablePartners,
  onAssignDelivery,
  assignLoading,
}: {
  order: Order
  onClose: () => void
  onStatusChange: (newStatus: string, reason?: string) => void
  availablePartners: any[]
  onAssignDelivery: (partnerId: number, reason: string) => void
  assignLoading: number | null
}) {
  const [reasonPrompt, setReasonPrompt] = useState<string | null>(null)
  const [reasonText, setReasonText] = useState('')

  const money = (minor: number | null | undefined) => (minor == null ? '₹0.00' : `₹${(minor / 100).toFixed(2)}`)
  const address = (() => {
    try { return JSON.parse(order.deliveryAddressJson || '{}') } catch { return {} }
  })()

  const allowed = nextStatuses[order.status]?.filter(s => !terminalStates.has(s)) || []

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-end bg-gray-900/60 backdrop-blur-sm" onClick={onClose}>
      <div className="w-full md:w-[640px] max-h-full bg-white border-l border-gray-200 shadow-2xl flex flex-col" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
          <div>
            <h2 className="text-lg font-bold text-gray-900">Order #{order.orderNumber}</h2>
            <p className="text-sm text-gray-500">{order.customerEmail} · {order.outletName || 'Outlet not set'}</p>
          </div>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-900 transition-colors">✕</button>
        </div>

        <div className="overflow-y-auto p-6 flex-1">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            <div className="space-y-2">
              <p className="text-xs text-gray-500 uppercase tracking-wider">Status</p>
              <span className={`px-3 py-1 rounded-full text-xs font-medium border ${statusColors[order.status] || 'bg-gray-100 text-gray-700 border-gray-200'}`}>
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
          </div>

          <div className="mb-6">
            <div className="flex justify-between items-center mb-3">
              <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Items</h3>
              <span className="text-xs font-medium text-restru-600">{order.items.length} item(s)</span>
            </div>
            <div className="border border-gray-100 rounded-xl overflow-hidden">
              {order.items.length === 0 ? (
                <div className="p-4 text-center text-gray-500 text-sm">No items</div>
              ) : (
                <table className="w-full text-left">
                  <thead>
                    <tr className="bg-gray-50 border-b border-gray-100 text-xs text-gray-500 uppercase">
                      <th className="px-4 py-3">Item</th>
                      <th className="px-4 py-3 text-right">Qty</th>
                      <th className="px-4 py-3 text-right">Unit</th>
                      <th className="px-4 py-3 text-right">Total</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {order.items.map(item => (
                      <tr key={item.id} className="group">
                        <td className="px-4 py-3">
                          <div className="text-sm text-gray-900">{item.itemNameSnapshot}</div>
                          {item.instructions && <div className="text-xs text-gray-500 italic">{item.instructions}</div>}
                          {item.modifiers.length > 0 && (
                            <div className="text-xs text-gray-600 mt-1">
                              {item.modifiers.map(m => `+ ${m.nameSnapshot} ×${m.quantity} (₹${((m.priceDeltaMinor || 0) / 100).toFixed(2)})`).join(' · ')}
                            </div>
                          )}
                        </td>
                        <td className="px-4 py-3 text-right text-sm text-gray-700">{item.quantity}</td>
                        <td className="px-4 py-3 text-right text-sm text-gray-700">{money(item.unitPriceMinor)}</td>
                        <td className="px-4 py-3 text-right text-sm font-bold text-gray-900">{money(item.lineTotalMinor)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="mb-6 border border-gray-100 rounded-xl p-4 bg-gray-50">
            <div className="flex justify-between items-center mb-3">
              <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider">Pricing</h3>
              <span className="text-xs font-medium text-restru-600">Grand total</span>
            </div>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between text-gray-600"><span>Subtotal</span><span>{money(order.subtotalMinor)}</span></div>
              <div className="flex justify-between text-gray-600"><span>Tax</span><span>{money(order.taxMinor)}</span></div>
              <div className="flex justify-between text-gray-600"><span>Delivery fee</span><span>{money(order.deliveryFeeMinor)}</span></div>
              <div className="flex justify-between border-t border-gray-200 pt-2 font-bold text-gray-900"><span>Total</span><span>{money(order.grandTotalMinor)}</span></div>
            </div>
          </div>

          <div className="mb-6">
            <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider mb-3">Delivery address</h3>
            <div className="text-sm text-gray-700 space-y-1">
              {address.fullAddress ? <p>{address.fullAddress}</p> : null}
              {address.city || address.state || address.pincode ? <p>{[address.city, address.state, address.pincode].filter(Boolean).join(', ')}</p> : null}
              {address.phone ? <p className="text-gray-500">{address.phone}</p> : null}
              {!address.fullAddress && <p className="text-gray-400 text-xs">No address recorded</p>}
            </div>
          </div>

          <div className="mb-6">
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
                    <span className={`w-2 h-2 rounded-full ${statusDotColors[event.newStatus || 'PLACED']}`}></span>
                    <div className="flex-1">
                      <div className="flex items-center gap-2 text-sm">
                        <span className={`font-medium ${statusColors[event.newStatus || 'PLACED']?.split(' ')[1] || 'text-gray-700'}`}>
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
            <p className="text-xs text-gray-500 mb-3 text-restru-600">Server-enforced state transitions only.</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {terminalStates.has(order.status) ? (
                <button disabled className="px-4 py-2.5 rounded-lg text-sm font-medium bg-gray-100 text-gray-400 cursor-not-allowed">
                  Terminal state
                </button>
              ) : (
                allowed.map(s => (
                  <button
                    key={s}
                    disabled={reasonPrompt !== null}
                    onClick={() => {
                      if (s === 'REJECTED' || s === 'CANCELLED') {
                        setReasonPrompt(s)
                        setReasonText('')
                        return
                      }
                      onStatusChange(s)
                    }}
                    className={`px-4 py-2.5 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 ${
                      reasonPrompt !== null ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    {labelFor(s)}
                  </button>
                ))
              )}
            </div>
            {reasonPrompt && (
              <div className="mt-4 space-y-3">
                <p className="text-sm text-gray-700">Reason for {labelFor(reasonPrompt)}:</p>
                <input
                  type="text"
                  autoFocus
                  value={reasonText}
                  onChange={e => setReasonText(e.target.value)}
                  className="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm focus:ring-2 focus:ring-restru-500 focus:border-transparent"
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
                <div className="flex gap-2">
                  <button onClick={() => { setReasonPrompt(null); setReasonText('') }} className="px-4 py-2.5 rounded-lg text-sm font-medium bg-gray-100 text-gray-700">
                    Cancel
                  </button>
                  <button
                    onClick={() => {
                      if (!reasonText.trim()) return
                      onStatusChange(reasonPrompt, reasonText.trim())
                      setReasonPrompt(null)
                      setReasonText('')
                    }}
                    className="px-4 py-2.5 rounded-lg text-sm font-medium bg-restru-600 text-white hover:bg-restru-700"
                  >
                    Confirm
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="pt-6 border-t border-gray-100">
            <h3 className="font-bold text-sm text-gray-900 uppercase tracking-wider mb-3">Assign delivery person</h3>
            <p className="text-xs text-gray-500 mb-3 text-restru-600">Assign an available delivery partner to this order.</p>
            {availablePartners.length === 0 ? (
              <p className="text-sm text-gray-500">No delivery partners are currently online.</p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {availablePartners.map((p) => (
                  <button
                    key={p.id}
                    disabled={assignLoading === order.id}
                    onClick={() => onAssignDelivery(p.id, 'Assigned by admin')}
                    className={`px-4 py-2.5 rounded-lg text-sm font-medium transition-colors disabled:opacity-50 ${
                      assignLoading === order.id ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    {p.userName || 'Delivery Partner'}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
