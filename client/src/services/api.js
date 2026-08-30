import axios from 'axios'

const AUTH_STORAGE_KEY = 'buildit.auth'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
})

api.interceptors.request.use((config) => {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)
  if (raw) {
    const { token } = JSON.parse(raw)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response?.data
    const normalized = {
      status: error.response?.status,
      message: data?.error || error.message || 'Something went wrong',
      fieldErrors: data?.fieldErrors || null,
    }

    if (error.response?.status === 401) {
      localStorage.removeItem(AUTH_STORAGE_KEY)
    }

    return Promise.reject(normalized)
  }
)

export { AUTH_STORAGE_KEY }
export default api
