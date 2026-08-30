import api from './api'

export function listAvailable() {
  return api.get('/products').then((res) => res.data)
}

export function getById(id) {
  return api.get(`/products/${id}`).then((res) => res.data)
}

export function listMine() {
  return api.get('/products/mine').then((res) => res.data)
}

export function listByVendor(vendorId) {
  return api.get(`/vendors/${vendorId}/products`).then((res) => res.data)
}

export function getVendor(vendorId) {
  return api.get(`/vendors/${vendorId}`).then((res) => res.data)
}

export function createProduct(payload) {
  return api.post('/products', payload).then((res) => res.data)
}

export function updateProduct(id, payload) {
  return api.put(`/products/${id}`, payload).then((res) => res.data)
}

export function deleteProduct(id) {
  return api.delete(`/products/${id}`).then((res) => res.data)
}
