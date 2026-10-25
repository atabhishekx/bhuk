import { useEffect, useState } from 'react'
import api from '@/api/axios'
import Navbar from '@/components/Navbar'
import { Loading } from '@/components/Common'

type Category = {
  id: number
  name: string
  description?: string
  displayOrder: number
  active: boolean
}

type Item = {
  id: number
  name: string
  description?: string
  basePriceMinor: number
  active: boolean
  available: boolean
  categoryName: string
}

type ModalType = 'category' | 'item' | null

export default function AdminMenu() {
  const [categories, setCategories] = useState<Category[]>([])
  const [items, setItems] = useState<Item[]>([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState<ModalType>(null)
  const [modalType, setModalType] = useState<'create' | 'edit'>('create')
const [formData, setFormData] = useState<any>({})

  useEffect(() => {
    fetchData()
  }, [])

  async function fetchData() {
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

  async function createCategory(data: { name: string; description?: string; displayOrder?: number; active?: boolean }) {
    try {
      await api.post('/api/v1/menu/admin/categories', data, { params: { outletId: 1 } })
      await fetchData()
      setModal(null)
    } catch (e: any) {
      alert(e.response?.data?.error?.message || 'Failed to create category')
    }
  }

  async function deleteCategory(id: number) {
    if (!confirm('Delete this category?')) return
    try {
      await api.delete(`/api/v1/menu/admin/categories/${id}`)
      await fetchData()
    } catch (e: any) {
      alert(e.response?.data?.error?.message || 'Failed to delete category')
    }
  }

  async function createItem(data: any) {
    try {
      await api.post('/api/v1/menu/admin/items', data, { params: { outletId: 1 } })
      await fetchData()
      setModal(null)
    } catch (e: any) {
      alert(e.response?.data?.error?.message || 'Failed to create item')
    }
  }

  async function deleteItem(id: number) {
    if (!confirm('Delete this item?')) return
    try {
      await api.delete(`/api/v1/menu/admin/items/${id}`)
      await fetchData()
    } catch (e: any) {
      alert(e.response?.data?.error?.message || 'Failed to delete item')
    }
  }

  if (loading) return <Loading />

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Menu Management</h1>
            <p className="text-gray-500">Manage categories and menu items</p>
          </div>
          <button
            onClick={() => { setModalType('create'); setFormData({}); setModal('category') }}
            className="btn-primary"
          >
            + Add Category
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Categories */}
          <div className="lg:col-span-1">
            <div className="card p-6">
              <h3 className="font-bold text-lg text-gray-900 mb-4">Categories</h3>
              {categories.length === 0 ? (
                <div className="text-center py-8 text-gray-500">No categories</div>
              ) : (
                <div className="space-y-2">
                  {categories.map((cat) => (
                    <div key={cat.id} className="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-white">
                      <div>
                        <p className="font-medium text-gray-900">{cat.name}</p>
                        <p className="text-xs text-gray-500">{cat.description || ''}</p>
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => {
                            setModalType('edit')
                            setFormData({ name: cat.name, description: cat.description, displayOrder: cat.displayOrder, active: cat.active })
                            setModal('category')
                          }}
                          className="text-xs text-restru-600 hover:text-restru-700"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => deleteCategory(cat.id)}
                          className="text-xs text-food-red-500 hover:text-food-red-700"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Items */}
          <div className="lg:col-span-2">
            <div className="card p-6">
              <h3 className="font-bold text-lg text-gray-900 mb-4">Menu Items</h3>
              <div className="flex justify-between items-end mb-4">
                <button
                  onClick={() => { setModalType('create'); setFormData({}); setModal('item') }}
                  className="btn-primary"
                >
                  + Add Item
                </button>
              </div>
              {items.length === 0 ? (
                <div className="text-center py-8 text-gray-500">No items</div>
              ) : (
                <div className="space-y-3">
                  {items.map((item) => (
                    <div key={item.id} className="card p-4 flex flex-col lg:flex-row lg:items-center gap-4">
                      <div className="w-16 h-16 rounded-xl bg-gray-100 flex items-center justify-center flex-shrink-0">
                        <span className="text-2xl">🍽️</span>
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex justify-between items-center mb-1">
                          <h4 className="font-bold text-gray-900">{item.name}</h4>
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                            item.available ? 'bg-food-green-100 text-food-green-700' : 'bg-gray-100 text-gray-500'
                          }`}>
                            {item.available ? 'Available' : 'Unavailable'}
                          </span>
                        </div>
                        <p className="text-sm text-gray-500">{item.description || 'No description'}</p>
                        <p className="text-sm text-gray-500">
                          Category: {item.categoryName} • ₹{(item.basePriceMinor / 100).toFixed(2)}
                        </p>
                      </div>
                      <div className="flex gap-2">
                        <button
                          onClick={() => {
                            setModalType('edit')
                            setFormData({ name: item.name, description: item.description, basePriceMinor: item.basePriceMinor, categoryId: (item as any).categoryId, active: item.active, available: item.available })
                            setModal('item')
                          }}
                          className="text-xs text-restru-600 hover:text-restru-700"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => deleteItem(item.id)}
                          className="text-xs text-food-red-500 hover:text-food-red-700"
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Modals */}
        {modal === 'category' && (
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm" onClick={() => setModal(null)} />
            <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full relative z-10">
              <h3 className="font-bold text-lg text-gray-900 mb-6">
                {modalType === 'create' ? 'Add Category' : 'Edit Category'}
              </h3>
              <form onSubmit={(e) => { e.preventDefault(); createCategory(formData) }} className="space-y-4">
                <div>
                  <label className="label">Category Name</label>
                  <input
                    name="name"
                    value={formData.name || ''}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="input-field" required placeholder="Enter category name"
                  />
                </div>
                <div>
                  <label className="label">Description</label>
                  <input
                    name="description"
                    value={formData.description || ''}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    className="input-field" placeholder="Enter description"
                  />
                </div>
                <div className="flex gap-4">
                  <div>
                    <label className="label">Display Order</label>
                    <input
                      type="number"
                      name="displayOrder"
                      value={formData.displayOrder || 0}
                      onChange={(e) => setFormData({ ...formData, displayOrder: parseInt(e.target.value) || 0 })}
                      className="input-field" placeholder="0"
                    />
                  </div>
                  <div className="flex items-end">
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        name="active"
                        checked={formData.active !== false}
                        onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                      />
                      Active
                    </label>
                  </div>
                </div>
                <div className="flex justify-end gap-3 mt-6">
                  <button
                    type="button"
                    onClick={() => setModal(null)}
                    className="btn-outline py-3"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-primary py-3"
                  >
                    {modalType === 'create' ? 'Create Category' : 'Update Category'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {modal === 'item' && (
          <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="fixed inset-0 bg-black bg-opacity-50 backdrop-blur-sm" onClick={() => setModal(null)} />
            <div className="bg-white rounded-2xl shadow-xl p-6 max-w-md w-full relative z-10">
              <h3 className="font-bold text-lg text-gray-900 mb-6">
                {modalType === 'create' ? 'Add Item' : 'Edit Item'}
              </h3>
              <form onSubmit={(e) => { e.preventDefault(); createItem(formData) }} className="space-y-4">
                <div>
                  <label className="label">Item Name</label>
                  <input
                    name="name"
                    value={formData.name || ''}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="input-field" required placeholder="Enter item name"
                  />
                </div>
                <div>
                  <label className="label">Category</label>
                  <select
                    name="categoryId"
                    value={formData.categoryId || ''}
                    onChange={(e) => setFormData({ ...formData, categoryId: parseInt(e.target.value) })}
                    className="input-field" required
                  >
                    <option value="">Select category</option>
                    {categories.map((cat) => (
                      <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="label">Description</label>
                  <input
                    name="description"
                    value={formData.description || ''}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    className="input-field" placeholder="Enter description"
                  />
                </div>
                <div>
                  <label className="label">Base Price (₹)</label>
                  <input
                    type="number"
                    name="basePriceMinor"
                    value={formData.basePriceMinor || ''}
                    onChange={(e) => setFormData({ ...formData, basePriceMinor: parseInt(e.target.value) || 0 })}
                    className="input-field" required placeholder="0.00"
                  />
                </div>
                <div className="flex gap-4 items-end">
                  <div className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      name="active"
                      checked={formData.active !== false}
                      onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                    />
                    <label>Active</label>
                  </div>
                  <div className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      name="available"
                      checked={formData.available !== false}
                      onChange={(e) => setFormData({ ...formData, available: e.target.checked })}
                    />
                    <label>Available</label>
                  </div>
                </div>
                <div className="flex justify-end gap-3 mt-6">
                  <button
                    type="button"
                    onClick={() => setModal(null)}
                    className="btn-outline py-3"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className="btn-primary py-3"
                  >
                    {modalType === 'create' ? 'Create Item' : 'Update Item'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
