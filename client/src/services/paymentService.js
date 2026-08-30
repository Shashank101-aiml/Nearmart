import api from './api'

export function verifyPayment(orderId, payload) {
  return api.post(`/orders/${orderId}/payment/verify`, payload).then((res) => res.data)
}

export function retryPayment(orderId) {
  return api.post(`/orders/${orderId}/payment/retry`).then((res) => res.data)
}
