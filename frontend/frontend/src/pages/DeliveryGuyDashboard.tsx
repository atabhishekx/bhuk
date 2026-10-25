import { useEffect, useState } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Loading, EmptyState } from '@/components/Common'

export default function DeliveryGuyDashboard() {
  const [assignments, setAssignments] = useState<any[]>([])
  const [partnerStatus, setPartnerStatus] = useState('OFFLINE')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchData()
  }, [])

  async function fetchData() {
    try {
      const [assignRes] = await Promise.all([
        api.get('/partner/assignments'),
      ])
      setAssignments(assignRes.data.data)
      try {
        const profRes = await api.get('/me')
        setPartnerStatus(profRes.data.data.roles.includes('DELIVERY_GUY') ? 'ONLINE' : 'OFFLINE')
      } catch {}
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  async function setAvailability(status: string) {
    try {
      await api.patch('/partner/availability', { status })
      setPartnerStatus(status)
    } catch (e: any) {
      alert(e.response?.data?.error?.message || 'Failed to update availability')
    }
  }

  async function acceptAssignment(id: number) {
    try {
      await api.post(`/partner/assignments/${id}/accept`)
      await fetchData()
    } catch (e) {
      console.error(e)
    }
  }

  async function declineAssignment(id: number) {
    const reason = prompt('Reason for declining:')
    if (!reason) return
    try {
      await api.post(`/partner/assignments/${id}/decline`, null, { params: { reason } })
      await fetchData()
    } catch (e) {
      console.error(e)
    }
  }

  async function markArrived(id: number) {
    try {
      await api.post(`/partner/deliveries/${id}/arrived`)
      await fetchData()
    } catch (e) {
      console.error(e)
    }
  }

  async function markPickedUp(id: number) {
    try {
      await api.post(`/partner/deliveries/${id}/pickup`)
      await fetchData()
    } catch (e) {
      console.error(e)
    }
  }

  async function markCompleted(id: number) {
    try {
      await api.post(`/partner/deliveries/${id}/complete`)
      await fetchData()
    } catch (e) {
      console.error(e)
    }
  }

  const statusColors: Record<string, string> = {
    OFFERED: 'bg-blue-100 text-blue-700 border border-blue-200',
    ASSIGNED: 'bg-purple-100 text-purple-700 border border-purple-200',
    ACCEPTED: 'bg-indigo-100 text-indigo-700 border border-indigo-200',
    PICKED_UP: 'bg-orange-100 text-orange-700 border border-orange-200',
    COMPLETED: 'bg-food-green-100 text-food-green-700 border border-food-green-200',
    DECLINED: 'bg-gray-100 text-gray-700 border border-gray-200',
    FAILED: 'bg-food-red-100 text-food-red-700 border border-food-red-200',
  }

  const parseAddress = (json?: string) => {
    try { return JSON.parse(json || '{}') } catch { return {} }
  }

  const offered = assignments.filter(a => a.state === 'OFFERED')
  const active = assignments.filter(a => a.state === 'ASSIGNED' || a.state === 'ACCEPTED' || a.state === 'PICKED_UP')
  const history = assignments.filter(a => a.state === 'COMPLETED' || a.state === 'DECLINED' || a.state === 'FAILED')

  function renderOfferedItem(a: any) {
    const address = parseAddress(a.deliveryAddressJson)
    return (
      <div key={a.id} className="card p-5 border-l-4 border-blue-500 shadow-sm bg-white flex flex-col lg:flex-row lg:items-center gap-4">
        <div className="flex-1">
          <div className="flex justify-between items-start mb-2">
            <div>
              <h4 className="font-bold text-lg text-gray-900">Order #{a.orderNumber}</h4>
              <p className="text-sm text-gray-500">
                👤 {a.customerName || 'Customer'} {a.customerPhone ? `· ${a.customerPhone}` : ''}
              </p>
            </div>
            <span className={`px-3 py-1 rounded-full text-xs font-medium border ${statusColors.OFFERED}`}>
              Ready for Pickup
            </span>
          </div>
          <div className="mt-3 bg-blue-50 rounded-xl p-4 border border-blue-100">
            <div className="flex items-center gap-2 text-sm mb-2">
              <span className="text-blue-600 font-medium">📍</span>
              <span className="text-gray-700 font-medium">{address.fullAddress || 'No delivery address recorded'}</span>
            </div>
            {address.city || address.state || address.pincode ? (
              <p className="text-xs text-gray-500 mt-1">
                {[address.city, address.state, address.pincode].filter(Boolean).join(', ')}
              </p>
            ) : null}
          </div>
        </div>
        <div className="flex gap-2 w-full lg:w-auto">
          <button
            onClick={() => acceptAssignment(a.id)}
            className="flex-1 lg:w-32 py-2.5 rounded-xl text-sm font-bold bg-food-green-600 text-white hover:bg-food-green-700 transition-colors shadow-md"
          >
            ✅ Accept
          </button>
          <button
            onClick={() => declineAssignment(a.id)}
            className="flex-1 lg:w-32 py-2.5 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
          >
            ❌ Decline
          </button>
        </div>
      </div>
    )
  }

  function renderActiveItem(a: any) {
    const address = parseAddress(a.deliveryAddressJson)
    return (
      <div key={a.id} className="card p-4 border-l-4 border-indigo-500 shadow-sm bg-white flex flex-col lg:flex-row lg:items-center gap-4">
        <div className="flex-1">
          <div className="flex justify-between items-start mb-2">
            <div>
              <h4 className="font-bold text-lg text-gray-900">Order #{a.orderNumber}</h4>
              <p className="text-sm text-gray-500">
                👤 {a.customerName || 'Customer'} {a.customerPhone ? `· ${a.customerPhone}` : ''}
              </p>
            </div>
            <span className={`px-3 py-1 rounded-full text-xs font-medium border ${statusColors[a.state] || 'bg-gray-100 text-gray-700 border-gray-200'}`}>
              {a.state.replace(/_/g, ' ').replace(/\b\w/g, (c: string) => c.toUpperCase())}
            </span>
          </div>
          <div className="mt-3 text-sm text-gray-700 space-y-1">
            {address.fullAddress ? (
              <div className="flex items-center gap-2">
                <span className="text-indigo-600">📍</span>
                <span>{address.fullAddress}</span>
              </div>
            ) : null}
            {address.city || address.state || address.pincode ? (
              <p className="text-xs text-gray-500 mt-1">
                {[address.city, address.state, address.pincode].filter(Boolean).join(', ')}
              </p>
            ) : null}
          </div>
        </div>
        <div className="flex gap-2 w-full lg:w-auto">
          {a.state === 'ASSIGNED' && (
            <button
              onClick={() => markArrived(a.id)}
              className="flex-1 lg:w-40 py-2.5 rounded-xl text-sm font-bold bg-bhuk-600 text-white hover:bg-bhuk-700 transition-colors"
            >
              📍 Arrived
            </button>
          )}
          {a.state === 'ACCEPTED' && (
            <>
              <button
                onClick={() => markArrived(a.id)}
                className="flex-1 lg:w-40 py-2.5 rounded-xl text-sm font-medium bg-bhuk-600 text-white hover:bg-bhuk-700 transition-colors"
              >
                📍 Arrived
              </button>
              <button
                onClick={() => markPickedUp(a.id)}
                className="flex-1 lg:w-40 py-2.5 rounded-xl text-sm font-medium bg-bhuk-600 text-white hover:bg-bhuk-700 transition-colors"
              >
                📦 Picked Up
              </button>
            </>
          )}
          {a.state === 'PICKED_UP' && (
            <button
              onClick={() => markCompleted(a.id)}
              className="flex-1 lg:w-40 py-2.5 rounded-xl text-sm font-bold bg-food-green-600 text-white hover:bg-food-green-700 transition-colors shadow-md"
            >
              ✅ Mark Delivered
            </button>
          )}
        </div>
      </div>
    )
  }

  function renderHistoryItem(a: any) {
    const address = parseAddress(a.deliveryAddressJson)
    return (
      <div key={a.id} className="card p-4 bg-gray-50 border border-gray-100 flex justify-between items-start gap-4">
        <div className="flex-1">
          <div className="flex justify-between items-start mb-2">
            <div>
              <h4 className="font-bold text-lg text-gray-900">Order #{a.orderNumber}</h4>
              <p className="text-sm text-gray-500">
                👤 {a.customerName || 'Customer'} {a.customerPhone ? `· ${a.customerPhone}` : ''}
              </p>
            </div>
            <span className={`px-3 py-1 rounded-full text-xs font-medium border ${statusColors[a.state] || 'bg-gray-100 text-gray-700 border-gray-200'}`}>
              {a.state.replace(/_/g, ' ').replace(/\b\w/g, (c: string) => c.toUpperCase())}
            </span>
          </div>
          <div className="mt-2 text-sm text-gray-600 space-y-1">
            {address.fullAddress ? <p>📍 {address.fullAddress}</p> : null}
            {a.rejectionReason ? <p className="text-food-red-600">Reason: {a.rejectionReason}</p> : null}
            {a.failureReason ? <p className="text-food-red-600">Issue: {a.failureReason}</p> : null}
          </div>
        </div>
        <span className="text-xs text-gray-400">
          {new Date(a.updatedAt).toLocaleString()}
        </span>
      </div>
    )
  }

  if (loading) return <Loading />

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Delivery Dashboard</h1>
            <p className="text-gray-500">Accept new orders and manage your deliveries</p>
          </div>
          <div className="flex items-center gap-3">
            <span className={`w-3 h-3 rounded-full ${partnerStatus === 'ONLINE' ? 'bg-food-green-500' : 'bg-gray-400'}`}></span>
            <span className="text-sm font-medium text-gray-700 capitalize">{partnerStatus}</span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Availability</h3>
            <div className="space-y-3">
              <button
                onClick={() => setAvailability('ONLINE')}
                className={`w-full py-3 rounded-xl text-sm font-medium transition-colors ${
                  partnerStatus === 'ONLINE' ? 'bg-bhuk-600 text-white shadow-sm' : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                }`}
              >
                🟢 Go Online
              </button>
              <button
                onClick={() => setAvailability('OFFLINE')}
                className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              >
                ⚫ Go Offline
              </button>
              <button
                onClick={() => setAvailability('BUSY')}
                className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              >
                🟡 Busy
              </button>
            </div>
          </div>

          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">New Orders</h3>
            <p className="text-sm text-gray-500 mb-4">
              {offered.length} order{offered.length !== 1 ? 's' : ''} ready for pickup
            </p>
            <div className="h-12 w-full rounded-xl bg-blue-50 flex items-center justify-center border border-blue-100">
              <span className={`text-sm font-medium ${offered.length > 0 ? 'text-blue-700' : 'text-gray-400'}`}>
                {offered.length > 0 ? 'Accept an order below' : 'No new orders'}
              </span>
            </div>
          </div>

          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Completed Today</h3>
            <p className="text-sm text-gray-500 mb-4">
              {history.filter(a => a.state === 'COMPLETED').length} deliveries
            </p>
            <div className="text-2xl font-bold text-gray-900">₹0.00</div>
          </div>
        </div>

        <div className="mt-8">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-lg text-gray-900 flex items-center gap-2">
              🔔 New Orders ({offered.length})
            </h3>
            {offered.length > 0 && (
              <span className="px-2 py-1 rounded-full bg-blue-100 text-blue-700 text-xs font-bold">
                Requires your action
              </span>
            )}
          </div>
          {offered.length === 0 ? (
            <EmptyState title="No new orders" description="Orders ready for pickup will appear here when they reach the ready-for-pickup stage. Go online to receive offers." icon="📦" />
          ) : (
            <div className="space-y-3">
              {offered.map(renderOfferedItem)}
            </div>
          )}
        </div>

        <div className="mt-8">
          <h3 className="font-bold text-lg text-gray-900 mb-4">Active Deliveries</h3>
          {active.length === 0 ? (
            <EmptyState title="No active deliveries" description="Accept a new order to start delivering." icon="🚚" />
          ) : (
            <div className="space-y-3">
              {active.map(renderActiveItem)}
            </div>
          )}
        </div>

        <div className="mt-8">
          <h3 className="font-bold text-lg text-gray-900 mb-4">Delivery History</h3>
          {history.length === 0 ? (
            <p className="text-sm text-gray-500">No completed, declined, or failed deliveries yet.</p>
          ) : (
            <div className="space-y-3">
              {history.map(renderHistoryItem)}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
