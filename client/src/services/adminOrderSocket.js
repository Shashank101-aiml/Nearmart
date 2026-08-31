const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

function adminOrdersWsUrl(token) {
  const httpBase = API_BASE_URL.replace(/\/api\/?$/, '')
  const wsBase = httpBase.replace(/^https:/, 'wss:').replace(/^http:/, 'ws:')
  return `${wsBase}/ws/admin/orders?token=${encodeURIComponent(token)}`
}

export function connectAdminOrders(token, onMessage) {
  const socket = new WebSocket(adminOrdersWsUrl(token))
  socket.onmessage = (event) => {
    try {
      onMessage(JSON.parse(event.data))
    } catch {
      // ignore malformed messages
    }
  }
  return socket
}
