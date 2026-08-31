const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

function inventoryWsUrl() {
  const httpBase = API_BASE_URL.replace(/\/api\/?$/, '')
  const wsBase = httpBase.replace(/^https:/, 'wss:').replace(/^http:/, 'ws:')
  return `${wsBase}/ws/inventory`
}

export function connectInventory(onMessage) {
  const socket = new WebSocket(inventoryWsUrl())
  socket.onmessage = (event) => {
    try {
      onMessage(JSON.parse(event.data))
    } catch {
      // ignore malformed messages
    }
  }
  return socket
}
