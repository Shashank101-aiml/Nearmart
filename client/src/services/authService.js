import api from './api'

export function register(payload) {
  return api.post('/auth/register', payload).then((res) => res.data)
}

export function login(payload) {
  return api.post('/auth/login', payload).then((res) => res.data)
}

export function requestOtp(phoneNumber) {
  return api.post('/auth/otp/request', { phoneNumber }).then((res) => res.data)
}

export function verifyOtp(payload) {
  return api.post('/auth/otp/verify', payload).then((res) => res.data)
}
