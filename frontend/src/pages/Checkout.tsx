import { useState, useEffect } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Loading, Alert } from '@/components/Common'

export default function Checkout() {
  const [cart, setCart] = useState<any>(null)
  const [quote, setQuote] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [orderId, setOrderId] = useState<number | null>(null)

  useEffect(() => {
    fetchData()
  }, [])

  async function fetchData() {
    try {
      const [cartRes, quoteRes] = await Promise.all([
        api.get('/cart'),
        api.get('/cart/checkout/quote'),
      ])
      setCart(cartRes.data.data)
      setQuote(quoteRes.data.data)
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to load cart')
    } finally {
      setLoading(false)
    }
  }

  async function placeOrder(e: React.FormEvent) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      const formData = new FormData(e.target as HTMLFormElement)
      const address = {
        fullAddress: formData.get('fullAddress') as string,
        city: formData.get('city') as string,
        state: formData.get('state') as string,
        pincode: formData.get('pincode') as string,
        phone: formData.get('phone') as string,
      }
      const deliveryAddressJson = JSON.stringify(address)

      const res = await api.post('/orders', {
        idempotencyKey: `order-${Date.now()}`,
        deliveryAddress: address,
        deliveryAddressJson,
      })
      setOrderId(res.data.data.id)
      setSuccess(true)
    } catch (e: any) {
      setError(e.response?.data?.error?.message || 'Failed to place order')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <Loading />

  if (!cart || cart.items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center text-4xl mb-4">
              🛒
            </div>
            <h3 className="text-lg font-semibold text-gray-900">Your cart is empty</h3>
            <p className="text-gray-500 mt-2">Add items before checkout.</p>
            <a href="/menu" className="mt-6 inline-block btn-primary">Browse Menu</a>
          </div>
        </div>
      </div>
    )
  }

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 text-center">
          <div className="w-16 h-16 bg-food-green-100 rounded-full flex items-center justify-center text-4xl mx-auto mb-4">
            ✅
          </div>
          <h2 className="text-2xl font-bold text-gray-900">Order Placed!</h2>
          <p className="text-gray-500 mt-2">Your order has been placed.</p>
          <a href={`/orders/${orderId}`} className="mt-6 inline-block btn-primary">
            View Order
          </a>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Left: Order Summary */}
          <div className="flex-1 lg:w-1/2">
            <div className="card p-6">
              <h3 className="font-bold text-lg text-gray-900 mb-6">Order Summary</h3>
              {cart.items.map((item: any) => (
                <div key={item.id} className="flex justify-between mb-3 pb-3 border-b border-gray-50 last:border-0">
                  <div>
                    <span className="font-medium text-gray-900">{item.itemName}</span>
                    <br />
                    <span className="text-sm text-gray-500">x{item.quantity}</span>
                  </div>
                  <span className="font-medium text-gray-900">
                    ₹{((item.unitPriceMinor || 0) * item.quantity / 100).toFixed(2)}
                  </span>
                </div>
              ))}
              <div className="mt-6 space-y-2">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span>
                  <span>₹{(quote.subtotalMinor / 100).toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Tax</span>
                  <span>₹{(quote.taxMinor / 100).toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Delivery Fee</span>
                  <span>₹{(quote.deliveryFeeMinor / 100).toFixed(2)}</span>
                </div>
                <div className="flex justify-between font-bold text-gray-900 pt-2 border-t border-gray-200">
                  <span>Total</span>
                  <span>₹{(quote.grandTotalMinor / 100).toFixed(2)}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Right: Delivery Address */}
          <div className="lg:w-1/2">
            <div className="card p-6">
              <h3 className="font-bold text-lg text-gray-900 mb-6">Delivery Address</h3>
              <form onSubmit={placeOrder} className="space-y-4">
                {error && <Alert type="error" message={error} />}
                <div>
                  <label className="label">Full Address</label>
                  <input
                    type="text"
                    name="fullAddress"
                    className="input-field"
                    required
                    placeholder="123 Main Street, Apt 4B"
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="label">City</label>
                    <input type="text" name="city" className="input-field" required placeholder="City" />
                  </div>
                  <div>
                    <label className="label">State</label>
                    <input type="text" name="state" className="input-field" required placeholder="State" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="label">Pincode</label>
                    <input type="text" name="pincode" className="input-field" required placeholder="110001" />
                  </div>
                  <div>
                    <label className="label">Phone</label>
                    <input type="tel" name="phone" className="input-field" required placeholder="+91-9999999999" />
                  </div>
                </div>
                <button
                  type="submit"
                  disabled={submitting}
                  className="btn-primary w-full py-3"
                >
                  {submitting ? 'Placing Order...' : 'Place Order'}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
