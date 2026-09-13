import { NavLink } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useCart } from '../../hooks/useCart'
import { roleHomePath } from '../../utils/roleHome'

const NAV_LINKS = {
  CUSTOMER: [
    { to: '/customer/orders', label: 'Orders' },
    { to: '/customer/notifications', label: 'Notifications' },
  ],
  VENDOR: [{ to: '/vendor/orders', label: 'Orders' }],
  ADMIN: [
    { to: '/admin/users', label: 'Users' },
    { to: '/admin/vendors', label: 'Vendors' },
    { to: '/admin/orders', label: 'Orders' },
  ],
}

const navLinkClasses = ({ isActive }) =>
  `text-sm ${isActive ? 'font-semibold text-accent underline' : 'text-text-h underline'}`

export default function Header() {
  const { user, logout } = useAuth()
  const { itemCount, openCart } = useCart()

  const links = NAV_LINKS[user.role] || []

  return (
    <header className="sticky top-0 z-10 flex items-center justify-between gap-4 border-b border-border bg-bg px-8 py-3">
      <NavLink to={roleHomePath(user.role)} className="text-lg font-bold text-accent">
        Nearmart
      </NavLink>
      <nav className="flex items-center gap-4">
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} className={navLinkClasses}>
            {link.label}
          </NavLink>
        ))}
        {user.role === 'CUSTOMER' && (
          <button type="button" onClick={openCart} className="relative cursor-pointer text-sm text-text-h underline">
            Cart
            {itemCount > 0 && (
              <span className="absolute -right-3 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-accent px-1 text-[10px] font-semibold text-white no-underline">
                {itemCount}
              </span>
            )}
          </button>
        )}
      </nav>
      <div className="flex items-center gap-3">
        <span className="text-sm text-text">
          {user.username} ({user.role})
        </span>
        <button
          type="button"
          onClick={logout}
          className="cursor-pointer whitespace-nowrap rounded-md border border-border bg-bg px-3.5 py-2 text-text-h"
        >
          Log out
        </button>
      </div>
    </header>
  )
}
