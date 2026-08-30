export function badgeClassFor(status) {
  if (status === 'PLACED') return 'badge-available'
  if (status === 'PENDING_PAYMENT') return 'badge-pending'
  if (status === 'PAYMENT_FAILED') return 'badge-failed'
  return 'badge-hidden'
}
