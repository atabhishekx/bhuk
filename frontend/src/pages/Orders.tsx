import { useEffect, useState } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { EmptyState, Loading } from '@/components/Common'

type Order = {
  id: number
  orderNumber: string
  status: string
  grandTotalMinor: number
  createdAt: string
  items: any[]
}

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchOrders()
  }, [])

  async function fetchOrders() {
    try {
      const res = await api.get('/orders')
      setOrders(res.data.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  const statusColors: Record<string, string> = {
    PLACED: 'bg-blue-100 text-blue-700',
    ACCEPTED: 'bg-blue-100 text-blue-700',
    PREPARING: 'bg-orange-100 text-orange-700',
    READY_FOR_PICKUP: 'bg-orange-100 text-orange-700',
    PICKED_UP: 'bg-purple-100 text-purple-700',
    OUT_FOR_DELIVERY: 'bg-indigo-100 text-indigo-700',
    DELIVERED: 'bg-food-green-100 text-food-green-700',
    REJECTED: 'bg-food-red-100 text-food-red-700',
    CANCELLED: 'bg-gray-100 text-gray-700',
  }

  if (loading) return <Loading />

  if (orders.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <EmptyState title="No orders yet" description="Place your first order from our menu." icon="📦" />
          <div className="mt-8 text-center">
            <a href="/menu" className="btn-primary">Browse Menu</a>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>
        <div className="space-y-4">
          {orders.map((order) => (
            <div key={order.id} className="card p-5 hover:shadow-md transition-shadow cursor-pointer" onClick={() => { window.location.href = `/orders/${order.id}` }}>
              <div className="flex justify-between items-start mb-3">
                <div>
                  <h3 className="font-bold text-gray-900">Order #{order.orderNumber}</h3>
                  <p className="text-sm text-gray-500">{new Date(order.createdAt).toLocaleDateString()}</p>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusColors[order.status] || 'bg-gray-100 text-gray-700'}`}>
                  {order.status.replace(/_/g, ' ').toLowerCase()}
                </span>
              </div>
              <div className="flex justify-between items-center mb-3">
                <span className="text-gray-600 text-sm">{order.items.length} item(s)</span>
                <span className="font-bold text-restru-600">₹{(order.grandTotalMinor / 100).toFixed(2)}</span>
              </div>
              <div className="h-1 bg-gray-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-restru-600"
                  style={{ width: order.status === 'DELIVERED' || order.status === 'CANCELLED' || order.status === 'REJECTED' ? '100%' : '50%' }}
                ></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
