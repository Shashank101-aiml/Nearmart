import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function LoginPage() {
  const { login } = useAuth()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (!form.username.trim() || !form.password) {
      setError('Username and password are required')
      return
    }

    setSubmitting(true)
    try {
      await login(form)
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center px-4 py-8">
      <form onSubmit={handleSubmit} className="flex w-full max-w-[380px] flex-col gap-3.5 text-left">
        <h1 className="m-0 mb-2 text-[32px] text-center">Log in</h1>
        {error && <p className="mb-1 text-sm text-red-600">{error}</p>}
        <label className="flex flex-col gap-1.5 text-sm text-text-h">
          Username
          <input
            name="username"
            value={form.username}
            onChange={handleChange}
            autoComplete="username"
            className="rounded-md border border-border bg-bg px-3 py-2.5 text-text-h focus:outline-2 focus:outline-offset-1 focus:outline-accent"
          />
        </label>
        <label className="flex flex-col gap-1.5 text-sm text-text-h">
          Password
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            autoComplete="current-password"
            className="rounded-md border border-border bg-bg px-3 py-2.5 text-text-h focus:outline-2 focus:outline-offset-1 focus:outline-accent"
          />
        </label>
        <button
          type="submit"
          disabled={submitting}
          className="mt-1.5 cursor-pointer rounded-md border-none bg-accent px-4 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-60"
        >
          {submitting ? 'Logging in...' : 'Log in'}
        </button>
        <p className="text-center text-sm">
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  )
}
