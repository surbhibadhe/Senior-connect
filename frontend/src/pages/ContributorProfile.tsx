import { useEffect, useState, useCallback } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  User,
  Building,
  Calendar,
  BookOpen,
  ArrowLeft,
  AlertCircle,
} from 'lucide-react'
import {
  getContributorProfile,
  getContributorResources,
} from '../services/authService'
import ResourceCard from '../components/ResourceCard'
import type {
  ContributorProfileResponse,
  ResourceResponse,
  PageResponse,
} from '../types'

export default function ContributorProfile() {
  const { id } = useParams<{ id: string }>()
  const numericId = parseInt(id || '0', 10)

  const [contributor, setContributor] =
    useState<ContributorProfileResponse | null>(null)
  const [resourcesData, setResourcesData] = useState<PageResponse<ResourceResponse>>({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    empty: true,
  })

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadData = useCallback(async () => {
    if (!numericId) return
    try {
      setLoading(true)
      setError(null)
      const [profile, resList] = await Promise.all([
        getContributorProfile(numericId),
        getContributorResources(numericId, 0, 10, 'newest'),
      ])
      setContributor(profile)
      setResourcesData(resList)
    } catch (err: any) {
      setError(err?.message || 'Could not load contributor profile.')
    } finally {
      setLoading(false)
    }
  }, [numericId])

  useEffect(() => {
    loadData()
  }, [loadData])

  if (loading) {
    return (
      <div className="page wrap">
        <div style={{ maxWidth: '820px', margin: '0 auto' }}>
          <div className="skeleton" style={{ height: '140px', width: '100%', marginBottom: '2rem' }} />
          <div className="skeleton" style={{ height: '80px', width: '100%' }} />
        </div>
      </div>
    )
  }

  if (error || !contributor) {
    return (
      <div className="page wrap">
        <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'center' }}>
          <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
            <AlertCircle size={20} />
            <span>{error || 'Contributor not found.'}</span>
          </div>
          <Link to="/resources" className="btn btn-secondary">
            <ArrowLeft size={16} /> Back to Resources
          </Link>
        </div>
      </div>
    )
  }

  const memberSince = contributor.createdAt
    ? new Date(contributor.createdAt).toLocaleDateString('en-IN', {
        month: 'long',
        year: 'numeric',
      })
    : 'Unknown'

  return (
    <div className="page wrap">
      <div style={{ maxWidth: '820px', margin: '0 auto' }}>
        <Link
          to="/resources"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.4rem',
            color: 'var(--text-secondary)',
            fontSize: '0.88rem',
            marginBottom: '1.5rem',
          }}
        >
          <ArrowLeft size={15} /> Back to Resources
        </Link>

        {/* Contributor Card */}
        <div
          className="card"
          style={{
            padding: '2rem 2.25rem',
            marginBottom: '2.5rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
            <div
              style={{
                width: '64px',
                height: '64px',
                borderRadius: '50%',
                backgroundColor: 'var(--accent-main)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.6rem',
                fontFamily: 'var(--sans)',
                fontWeight: 700,
                color: 'var(--text-main)',
                overflow: 'hidden',
                border: '2px solid var(--border)',
              }}
            >
              {contributor.profilePicture ? (
                <img
                  src={contributor.profilePicture}
                  alt={contributor.name}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              ) : (
                contributor.name.charAt(0).toUpperCase()
              )}
            </div>

            <div>
              <h1
                style={{
                  fontFamily: 'var(--sans)',
                  fontSize: '1.75rem',
                  fontWeight: 700,
                  color: 'var(--text-main)',
                  margin: '0 0 0.25rem',
                }}
              >
                {contributor.name}
              </h1>

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
                {contributor.department && (
                  <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Building size={13} /> {contributor.department}
                  </span>
                )}
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Calendar size={13} /> Member since {memberSince}
                </span>
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
                  <BookOpen size={13} /> {contributor.totalResources} resource
                  {contributor.totalResources === 1 ? '' : 's'} shared
                </span>
              </div>
            </div>
          </div>

          {contributor.bio && (
            <div
              style={{
                marginTop: '1.25rem',
                paddingTop: '1rem',
                borderTop: '1px solid var(--border-subtle)',
              }}
            >
              <p
                style={{
                  margin: 0,
                  fontSize: '0.94rem',
                  color: 'var(--text-main)',
                  lineHeight: 1.6,
                }}
              >
                {contributor.bio}
              </p>
            </div>
          )}
        </div>

        {/* Contributor's Shared Resources */}
        <section>
          <h2
            style={{
              fontFamily: 'var(--sans)',
              fontSize: '1.35rem',
              fontWeight: 700,
              color: 'var(--text-main)',
              margin: '0 0 1rem',
            }}
          >
            Shared by {contributor.name}
          </h2>

          {resourcesData.content.length === 0 ? (
            <div className="empty-state">
              <h3 className="empty-state-title">No resources available</h3>
              <p className="empty-state-desc">
                This student has not published any public resources yet.
              </p>
            </div>
          ) : (
            <div className="resource-list">
              {resourcesData.content.map((res) => (
                <ResourceCard key={res.id} resource={res} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
