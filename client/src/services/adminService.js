import api from './api'

export function listUsers() {
  return api.get('/admin/users').then((res) => res.data)
}

export function setUserEnabled(id, enabled) {
  return api.patch(`/admin/users/${id}/status`, { enabled }).then((res) => res.data)
}

export function deleteUser(id) {
  return api.delete(`/admin/users/${id}`)
}

export function listVendors() {
  return api.get('/admin/vendors').then((res) => res.data)
}

export function updateVendor(id, payload) {
  return api.put(`/admin/vendors/${id}`, payload).then((res) => res.data)
}

export function listDeliveryPartners() {
  return api.get('/admin/delivery-partners').then((res) => res.data)
}

export function listOrders() {
  return api.get('/admin/orders').then((res) => res.data)
}

export function getOrder(id) {
  return api.get(`/admin/orders/${id}`).then((res) => res.data)
}

export function setProductAvailability(id, available) {
  return api.patch(`/admin/products/${id}/status`, { available }).then((res) => res.data)
}
