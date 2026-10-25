import { Link } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import logo from '@/assets/logo.png'

interface NavbarProps {
  cartCount?: number
}

export default function Navbar({ cartCount = 0 }: NavbarProps) {
  const { user, logout } = useAuth()

  const isAdmin = user?.roles.includes('ADMIN')
  const isDeliveryGuy = user?.roles.includes('DELIVERY_GUY')

  return (
    <nav className="bg-white border-b border-gray-100 sticky top-0 z-50 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => window.location.href = '/'}>
            <img src={logo} alt="Bhuk logo" className="w-10 h-10 rounded-full object-cover" />
            <span className="font-bold text-bhuk-700 text-lg tracking-tight">Bhuk</span>
          </div>
          <div className="hidden md:flex items-center gap-6">
            {!isAdmin && !isDeliveryGuy && (
              <>
                <Link to="/menu" className="text-gray-600 hover:text-bhuk-600 font-medium px-3 py-2 rounded-lg transition-colors">
                  Menu
                </Link>
                <Link to="/orders" className="text-gray-600 hover:text-bhuk-600 font-medium px-3 py-2 rounded-lg transition-colors">
                  Orders
                </Link>
              </>
            )}
            {isDeliveryGuy && (
              <>
                <Link to="/delivery/dashboard" className="text-bhuk-600 hover:text-bhuk-700 font-medium px-3 py-2 rounded-lg transition-colors bg-bhuk-50">
                  Delivery Dashboard
                </Link>
              </>
            )}
            {isAdmin && !isDeliveryGuy && (
              <>
                <Link to="/admin/dashboard" className="text-gray-600 hover:text-bhuk-600 font-medium px-3 py-2 rounded-lg transition-colors">
                  Admin Dashboard
                </Link>
                <Link to="/admin/orders" className="text-bhuk-600 hover:text-bhuk-700 font-medium px-3 py-2 rounded-lg transition-colors bg-bhuk-50">
                  Admin Orders
                </Link>
              </>
            )}
          </div>
          <div className="flex items-center gap-4">
            <Link to="/cart" className="relative text-gray-600 hover:text-bhuk-600 p-2 rounded-lg hover:bg-gray-50 transition-colors">
              <span className="text-xl">🛒</span>
              {cartCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-bhuk-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                  {cartCount > 9 ? '9+' : cartCount}
                </span>
              )}
            </Link>
            <div className="flex items-center gap-3 pl-4 border-l border-gray-200">
              <div className="hidden md:block">
                <p className="text-sm font-medium text-gray-900">{user?.name}</p>
                <p className="text-xs text-gray-500 capitalize">{user?.roles.join(', ') || 'customer'}</p>
              </div>
              <button onClick={logout} className="text-bhuk-600 hover:text-bhuk-700 text-sm font-medium hover:underline">
                Sign out
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}
