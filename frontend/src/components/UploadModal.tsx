import { useState, type FormEvent } from 'react'
import { X, UploadCloud, Link as LinkIcon, FileText, AlertCircle } from 'lucide-react'
import {
  uploadResourceFile,
  createResource,
} from '../services/resourceService'
import type {
  ResourceCategory,
  ResourceType,
  ResourceResponse,
} from '../types'
import { CATEGORY_LABELS } from '../types'

interface UploadModalProps {
  isOpen: boolean
  onClose: () => void
  onSuccess: (resource: ResourceResponse) => void
}

type Mode = 'FILE' | 'LINK' | 'TEXT'

export default function UploadModal({
  isOpen,
  onClose,
  onSuccess,
}: UploadModalProps) {
  const [mode, setMode] = useState<Mode>('FILE')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState<ResourceCategory>('NOTES')
  const [file, setFile] = useState<File | null>(null)
  const [linkType, setLinkType] = useState<
    'GOOGLE_DRIVE_LINK' | 'EXTERNAL_LINK'
  >('GOOGLE_DRIVE_LINK')
  const [resourceUrl, setResourceUrl] = useState('')
  const [textContent, setTextContent] = useState('')

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (!isOpen) return null

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selected = e.target.files[0]
      if (selected.size > 10 * 1024 * 1024) {
        setError('File size exceeds the 10MB limit.')
        setFile(null)
        return
      }
      setError(null)
      setFile(selected)
    }
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!title.trim()) {
      setError('Resource title is required.')
      return
    }

    try {
      setLoading(true)
      let created: ResourceResponse

      if (mode === 'FILE') {
        if (!file) {
          setError('Please select a document file (.pdf, .doc, .docx).')
          setLoading(false)
          return
        }
        created = await uploadResourceFile(title.trim(), description.trim() || undefined, category, file)
      } else if (mode === 'LINK') {
        if (!resourceUrl.trim()) {
          setError('Please provide a valid URL for the resource.')
          setLoading(false)
          return
        }
        created = await createResource({
          title: title.trim(),
          description: description.trim() || undefined,
          category,
          type: linkType as ResourceType,
          resourceUrl: resourceUrl.trim(),
        })
      } else {
        // TEXT
        if (!textContent.trim()) {
          setError('Text notes or instructions cannot be blank.')
          setLoading(false)
          return
        }
        created = await createResource({
          title: title.trim(),
          description: description.trim() || undefined,
          category,
          type: 'TEXT',
          textContent: textContent.trim(),
        })
      }

      onSuccess(created)
      onClose()
      // Reset form
      setTitle('')
      setDescription('')
      setFile(null)
      setResourceUrl('')
      setTextContent('')
    } catch (err: any) {
      setError(err?.message || 'Failed to submit resource. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose} role="dialog" aria-modal="true">
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 className="modal-title">Share Academic Resource</h2>
          <button
            type="button"
            className="modal-close-btn"
            onClick={onClose}
            aria-label="Close dialog"
          >
            <X size={20} />
          </button>
        </div>

        {error && (
          <div className="alert alert-danger" role="alert">
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        {/* Mode selector */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr 1fr',
            gap: '0.5rem',
            marginBottom: '1.25rem',
            background: 'var(--bg-secondary)',
            padding: '0.25rem',
            borderRadius: 'var(--radius)',
          }}
        >
          <button
            type="button"
            className="btn btn-sm"
            style={{
              background: mode === 'FILE' ? 'var(--bg-card)' : 'transparent',
              borderColor: mode === 'FILE' ? 'var(--border)' : 'transparent',
              fontWeight: mode === 'FILE' ? 600 : 400,
            }}
            onClick={() => {
              setMode('FILE')
              setError(null)
            }}
          >
            <UploadCloud size={14} /> File (.pdf, .doc)
          </button>
          <button
            type="button"
            className="btn btn-sm"
            style={{
              background: mode === 'LINK' ? 'var(--bg-card)' : 'transparent',
              borderColor: mode === 'LINK' ? 'var(--border)' : 'transparent',
              fontWeight: mode === 'LINK' ? 600 : 400,
            }}
            onClick={() => {
              setMode('LINK')
              setError(null)
            }}
          >
            <LinkIcon size={14} /> Drive / Link
          </button>
          <button
            type="button"
            className="btn btn-sm"
            style={{
              background: mode === 'TEXT' ? 'var(--bg-card)' : 'transparent',
              borderColor: mode === 'TEXT' ? 'var(--border)' : 'transparent',
              fontWeight: mode === 'TEXT' ? 600 : 400,
            }}
            onClick={() => {
              setMode('TEXT')
              setError(null)
            }}
          >
            <FileText size={14} /> Text Note
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="res-title">
              Resource Title *
            </label>
            <input
              id="res-title"
              className="input-field"
              type="text"
              maxLength={200}
              placeholder="e.g. Data Structures Unit 3 Notes (Trees & Graphs)"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="res-category">
              Category *
            </label>
            <select
              id="res-category"
              className="select-field"
              value={category}
              onChange={(e) => setCategory(e.target.value as ResourceCategory)}
              required
            >
              {Object.entries(CATEGORY_LABELS).map(([k, label]) => (
                <option key={k} value={k}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="res-desc">
              Description (Optional)
            </label>
            <textarea
              id="res-desc"
              className="textarea-field"
              maxLength={2000}
              placeholder="Provide context, topics covered, or instructions for juniors..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
            />
          </div>

          {mode === 'FILE' && (
            <div className="form-group">
              <label className="form-label" htmlFor="res-file">
                Document File (.pdf, .doc, .docx - max 10MB) *
              </label>
              <input
                id="res-file"
                className="input-field"
                type="file"
                accept=".pdf,.doc,.docx"
                onChange={handleFileChange}
                required
              />
              <p className="form-hint">
                Files are securely stored in the backend and scanned for academic integrity.
              </p>
            </div>
          )}

          {mode === 'LINK' && (
            <>
              <div className="form-group">
                <label className="form-label" htmlFor="res-link-type">
                  Link Type
                </label>
                <select
                  id="res-link-type"
                  className="select-field"
                  value={linkType}
                  onChange={(e) =>
                    setLinkType(e.target.value as 'GOOGLE_DRIVE_LINK' | 'EXTERNAL_LINK')
                  }
                >
                  <option value="GOOGLE_DRIVE_LINK">Google Drive Folder / Document</option>
                  <option value="EXTERNAL_LINK">External Website / Academic Portal</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="res-url">
                  Resource URL *
                </label>
                <input
                  id="res-url"
                  className="input-field"
                  type="url"
                  maxLength={2048}
                  placeholder="https://drive.google.com/..."
                  value={resourceUrl}
                  onChange={(e) => setResourceUrl(e.target.value)}
                  required
                />
              </div>
            </>
          )}

          {mode === 'TEXT' && (
            <div className="form-group">
              <label className="form-label" htmlFor="res-text">
                Notes / Syllabus Reference *
              </label>
              <textarea
                id="res-text"
                className="textarea-field"
                style={{ minHeight: '130px' }}
                placeholder="Write or paste study pointers, recommended reading, exam tips..."
                value={textContent}
                onChange={(e) => setTextContent(e.target.value)}
                required
              />
            </div>
          )}

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
              {loading ? 'Submitting…' : 'Share Resource'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
