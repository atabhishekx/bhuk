import { useState, useEffect, useCallback } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import api from '@/api/axios'
import PageLayout from '@/components/PageLayout'
import { EmptyState, Skeleton } from '@/components/Common'
import { useToastHelpers } from '@/components/Toast'
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
  imageUrl?: string
  dietaryType?: string
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
  const [addingToCart, setAddingToCart] = useState<number | null>(null)
  const { success: toastSuccess, error: toastError } = useToastHelpers()

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

  const addToCart = useCallback(async (item: Item) => {
    if (!token) {
      setAddToCartError('Please log in to add items to your cart.')
      return
    }
    setAddingToCart(item.id)
    setAddToCartError(null)
    try {
      await api.post('/cart/items', { itemId: item.id, quantity: 1 })
      setCartCount(prev => prev + 1)
      toastSuccess(`${item.name} added to cart`)
    } catch (e: any) {
      if (e.response?.status === 401) {
        window.location.href = '/login'
      } else {
        const msg = e.response?.data?.error?.message || 'Failed to add to cart'
        setAddToCartError(msg)
        toastError(msg)
      }
    } finally {
      setAddingToCart(null)
    }
  }, [token, toastSuccess, toastError])

  if (!token) {
    return (
      <PageLayout title="Login Required" subtitle="Please sign in to browse the menu and order food." cartCount={0}>
        <div className="text-center animate-in">
          <img src={logo} alt="Bhuk logo" className="w-16 h-16 rounded-full object-cover mx-auto mb-4 animate-fade-in" />
          <a href="/login" className="btn btn-primary btn-lg btn-block animate-slide-up">Sign In</a>
        </div>
      </PageLayout>
    )
  }

  if (loading) {
    return (
      <PageLayout title="Menu" subtitle="Loading delicious items..." cartCount={cartCount}>
        <div className="flex flex-col lg:flex-row gap-6">
          <aside className="w-full lg:w-64 flex-shrink-0">
            <div className="card p-4 space-y-2 animate-in">
              <Skeleton className="h-6 w-3/4" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          </aside>
          <section className="flex-1">
            <div className="menu-grid">
              {[...Array(6)].map((_, i) => (
                <SkeletonCard key={i} style={{ animationDelay: `${i * 50}ms` }} />
              ))}
            </div>
          </section>
        </div>
      </PageLayout>
    )
  }

  const filteredItems = selectedCategory === null ? items : items.filter(i => i.categoryId === selectedCategory)

  return (
    <PageLayout
      title={selectedCategory === null ? 'Menu' : categories.find(c => c.id === selectedCategory)?.name || 'Menu'}
      subtitle={`${filteredItems.length} item${filteredItems.length !== 1 ? 's' : ''} available`}
      cartCount={cartCount}
    >
      {addToCartError && (
        <div className="mb-6 p-3 rounded-xl bg-food-red-50 border border-food-red-200 text-food-red-700 text-sm animate-in animate-shake" role="alert">
          {addToCartError}
        </div>
      )}

      <div className="flex flex-col lg:flex-row gap-6">
        <aside className="w-full lg:w-64 flex-shrink-0 animate-in" style={{ animationDelay: '0ms' }}>
          <div className="card p-4 space-y-2">
            <h2 className="font-bold text-gray-900 animate-fade-in">Categories</h2>
            <button
              onClick={() => setSelectedCategory(null)}
              className={`w-full text-left px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 animate-slide-up ${
                selectedCategory === null ? 'bg-bhuk-600 text-white shadow-lg' : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 hover:border-bhuk-200'
              }`}
              style={{ animationDelay: '50ms' }}
            >
              All Items ({items.length})
            </button>
            {categories.map((cat, index) => (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`w-full text-left px-4 py-2 rounded-xl text-sm font-medium transition-all duration-200 animate-slide-up ${
                  selectedCategory === cat.id ? 'bg-bhuk-600 text-white shadow-lg' : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 hover:border-bhuk-200'
                }`}
                style={{ animationDelay: `${100 + index * 50}ms` }}
              >
                {cat.name}
              </button>
            ))}
          </div>
        </aside>

        <section className="flex-1 animate-in" style={{ animationDelay: '100ms' }}>
          {filteredItems.length === 0 ? (
            <EmptyState title="No items available" description="No menu items found in this category." icon="🍽️" />
          ) : (
            <div className="menu-grid" role="list">
              {filteredItems.map((item, index) => (
                <article
                  key={item.id}
                  className="card p-4 hover:shadow-lg transition-all duration-300 group animate-in relative"
                  style={{ animationDelay: `${Math.min(index * 40, 400)}ms` }}
                  role="listitem"
                >
                  <div className="menu-card-image mb-3 relative overflow-hidden">
                    {item.imageUrl ? (
                      <>
                        <img
                          src={item.imageUrl}
                          alt={item.name}
                          className="w-full h-full object-cover transition-all duration-700 group-hover:scale-110 group-hover:brightness-105"
                          loading="lazy"
                        />
                        <div className="absolute inset-0 bg-gradient-to-t from-black/30 via-black/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />
                        <div className="absolute top-2 left-2 flex gap-1.5 opacity-0 group-hover:opacity-100 translate-y-1 group-hover:translate-y-0 transition-all duration-300">
                          {item.dietaryType && (
                            <span className={`badge ${item.dietaryType === 'veg' ? 'badge-success' : 'badge-error'} px-2 py-1 text-xs`}>
                              {item.dietaryType === 'veg' ? '🌱 Veg' : '🍖 Non-Veg'}
                            </span>
                          )}
                        </div>
                        {!item.available && (
                          <div className="absolute inset-0 bg-black/50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                            <span className="badge badge-error text-lg px-4 py-2">Unavailable</span>
                          </div>
                        )}
                      </>
                    ) : (
                      <div className="absolute inset-0 flex items-center justify-center text-gray-400 text-4xl bg-gradient-to-br from-gray-100 to-gray-50 animate-pulse">
                        🍽️
                      </div>
                    )}
                  </div>
                  <div className="mb-2">
                    <h3 className="font-bold text-gray-900 text-base truncate group-hover:text-bhuk-600 transition-colors pr-4">{item.name}</h3>
                  </div>
                  <p className="text-sm text-gray-500 line-clamp-2 mb-3 min-h-[2.5rem]">{item.description || 'No description available'}</p>
                  <div className="flex items-center justify-between pt-2 border-t border-gray-100">
                    <span className="font-bold text-bhuk-600 text-xl animate-fade-in">₹{(item.basePriceMinor / 100).toFixed(2)}</span>
                    {!item.available ? (
                      <span className="text-sm text-food-red-500 font-medium">Unavailable</span>
                    ) : (
                      <button
                        onClick={e => { e.stopPropagation(); addToCart(item) }}
                        disabled={addingToCart === item.id}
                        className="btn btn-primary btn-sm transition-all duration-200 hover:scale-105 active:scale-95 min-w-[100px]"
                      >
                        {addingToCart === item.id ? (
                          <>
                            <svg className="animate-spin -mr-1 h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                            </svg>
                            Adding...
                          </>
                        ) : 'Add to Cart'}
                      </button>
                    )}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </PageLayout>
  )
}

function SkeletonCard({ style }: { style?: React.CSSProperties }) {
  return (
    <article className="card p-4 animate-in" style={style}>
      <Skeleton className="h-32 w-full rounded-xl mb-3" />
      <Skeleton className="h-5 w-3/4 mb-1" />
      <Skeleton className="h-4 w-1/2 mb-3" />
      <Skeleton className="h-4 w-1/3 mb-3" />
      <div className="flex items-center justify-between pt-2 border-t border-gray-100">
        <Skeleton className="h-6 w-20" />
        <Skeleton className="h-10 w-28 rounded-lg" />
      </div>
    </article>
  )
}