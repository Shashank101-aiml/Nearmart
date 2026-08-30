import api from './api'

export function listOrders() {
  return api.get('/vendor/orders').then((res) => res.data)
}

export function getOrder(id) {
  return api.get(`/vendor/orders/${id}`).then((res) => res.data)
}
