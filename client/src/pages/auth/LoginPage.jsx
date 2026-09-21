import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useToast } from '../../hooks/useToast'
import * as authService from '../../services/authService'

const fieldClasses =
  'rounded-md border border-border bg-bg px-3 py-2.5 text-text-h focus:outline-2 focus:outline-offset-1 focus:outline-accent'
const labelClasses = 'flex flex-col gap-1.5 text-sm text-text-h'
const submitButtonClasses =
  'mt-1.5 cursor-pointer rounded-md border-none bg-accent px-4 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-60'

function PasswordLoginForm() {
  const { login } = useAuth()
  const { showToast } = useToast()
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
      const message = err.message || 'Login failed'
      setError(message)
      showToast(message, 'error')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3.5">
      {error && <p className="mb-1 text-sm text-red-600">{error}</p>}
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
      <label className={labelClasses}>
        Password
        <input
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          autoComplete="current-password"
          className={fieldClasses}
        />
      </label>
      <button type="submit" disabled={submitting} className={submitButtonClasses}>
        {submitting ? 'Logging in...' : 'Log in'}
      </button>
    </form>
  )
}

function OtpLoginForm() {
  const { loginWithOtp } = useAuth()
  const { showToast } = useToast()
  const [phoneNumber, setPhoneNumber] = useState('')
  const [code, setCode] = useState('')
  const [simulatedOtp, setSimulatedOtp] = useState('')
  const [error, setError] = useState('')
  const [requesting, setRequesting] = useState(false)
  const [verifying, setVerifying] = useState(false)

  const handleRequestOtp = async (e) => {
    e.preventDefault()
    setError('')

    if (!phoneNumber.trim()) {
      setError('Phone number is required')
      return
    }

    setRequesting(true)
    try {
      const data = await authService.requestOtp(phoneNumber.trim())
      setSimulatedOtp(data.otp)
    } catch (err) {
      const message = err.message || 'Failed to send OTP'
      setError(message)
      showToast(message, 'error')
    } finally {
      setRequesting(false)
    }
  }

  const handleVerifyOtp = async (e) => {
    e.preventDefault()
    setError('')

    if (!code.trim()) {
      setError('Enter the OTP code')
      return
    }

    setVerifying(true)
    try {
      await loginWithOtp(phoneNumber.trim(), code.trim())
    } catch (err) {
      const message = err.message || 'OTP verification failed'
      setError(message)
      showToast(message, 'error')
    } finally {
      setVerifying(false)
    }
  }

  if (!simulatedOtp) {
    return (
      <form onSubmit={handleRequestOtp} className="flex flex-col gap-3.5">
        {error && <p className="mb-1 text-sm text-red-600">{error}</p>}
        <label className={labelClasses}>
          Phone number
          <input
            name="phoneNumber"
            type="tel"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            autoComplete="tel"
            className={fieldClasses}
          />
        </label>
        <button type="submit" disabled={requesting} className={submitButtonClasses}>
          {requesting ? 'Sending OTP...' : 'Send OTP'}
        </button>
      </form>
    )
  }

  return (
    <form onSubmit={handleVerifyOtp} className="flex flex-col gap-3.5">
      {error && <p className="mb-1 text-sm text-red-600">{error}</p>}
      <p className="rounded-md border border-accent-border bg-accent-bg px-3 py-2.5 text-sm text-accent">
        Simulated OTP (for testing): <strong>{simulatedOtp}</strong>
      </p>
      <label className={labelClasses}>
        Enter OTP
        <input
          name="code"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          autoComplete="one-time-code"
          className={fieldClasses}
        />
      </label>
      <button type="submit" disabled={verifying} className={submitButtonClasses}>
        {verifying ? 'Verifying...' : 'Verify & log in'}
      </button>
    </form>
  )
}

export default function LoginPage() {
  const [mode, setMode] = useState('password')

  return (
    <div className="flex flex-1 flex-col items-center justify-center gap-6 px-4 py-8">
      <span className="text-2xl font-bold text-accent">Nearmart</span>
      <div className="flex w-full max-w-[380px] flex-col gap-3.5 rounded-lg border border-border bg-bg p-8 shadow-sm text-left">
        <h1 className="m-0 mb-2 text-[32px] text-center">Log in</h1>

        {mode === 'password' ? <PasswordLoginForm /> : <OtpLoginForm />}

        <button
          type="button"
          onClick={() => setMode(mode === 'password' ? 'otp' : 'password')}
          className="cursor-pointer border-none bg-transparent p-0 text-center text-sm text-accent hover:underline"
        >
          {mode === 'password' ? 'Log in with phone instead' : 'Log in with username & password instead'}
        </button>

        <p className="text-center text-sm">
          Don't have an account? <Link to="/register">Register</Link>
        </p>
      </div>
    </div>
  )
}
