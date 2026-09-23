import api from './api'

export function getProfile() {
  return api.get('/delivery/profile').then((res) => res.data)
}

export function listAvailable() {
  return api.get('/delivery/available').then((res) => res.data)
}

export function listMine() {
  return api.get('/delivery/mine').then((res) => res.data)
}

export function claimItem(itemId) {
  return api.post(`/delivery/items/${itemId}/claim`).then((res) => res.data)
}

export function updateItemStatus(itemId, status) {
  return api.patch(`/delivery/items/${itemId}/status`, { status }).then((res) => res.data)
}

export function setAvailability(available) {
  return api.patch('/delivery/availability', { available }).then((res) => res.data)
}
