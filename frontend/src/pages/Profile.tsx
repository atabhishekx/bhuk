import { useState, useEffect } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Alert } from '@/components/Common'

export default function Profile() {
  const { user, logout } = useAuth()
  const [addresses, setAddresses] = useState<any[]>([])
  const [showForm, setShowForm] = useState(false)
  const [newAddress, setNewAddress] = useState({
    fullAddress: '', city: '', state: '', pincode: '', phone: '', isDefault: false
  })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    fetchAddresses()
  }, [])

  async function fetchAddresses() {
    try {
      const res = await api.get('/me/addresses')
      setAddresses(res.data.data)
    } catch (e) {
      console.error(e)
    }
  }

  async function addAddress(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await api.post('/me/addresses', newAddress)
      setShowForm(false)
      setNewAddress({ fullAddress: '', city: '', state: '', pincode: '', phone: '', isDefault: false })
      await fetchAddresses()
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to add address')
    } finally {
      setSubmitting(false)
    }
  }

  async function deleteAddress(id: number) {
    if (!confirm('Delete this address?')) return
    try {
      await api.delete(`/me/addresses/${id}`)
      await fetchAddresses()
    } catch (e) {
      console.error(e)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex flex-col lg:flex-row lg:items-start gap-8">
          <div className="flex-1 lg:w-1/3">
            <div className="card p-6">
              <div className="flex items-center gap-4 mb-6">
                <div className="w-16 h-16 rounded-full bg-restru-600 flex items-center justify-center text-white font-bold text-2xl">
                  {user?.name?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <div>
                  <h2 className="font-bold text-gray-900">{user?.name}</h2>
                  <p className="text-sm text-gray-500">{user?.email}</p>
                  <p className="text-xs text-restru-600 capitalize mt-1">{user?.roles.join(', ') || 'customer'}</p>
                </div>
              </div>
              <div className="space-y-4">
                <div className="flex items-center gap-4 p-3 bg-gray-50 rounded-xl">
                  <span className="text-gray-500">📱</span>
                  <span className="text-gray-900">{user?.phone || 'Not provided'}</span>
                </div>
                <div className="flex items-center gap-4 p-3 bg-gray-50 rounded-xl">
                  <span className="text-gray-500">🏷️</span>
                  <span className="text-gray-900">{user?.roles.join(', ') || 'customer'}</span>
                </div>
              </div>
              <button onClick={logout} className="mt-6 w-full btn-outline">
                Sign Out
              </button>
            </div>
          </div>

          <div className="lg:w-2/3">
            <div className="card p-6">
              <div className="flex justify-between items-center mb-6">
                <div>
                  <h2 className="font-bold text-lg text-gray-900">Delivery Addresses</h2>
                  <p className="text-sm text-gray-500">Manage your saved addresses</p>
                </div>
                <button
                  onClick={() => setShowForm(!showForm)}
                  className="text-sm font-medium text-restru-600 hover:text-restru-700"
                >
                  {showForm ? 'Cancel' : 'Add Address'}
                </button>
              </div>

              {error && <Alert type="error" message={error} />}

              {showForm && (
                <div className="mb-6">
                  <form onSubmit={addAddress} className="space-y-4">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="label">Full Address</label>
                        <input
                          name="fullAddress"
                          value={newAddress.fullAddress}
                          onChange={(e) => setNewAddress({ ...newAddress, fullAddress: e.target.value })}
                          className="input-field" required placeholder="123 Main Street"
                        />
                      </div>
                      <div>
                        <label className="label">City</label>
                        <input
                          name="city"
                          value={newAddress.city}
                          onChange={(e) => setNewAddress({ ...newAddress, city: e.target.value })}
                          className="input-field" required placeholder="City"
                        />
                      </div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="label">State</label>
                        <input
                          name="state"
                          value={newAddress.state}
                          onChange={(e) => setNewAddress({ ...newAddress, state: e.target.value })}
                          className="input-field" required placeholder="State"
                        />
                      </div>
                      <div>
                        <label className="label">Pincode</label>
                        <input
                          name="pincode"
                          value={newAddress.pincode}
                          onChange={(e) => setNewAddress({ ...newAddress, pincode: e.target.value })}
                          className="input-field" required placeholder="110001"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="label">Phone</label>
                      <input
                        name="phone"
                        value={newAddress.phone}
                        onChange={(e) => setNewAddress({ ...newAddress, phone: e.target.value })}
                        className="input-field" required placeholder="+91-9999999999"
                      />
                    </div>
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        name="isDefault"
                        checked={newAddress.isDefault}
                        onChange={(e) => setNewAddress({ ...newAddress, isDefault: e.target.checked })}
                      />
                      Set as default address
                    </label>
                    <button
                      type="submit"
                      disabled={submitting}
                      className="btn-primary w-full py-3"
                    >
                      {submitting ? 'Saving...' : 'Save Address'}
                    </button>
                  </form>
                </div>
              )}

              {addresses.length === 0 ? (
                <div className="text-center py-12">
                  <span className="text-4xl">📍</span>
                  <h3 className="mt-4 text-lg font-semibold text-gray-900">No saved addresses</h3>
                  <p className="text-gray-500">Add an address to use during checkout.</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {addresses.map((addr) => (
                    <div key={addr.id} className="flex items-start gap-3 p-3 rounded-xl border border-gray-100 bg-white ${addr.isDefault ? 'bg-restru-50 border-restru-100' : ''}">
                      <span className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${addr.isDefault ? 'bg-restru-600 text-white' : 'bg-gray-200 text-gray-600'}`}>
                        {addr.isDefault ? 'DEFAULT' : addr.city?.charAt(0)?.toUpperCase()}
                      </span>
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{addr.fullAddress}</p>
                        <p className="text-sm text-gray-500">{addr.city}, {addr.state} - {addr.pincode}</p>
                        <p className="text-sm text-gray-500">{addr.phone}</p>
                      </div>
                      <button onClick={() => deleteAddress(addr.id)} className="text-food-red-500 hover:text-food-red-700 text-sm">
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
