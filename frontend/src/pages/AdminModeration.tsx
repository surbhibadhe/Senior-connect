import { useEffect, useState, useCallback } from 'react'
import { Link, Navigate } from 'react-router-dom'
import {
  ShieldAlert,
  CheckCircle,
  XCircle,
  Trash2,
  AlertCircle,
  ExternalLink,
  MessageSquare,
  Clock,
  User,
} from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import {
  getAdminReports,
  updateAdminReportStatus,
  removeResourceByReport,
} from '../services/resourceService'
import type {
  ResourceReportResponse,
  ReportStatus,
  PageResponse,
} from '../types'
import { REPORT_REASON_LABELS } from '../types'

export default function AdminModeration() {
  const { user } = useAuth()
  const [statusFilter, setStatusFilter] = useState<ReportStatus | 'ALL'>('OPEN')
  const [reportsData, setReportsData] = useState<PageResponse<ResourceReportResponse>>({
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
  const [actionBusy, setActionBusy] = useState(false)
  const [actionNote, setActionNote] = useState<{ [reportId: number]: string }>({})

  // Only admins may access
  if (user && user.role !== 'ADMIN') {
    return <Navigate to="/home" replace />
  }

  const loadReports = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const filter = statusFilter === 'ALL' ? undefined : statusFilter
      const res = await getAdminReports(filter, 0, 20)
      setReportsData(res)
    } catch (err: any) {
      setError(err?.message || 'Failed to load moderation reports.')
    } finally {
      setLoading(false)
    }
  }, [statusFilter])

  useEffect(() => {
    loadReports()
  }, [loadReports])

  const handleUpdateStatus = async (
    reportId: number,
    newStatus: ReportStatus
  ) => {
    try {
      setActionBusy(true)
      const note = actionNote[reportId]?.trim() || undefined
      await updateAdminReportStatus(reportId, {
        status: newStatus,
        moderatorNotes: note,
      })
      await loadReports()
    } catch (err: any) {
      alert(err?.message || 'Failed to update report status.')
    } finally {
      setActionBusy(false)
    }
  }

  const handleRemoveResource = async (reportId: number) => {
    if (
      !window.confirm(
        'Are you sure you want to delete this resource from SeniorConnect following this report?'
      )
    ) {
      return
    }

    try {
      setActionBusy(true)
      await removeResourceByReport(reportId)
      alert('Resource removed successfully and report resolved.')
      await loadReports()
    } catch (err: any) {
      alert(err?.message || 'Failed to remove resource.')
    } finally {
      setActionBusy(false)
    }
  }

  return (
    <div className="page wrap">
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
          <ShieldAlert size={26} color="var(--accent-terracotta)" />
          <h1 className="page-title">Resource Moderation</h1>
        </div>
        <p className="page-subtitle">
          Review community reports regarding inaccurate, copyrighted, or inappropriate student uploads.
        </p>
      </div>

      {/* Status Filter Tabs */}
      <div
        style={{
          display: 'flex',
          gap: '0.5rem',
          marginBottom: '2rem',
          borderBottom: '1px solid var(--border)',
          paddingBottom: '0.75rem',
        }}
      >
        {(['OPEN', 'REVIEWED', 'DISMISSED', 'ALL'] as const).map((tab) => (
          <button
            key={tab}
            type="button"
            className={`btn btn-sm ${
              statusFilter === tab ? 'btn-primary' : 'btn-outline'
            }`}
            onClick={() => setStatusFilter(tab)}
          >
            {tab === 'OPEN' && 'Open Reports'}
            {tab === 'REVIEWED' && 'Reviewed'}
            {tab === 'DISMISSED' && 'Dismissed'}
            {tab === 'ALL' && 'All Reports'}
          </button>
        ))}
      </div>

      {loading && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {[1, 2].map((i) => (
            <div key={i} className="card">
              <div className="skeleton" style={{ height: '22px', width: '50%', marginBottom: '1rem' }} />
              <div className="skeleton" style={{ height: '14px', width: '80%' }} />
            </div>
          ))}
        </div>
      )}

      {error && !loading && (
        <div className="alert alert-danger" role="alert">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {!loading && !error && reportsData.content.length === 0 && (
        <div className="empty-state">
          <CheckCircle size={36} color="var(--success)" style={{ marginBottom: '1rem' }} />
          <h3 className="empty-state-title">No reports pending</h3>
          <p className="empty-state-desc">
            All submitted reports in this category have been addressed. The academic library is in good standing.
          </p>
        </div>
      )}

      {!loading && !error && reportsData.content.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {reportsData.content.map((rep) => {
            const reportDate = new Date(rep.createdAt).toLocaleDateString('en-IN', {
              day: 'numeric',
              month: 'short',
              year: 'numeric',
            })

            return (
              <div key={rep.id} className="card">
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    justifyContent: 'space-between',
                    flexWrap: 'wrap',
                    gap: '1rem',
                    marginBottom: '0.75rem',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.35rem' }}>
                      <span
                        className="role-badge"
                        style={{
                          backgroundColor:
                            rep.status === 'OPEN'
                              ? 'var(--warning-soft)'
                              : rep.status === 'REVIEWED'
                              ? 'var(--success-soft)'
                              : 'var(--bg-secondary)',
                          color:
                            rep.status === 'OPEN'
                              ? 'var(--warning)'
                              : rep.status === 'REVIEWED'
                              ? 'var(--success)'
                              : 'var(--text-secondary)',
                        }}
                      >
                        {rep.status}
                      </span>
                      <span className="tag" style={{ color: 'var(--danger)', borderColor: '#ECCECB' }}>
                        {REPORT_REASON_LABELS[rep.reason] || rep.reason}
                      </span>
                    </div>

                    <h3 style={{ margin: '0 0 0.35rem', fontSize: '1.15rem', fontWeight: 600 }}>
                      <Link
                        to={`/resources/${rep.resourceId}`}
                        style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                      >
                        {rep.resourceTitle} <ExternalLink size={14} />
                      </Link>
                    </h3>

                    <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', display: 'flex', gap: '1rem' }}>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                        <User size={12} /> Reported by {rep.reporterName}
                      </span>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.3rem' }}>
                        <Clock size={12} /> {reportDate}
                      </span>
                    </div>
                  </div>

                  {rep.status === 'OPEN' && (
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      <button
                        type="button"
                        className="btn btn-sm btn-secondary"
                        onClick={() => handleUpdateStatus(rep.id, 'REVIEWED')}
                        disabled={actionBusy}
                      >
                        <CheckCircle size={14} /> Mark Reviewed
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline"
                        onClick={() => handleUpdateStatus(rep.id, 'DISMISSED')}
                        disabled={actionBusy}
                      >
                        <XCircle size={14} /> Dismiss
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-danger"
                        onClick={() => handleRemoveResource(rep.id)}
                        disabled={actionBusy}
                      >
                        <Trash2 size={14} /> Delete Resource
                      </button>
                    </div>
                  )}
                </div>

                {/* Explanation */}
                {rep.explanation && (
                  <div
                    style={{
                      background: 'var(--bg-secondary)',
                      padding: '0.85rem 1rem',
                      borderRadius: 'var(--radius)',
                      fontSize: '0.88rem',
                      color: 'var(--text-main)',
                      marginBottom: '0.75rem',
                    }}
                  >
                    <strong>Reporter note:</strong> {rep.explanation}
                  </div>
                )}

                {/* Moderator Notes input (if OPEN) or displayed (if RESOLVED) */}
                {rep.status === 'OPEN' ? (
                  <div style={{ marginTop: '0.5rem' }}>
                    <input
                      type="text"
                      className="input-field"
                      placeholder="Optional moderator notes before resolving..."
                      value={actionNote[rep.id] || ''}
                      onChange={(e) =>
                        setActionNote((prev) => ({
                          ...prev,
                          [rep.id]: e.target.value,
                        }))
                      }
                      style={{ fontSize: '0.85rem', padding: '0.45rem 0.75rem' }}
                    />
                  </div>
                ) : (
                  rep.moderatorNotes && (
                    <div style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                      <strong>Resolution Note:</strong> {rep.moderatorNotes}{' '}
                      {rep.resolvedByName && `(Resolved by ${rep.resolvedByName})`}
                    </div>
                  )
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
