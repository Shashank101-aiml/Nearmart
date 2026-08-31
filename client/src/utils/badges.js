const BASE = 'rounded-full px-2 py-0.5 text-xs'

export function badgeClassFor(status) {
  if (status === 'PLACED') return `${BASE} bg-accent-bg text-accent`
  if (status === 'PENDING_PAYMENT') return `${BASE} bg-code-bg text-text`
  if (status === 'PAYMENT_FAILED') return `${BASE} bg-red-600/10 text-red-600`
  return `${BASE} bg-code-bg text-text`
}

export function fulfillmentBadgeClassFor(status) {
  if (status === 'SHIPPED') return `${BASE} bg-blue-600/10 text-blue-600`
  if (status === 'DELIVERED') return `${BASE} bg-green-600/10 text-green-600`
  return `${BASE} bg-code-bg text-text`
}
