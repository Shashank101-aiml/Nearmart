export function roleHomePath(role) {
  if (role === 'DELIVERY_PARTNER') return '/delivery'
  return `/${role.toLowerCase()}`
}
