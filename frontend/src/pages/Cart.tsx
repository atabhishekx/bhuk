import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Loading } from '@/components/Common'

export default function Cart() {
  const [cart, setCart] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    fetchCart()
  }, [])

  async function fetchCart() {
    try {
      const res = await api.get('/cart')
      setCart(res.data.data)
    } catch (e: any) {
      if (e.response?.status === 404) {
        setCart(null)
      } else {
        console.error(e)
      }
    } finally {
      setLoading(false)
    }
  }

  async function updateQty(cartItemId: number, qty: number) {
    if (qty <= 0) {
      await api.delete(`/cart/items/${cartItemId}`)
    } else {
      await api.put(`/cart/items/${cartItemId}/quantity`, null, { params: { quantity: qty } })
    }
    await fetchCart()
  }

  async function removeItem(cartItemId: number) {
    await api.delete(`/cart/items/${cartItemId}`)
    await fetchCart()
  }

  async function clearCart() {
    await api.delete('/cart')
    await fetchCart()
  }

  if (loading) return <Loading />

  const items = cart?.items || []

  if (!cart || items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center text-4xl mb-4">
              🛒
            </div>
            <h3 className="text-lg font-semibold text-gray-900">Your cart is empty</h3>
            <p className="text-gray-500 mt-2">Add some delicious items from our menu to get started.</p>
          </div>
          <div className="mt-8 text-center">
            <a href="/menu" className="btn-primary">
              Browse Menu
            </a>
          </div>
        </div>
      </div>
    )
  }

  const subtotal = cart.subtotalMinor
  const tax = cart.taxMinor
  const deliveryFee = cart.deliveryFeeMinor
  const grandTotal = cart.grandTotalMinor

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex flex-col lg:flex-row gap-8">
          <div className="flex-1 lg:w-2/3">
            <h1 className="text-2xl font-bold text-gray-900 mb-6">Your Cart</h1>
            <div className="space-y-4">
              {items.map((item: any) => (
                <div key={item.id} className="card p-4 flex flex-col lg:flex-row lg:items-center gap-4">
                  <div className="w-20 h-20 rounded-xl bg-gray-100 flex items-center justify-center flex-shrink-0">
                    <span className="text-3xl">🍔</span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <h4 className="font-bold text-gray-900">{item.itemName}</h4>
                    {item.instructions && (
                      <p className="text-sm text-gray-500 mt-1">📝 {item.instructions}</p>
                    )}
                    {item.modifiers?.length && (
                      <div className="flex gap-2 mt-1 flex-wrap">
                        {item.modifiers.map((m: any) => (
                          <span key={m.id} className="px-2 py-1 bg-restru-50 text-restru-700 text-xs rounded-full border border-restru-100">
                            +{m.modifierName}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                  <div className="flex items-center gap-4 mt-4 lg:mt-0">
                    <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
                      <button
                        onClick={() => updateQty(item.id, item.quantity - 1)}
                        className="px-3 py-2 bg-gray-50 hover:bg-gray-100 border-r border-gray-200 text-gray-600"
                      >
                        −
                      </button>
                      <span className="px-4 py-2 font-medium text-gray-900">{item.quantity}</span>
                      <button
                        onClick={() => updateQty(item.id, item.quantity + 1)}
                        className="px-3 py-2 bg-gray-50 hover:bg-gray-100 border-l border-gray-200 text-gray-600"
                      >
                        +
                      </button>
                    </div>
                    <span className="font-bold text-restru-600 ml-auto">
                      ₹{((item.unitPriceMinor + (item.modifiers?.reduce((s: number, m: any) => s + (m.priceDeltaMinor || 0), 0) || 0)) * item.quantity / 100).toFixed(2)}
                    </span>
                    <button
                      onClick={() => removeItem(item.id)}
                      className="text-food-red-500 hover:text-food-red-700 text-sm font-medium"
                    >
                      Remove
                    </button>
                  </div>
                </div>
              ))}
              <button onClick={clearCart} className="text-food-red-500 hover:text-food-red-700 text-sm mt-4 font-medium">
                Clear Cart
              </button>
            </div>
          </div>

          <div className="lg:w-1/3">
            <div className="card p-6 sticky top-24">
              <h3 className="font-bold text-lg text-gray-900 mb-6">Order Summary</h3>
              <div className="space-y-3 mb-6">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal ({cart.itemCount} items)</span>
                  <span>₹{(subtotal / 100).toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Tax (5%)</span>
                  <span>₹{(tax / 100).toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Delivery Fee</span>
                  <span>₹{(deliveryFee / 100).toFixed(2)}</span>
                </div>
              </div>
              <div className="border-t border-gray-200 pt-4 mb-6">
                <div className="flex justify-between font-bold text-gray-900 text-lg">
                  <span>Total</span>
                  <span>₹{(grandTotal / 100).toFixed(2)}</span>
                </div>
              </div>
                <button
                  onClick={() => navigate('/checkout')}
                  className="btn-primary w-full mb-4"
                  disabled={items.length === 0}
                >
                  Proceed to Checkout
                </button>
              <p className="text-sm text-gray-500 text-center">
                Prices include all applicable taxes.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
