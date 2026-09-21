import { AuthProvider } from './context/AuthContext'
import { CartProvider } from './context/CartContext'
import { ToastProvider } from './context/ToastContext'
import ToastContainer from './components/common/ToastContainer'
import AppRoutes from './routes/AppRoutes'
import './App.css'

function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <CartProvider>
          <AppRoutes />
        </CartProvider>
      </AuthProvider>
      <ToastContainer />
    </ToastProvider>
  )
}

export default App
