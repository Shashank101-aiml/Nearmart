const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

function trackingWsUrl(token) {
  const httpBase = API_BASE_URL.replace(/\/api\/?$/, '')
  const wsBase = httpBase.replace(/^https:/, 'wss:').replace(/^http:/, 'ws:')
  return `${wsBase}/ws/tracking?token=${encodeURIComponent(token)}`
}

export function connectTracking(token, onMessage) {
  const socket = new WebSocket(trackingWsUrl(token))
  socket.onmessage = (event) => {
    try {
      onMessage(JSON.parse(event.data))
    } catch {
      // ignore malformed messages
    }
  }
  return socket
}
