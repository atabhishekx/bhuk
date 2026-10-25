import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '@/api/axios'
import PageLayout from '@/components/PageLayout'
import { EmptyState, Loading } from '@/components/Common'

export default function Cart() {
  const [cart, setCart] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => { fetchCart() }, [])

  async function fetchCart() {
    try {
      const res = await api.get('/cart')
      setCart(res.data.data)
    } catch (e: any) {
      if (e.response?.status === 404) setCart(null)
      else console.error(e)
    } finally {
      setLoading(false)
    }
  }

  async function updateQty(cartItemId: number, qty: number) {
    if (qty <= 0) await api.delete(`/cart/items/${cartItemId}`)
    else await api.put(`/cart/items/${cartItemId}/quantity`, null, { params: { quantity: qty } })
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
      <PageLayout title="Your Cart" subtitle="Add some delicious items from our menu to get started." cartCount={0}>
        <EmptyState
          title="Your cart is empty"
          description="Add some delicious items from our menu to get started."
          icon="🛒"
        />
        <div className="text-center mt-8">
          <a href="/menu" className="btn btn-primary btn-lg">Browse Menu</a>
        </div>
      </PageLayout>
    )
  }

  const subtotal = cart.subtotalMinor
  const tax = cart.taxMinor
  const deliveryFee = cart.deliveryFeeMinor
  const grandTotal = cart.grandTotalMinor

  const money = (minor: number | null | undefined) => (minor == null ? '₹0.00' : `₹${(minor / 100).toFixed(2)}`)

  return (
    <PageLayout title="Your Cart" subtitle={`${cart.itemCount} item${cart.itemCount !== 1 ? 's' : ''} in your cart`} cartCount={cart.itemCount}>
      <div className="flex flex-col lg:flex-row gap-8">
        <section className="flex-1 lg:w-2/3">
          <div className="space-y-4">
            {items.map((item: any) => (
              <article key={item.id} className="card p-4 flex flex-col lg:flex-row lg:items-center gap-4">
                <div className="w-20 h-20 rounded-xl bg-gray-100 flex items-center justify-center flex-shrink-0">
                  <span className="text-3xl">🍽️</span>
                </div>
                <div className="flex-1 min-w-0">
                  <h4 className="font-bold text-gray-900">{item.itemName}</h4>
                  {item.instructions && (
                    <p className="text-sm text-gray-500 mt-1">{item.instructions}</p>
                  )}
                  {item.modifiers?.length && (
                    <div className="flex gap-2 mt-1 flex-wrap">
                      {item.modifiers.map((m: any) => (
                        <span key={m.id} className="badge badge-neutral">
                          +{m.modifierName}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
                <div className="flex items-center gap-4 mt-4 lg:mt-0 flex-wrap">
                  <div className="qty-selector">
                    <button
                      onClick={() => updateQty(item.id, item.quantity - 1)}
                      className="qty-btn"
                      aria-label="Decrease quantity"
                    >
                      −
                    </button>
                    <span className="qty-value">{item.quantity}</span>
                    <button
                      onClick={() => updateQty(item.id, item.quantity + 1)}
                      className="qty-btn"
                      aria-label="Increase quantity"
                    >
                      +
                    </button>
                  </div>
                  <span className="font-bold text-bhuk-600 text-lg ml-auto">
                    ₹{((item.unitPriceMinor + (item.modifiers?.reduce((s: number, m: any) => s + (m.priceDeltaMinor || 0), 0) || 0)) * item.quantity / 100).toFixed(2)}
                  </span>
                  <button
                    onClick={() => removeItem(item.id)}
                    className="text-food-red-500 hover:text-food-red-700 text-sm font-medium btn btn-ghost btn-sm"
                  >
                    Remove
                  </button>
                </div>
              </article>
            ))}
            <button onClick={clearCart} className="btn btn-ghost btn-sm text-food-red-500 hover:text-food-red-700">
              Clear Cart
            </button>
          </div>
        </section>

        <aside className="lg:w-1/3">
          <div className="card p-6 sticky top-24">
            <h3 className="font-bold text-lg text-gray-900 mb-6">Order Summary</h3>
            <div className="space-y-3 mb-6">
              <div className="flex justify-between text-gray-600">
                <span>Subtotal ({cart.itemCount} items)</span>
                <span>{money(subtotal)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Tax (5%)</span>
                <span>{money(tax)}</span>
              </div>
              <div className="flex justify-between text-gray-600">
                <span>Delivery Fee</span>
                <span>{money(deliveryFee)}</span>
              </div>
            </div>
            <div className="border-t border-gray-200 pt-4 mb-6">
              <div className="flex justify-between font-bold text-gray-900 text-lg">
                <span>Total</span>
                <span>{money(grandTotal)}</span>
              </div>
            </div>
            <button
              onClick={() => navigate('/checkout')}
              className="btn btn-primary btn-lg btn-block"
              disabled={items.length === 0}
            >
              Proceed to Checkout
            </button>
            <p className="text-sm text-gray-500 text-center mt-4">
              Prices include all applicable taxes.
            </p>
          </div>
        </aside>
      </div>
    </PageLayout>
  )
}