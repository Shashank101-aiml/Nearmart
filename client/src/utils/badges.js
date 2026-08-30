export function badgeClassFor(status) {
  if (status === 'PLACED') return 'badge-available'
  if (status === 'PENDING_PAYMENT') return 'badge-pending'
  if (status === 'PAYMENT_FAILED') return 'badge-failed'
  return 'badge-hidden'
}

export function fulfillmentBadgeClassFor(status) {
  if (status === 'SHIPPED') return 'badge-shipped'
  if (status === 'DELIVERED') return 'badge-delivered'
  return 'badge-pending'
}
