import api from './api'

export function placeOrder() {
  return api.post('/orders').then((res) => res.data)
}

export function listOrders() {
  return api.get('/orders').then((res) => res.data)
}

export function getOrder(id) {
  return api.get(`/orders/${id}`).then((res) => res.data)
}
