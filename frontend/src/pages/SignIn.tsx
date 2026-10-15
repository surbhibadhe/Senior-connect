import { Link, Navigate, useSearchParams } from 'react-router-dom'
import {
  GraduationCap,
  ArrowRight,
  Lock,
  ShieldCheck,
  AlertCircle,
  ArrowLeft,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import { useAuth } from '../hooks/useAuth'

export default function SignIn() {
  const { user, signIn, loading } = useAuth()
  const [searchParams] = useSearchParams()
  const authError = searchParams.get('error')

  if (loading) {
    return (
      <div className="modal-backdrop" style={{ background: '#FFFFFF' }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.96rem' }}>Verifying institutional session…</p>
      </div>
    )
  }

  if (user) {
    return <Navigate to="/home" replace />
  }

  return (
    <>
      <Navbar />

      <main className="signin-page-viewport">
        <div className="signin-card-container">
          {/* Soft Sage Icon Square */}
          <div className="signin-sage-icon-box">
            <GraduationCap size={28} color="#00543F" strokeWidth={2} />
          </div>

          {/* Uppercase Eyebrow */}
          <div className="signin-eyebrow-tag">
            ONE CAMPUS. COUNTLESS POSSIBILITIES.
          </div>

          {/* Main 2-line Heading */}
          <h1 className="signin-display-title">
            Your people.
            <br />
            Your shared space.
          </h1>

          {/* Welcoming Subtext */}
          <p className="signin-welcome-desc">
            Good to have you here. Join your seniors and juniors in making college a little easier.
          </p>

          {/* Auth Error Banner if redirected from OAuth2 failure */}
          {authError && (
            <div className="alert alert-danger" style={{ marginBottom: '1.5rem', textAlign: 'left' }}>
              <AlertCircle size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
              <div>
                <strong style={{ display: 'block', fontSize: '0.88rem', marginBottom: '2px' }}>Authentication Error</strong>
                <span style={{ fontSize: '0.84rem' }}>
                  {authError.includes('despu.edu.in')
                    ? 'Access is strictly restricted to verified @despu.edu.in accounts. Please choose your college email address.'
                    : authError}
                </span>
              </div>
            </div>
          )}

          {/* Continue with Google Button */}
          <button
            type="button"
            className="btn btn-google-signin"
            onClick={signIn}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <svg width="18" height="18" viewBox="0 0 24 24">
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <span>Continue with Google</span>
            </div>
            <ArrowRight size={16} strokeWidth={2.2} />
          </button>

          {/* Institutional note */}
          <div className="signin-lock-row">
            <Lock size={13} />
            <span>Only @despu.edu.in accounts</span>
          </div>

          {/* Divider */}
          <div className="signin-divider-label">
            <span>TAKE A LOOK AROUND</span>
          </div>

          {/* Explore Student Preview Button */}
          <Link to="/resources" className="btn btn-preview-action">
            <span>Explore the student preview</span>
            <ArrowRight size={16} strokeWidth={2.2} />
          </Link>
          <div className="signin-preview-caption">
            Sample content only. No account required for the preview.
          </div>

          {/* Bottom Trust Badge */}
          <div className="signin-trust-badge">
            <ShieldCheck size={16} color="var(--primary-green)" />
            <span>A student space. Your college email keeps it that way.</span>
          </div>

          {/* Back link */}
          <div style={{ marginTop: '2rem' }}>
            <Link to="/" className="signin-back-link">
              <ArrowLeft size={14} />
              <span>Back to Senior Connect</span>
            </Link>
          </div>
        </div>
      </main>
    </>
  )
}
