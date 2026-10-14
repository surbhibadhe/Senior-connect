import { useState, type FormEvent } from 'react'
import { X, UserCheck } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'

interface EditProfileModalProps {
  isOpen: boolean
  onClose: () => void
}

export default function EditProfileModal({
  isOpen,
  onClose,
}: EditProfileModalProps) {
  const { user, updateProfile } = useAuth()
  const [department, setDepartment] = useState(user?.department || '')
  const [bio, setBio] = useState(user?.bio || '')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!isOpen) return null

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    try {
      setLoading(true)
      await updateProfile({
        department: department.trim() || undefined,
        bio: bio.trim() || undefined,
      })
      onClose()
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <UserCheck size={20} color="var(--accent-terracotta)" />
            <h2 className="modal-title" style={{ fontSize: '1.3rem' }}>
              Edit Student Profile
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

        {error && (
          <div className="alert alert-danger" role="alert">
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="prof-dept">
              Department / Discipline
            </label>
            <input
              id="prof-dept"
              className="input-field"
              type="text"
              maxLength={100}
              placeholder="e.g. Computer Science, Mechanical Engineering..."
              value={department}
              onChange={(e) => setDepartment(e.target.value)}
            />
            <p className="form-hint">
              Helps classmates identify course materials suited for your degree.
            </p>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="prof-bio">
              Short Bio (Max 500 characters)
            </label>
            <textarea
              id="prof-bio"
              className="textarea-field"
              maxLength={500}
              placeholder="Share your academic interests, focus areas, or tips for juniors..."
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              rows={3}
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
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Saving…' : 'Save Profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
