import { useAuth } from '../../hooks/useAuth'

export default function AuthenticatedLanding({ heading }) {
  const { user, logout } = useAuth()

  return (
    <div className="authenticated-landing">
      <h1>{heading}</h1>
      <p>
        Signed in as <strong>{user.username}</strong> ({user.role})
      </p>
      <button type="button" onClick={logout}>
        Log out
      </button>
    </div>
  )
}
