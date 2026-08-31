import api from './api'

export function listNotifications() {
  return api.get('/notifications').then((res) => res.data)
}

export function markRead(id) {
  return api.patch(`/notifications/${id}/read`).then((res) => res.data)
}
