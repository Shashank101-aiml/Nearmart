import { Navigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import Header from '../components/common/Header'
import CartDrawer from '../components/common/CartDrawer'

export default function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, user } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  return (
    <>
      <Header />
      <CartDrawer />
      {children}
    </>
  )
}
