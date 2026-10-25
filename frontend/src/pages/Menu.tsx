import { useState, useEffect } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { EmptyState, Loading } from '@/components/Common'
import logo from '@/assets/logo.png'

type Category = {
  id: number
  name: string
  description?: string
}

type Item = {
  id: number
  name: string
  description?: string
  basePriceMinor: number
  available: boolean
  categoryId?: number
}

export default function Menu() {
  const { token } = useAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [items, setItems] = useState<Item[]>([])
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null)
  const [loading, setLoading] = useState(true)
  const [cartCount, setCartCount] = useState(0)
  const [addToCartError, setAddToCartError] = useState<string | null>(null)

  useEffect(() => {
    if (token) {
      fetchMenu()
      fetchCartCount()
    } else {
      setLoading(false)
    }
  }, [token])

  async function fetchMenu() {
    try {
      const [catRes, itemRes] = await Promise.all([
        api.get('/menu/categories'),
        api.get('/menu/items'),
      ])
      setCategories(catRes.data.data)
      setItems(itemRes.data.data)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  async function fetchCartCount() {
    try {
      const res = await api.get('/cart')
      setCartCount(res.data.data.itemCount || 0)
    } catch (e) {
      setCartCount(0)
    }
  }

  async function addToCart(item: Item) {
    if (!token) {
      setAddToCartError('Please log in to add items to your cart.')
      return
    }
    setAddToCartError(null)
    try {
      await api.post('/cart/items', {
        itemId: item.id,
        quantity: 1,
      })
      setCartCount((prev) => prev + 1)
    } catch (e: any) {
      if (e.response?.status === 401) {
        window.location.href = '/login'
      } else {
        setAddToCartError(e.response?.data?.error?.message || 'Failed to add to cart')
      }
    }
  }

  if (!token) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="w-full max-w-md">
          <div className="text-center mb-8">
            <img src={logo} alt="Bhuk logo" className="w-16 h-16 rounded-full object-cover mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900">Login Required</h2>
            <p className="text-gray-500">Please sign in to browse the menu and order food.</p>
          </div>
          <div className="text-center mt-8">
            <button
              onClick={() => window.location.href = '/login'}
              className="btn-primary w-full py-3"
            >
              Sign In
            </button>
          </div>
        </div>
      </div>
    )
  }

  if (loading) return <Loading />

  if (addToCartError) {
    setTimeout(() => setAddToCartError(null), 4000)
  }

  const filteredItems = selectedCategory === null ? items : items.filter(i => i.categoryId === selectedCategory)

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar cartCount={cartCount} />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {addToCartError && (
          <div className="mb-6 p-3 rounded-xl bg-food-red-50 border border-food-red-200 text-food-red-700 text-sm">
            {addToCartError}
          </div>
        )}
        <div className="flex flex-col lg:flex-row lg:items-start gap-6">
          <div className="w-full lg:w-64 flex-shrink-0">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Categories</h2>
            <div className="space-y-2">
              <button
                onClick={() => setSelectedCategory(null)}
                className={`w-full text-left px-4 py-2 rounded-xl text-sm font-medium transition-colors ${
                  selectedCategory === null
                    ? 'bg-bhuk-600 text-white'
                    : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                }`}
              >
                All Items ({items.length})
              </button>
              {categories.map((cat) => (
                <button
                  key={cat.id}
                  onClick={() => setSelectedCategory(cat.id)}
                  className={`w-full text-left px-4 py-2 rounded-xl text-sm font-medium transition-colors ${
                    selectedCategory === cat.id
                      ? 'bg-bhuk-600 text-white'
                      : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  {cat.name}
                </button>
              ))}
            </div>
          </div>

          <div className="flex-1">
            <div className="flex items-center justify-between mb-6">
              <h1 className="text-2xl font-bold text-gray-900">
                {selectedCategory === null ? 'Menu' : categories.find(c => c.id === selectedCategory)?.name}
              </h1>
              <span className="text-sm text-gray-500">{filteredItems.length} items</span>
            </div>

            {filteredItems.length === 0 ? (
              <EmptyState title="No items available" description="No menu items found in this category." icon="🍽️" />
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {filteredItems.map((item) => (
                  <div key={item.id} className="card p-5 hover:shadow-md transition-shadow cursor-pointer group">
                    <div className="w-full h-32 rounded-xl bg-gray-100 flex items-center justify-center overflow-hidden mb-3 relative group-hover:scale-105 transition-transform">
                      <span className="absolute inset-0 flex items-center justify-center text-gray-400 text-3xl">
                        🍽️
                      </span>
                    </div>
                    <div className="flex items-center justify-between mb-1">
                      <h3 className="font-bold text-gray-900 text-lg truncate">{item.name}</h3>
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                        item.available ? 'bg-food-green-100 text-food-green-700' : 'bg-gray-100 text-gray-500'
                      }`}>
                        {item.available ? 'Available' : 'Unavailable'}
                      </span>
                    </div>
                    <p className="text-sm text-gray-500 line-clamp-2 mb-3">{item.description || 'No description'}</p>
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-bhuk-600">₹{(item.basePriceMinor / 100).toFixed(2)}</span>
                      {!item.available ? (
                        <span className="text-sm text-food-red-500 font-medium">Unavailable</span>
                      ) : (
                        <button
                          onClick={(e) => {
                            e.stopPropagation()
                            addToCart(item)
                          }}
                          className="bg-bhuk-600 hover:bg-bhuk-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors active:scale-95"
                        >
                          Add
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
    </div>
  )
}
