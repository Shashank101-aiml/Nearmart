import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { roleHomePath } from '../../utils/roleHome'
import * as cartService from '../../services/cartService'

const NAV_LINKS = {
  CUSTOMER: [
    { to: '/customer/orders', label: 'Orders' },
    { to: '/customer/notifications', label: 'Notifications' },
    { to: '/customer/cart', label: 'Cart', showCartBadge: true },
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
  const [cartCount, setCartCount] = useState(0)

  useEffect(() => {
    if (user.role !== 'CUSTOMER') return
    cartService
      .getCart()
      .then((cart) => setCartCount(cart.items.reduce((sum, item) => sum + item.quantity, 0)))
      .catch(() => {})
  }, [user.role])

  const links = NAV_LINKS[user.role] || []

  return (
    <header className="sticky top-0 z-10 flex items-center justify-between gap-4 border-b border-border bg-bg px-8 py-3">
      <NavLink to={roleHomePath(user.role)} className="text-lg font-bold text-accent">
        Nearmart
      </NavLink>
      <nav className="flex items-center gap-4">
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} className={navLinkClasses}>
            <span className="relative">
              {link.label}
              {link.showCartBadge && cartCount > 0 && (
                <span className="absolute -right-3 -top-2 flex h-4 min-w-4 items-center justify-center rounded-full bg-accent px-1 text-[10px] font-semibold text-white no-underline">
                  {cartCount}
                </span>
              )}
            </span>
          </NavLink>
        ))}
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
