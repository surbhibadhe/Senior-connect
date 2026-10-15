import { useEffect, useState, useCallback } from 'react'
import {
  User,
  Mail,
  Building,
  Calendar,
  Sparkles,
  Edit3,
  BookOpen,
  PlusCircle,
  AlertCircle,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { getContributorResources } from '../services/authService'
import ResourceCard from '../components/ResourceCard'
import EditProfileModal from '../components/EditProfileModal'
import UploadModal from '../components/UploadModal'
import type { ResourceResponse } from '../types'

export default function Profile() {
  const { user } = useAuth()
  const [contributions, setContributions] = useState<ResourceResponse[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [isUploadOpen, setIsUploadOpen] = useState(false)

  const loadContributions = useCallback(async () => {
    if (!user?.id) return
    try {
      setLoading(true)
      setError(null)
      const res = await getContributorResources(user.id, 0, 20, 'newest')
      setContributions(res.content || [])
      setTotalCount(res.totalElements || 0)
    } catch (err: any) {
      setError(err?.message || 'Failed to load your shared resources.')
    } finally {
      setLoading(false)
    }
  }, [user?.id])

  useEffect(() => {
    loadContributions()
  }, [loadContributions])

  if (!user) return null

  const memberSince = user.createdAt
    ? new Date(user.createdAt).toLocaleDateString('en-IN', {
        month: 'long',
        year: 'numeric',
      })
    : 'Unknown'

  return (
    <div className="page wrap">
      {/* Profile Header Card */}
      <div
        className="card"
        style={{
          padding: '2rem 2.25rem',
          marginBottom: '2.5rem',
          background: 'var(--bg-card)',
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1.5rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
            <div
              style={{
                width: '68px',
                height: '68px',
                borderRadius: '50%',
                backgroundColor: 'var(--accent-main)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.8rem',
                fontFamily: 'var(--sans)',
                fontWeight: 700,
                color: 'var(--text-main)',
                overflow: 'hidden',
                border: '2px solid var(--border)',
              }}
            >
              {user.profilePicture ? (
                <img
                  src={user.profilePicture}
                  alt={user.name}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              ) : (
                user.name.charAt(0).toUpperCase()
              )}
            </div>

            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', marginBottom: '0.25rem' }}>
                <h1
                  style={{
                    fontFamily: 'var(--sans)',
                    fontSize: '1.75rem',
                    fontWeight: 700,
                    color: 'var(--text-main)',
                    margin: 0,
                  }}
                >
                  {user.name}
                </h1>
                <span className={`role-badge ${user.role.toLowerCase()}`}>
                  {user.role}
                </span>
              </div>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '0.9rem',
                  fontSize: '0.84rem',
                  color: 'var(--text-secondary)',
                }}
              >
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Mail size={13} /> {user.email}
                </span>
                {user.department && (
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Building size={13} /> {user.department}
                  </span>
                )}
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Calendar size={13} /> Joined {memberSince}
                </span>
              </div>
            </div>
          </div>

          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setIsEditModalOpen(true)}
          >
            <Edit3 size={14} /> Edit Profile
          </button>
        </div>

        {/* Bio */}
        <div
          style={{
            marginTop: '1.5rem',
            paddingTop: '1.25rem',
            borderTop: '1px solid var(--border-subtle)',
          }}
        >
          <div style={{ fontSize: '0.82rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.35rem' }}>
            ACADEMIC BIO
          </div>
          <p
            style={{
              margin: 0,
              fontSize: '0.94rem',
              color: user.bio ? 'var(--text-main)' : 'var(--text-muted)',
              fontStyle: user.bio ? 'normal' : 'italic',
            }}
          >
            {user.bio || 'No bio provided yet. Click "Edit Profile" to share your focus areas or study advice.'}
          </p>
        </div>
      </div>

      {/* Contributions Section */}
      <section>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: '1.25rem',
          }}
        >
          <div>
            <h2
              style={{
                fontFamily: 'var(--sans)',
                fontSize: '1.45rem',
                fontWeight: 700,
                color: 'var(--text-main)',
                margin: '0 0 0.2rem',
              }}
            >
              My Contributions
            </h2>
            <p style={{ margin: 0, fontSize: '0.86rem', color: 'var(--text-secondary)' }}>
              Resources and notes you have published to the university repository.
            </p>
          </div>

          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => setIsUploadOpen(true)}
          >
            <PlusCircle size={14} /> Share Another
          </button>
        </div>

        {loading && (
          <div className="resource-list">
            {[1, 2].map((i) => (
              <div key={i} className="card" style={{ padding: '1.5rem' }}>
                <div className="skeleton" style={{ height: '20px', width: '60%', marginBottom: '0.75rem' }} />
                <div className="skeleton" style={{ height: '14px', width: '90%' }} />
              </div>
            ))}
          </div>
        )}

        {error && !loading && (
          <div className="alert alert-danger" role="alert">
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {!loading && !error && contributions.length === 0 && (
          <div className="empty-state">
            <BookOpen
              size={36}
              color="var(--accent-terracotta)"
              style={{ marginBottom: '1rem' }}
            />
            <h3 className="empty-state-title">No contributions shared yet</h3>
            <p className="empty-state-desc">
              Have you solved an experiment manual or compiled exam notes? Share them to assist following batches.
            </p>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => setIsUploadOpen(true)}
            >
              <PlusCircle size={15} /> Upload Your First Resource
            </button>
          </div>
        )}

        {!loading && !error && contributions.length > 0 && (
          <div className="resource-list">
            {contributions.map((res) => (
              <ResourceCard key={res.id} resource={res} />
            ))}
          </div>
        )}
      </section>

      {/* Modals */}
      <EditProfileModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
      />

      <UploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onSuccess={() => loadContributions()}
      />
    </div>
  )
}
