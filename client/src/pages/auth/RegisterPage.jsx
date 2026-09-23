import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useToast } from '../../hooks/useToast'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const VALID_ROLES = ['CUSTOMER', 'VENDOR', 'DELIVERY_PARTNER']

function initialFormFor(roleParam) {
  return {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: VALID_ROLES.includes(roleParam) ? roleParam : 'CUSTOMER',
    displayName: '',
    address: '',
    phoneNumber: '',
    vehicleNumber: '',
  }
}

function validate(form) {
  const errors = {}
  if (form.username.trim().length < 3) errors.username = 'Username must be at least 3 characters'
  if (!EMAIL_PATTERN.test(form.email)) errors.email = 'Enter a valid email address'
  if (form.password.length < 8) errors.password = 'Password must be at least 8 characters'
  if (form.confirmPassword !== form.password) errors.confirmPassword = 'Passwords do not match'
  if (!form.displayName.trim()) {
    errors.displayName = form.role === 'VENDOR' ? 'Store name is required' : 'Name is required'
  }
  if (!form.address.trim()) errors.address = 'Address is required'
  if (
    (form.role === 'CUSTOMER' || form.role === 'DELIVERY_PARTNER') &&
    form.phoneNumber &&
    !/^\d{10}$/.test(form.phoneNumber)
  ) {
    errors.phoneNumber = 'Please enter a valid Number'
  }
  return errors
}

export default function RegisterPage() {
  const { register } = useAuth()
  const { showToast } = useToast()
  const [searchParams] = useSearchParams()
  const [form, setForm] = useState(() => initialFormFor(searchParams.get('role')))
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    const clientErrors = validate(form)
    setFieldErrors(clientErrors)
    if (Object.keys(clientErrors).length > 0) return

    setSubmitting(true)
    try {
      const payload = {
        username: form.username,
        email: form.email,
        password: form.password,
        role: form.role,
        displayName: form.displayName,
        address: form.address,
        phoneNumber:
          form.role === 'CUSTOMER' || form.role === 'DELIVERY_PARTNER' ? form.phoneNumber || null : null,
        vehicleNumber: form.role === 'DELIVERY_PARTNER' ? form.vehicleNumber || null : null,
      }
      await register(payload)
    } catch (err) {
      const message = err.message || 'Registration failed'
      setError(message)
      showToast(message, 'error')
      if (err.fieldErrors) setFieldErrors(err.fieldErrors)
    } finally {
      setSubmitting(false)
    }
  }

  const fieldClasses =
    'rounded-md border border-border bg-bg px-3 py-2.5 text-text-h focus:outline-2 focus:outline-offset-1 focus:outline-accent'
  const labelClasses = 'flex flex-col gap-1.5 text-sm text-text-h'
  const fieldErrorClasses = '-mt-2 text-xs text-red-600'

  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-6 px-4 py-8">
      <span className="text-2xl font-bold text-accent">Nearmart</span>
      <form
        onSubmit={handleSubmit}
        className="flex w-full max-w-[380px] flex-col gap-3.5 rounded-lg border border-border bg-bg p-8 shadow-sm text-left"
      >
        <h1 className="m-0 mb-2 text-[32px] text-center">Register</h1>
        {error && <p className="mb-1 text-sm text-red-600">{error}</p>}

        <label className={labelClasses}>
          I am a
          <select name="role" value={form.role} onChange={handleChange} className={fieldClasses}>
            <option value="CUSTOMER">Customer</option>
            <option value="VENDOR">Vendor</option>
            <option value="DELIVERY_PARTNER">Delivery Partner</option>
          </select>
        </label>

        <label className={labelClasses}>
          Username
          <input
            name="username"
            value={form.username}
            onChange={handleChange}
            autoComplete="username"
            className={fieldClasses}
          />
        </label>
        {fieldErrors.username && <p className={fieldErrorClasses}>{fieldErrors.username}</p>}

        <label className={labelClasses}>
          Email
          <input
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            autoComplete="email"
            className={fieldClasses}
          />
        </label>
        {fieldErrors.email && <p className={fieldErrorClasses}>{fieldErrors.email}</p>}

        <label className={labelClasses}>
          {form.role === 'VENDOR' ? 'Store name' : 'Full name'}
          <input name="displayName" value={form.displayName} onChange={handleChange} className={fieldClasses} />
        </label>
        {fieldErrors.displayName && <p className={fieldErrorClasses}>{fieldErrors.displayName}</p>}

        <label className={labelClasses}>
          Address
          <input name="address" value={form.address} onChange={handleChange} className={fieldClasses} />
        </label>
        {fieldErrors.address && <p className={fieldErrorClasses}>{fieldErrors.address}</p>}

        {(form.role === 'CUSTOMER' || form.role === 'DELIVERY_PARTNER') && (
          <label className={labelClasses}>
            {form.role === 'CUSTOMER' ? 'Phone number (optional — enables quick phone login)' : 'Phone number'}
            <input
              name="phoneNumber"
              type="tel"
              value={form.phoneNumber}
              onChange={(e) => setForm({ ...form, phoneNumber: e.target.value.replace(/\D/g, '').slice(0, 10) })}
              autoComplete="tel"
              maxLength={10}
              className={fieldClasses}
            />
          </label>
        )}
        {fieldErrors.phoneNumber && <p className={fieldErrorClasses}>{fieldErrors.phoneNumber}</p>}

        {form.role === 'DELIVERY_PARTNER' && (
          <label className={labelClasses}>
            Vehicle number (optional)
            <input
              name="vehicleNumber"
              value={form.vehicleNumber}
              onChange={handleChange}
              className={fieldClasses}
            />
          </label>
        )}

        <label className={labelClasses}>
          Password
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            autoComplete="new-password"
            className={fieldClasses}
          />
        </label>
        {fieldErrors.password && <p className={fieldErrorClasses}>{fieldErrors.password}</p>}

        <label className={labelClasses}>
          Confirm password
          <input
            name="confirmPassword"
            type="password"
            value={form.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
            className={fieldClasses}
          />
        </label>
        {fieldErrors.confirmPassword && <p className={fieldErrorClasses}>{fieldErrors.confirmPassword}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="mt-1.5 cursor-pointer rounded-md border-none bg-accent px-4 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          {submitting ? 'Creating account...' : 'Register'}
        </button>
        <p className="text-center text-sm">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  )
}
