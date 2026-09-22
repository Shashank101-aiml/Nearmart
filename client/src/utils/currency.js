export function formatPrice(value) {
  return `₹${Math.round(value).toLocaleString('en-IN')}`
}
