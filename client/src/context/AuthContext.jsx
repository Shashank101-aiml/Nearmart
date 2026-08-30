import { useEffect, useState } from 'react'
import * as authService from '../services/authService'
import { AUTH_STORAGE_KEY } from '../services/api'
import { AuthContext } from './auth-context'

function loadStoredAuth() {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(loadStoredAuth)

  useEffect(() => {
    if (auth) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
    } else {
      localStorage.removeItem(AUTH_STORAGE_KEY)
    }
  }, [auth])

  const applyAuthResponse = (data) => {
    setAuth({
      token: data.token,
      user: { userId: data.userId, username: data.username, role: data.role },
    })
  }

  const login = async (payload) => {
    const data = await authService.login(payload)
    applyAuthResponse(data)
    return data
  }

  const register = async (payload) => {
    const data = await authService.register(payload)
    applyAuthResponse(data)
    return data
  }

  const logout = () => setAuth(null)

  const value = {
    user: auth?.user ?? null,
    token: auth?.token ?? null,
    isAuthenticated: Boolean(auth?.token),
    login,
    register,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
