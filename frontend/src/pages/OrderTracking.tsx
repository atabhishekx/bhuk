import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Loading, EmptyState } from '@/components/Common'

type OrderDetail = {
  id: number
  orderNumber: string
  status: string
  grandTotalMinor: number
  items: any[]
  statusEvents: any[]
}

const statusOrder: string[] = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY_FOR_PICKUP', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED']

export default function OrderTracking() {
  const { id } = useParams()
  const [order, setOrder] = useState<OrderDetail | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchOrder()
  }, [id])

  async function fetchOrder() {
    try {
      const res = await api.get(`/orders/${id}`)
      setOrder(res.data.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <Loading />
  if (!order) return <EmptyState title="Order not found" description="The order you are looking for does not exist." icon="❌" />

  const currentIndex = statusOrder.indexOf(order.status)
  const progress = ((currentIndex + 1) / statusOrder.length) * 100

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Order #{order.orderNumber}</h1>
          <span className={`px-3 py-1 rounded-full text-xs font-medium ${
            order.status === 'DELIVERED' ? 'bg-food-green-100 text-food-green-700' :
            order.status === 'REJECTED' || order.status === 'CANCELLED' ? 'bg-food-red-100 text-food-red-700' :
            'bg-blue-100 text-blue-700'
          }`}>
            {order.status.replace(/_/g, ' ').toLowerCase()}
          </span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Order Progress */}
          <div className="lg:col-span-2">
            <div className="card p-6 mb-6">
              <h3 className="font-bold text-lg text-gray-900 mb-4">Order Status</h3>
              <div className="relative">
                <div className="h-1 bg-gray-200 rounded-full w-full"></div>
                <div className="h-1 bg-restru-600 rounded-full" style={{ width: `${progress}%` }}></div>
                <div className="flex justify-between mt-4 text-xs text-gray-500">
                  {statusOrder.map((s, i) => (
                    <span key={s} className={`text-center font-medium ${
                      i <= currentIndex ? 'text-restru-700' : 'text-gray-400'
                    }`}>
                      {s.replace(/_/g, ' ').toLowerCase()}
                    </span>
                  ))}
                </div>
              </div>
            </div>

            {/* Order Items */}
            <div className="card p-6">
              <h3 className="font-bold text-lg text-gray-900 mb-4">Items</h3>
              <div className="space-y-3">
                {order.items.map((item: any) => (
                  <div key={item.id} className="flex justify-between items-start py-3 border-b border-gray-50 last:border-0">
                    <div className="flex-1">
                      <div className="flex justify-between">
                        <span className="font-medium text-gray-900">{item.itemName}</span>
                        <span className="font-medium text-gray-900">₹{(item.lineTotalMinor / 100).toFixed(2)}</span>
                      </div>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-sm text-gray-500">Qty: {item.quantity}</span>
                        {item.modifiers?.length && (
                          <span className="text-xs bg-restru-50 text-restru-700 px-2 py-0.5 rounded border border-restru-100">
                            +{item.modifiers.map((m: any) => m.nameSnapshot).join(', ')}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Summary */}
          <div className="lg:col-span-1">
            <div className="card p-6 sticky top-24">
              <h3 className="font-bold text-lg text-gray-900 mb-6">Order Summary</h3>
              <div className="space-y-3 mb-6">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span>
                  <span>₹{(order.grandTotalMinor / 100).toFixed(2)}</span>
                </div>
              </div>
              <div className="pt-4 border-t border-gray-200">
                <p className="text-sm text-gray-500 mb-4">Status History</p>
                <div className="space-y-3">
                  {order.statusEvents?.map((event: any) => (
                    <div key={event.id} className="flex items-start gap-3">
                      <div className="w-3 h-3 rounded-full bg-gray-300 mt-1 flex-shrink-0"></div>
                      <div>
                        <p className="text-sm text-gray-900">{event.newStatus.replace(/_/g, ' ').toLowerCase()}</p>
                        <p className="text-xs text-gray-500">{new Date(event.createdAt).toLocaleString()}</p>
                        {event.reason && <p className="text-xs text-gray-400 italic">{event.reason}</p>}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
