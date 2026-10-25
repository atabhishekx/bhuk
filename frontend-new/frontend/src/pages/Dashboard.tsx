import Navbar from '@/components/Navbar'
import logo from '@/assets/logo.png'

export default function Dashboard() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between mb-8">
          <div>
            <img src={logo} alt="Bhuk logo" className="w-10 h-10 rounded-full object-cover inline-block mr-3 align-middle" />
            <h1 className="text-2xl font-bold text-gray-900 inline-block align-middle">Welcome to Bhuk</h1>
            <p className="text-gray-500 mt-1">Your Solution for all the Cravings and Snacks</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Featured Items</h3>
            <p className="text-gray-500 text-sm mb-4">Browse our delicious menu items.</p>
            <button className="btn-primary w-full">View Full Menu</button>
          </div>
          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Quick Reorder</h3>
            <p className="text-gray-500 text-sm mb-4">Reorder your favorite items.</p>
            <button className="btn-outline w-full">View Past Orders</button>
          </div>
          <div className="card p-6">
            <h3 className="font-bold text-lg text-gray-900 mb-4">Track Delivery</h3>
            <p className="text-gray-500 text-sm mb-4">Track your active orders.</p>
            <button className="btn-outline w-full">View Orders</button>
          </div>
        </div>
      </div>
    </div>
  )
}
