import { useCallback, useEffect, useRef, useState } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Alert, EmptyState, Loading } from '@/components/Common'

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
}

const labelFor = (s: string) => s.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())
const money = (minor: number | null | undefined) => (minor == null ? '₹0.00' : `₹${(minor / 100).toFixed(2)}`)

export default function AdminDashboard() {
  const [stats, setStats] = useState<any>(null)
  const [orders, setOrders] = useState<any[]>([])
  const [outlets, setOutlets] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [liveEnabled, setLiveEnabled] = useState(true)
  const [lastRefreshed, setLastRefreshed] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const fetchStats = useCallback(async () => {
    try {
      const res = await api.get('/admin/reports/operations')
      setStats(res.data.data)
    } catch (e) {
      console.error('Failed to fetch stats', e)
    }
  }, [])

  const fetchRecentOrders = useCallback(async () => {
    try {
      setLoading(true)
      const res = await api.get<{ data: any[] }>('/admin/orders')
      const data = res.data.data ?? []
      setOrders(data)
      setLastRefreshed(new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }))
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to load orders')
    } finally {
      setLoading(false)
    }
  }, [])

  async function fetchOutlets() {
    try {
      const res = await api.get('/admin/outlets')
      setOutlets(res.data.data || [])
    } catch (e) {
      console.error('Failed to fetch outlets', e)
    }
  }

  useEffect(() => {
    fetchStats()
    fetchRecentOrders()
    fetchOutlets()
  }, [fetchStats, fetchRecentOrders])

  useEffect(() => {
    if (!liveEnabled) {
      if (pollRef.current) clearInterval(pollRef.current)
      pollRef.current = null
      return
    }
    pollRef.current = setInterval(fetchRecentOrders, 5000)
    return () => {
      if (pollRef.current) clearInterval(pollRef.current)
      pollRef.current = null
    }
  }, [liveEnabled, fetchRecentOrders])

  if (loading) return <Loading />

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <Alert type="error" message={error} />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
            <p className="text-gray-500">
              Chain-wide operations overview <span className="text-xs text-gray-400">· Last refreshed {lastRefreshed || '—'}</span>
            </p>
          </div>
          <div className="flex items-center gap-3">
            <span className={`w-3 h-3 rounded-full bg-restru-600`}></span>
            <span className="text-sm font-medium text-gray-700">System Operational</span>
            <label className="flex items-center gap-2 text-sm text-gray-600 cursor-pointer select-none ml-4">
              <input
                type="checkbox"
                checked={liveEnabled}
                onChange={e => setLiveEnabled(e.target.checked)}
                className="accent-restru-600"
              />
              Live updates (5s)
            </label>
          </div>
        </div>

        {/* Outlet selector */}
        {outlets.length > 0 && (
          <div className="mb-6">
            <div className="card p-4">
              <div className="flex items-center gap-3">
                <span className="text-sm font-medium text-gray-700">Viewing outlet:</span>
                <select
                  className="px-3 py-2 rounded-lg border border-gray-200 text-sm bg-white focus:ring-2 focus:ring-restru-500 focus:border-transparent"
                  defaultValue={stats?.outletId || outlets[0]?.id}
                >
                  {outlets.map((o: any) => (
                    <option key={o.id} value={o.id}>
                      {o.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>
        )}

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">Total Orders</p>
                <p className="text-2xl font-bold text-gray-900">{stats?.totalOrders || 0}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center text-blue-600 text-xl">
                📦
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">Total Revenue</p>
                <p className="text-2xl font-bold text-gray-900">
                  ₹{((stats?.grandTotalRevenueMinor || 0) / 100).toFixed(2)}
                </p>
              </div>
              <div className="w-12 h-12 bg-food-green-100 rounded-xl flex items-center justify-center text-food-green-600 text-xl">
                ₹
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">Delivered</p>
                <p className="text-2xl font-bold text-gray-900">{stats?.ordersByStatusDelivered || 0}</p>
              </div>
              <div className="w-12 h-12 bg-food-green-100 rounded-xl flex items-center justify-center text-food-green-600 text-xl">
                ✅
              </div>
            </div>
          </div>
          <div className="card p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">Pending</p>
                <p className="text-2xl font-bold text-gray-900">
                  {(stats?.ordersByStatusPlaced || 0) + (stats?.ordersByStatusAccepted || 0)}
                </p>
              </div>
              <div className="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center text-orange-600 text-xl">
                ⏳
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Status Distribution */}
          <div className="lg:col-span-2">
            <div className="card p-6">
              <h3 className="font-bold text-lg text-gray-900 mb-6">Orders by Status</h3>
              <div className="space-y-3">
                {[
                  { key: 'PLACED', label: 'Placed', value: stats?.ordersByStatusPlaced || 0, color: 'bg-blue-100 text-blue-700' },
                  { key: 'ACCEPTED', label: 'Accepted', value: stats?.ordersByStatusAccepted || 0, color: 'bg-blue-100 text-blue-700' },
                  { key: 'PREPARING', label: 'Preparing', value: stats?.ordersByStatusPreparing || 0, color: 'bg-orange-100 text-orange-700' },
                  { key: 'READY_FOR_PICKUP', label: 'Ready for Pickup', value: stats?.ordersByStatusReadyForPickup || 0, color: 'bg-orange-100 text-orange-700' },
                  { key: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', value: stats?.ordersByStatusOutForDelivery || 0, color: 'bg-indigo-100 text-indigo-700' },
                  { key: 'DELIVERED', label: 'Delivered', value: stats?.ordersByStatusDelivered || 0, color: 'bg-food-green-100 text-food-green-700' },
                  { key: 'CANCELLED', label: 'Cancelled', value: stats?.ordersByStatusCancelled || 0, color: 'bg-gray-100 text-gray-700' },
                  { key: 'REJECTED', label: 'Rejected', value: stats?.ordersByStatusRejected || 0, color: 'bg-food-red-100 text-food-red-700' },
                ].map((s) => (
                  <div key={s.key} className="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-white">
                    <span className="text-sm text-gray-700">{s.label}</span>
                    <span className={`px-2 py-1 rounded text-xs font-medium ${s.color}`}>
                      {s.value}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-6">Quick Actions</h3>
            <div className="space-y-3">
              <a href="/admin/orders" className="w-full py-3 rounded-xl text-sm font-medium bg-restru-600 text-white hover:bg-restru-700 transition-colors flex items-center justify-between">
                <span>View All Orders</span>
                <span className="text-restru-200">→</span>
              </a>
              <a href="/admin/menu" className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors flex items-center justify-between">
                <span>Manage Menu Items</span>
                <span className="text-gray-400">→</span>
              </a>
              <button className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors flex items-center justify-between disabled:opacity-50 disabled:pointer-events-none">
                <span>Manage Users & Roles</span>
                <span className="text-gray-400">→</span>
              </button>
              <button className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors flex items-center justify-between disabled:opacity-50 disabled:pointer-events-none">
                <span>View Reports</span>
                <span className="text-gray-400">→</span>
              </button>
            </div>
          </div>
        </div>

        {/* Recent Orders */}
        <div className="mt-8">
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-bold text-lg text-gray-900">Recent Orders</h3>
            <a href="/admin/orders" className="text-sm font-medium text-restru-600 hover:text-restru-700">
              View All Orders →
            </a>
          </div>
          {orders.length === 0 ? (
            <EmptyState title="No orders yet" description="No orders have been placed." icon="📦" />
          ) : (
            <div className="space-y-3">
              {orders.slice(0, 5).map((order: any) => (
                <a
                  key={order.id}
                  href={`/admin/orders`}
                  className="card p-4 flex justify-between items-start hover:shadow-md transition-shadow border"
                  onClick={(e) => {
                    e.preventDefault()
                  }}
                >
                  <div className="flex-1">
                    <div className="flex justify-between items-start mb-2">
                      <h4 className="font-bold text-gray-900">Order #{order.orderNumber}</h4>
                      <span className={`px-2 py-1 rounded text-xs font-medium ${statusColors[order.status] || 'bg-gray-100 text-gray-700 border-gray-200'}`}>
                        {labelFor(order.status)}
                      </span>
                    </div>
                    <p className="text-sm text-gray-500">{order.customerEmail}</p>
                  </div>
                  <span className="font-bold text-restru-600">{money(order.grandTotalMinor)}</span>
                </a>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
