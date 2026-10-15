import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Search,
  BookOpen,
  PlusCircle,
  Sparkles,
  ArrowRight,
  AlertCircle,
  RefreshCw,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { getResources } from '../services/resourceService'
import ResourceCard from '../components/ResourceCard'
import UploadModal from '../components/UploadModal'
import EditProfileModal from '../components/EditProfileModal'
import type { ResourceResponse, ResourceCategory } from '../types'
import { CATEGORY_LABELS } from '../types'

export default function Home() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [searchQuery, setSearchQuery] = useState('')
  const [recentResources, setRecentResources] = useState<ResourceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [isUploadOpen, setIsUploadOpen] = useState(false)
  const [isProfileModalOpen, setIsProfileModalOpen] = useState(false)

  const fetchRecent = async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getResources({ page: 0, size: 5, sort: 'newest' })
      setRecentResources(res.content || [])
    } catch (err: any) {
      setError(err?.message || 'Unable to load recently added resources.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchRecent()
  }, [])

  const handleSearchSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      navigate(`/resources?search=${encodeURIComponent(searchQuery.trim())}`)
    } else {
      navigate('/resources')
    }
  }

  const handleUploadSuccess = (created: ResourceResponse) => {
    setRecentResources((prev) => [created, ...prev])
  }

  return (
    <div className="page wrap">
      {/* Profile Completion Reminder Banner */}
      {user && !user.profileComplete && (
        <div
          className="alert alert-warning"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '0.75rem',
            marginBottom: '2rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <Sparkles size={18} color="var(--warning)" style={{ flexShrink: 0 }} />
            <span>
              Your student profile is incomplete. Add your department and academic bio so peers can recognize your contributions.
            </span>
          </div>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setIsProfileModalOpen(true)}
          >
            Complete Profile
          </button>
        </div>
      )}

      {/* Greeting & Header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '2.25rem',
        }}
      >
        <div>
          <h1 className="page-title">
            Hello, {user?.name.split(' ')[0] || 'Student'}.
          </h1>
          <p className="page-subtitle">
            Welcome to the DESPU academic hub. Browse course materials, discover past papers,
            or share knowledge with your peers.
          </p>
        </div>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setIsUploadOpen(true)}
        >
          <PlusCircle size={16} /> Share Resource
        </button>
      </div>

      {/* Quick Search */}
      <form onSubmit={handleSearchSubmit} role="search" style={{ marginBottom: '2.5rem' }}>
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="search"
            className="input-field"
            placeholder="Search notes, lab manuals, authors, or topics..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
      </form>

      {/* Category Shortcuts */}
      <section style={{ marginBottom: '3rem' }}>
        <h2
          style={{
            fontFamily: 'var(--sans)',
            fontSize: '1.35rem',
            fontWeight: 700,
            color: 'var(--text-main)',
            margin: '0 0 1.15rem',
          }}
        >
          Academic Categories
        </h2>

        <div className="categories-showcase">
          {Object.entries(CATEGORY_LABELS).map(([catKey, label]) => (
            <Link
              key={catKey}
              to={`/resources?category=${catKey}`}
              className="category-chip"
            >
              <BookOpen size={16} color="var(--accent-terracotta)" />
              <span style={{ fontSize: '0.98rem' }}>{label}</span>
            </Link>
          ))}
        </div>
      </section>

      {/* Recently Added Section */}
      <section>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: '1.25rem',
          }}
        >
          <h2
            style={{
              fontFamily: 'var(--sans)',
              fontSize: '1.35rem',
              fontWeight: 700,
              color: 'var(--text-main)',
              margin: 0,
            }}
          >
            Recently Added
          </h2>
          <Link
            to="/resources"
            className="nav-link"
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.3rem',
              fontSize: '0.88rem',
            }}
          >
            View all <ArrowRight size={14} />
          </Link>
        </div>

        {loading && (
          <div className="resource-list">
            {[1, 2, 3].map((i) => (
              <div key={i} className="card" style={{ padding: '1.5rem' }}>
                <div className="skeleton" style={{ height: '20px', width: '60%', marginBottom: '0.75rem' }} />
                <div className="skeleton" style={{ height: '14px', width: '90%', marginBottom: '0.5rem' }} />
                <div className="skeleton" style={{ height: '14px', width: '40%' }} />
              </div>
            ))}
          </div>
        )}

        {error && (
          <div className="alert alert-danger" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <div style={{ flex: 1 }}>
              <p style={{ margin: '0 0 0.5rem', fontWeight: 500 }}>Failed to load recent resources</p>
              <p style={{ margin: 0, fontSize: '0.84rem' }}>{error}</p>
            </div>
            <button
              type="button"
              className="btn btn-sm btn-secondary"
              onClick={fetchRecent}
            >
              <RefreshCw size={13} /> Retry
            </button>
          </div>
        )}

        {!loading && !error && recentResources.length === 0 && (
          <div className="empty-state">
            <h3 className="empty-state-title">No resources yet</h3>
            <p className="empty-state-desc">
              Be the first student to upload lecture notes or lab manuals to SeniorConnect.
            </p>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => setIsUploadOpen(true)}
            >
              <PlusCircle size={15} /> Upload First Resource
            </button>
          </div>
        )}

        {!loading && !error && recentResources.length > 0 && (
          <div className="resource-list">
            {recentResources.map((res) => (
              <ResourceCard key={res.id} resource={res} />
            ))}
          </div>
        )}
      </section>

      {/* Modals */}
      <UploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onSuccess={handleUploadSuccess}
      />

      <EditProfileModal
        isOpen={isProfileModalOpen}
        onClose={() => setIsProfileModalOpen(false)}
      />
    </div>
  )
}
