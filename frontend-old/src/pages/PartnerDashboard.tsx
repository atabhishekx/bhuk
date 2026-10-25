?import { useEffect, useState } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Loading, EmptyState } from '@/components/Common'

export default function PartnerDashboard() {
  const [assignments, setAssignments] = useState<any[]>([])
  const [partnerStatus, setPartnerStatus] = useState('OFFLINE')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchData()
  }, [])

  async function fetchData() {
    try {
      const [assignRes, ] = await Promise.all([
        api.get('/partner/assignments'),
      ])
      setAssignments(assignRes.data.data)
      // Try to get partner status from profile
      try {
        const profRes = await api.get('/me')
        setPartnerStatus(profRes.data.data.roles.includes('PARTNER') ? 'ONLINE' : 'OFFLINE')
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
    OFFERED: 'bg-blue-100 text-blue-700',
    ASSIGNED: 'bg-purple-100 text-purple-700',
    ACCEPTED: 'bg-indigo-100 text-indigo-700',
    PICKED_UP: 'bg-orange-100 text-orange-700',
    COMPLETED: 'bg-food-green-100 text-food-green-700',
    DECLINED: 'bg-gray-100 text-gray-700',
    FAILED: 'bg-food-red-100 text-food-red-700',
  }

  if (loading) return <Loading />

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Delivery Dashboard</h1>
            <p className="text-gray-500">Manage your deliveries and availability</p>
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
                �YY� Go Online
              </button>
              <button
                onClick={() => setAvailability('OFFLINE')}
                className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              >
                �s� Go Offline
              </button>
              <button
                onClick={() => setAvailability('BUSY')}
                className="w-full py-3 rounded-xl text-sm font-medium bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              >
                �YY� Busy
              </button>
            </div>
          </div>

          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Active Assignments</h3>
            <p className="text-sm text-gray-500 mb-4">
              {assignments.filter(a => a.state !== 'COMPLETED' && a.state !== 'DECLINED' && a.state !== 'FAILED').length} active
            </p>
            <div className="h-12 w-full rounded-xl bg-gray-100 flex items-center justify-center">
              <span className="text-gray-400 text-sm">{assignments.length} total</span>
            </div>
          </div>

          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Completed Today</h3>
            <p className="text-sm text-gray-500 mb-4">
              {assignments.filter(a => a.state === 'COMPLETED').length} deliveries
            </p>
            <div className="text-2xl font-bold text-gray-900">�,�0.00</div>
          </div>
        </div>

        <div className="mt-8">
          <h3 className="font-bold text-lg text-gray-900 mb-4">Assignment History</h3>
          {assignments.length === 0 ? (
            <EmptyState title="No assignments" description="No delivery assignments yet." icon="�Yss" />
          ) : (
            <div className="space-y-3">
              {assignments.map((a) => (
                <div key={a.id} className="card p-4 flex justify-between items-start">
                  <div className="flex-1">
                    <h4 className="font-bold text-gray-900">Order #{a.orderNumber}</h4>
                    <p className="text-sm text-gray-500">{a.partnerName || 'Unassigned'}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 rounded text-xs font-medium ${statusColors[a.state] || 'bg-gray-100 text-gray-700'}`}>
                      {a.state.replace(/_/g, ' ').toLowerCase()}
                    </span>
                    {a.state === 'OFFERED' && (
                      <div className="flex gap-2">
                        <button onClick={() => acceptAssignment(a.id)} className="px-3 py-1 bg-food-green-50 text-food-green-700 text-sm rounded hover:bg-food-green-100">
                          Accept
                        </button>
                        <button onClick={() => declineAssignment(a.id)} className="px-3 py-1 bg-food-red-50 text-food-red-700 text-sm rounded hover:bg-food-red-100">
                          Decline
                        </button>
                      </div>
                    )}
                    {a.state === 'ACCEPTED' && (
                      <button onClick={() => markPickedUp(a.id)} className="px-3 py-1 bg-bhuk-600 text-white text-sm rounded hover:bg-bhuk-700">
                        Picked Up
                      </button>
                    )}
                    {a.state === 'PICKED_UP' && (
                      <button onClick={() => markCompleted(a.id)} className="px-3 py-1 bg-food-green-600 text-white text-sm rounded hover:bg-food-green-700">
                        Complete
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

