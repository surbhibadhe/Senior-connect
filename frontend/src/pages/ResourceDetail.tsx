import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import {
  Download,
  ExternalLink,
  ThumbsUp,
  Bookmark,
  AlertTriangle,
  Trash2,
  Calendar,
  User,
  ArrowLeft,
  FileText,
  FileCode,
  Check,
  AlertCircle,
  MessageSquare,
} from 'lucide-react'
import DiscussionSection from '../components/DiscussionSection'
import {
  getResourceById,
  downloadResource,
  deleteResource,
  getBookmarkStatus,
  bookmarkResource,
  removeBookmark,
  getHelpfulStatus,
  markHelpful,
  removeHelpful,
} from '../services/resourceService'
import { useAuth } from '../hooks/useAuth'
import ReportModal from '../components/ReportModal'
import type { ResourceResponse } from '../types'
import { CATEGORY_LABELS } from '../types'

export default function ResourceDetail() {
  const { id } = useParams<{ id: string }>()
  const numericId = parseInt(id || '0', 10)
  const navigate = useNavigate()
  const { user } = useAuth()

  const [resource, setResource] = useState<ResourceResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Engagement states
  const [isBookmarked, setIsBookmarked] = useState(false)
  const [isHelpful, setIsHelpful] = useState(false)
  const [helpfulCount, setHelpfulCount] = useState(0)
  const [commentCount, setCommentCount] = useState(0)
  const [actionBusy, setActionBusy] = useState(false)

  // Modals
  const [isReportOpen, setIsReportOpen] = useState(false)
  const [deleteConfirm, setDeleteConfirm] = useState(false)

  const loadDetails = useCallback(async () => {
    if (!numericId) return
    try {
      setLoading(true)
      setError(null)
      const res = await getResourceById(numericId)
      setResource(res)
      setHelpfulCount(res.helpfulCount || 0)
      setCommentCount(res.commentCount || 0)

      // Fetch bookmark & helpful status in parallel
      try {
        const [bmStatus, hStatus] = await Promise.all([
          getBookmarkStatus(numericId),
          getHelpfulStatus(numericId),
        ])
        setIsBookmarked(bmStatus.bookmarked)
        setIsHelpful(hStatus.markedHelpful)
        if (typeof hStatus.helpfulCount === 'number') {
          setHelpfulCount(hStatus.helpfulCount)
        }
      } catch {
        // If unauthenticated or status fails, keep default false
      }
    } catch (err: any) {
      setError(err?.message || 'Resource could not be loaded.')
    } finally {
      setLoading(false)
    }
  }, [numericId])

  useEffect(() => {
    loadDetails()
  }, [loadDetails])

  const handleToggleBookmark = async () => {
    if (!numericId || actionBusy) return
    try {
      setActionBusy(true)
      if (isBookmarked) {
        await removeBookmark(numericId)
        setIsBookmarked(false)
      } else {
        await bookmarkResource(numericId)
        setIsBookmarked(true)
      }
    } catch (err: any) {
      alert(err?.message || 'Could not update bookmark.')
    } finally {
      setActionBusy(false)
    }
  }

  const handleToggleHelpful = async () => {
    if (!numericId || actionBusy) return
    try {
      setActionBusy(true)
      if (isHelpful) {
        const res = await removeHelpful(numericId)
        setIsHelpful(false)
        setHelpfulCount(res.helpfulCount)
      } else {
        const res = await markHelpful(numericId)
        setIsHelpful(true)
        setHelpfulCount(res.helpfulCount)
      }
    } catch (err: any) {
      alert(err?.message || 'Could not update helpful mark.')
    } finally {
      setActionBusy(false)
    }
  }

  const handleDownload = async () => {
    if (!numericId || !resource) return
    try {
      await downloadResource(numericId, resource.originalFileName || undefined)
    } catch (err: any) {
      alert(err?.message || 'Download failed.')
    }
  }

  const handleDelete = async () => {
    if (!numericId) return
    try {
      setActionBusy(true)
      await deleteResource(numericId)
      navigate('/resources', { replace: true })
    } catch (err: any) {
      alert(err?.message || 'Failed to delete resource.')
      setActionBusy(false)
    }
  }

  const isOwnerOrAdmin =
    user && resource && (user.name === resource.uploadedByName || user.role === 'ADMIN')

  if (loading) {
    return (
      <div className="page wrap">
        <div style={{ maxWidth: '780px', margin: '0 auto' }}>
          <div className="skeleton" style={{ height: '24px', width: '30%', marginBottom: '1.5rem' }} />
          <div className="skeleton" style={{ height: '40px', width: '80%', marginBottom: '1rem' }} />
          <div className="skeleton" style={{ height: '80px', width: '100%', marginBottom: '2rem' }} />
          <div className="skeleton" style={{ height: '140px', width: '100%' }} />
        </div>
      </div>
    )
  }

  if (error || !resource) {
    return (
      <div className="page wrap">
        <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'center' }}>
          <div className="alert alert-danger" style={{ marginBottom: '1.5rem' }}>
            <AlertCircle size={20} />
            <span>{error || 'Resource not found.'}</span>
          </div>
          <Link to="/resources" className="btn btn-secondary">
            <ArrowLeft size={16} /> Back to Resources
          </Link>
        </div>
      </div>
    )
  }

  const formattedDate = new Date(resource.createdAt).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })

  return (
    <div className="page wrap">
      <div style={{ maxWidth: '820px', margin: '0 auto' }}>
        {/* Back Link */}
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
          <ArrowLeft size={15} /> All Resources
        </Link>

        {/* Header Section */}
        <div style={{ marginBottom: '2rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem' }}>
            <span className="tag tag-accent">
              {CATEGORY_LABELS[resource.category] || resource.category}
            </span>
            <span className="tag">{resource.type.replace(/_/g, ' ')}</span>
          </div>

          <h1
            style={{
              fontFamily: 'var(--sans)',
              fontSize: '2.1rem',
              fontWeight: 800,
              lineHeight: 1.25,
              letterSpacing: '-0.02em',
              margin: '0 0 1rem',
              color: 'var(--text-main)',
            }}
          >
            {resource.title}
          </h1>

          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              flexWrap: 'wrap',
              gap: '1.25rem',
              color: 'var(--text-secondary)',
              fontSize: '0.88rem',
              paddingBottom: '1.25rem',
              borderBottom: '1px solid var(--border)',
            }}
          >
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              <User size={15} />
              Shared by <strong style={{ color: 'var(--text-main)' }}>{resource.uploadedByName}</strong>
            </span>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}>
              <Calendar size={15} />
              {formattedDate}
            </span>
            {resource.updatedAt && (
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                (Updated)
              </span>
            )}
          </div>
        </div>

        {/* Description */}
        {resource.description && (
          <div style={{ marginBottom: '2rem' }}>
            <h2
              style={{
                fontFamily: 'var(--sans)',
                fontSize: '1.2rem',
                fontWeight: 700,
                margin: '0 0 0.5rem',
                color: 'var(--text-main)',
              }}
            >
              Overview
            </h2>
            <p
              style={{
                color: 'var(--text-secondary)',
                fontSize: '0.96rem',
                lineHeight: 1.65,
                margin: 0,
                whiteSpace: 'pre-line',
              }}
            >
              {resource.description}
            </p>
          </div>
        )}

        {/* Main Content Display depending on Type */}
        <div style={{ marginBottom: '2.5rem' }}>
          {['PDF', 'DOC', 'DOCX'].includes(resource.type) && (
            <div
              className="card"
              style={{
                background: 'var(--bg-secondary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1rem',
                padding: '1.5rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                <div
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: 'var(--radius)',
                    background: 'var(--bg-card)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    border: '1px solid var(--border)',
                  }}
                >
                  <FileText size={22} color="var(--accent-terracotta)" />
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.96rem' }}>
                    {resource.originalFileName || `${resource.title}.${resource.type.toLowerCase()}`}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    Original Document Attachment ({resource.type})
                  </div>
                </div>
              </div>

              <button
                type="button"
                className="btn btn-primary"
                onClick={handleDownload}
              >
                <Download size={16} /> Download Attachment
              </button>
            </div>
          )}

          {['GOOGLE_DRIVE_LINK', 'EXTERNAL_LINK'].includes(resource.type) && resource.resourceUrl && (
            <div
              className="card"
              style={{
                background: 'var(--bg-secondary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '1rem',
                padding: '1.5rem',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
                <div
                  style={{
                    width: '42px',
                    height: '42px',
                    borderRadius: 'var(--radius)',
                    background: 'var(--bg-card)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    border: '1px solid var(--border)',
                  }}
                >
                  <ExternalLink size={22} color="var(--accent-terracotta)" />
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: '0.96rem' }}>
                    {resource.type === 'GOOGLE_DRIVE_LINK' ? 'Google Drive Repository' : 'External Academic Link'}
                  </div>
                  <div
                    style={{
                      fontSize: '0.8rem',
                      color: 'var(--text-secondary)',
                      maxWidth: '360px',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {resource.resourceUrl}
                  </div>
                </div>
              </div>

              <a
                href={resource.resourceUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-primary"
              >
                <ExternalLink size={16} /> Open Resource Link
              </a>
            </div>
          )}

          {resource.type === 'TEXT' && (
            <div className="card" style={{ padding: '2rem' }}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  marginBottom: '1rem',
                  color: 'var(--text-secondary)',
                  fontSize: '0.85rem',
                }}
              >
                <FileCode size={16} /> Notes / Syllabus Content
              </div>
              <div
                style={{
                  whiteSpace: 'pre-wrap',
                  lineHeight: 1.7,
                  fontFamily: 'inherit',
                  fontSize: '0.96rem',
                  color: 'var(--text-main)',
                }}
              >
                {resource.textContent || 'No text content provided.'}
              </div>
            </div>
          )}
        </div>

        {/* Student Interactions & Actions Row */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '0.75rem',
            padding: '1.25rem 0',
            borderTop: '1px solid var(--border)',
            borderBottom: '1px solid var(--border)',
          }}
        >
          <div style={{ display: 'flex', gap: '0.65rem' }}>
            {/* Helpful Toggle */}
            <button
              type="button"
              className={`btn btn-sm ${isHelpful ? 'btn-primary' : 'btn-secondary'}`}
              onClick={handleToggleHelpful}
              disabled={actionBusy}
            >
              <ThumbsUp size={14} />
              <span>{isHelpful ? 'Helpful' : 'Mark as Helpful'}</span>
              <span style={{ opacity: 0.85 }}>({helpfulCount})</span>
            </button>

            {/* Bookmark Toggle */}
            <button
              type="button"
              className={`btn btn-sm ${isBookmarked ? 'btn-primary' : 'btn-secondary'}`}
              onClick={handleToggleBookmark}
              disabled={actionBusy}
            >
              <Bookmark size={14} />
              <span>{isBookmarked ? 'Saved in Bookmarks' : 'Save for Later'}</span>
            </button>

            {/* Jump to Reddit Discussion */}
            <a
              href="#discussion"
              className="btn btn-sm btn-secondary"
              title="Jump to discussion"
            >
              <MessageSquare size={14} />
              <span>Discussion</span>
              <span style={{ opacity: 0.85 }}>({commentCount})</span>
            </a>
          </div>

          <div style={{ display: 'flex', gap: '0.65rem' }}>
            {/* Report */}
            <button
              type="button"
              className="btn btn-sm btn-outline"
              onClick={() => setIsReportOpen(true)}
              title="Report issue to moderators"
            >
              <AlertTriangle size={14} /> Report
            </button>

            {/* Delete (if owner or admin) */}
            {isOwnerOrAdmin && (
              <>
                {deleteConfirm ? (
                  <div style={{ display: 'flex', gap: '0.4rem' }}>
                    <button
                      type="button"
                      className="btn btn-sm btn-danger"
                      onClick={handleDelete}
                      disabled={actionBusy}
                    >
                      Confirm Delete
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline"
                      onClick={() => setDeleteConfirm(false)}
                    >
                      Cancel
                    </button>
                  </div>
                ) : (
                  <button
                    type="button"
                    className="btn btn-sm btn-outline"
                    style={{ color: 'var(--danger)' }}
                    onClick={() => setDeleteConfirm(true)}
                  >
                    <Trash2 size={14} /> Delete
                  </button>
                )}
              </>
            )}
          </div>
        </div>

        {/* Reddit-Style Discussion / Comments Thread */}
        <DiscussionSection
          resource={resource}
          onCommentCountChange={(count) => setCommentCount(count)}
        />
      </div>

      {/* Report Modal */}
      <ReportModal
        resourceId={numericId}
        resourceTitle={resource.title}
        isOpen={isReportOpen}
        onClose={() => setIsReportOpen(false)}
      />
    </div>
  )
}
