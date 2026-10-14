import { useState, type FormEvent } from 'react'
import { X, AlertTriangle, CheckCircle2 } from 'lucide-react'
import { reportResource } from '../services/resourceService'
import type { ReportReason } from '../types'
import { REPORT_REASON_LABELS } from '../types'

interface ReportModalProps {
  resourceId: number
  resourceTitle: string
  isOpen: boolean
  onClose: () => void
}

export default function ReportModal({
  resourceId,
  resourceTitle,
  isOpen,
  onClose,
}: ReportModalProps) {
  const [reason, setReason] = useState<ReportReason>('INCORRECT_CONTENT')
  const [explanation, setExplanation] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  if (!isOpen) return null

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    try {
      setLoading(true)
      await reportResource(resourceId, {
        reason,
        explanation: explanation.trim() || undefined,
      })
      setSuccess(true)
      setTimeout(() => {
        setSuccess(false)
        onClose()
      }, 1800)
    } catch (err: any) {
      setError(err?.message || 'Failed to submit report. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <AlertTriangle size={20} color="var(--danger)" />
            <h2 className="modal-title" style={{ fontSize: '1.25rem' }}>
              Report Resource
            </h2>
          </div>
          <button
            type="button"
            className="modal-close-btn"
            onClick={onClose}
            aria-label="Close dialog"
          >
            <X size={18} />
          </button>
        </div>

        {success ? (
          <div style={{ textAlign: 'center', padding: '1.5rem 0' }}>
            <CheckCircle2
              size={36}
              color="var(--success)"
              style={{ marginBottom: '0.75rem' }}
            />
            <p style={{ margin: 0, fontWeight: 500 }}>
              Report submitted for administrative review. Thank you for keeping SeniorConnect safe.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', margin: '0 0 1.25rem' }}>
              Reporting <strong style={{ color: 'var(--text-main)' }}>{resourceTitle}</strong>
            </p>

            {error && (
              <div className="alert alert-danger" role="alert">
                <span>{error}</span>
              </div>
            )}

            <div className="form-group">
              <label className="form-label" htmlFor="rep-reason">
                Reason for report *
              </label>
              <select
                id="rep-reason"
                className="select-field"
                value={reason}
                onChange={(e) => setReason(e.target.value as ReportReason)}
                required
              >
                {Object.entries(REPORT_REASON_LABELS).map(([k, label]) => (
                  <option key={k} value={k}>
                    {label}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="rep-explanation">
                Explanation (Optional, max 1000 characters)
              </label>
              <textarea
                id="rep-explanation"
                className="textarea-field"
                maxLength={1000}
                placeholder="Describe what is wrong or misleading about this material..."
                value={explanation}
                onChange={(e) => setExplanation(e.target.value)}
                rows={4}
              />
            </div>

            <div className="modal-actions">
              <button
                type="button"
                className="btn btn-outline"
                onClick={onClose}
                disabled={loading}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-danger"
                disabled={loading}
              >
                {loading ? 'Submitting…' : 'Submit Report'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
