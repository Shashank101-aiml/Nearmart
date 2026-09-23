import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useCart } from '../../hooks/useCart'
import { roleHomePath } from '../../utils/roleHome'

const NAV_LINKS = {
  CUSTOMER: [
    { to: '/customer/orders', label: 'Orders' },
    { to: '/customer/notifications', label: 'Notifications' },
  ],
  VENDOR: [{ to: '/vendor/orders', label: 'Orders' }],
  DELIVERY_PARTNER: [{ to: '/delivery', label: 'Deliveries' }],
  ADMIN: [
    { to: '/admin/users', label: 'Users' },
    { to: '/admin/vendors', label: 'Vendors' },
    { to: '/admin/delivery-partners', label: 'Delivery Partners' },
    { to: '/admin/orders', label: 'Orders' },
  ],
}

const SAVED_PLACES = [
  'HSR Layout, Bengaluru',
  'Koramangala 4th Block, Bengaluru',
  'Indiranagar 100ft Rd, Bengaluru',
  'Bellandur Tech Park, Bengaluru',
]

const navLinkClasses = ({ isActive }) =>
  `text-sm font-semibold ${isActive ? 'text-accent underline' : 'text-text-h hover:text-accent'}`

export default function Header() {
  const { user, logout } = useAuth()
  const { itemCount, openCart } = useCart()
  const navigate = useNavigate()
  const [location, setLocation] = useState('HSR Layout, Bengaluru')
  const [locationModalOpen, setLocationModalOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')

  const links = user ? NAV_LINKS[user.role] || [] : []
  const showCustomerChrome = !user || user.role === 'CUSTOMER'

  const handleSearchSubmit = (e) => {
    e.preventDefault()
    const params = searchQuery.trim() ? `?q=${encodeURIComponent(searchQuery.trim())}` : ''
    navigate(`/customer${params}`)
  }

  return (
    <header className="sticky top-0 z-10 border-b border-border bg-bg shadow-sm">
      <div className="mx-auto flex w-full max-w-7xl flex-wrap items-center justify-between gap-4 px-8 py-3">
        <div className="flex items-center gap-4">
          <NavLink to={user ? roleHomePath(user.role) : '/customer'} className="flex items-center gap-2.5">
            <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-gradient-to-tr from-accent-dark to-accent text-lg font-black text-white shadow-sm">
              N
            </div>
            <div className="flex flex-col items-start leading-none">
              <span className="text-xl font-black text-text-h">nearmart</span>
              <span className="mt-0.5 text-[9px] font-black tracking-widest text-accent uppercase">
                Express 15m
              </span>
            </div>
          </NavLink>

          {showCustomerChrome && (
            <button
              type="button"
              onClick={() => setLocationModalOpen(true)}
              className="flex cursor-pointer items-center gap-2 rounded-xl border border-border bg-code-bg px-3 py-2 text-left"
            >
              <i className="fa-solid fa-location-dot text-accent" />
              <div className="flex flex-col leading-tight">
                <span className="text-[10px] font-black tracking-wider text-accent uppercase">
                  &#9889; 15 mins
                </span>
                <span className="max-w-[160px] truncate text-xs font-bold text-text-h">{location}</span>
              </div>
              <i className="fa-solid fa-chevron-down text-[10px] text-text" />
            </button>
          )}
        </div>

        {showCustomerChrome && (
          <form onSubmit={handleSearchSubmit} className="hidden max-w-xl flex-1 md:flex">
            <div className="relative w-full">
              <i className="fa-solid fa-magnifying-glass absolute top-1/2 left-4 -translate-y-1/2 text-text" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search 'milk', 'bananas', or 'pain relief'"
                className="w-full rounded-2xl border border-border bg-code-bg py-2.5 pr-4 pl-11 text-sm text-text-h outline-none focus:ring-2 focus:ring-accent"
              />
            </div>
          </form>
        )}

        <div className="flex items-center gap-4">
          <nav className="flex items-center gap-4">
            {links.map((link) => (
              <NavLink key={link.to} to={link.to} className={navLinkClasses}>
                {link.label}
              </NavLink>
            ))}
          </nav>
          {showCustomerChrome && (
            <button
              type="button"
              onClick={openCart}
              className="relative flex cursor-pointer items-center gap-2 rounded-xl bg-accent px-4 py-2.5 text-sm font-bold text-white shadow-sm hover:bg-accent-hover"
            >
              <i className="fa-solid fa-bag-shopping" />
              <span className="hidden sm:inline">Cart</span>
              <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-white px-1 text-xs font-black text-accent">
                {itemCount}
              </span>
            </button>
          )}
          {user ? (
            <>
              <span className="hidden text-sm text-text sm:inline">
                {user.username} ({user.role})
              </span>
              <button
                type="button"
                onClick={logout}
                className="cursor-pointer rounded-xl border border-border bg-bg px-3.5 py-2 text-sm font-semibold text-text-h hover:bg-code-bg"
              >
                Log out
              </button>
            </>
          ) : (
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="flex cursor-pointer items-center gap-2 rounded-xl border border-border bg-bg px-3.5 py-2 text-sm font-semibold text-text-h hover:bg-code-bg"
            >
              <i className="fa-solid fa-user" />
              Sign In
            </button>
          )}
        </div>
      </div>

      {locationModalOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
          onClick={() => setLocationModalOpen(false)}
        >
          <div
            className="w-full max-w-md rounded-2xl border border-border bg-bg p-6 text-left shadow-lg"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="mb-4 flex items-center justify-between">
              <h3 className="m-0 text-lg font-black text-text-h">Select delivery location</h3>
              <button
                type="button"
                onClick={() => setLocationModalOpen(false)}
                aria-label="Close"
                className="cursor-pointer text-text hover:text-text-h"
              >
                <i className="fa-solid fa-xmark" />
              </button>
            </div>
            <div className="flex flex-col gap-1">
              {SAVED_PLACES.map((place) => (
                <button
                  key={place}
                  type="button"
                  onClick={() => {
                    setLocation(place)
                    setLocationModalOpen(false)
                  }}
                  className="cursor-pointer rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-text-h hover:bg-accent-bg hover:text-accent"
                >
                  {place}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </header>
  )
}
