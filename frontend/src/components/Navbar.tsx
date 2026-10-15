import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import {
  GraduationCap,
  Plus,
  Bookmark,
  Compass,
  ShieldAlert,
  LogOut,
  Menu,
  X,
  ArrowUpRight,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'

interface NavbarProps {
  onOpenUpload?: () => void
}

export default function Navbar({ onOpenUpload }: NavbarProps) {
  const { user, signOut } = useAuth()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  return (
    <header className="navbar-wrapper">
      <div className="wrap navbar">
        {/* Brand matching screenshot: rounded dark-green square with graduation cap + 'senior connect ↗' */}
        <Link to={user ? '/home' : '/'} className="brand">
          <div className="brand-logo-icon">
            <GraduationCap size={18} color="#FFFFFF" strokeWidth={2.2} />
          </div>
          <div className="brand-name">
            <span className="brand-senior">senior</span>{' '}
            <span className="brand-connect">connect</span>
            <span className="brand-arrow">↗</span>
          </div>
        </Link>

        {user ? (
          <>
            {/* Authenticated Desktop Navigation */}
            <nav className="nav-links hide-mobile" aria-label="Main Navigation">
              <NavLink
                to="/home"
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                Home
              </NavLink>
              <NavLink
                to="/resources"
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Compass size={15} />
                  Resources
                </span>
              </NavLink>
              <NavLink
                to="/bookmarks"
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Bookmark size={15} />
                  Saved
                </span>
              </NavLink>

              {user.role === 'ADMIN' && (
                <NavLink
                  to="/admin"
                  className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                >
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <ShieldAlert size={15} />
                    Moderation
                  </span>
                </NavLink>
              )}
            </nav>

            {/* Authenticated Actions */}
            <div className="nav-actions hide-mobile">
              {onOpenUpload && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={onOpenUpload}
                >
                  <Plus size={15} />
                  Share Resource
                </button>
              )}

              <Link to="/profile" className="user-pill" title="View Profile">
                <div className="user-avatar">
                  {user.profilePicture ? (
                    <img src={user.profilePicture} alt={user.name} />
                  ) : (
                    user.name.charAt(0).toUpperCase()
                  )}
                </div>
                <span>{user.name.split(' ')[0]}</span>
                <span className={`role-badge ${user.role.toLowerCase()}`}>
                  {user.role}
                </span>
              </Link>

              <button
                type="button"
                className="btn btn-outline btn-sm btn-icon"
                onClick={() => void signOut()}
                title="Sign out"
                aria-label="Sign out"
              >
                <LogOut size={15} />
              </button>
            </div>
          </>
        ) : (
          /* Public Navigation matching Screenshot 1 */
          <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
            <nav className="nav-links hide-mobile">
              <a href="/#the-idea" className="nav-link">
                The idea
              </a>
              <Link to="/resources" className="nav-link">
                Explore resources
              </Link>
              <a href="/#student-stories" className="nav-link">
                Student stories
              </a>
            </nav>

            <Link to="/signin" className="btn btn-primary btn-nav-signin">
              <span>Sign in</span>
              <ArrowUpRight size={15} strokeWidth={2.4} />
            </Link>
          </div>
        )}

        {/* Mobile Toggle */}
        <button
          type="button"
          className="btn btn-outline btn-icon"
          style={{ display: 'none' }}
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle navigation menu"
        >
          {mobileMenuOpen ? <X size={18} /> : <Menu size={18} />}
        </button>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div
          style={{
            background: 'var(--bg-card)',
            borderBottom: '1px solid var(--border)',
            padding: '1.25rem 1.5rem',
            display: 'flex',
            flexDirection: 'column',
            gap: '1rem',
          }}
        >
          {user ? (
            <>
              <Link to="/home" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                Home
              </Link>
              <Link to="/resources" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                Resources
              </Link>
              <Link to="/bookmarks" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                Saved Resources
              </Link>
              {user.role === 'ADMIN' && (
                <Link to="/admin" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                  Moderation
                </Link>
              )}
              <Link to="/profile" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                Profile ({user.name})
              </Link>
              {onOpenUpload && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => {
                    setMobileMenuOpen(false)
                    onOpenUpload()
                  }}
                >
                  <Plus size={15} /> Share Resource
                </button>
              )}
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => void signOut()}
              >
                <LogOut size={15} /> Sign out
              </button>
            </>
          ) : (
            <>
              <a href="/#the-idea" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                The idea
              </a>
              <Link to="/resources" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                Explore resources
              </Link>
              <a href="/#student-stories" className="nav-link" onClick={() => setMobileMenuOpen(false)}>
                Student stories
              </a>
              <Link
                to="/signin"
                className="btn btn-primary btn-sm"
                onClick={() => setMobileMenuOpen(false)}
              >
                Sign In ↗
              </Link>
            </>
          )}
        </div>
      )}
    </header>
  )
}
