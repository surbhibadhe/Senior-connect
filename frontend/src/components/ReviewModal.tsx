import { useState, type FormEvent } from 'react'
import { X, Star, Send, AlertCircle, Sparkles } from 'lucide-react'
import { createReview } from '../services/reviewService'
import { useAuth } from '../hooks/useAuth'
import type { ReviewResponse } from '../types'

interface ReviewModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: (newReview: ReviewResponse) => void
}

export default function ReviewModal({
  isOpen,
  onClose,
  onSuccess,
}: ReviewModalProps) {
  const { user } = useAuth()
  const [rating, setRating] = useState(5)
  const [hoverRating, setHoverRating] = useState(0)
  const [content, setContent] = useState('')
  const [perspectiveTag, setPerspectiveTag] = useState('The student perspective')
  const [authorSubtitle, setAuthorSubtitle] = useState(
    user?.department ? user.department : 'DESPU Student'
  )
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!isOpen) return null

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!content.trim()) {
      setError('Please write your review before submitting.')
      return
    }

    try {
      setLoading(true)
      setError(null)
      const created = await createReview({
        content: content.trim(),
        rating,
        perspectiveTag: perspectiveTag.trim() || undefined,
        authorSubtitle: authorSubtitle.trim() || undefined,
      })
      onSuccess(created)
      setContent('')
      onClose()
    } catch (err: any) {
      setError(err?.message || 'Could not submit your review. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div
        className="modal-content"
        style={{ maxWidth: '500px' }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
            <div
              style={{
                width: '36px',
                height: '36px',
                borderRadius: '8px',
                backgroundColor: 'var(--pastel-sage)',
                color: 'var(--primary-green)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Sparkles size={18} />
            </div>
            <div>
              <h3 className="modal-title" style={{ fontSize: '1.25rem', fontFamily: 'var(--sans)', fontWeight: 700 }}>
                Share Your Experience
              </h3>
              <p style={{ margin: 0, fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                How has SeniorConnect helped your studies at DES Pune University?
              </p>
            </div>
          </div>
          <button
            type="button"
            className="modal-close-btn"
            onClick={onClose}
            aria-label="Close"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {error && (
            <div className="alert alert-danger" style={{ marginBottom: '1.25rem' }}>
              <AlertCircle size={16} />
              <span>{error}</span>
            </div>
          )}

          {/* Rating Stars */}
          <div style={{ marginBottom: '1.25rem' }}>
            <label style={{ display: 'block', fontSize: '0.84rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
              Rating
            </label>
            <div style={{ display: 'flex', gap: '0.4rem' }}>
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  type="button"
                  key={star}
                  style={{
                    background: 'none',
                    border: 'none',
                    padding: '2px',
                    cursor: 'pointer',
                    color: (hoverRating || rating) >= star ? '#00543F' : '#D8E0D9',
                    transition: 'color 0.15s ease',
                  }}
                  onMouseEnter={() => setHoverRating(star)}
                  onMouseLeave={() => setHoverRating(0)}
                  onClick={() => setRating(star)}
                >
                  <Star size={24} fill={(hoverRating || rating) >= star ? '#00543F' : 'none'} />
                </button>
              ))}
            </div>
          </div>

          {/* Review Text */}
          <div style={{ marginBottom: '1.25rem' }}>
            <label style={{ display: 'block', fontSize: '0.84rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
              Your Review / Moment
            </label>
            <textarea
              className="input-field"
              style={{ minHeight: '100px', resize: 'vertical', padding: '0.75rem', fontFamily: 'inherit' }}
              placeholder='e.g. "That moment when a senior’s notes made a whole unit click..."'
              rows={4}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              required
            />
          </div>

          {/* Perspective Tag & Subtitle */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.84rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
                Perspective Title
              </label>
              <input
                type="text"
                className="input-field"
                style={{ padding: '0.55rem 0.75rem' }}
                placeholder="e.g. The first-year feeling"
                value={perspectiveTag}
                onChange={(e) => setPerspectiveTag(e.target.value)}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.84rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
                Tagline / Branch
              </label>
              <input
                type="text"
                className="input-field"
                style={{ padding: '0.55rem 0.75rem' }}
                placeholder="e.g. Good knowledge travels"
                value={authorSubtitle}
                onChange={(e) => setAuthorSubtitle(e.target.value)}
              />
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.65rem' }}>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={loading || !content.trim()}
            >
              <Send size={13} />
              <span>{loading ? 'Submitting…' : 'Publish Review'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
