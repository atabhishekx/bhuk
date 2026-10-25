import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider, useAuth } from '@/contexts/AuthContext'
import { Loading } from '@/components/Common'
import Login from '@/pages/Login'
import Register from '@/pages/Register'
import Menu from '@/pages/Menu'
import Cart from '@/pages/Cart'
import Checkout from '@/pages/Checkout'
import Orders from '@/pages/Orders'
import OrderTracking from '@/pages/OrderTracking'
import Profile from '@/pages/Profile'
import DeliveryGuyDashboard from '@/pages/DeliveryGuyDashboard'
import AdminDashboard from '@/pages/admin/AdminDashboard'
import AdminOrders from '@/pages/admin/AdminOrders'
import AdminMenu from '@/pages/admin/AdminMenu'

function RoleRoute({ children, allowedRoles }: { children: ReactNode, allowedRoles: string[] }) {
  const { user, isLoading } = useAuth()
  if (isLoading) return <Loading />
  if (!user) return <Navigate to="/login" replace />
  const hasRole = allowedRoles.some(r => user.roles.includes(r))
  if (!hasRole) return <Navigate to="/dashboard" replace />
  return children
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        path="/dashboard"
        element={
          <Navigate to="/menu" replace />
        }
      />
      <Route path="/" element={<Navigate to="/menu" replace />} />

      {/* Customer routes */}
      <Route path="/menu" element={<Menu />} />
      <Route path="/cart" element={<Cart />} />
      <Route path="/checkout" element={<Checkout />} />
      <Route path="/orders" element={<Orders />} />
      <Route path="/orders/:id" element={<OrderTracking />} />
      <Route path="/profile" element={<Profile />} />

      {/* Delivery person routes */}
      <Route
        path="/delivery/dashboard"
        element={
          <RoleRoute allowedRoles={['DELIVERY_GUY']}>
            <DeliveryGuyDashboard />
          </RoleRoute>
        }
      />

      {/* Admin routes */}
      <Route
        path="/admin/dashboard"
        element={
          <RoleRoute allowedRoles={['ADMIN']}>
            <AdminDashboard />
          </RoleRoute>
        }
      />
      <Route
        path="/admin/orders"
        element={
          <RoleRoute allowedRoles={['ADMIN']}>
            <AdminOrders />
          </RoleRoute>
        }
      />
      <Route
        path="/admin/menu"
        element={
          <RoleRoute allowedRoles={['ADMIN']}>
            <AdminMenu />
          </RoleRoute>
        }
      />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}
