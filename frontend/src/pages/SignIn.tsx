import { Link, Navigate, useSearchParams } from 'react-router-dom'
import { AlertCircle, ShieldCheck } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'

export default function SignIn() {
  const { user, signIn, loading } = useAuth()
  const [searchParams] = useSearchParams()
  const authError = searchParams.get('error')

  if (loading) {
    return (
      <div className="modal-backdrop" style={{ background: 'var(--bg-main)' }}>
        <p style={{ color: 'var(--text-secondary)' }}>Verifying session…</p>
      </div>
    )
  }

  if (user) {
    return <Navigate to="/home" replace />
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--bg-main)',
        padding: '1.5rem',
      }}
    >
      <div
        className="card"
        style={{
          maxWidth: '420px',
          width: '100%',
          padding: '2.5rem 2rem',
          textAlign: 'center',
          boxShadow: '0 8px 24px rgba(51, 47, 45, 0.05)',
        }}
      >
        <Link
          to="/"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.5rem',
            marginBottom: '1.5rem',
            color: 'var(--text-main)',
          }}
        >
          <span style={{ fontFamily: 'var(--serif)', fontSize: '1.5rem' }}>
            SeniorConnect
          </span>
          <span className="brand-badge">DESPU</span>
        </Link>

        <h1
          style={{
            fontFamily: 'var(--serif)',
            fontSize: '1.85rem',
            margin: '0 0 0.6rem',
            fontWeight: 400,
          }}
        >
          Welcome Back
        </h1>

        <p
          style={{
            color: 'var(--text-secondary)',
            fontSize: '0.92rem',
            margin: '0 0 1.5rem',
            lineHeight: 1.5,
          }}
        >
          Sign in to access course notes, lab manuals, past exam papers, and shared materials.
        </p>

        {authError && (
          <div
            className="alert alert-danger"
            style={{ textAlign: 'left', marginBottom: '1.5rem' }}
          >
            <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <strong style={{ display: 'block', marginBottom: '2px' }}>Authentication Error</strong>
              <span>
                {authError.includes('despu.edu.in')
                  ? 'Access is restricted to verified @despu.edu.in accounts. Please choose your college email address.'
                  : authError}
              </span>
            </div>
          </div>
        )}

        <button
          type="button"
          className="btn btn-primary"
          style={{
            width: '100%',
            padding: '0.75rem',
            fontSize: '0.96rem',
            marginBottom: '1.5rem',
          }}
          onClick={signIn}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" style={{ marginRight: '6px' }}>
            <path
              fill="#FFFFFF"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#FFFFFF"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FFFFFF"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#FFFFFF"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          Continue with Google
        </button>

        <div
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '0.65rem',
            textAlign: 'left',
            padding: '0.85rem 1rem',
            background: 'var(--bg-secondary)',
            borderRadius: 'var(--radius)',
            border: '1px solid var(--border)',
            fontSize: '0.8rem',
            color: 'var(--text-secondary)',
          }}
        >
          <ShieldCheck size={18} color="var(--accent-terracotta)" style={{ flexShrink: 0, marginTop: '2px' }} />
          <span>
            Strict institutional verification: only active students and faculty with verified{' '}
            <strong style={{ color: 'var(--text-main)' }}>@despu.edu.in</strong> email addresses can log in.
          </span>
        </div>

        <div style={{ marginTop: '2rem', fontSize: '0.84rem' }}>
          <Link to="/" style={{ color: 'var(--text-secondary)' }}>
            ← Back to SeniorConnect overview
          </Link>
        </div>
      </div>
    </div>
  )
}
