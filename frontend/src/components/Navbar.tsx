import { useState } from 'react'
import { Link, NavLink } from 'react-router-dom'
import {
  Compass,
  Bookmark,
  PlusCircle,
  ShieldAlert,
  LogOut,
  User as UserIcon,
  Menu,
  X,
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
        <Link to={user ? '/home' : '/'} className="brand">
          <span className="brand-title">SeniorConnect</span>
          <span className="brand-badge">DESPU</span>
        </Link>

        {user ? (
          <>
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
                  <Compass size={16} />
                  Browse
                </span>
              </NavLink>
              <NavLink
                to="/bookmarks"
                className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              >
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Bookmark size={16} />
                  Saved
                </span>
              </NavLink>

              {user.role === 'ADMIN' && (
                <NavLink
                  to="/admin"
                  className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                >
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <ShieldAlert size={16} />
                    Moderation
                  </span>
                </NavLink>
              )}
            </nav>

            <div className="nav-actions hide-mobile">
              {onOpenUpload && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={onOpenUpload}
                >
                  <PlusCircle size={15} />
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
                <LogOut size={16} />
              </button>
            </div>

            {/* Mobile Hamburger toggle */}
            <button
              type="button"
              className="btn btn-outline btn-icon"
              style={{ display: 'none' }}
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              aria-label="Toggle navigation menu"
            >
              {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </>
        ) : (
          <nav className="nav-links">
            <a href="/#about" className="nav-link hide-mobile">
              About
            </a>
            <Link to="/signin" className="btn btn-secondary btn-sm">
              Sign In
            </Link>
            <Link to="/signin" className="btn btn-primary btn-sm">
              Get Started
            </Link>
          </nav>
        )}
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && user && (
        <div
          style={{
            padding: '1rem 1.5rem 1.5rem',
            borderTop: '1px solid var(--border)',
            background: 'var(--bg-main)',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.85rem',
          }}
        >
          <Link
            to="/home"
            className="nav-link"
            onClick={() => setMobileMenuOpen(false)}
          >
            Home
          </Link>
          <Link
            to="/resources"
            className="nav-link"
            onClick={() => setMobileMenuOpen(false)}
          >
            Browse Resources
          </Link>
          <Link
            to="/bookmarks"
            className="nav-link"
            onClick={() => setMobileMenuOpen(false)}
          >
            Saved Bookmarks
          </Link>
          {user.role === 'ADMIN' && (
            <Link
              to="/admin"
              className="nav-link"
              onClick={() => setMobileMenuOpen(false)}
            >
              Admin Moderation
            </Link>
          )}
          <Link
            to="/profile"
            className="nav-link"
            onClick={() => setMobileMenuOpen(false)}
          >
            <UserIcon size={16} style={{ display: 'inline', marginRight: '6px' }} />
            Profile ({user.name})
          </Link>
          {onOpenUpload && (
            <button
              type="button"
              className="btn btn-primary btn-sm"
              style={{ width: '100%', marginTop: '0.5rem' }}
              onClick={() => {
                setMobileMenuOpen(false)
                onOpenUpload()
              }}
            >
              <PlusCircle size={16} /> Share Resource
            </button>
          )}
          <button
            type="button"
            className="btn btn-outline btn-sm"
            style={{ width: '100%' }}
            onClick={() => void signOut()}
          >
            <LogOut size={16} /> Sign Out
          </button>
        </div>
      )}
    </header>
  )
}
