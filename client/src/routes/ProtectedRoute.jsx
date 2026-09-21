import { Navigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import Header from '../components/common/Header'
import CartDrawer from '../components/common/CartDrawer'
import Footer from '../components/common/Footer'

export default function ProtectedRoute({ children, allowedRoles, requireAuth = true }) {
  const { isAuthenticated, user } = useAuth()

  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (isAuthenticated && allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  return (
    <>
      <Header />
      <CartDrawer />
      {children}
      <Footer />
    </>
  )
}
