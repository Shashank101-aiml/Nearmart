import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import ProtectedRoute from './ProtectedRoute'
import LoginPage from '../pages/auth/LoginPage'
import RegisterPage from '../pages/auth/RegisterPage'
import CustomerHome from '../pages/customer/CustomerHome'
import VendorStorefront from '../pages/customer/VendorStorefront'
import CartPage from '../pages/customer/CartPage'
import OrdersPage from '../pages/customer/OrdersPage'
import NotificationsPage from '../pages/customer/NotificationsPage'
import VendorHome from '../pages/vendor/VendorHome'
import VendorOrdersPage from '../pages/vendor/VendorOrdersPage'
import AdminHome from '../pages/admin/AdminHome'
import AdminUsersPage from '../pages/admin/AdminUsersPage'
import AdminVendorsPage from '../pages/admin/AdminVendorsPage'
import AdminOrdersPage from '../pages/admin/AdminOrdersPage'

function roleHomePath(role) {
  return `/${role.toLowerCase()}`
}

function HomeRedirect() {
  const { isAuthenticated, user } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <Navigate to={roleHomePath(user.role)} replace />
}

function PublicOnlyRoute({ children }) {
  const { isAuthenticated, user } = useAuth()
  if (isAuthenticated) return <Navigate to={roleHomePath(user.role)} replace />
  return children
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route
        path="/login"
        element={
          <PublicOnlyRoute>
            <LoginPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicOnlyRoute>
            <RegisterPage />
          </PublicOnlyRoute>
        }
      />
      <Route
        path="/customer"
        element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}>
            <CustomerHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/customer/vendors/:vendorId"
        element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}>
            <VendorStorefront />
          </ProtectedRoute>
        }
      />
      <Route
        path="/customer/cart"
        element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}>
            <CartPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/customer/orders"
        element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}>
            <OrdersPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/customer/notifications"
        element={
          <ProtectedRoute allowedRoles={['CUSTOMER']}>
            <NotificationsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/vendor"
        element={
          <ProtectedRoute allowedRoles={['VENDOR']}>
            <VendorHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/vendor/orders"
        element={
          <ProtectedRoute allowedRoles={['VENDOR']}>
            <VendorOrdersPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminUsersPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/vendors"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminVendorsPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/orders"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <AdminOrdersPage />
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
