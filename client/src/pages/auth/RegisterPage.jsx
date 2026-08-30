import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const initialForm = {
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'CUSTOMER',
  displayName: '',
  address: '',
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
  return errors
}

export default function RegisterPage() {
  const { register } = useAuth()
  const [form, setForm] = useState(initialForm)
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
      }
      await register(payload)
    } catch (err) {
      setError(err.message || 'Registration failed')
      if (err.fieldErrors) setFieldErrors(err.fieldErrors)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <form onSubmit={handleSubmit} className="auth-form">
        <h1>Register</h1>
        {error && <p className="auth-error">{error}</p>}

        <label>
          I am a
          <select name="role" value={form.role} onChange={handleChange}>
            <option value="CUSTOMER">Customer</option>
            <option value="VENDOR">Vendor</option>
          </select>
        </label>

        <label>
          Username
          <input name="username" value={form.username} onChange={handleChange} autoComplete="username" />
        </label>
        {fieldErrors.username && <p className="field-error">{fieldErrors.username}</p>}

        <label>
          Email
          <input name="email" type="email" value={form.email} onChange={handleChange} autoComplete="email" />
        </label>
        {fieldErrors.email && <p className="field-error">{fieldErrors.email}</p>}

        <label>
          {form.role === 'VENDOR' ? 'Store name' : 'Full name'}
          <input name="displayName" value={form.displayName} onChange={handleChange} />
        </label>
        {fieldErrors.displayName && <p className="field-error">{fieldErrors.displayName}</p>}

        <label>
          Address
          <input name="address" value={form.address} onChange={handleChange} />
        </label>
        {fieldErrors.address && <p className="field-error">{fieldErrors.address}</p>}

        <label>
          Password
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            autoComplete="new-password"
          />
        </label>
        {fieldErrors.password && <p className="field-error">{fieldErrors.password}</p>}

        <label>
          Confirm password
          <input
            name="confirmPassword"
            type="password"
            value={form.confirmPassword}
            onChange={handleChange}
            autoComplete="new-password"
          />
        </label>
        {fieldErrors.confirmPassword && <p className="field-error">{fieldErrors.confirmPassword}</p>}

        <button type="submit" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Register'}
        </button>
        <p>
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  )
}
