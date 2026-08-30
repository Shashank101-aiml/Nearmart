import api from './api'

export function getCart() {
  return api.get('/cart').then((res) => res.data)
}

export function addItem(productId, quantity = 1) {
  return api.post('/cart/items', { productId, quantity }).then((res) => res.data)
}

export function updateItem(productId, quantity) {
  return api.put(`/cart/items/${productId}`, { quantity }).then((res) => res.data)
}

export function removeItem(productId) {
  return api.delete(`/cart/items/${productId}`).then((res) => res.data)
}
