import { Link } from 'react-router-dom'
import {
  FileText,
  FileCode,
  ExternalLink,
  ThumbsUp,
  Calendar,
  User,
} from 'lucide-react'
import type { ResourceResponse } from '../types'
import { CATEGORY_LABELS } from '../types'

interface ResourceCardProps {
  resource: ResourceResponse
}

export default function ResourceCard({ resource }: ResourceCardProps) {
  const formattedDate = new Date(resource.createdAt).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })

  const renderTypeIcon = () => {
    switch (resource.type) {
      case 'PDF':
      case 'DOC':
      case 'DOCX':
        return <FileText size={13} />
      case 'TEXT':
        return <FileCode size={13} />
      case 'GOOGLE_DRIVE_LINK':
      case 'EXTERNAL_LINK':
        return <ExternalLink size={13} />
      default:
        return <FileText size={13} />
    }
  }

  const formatTypeLabel = (type: string) => {
    switch (type) {
      case 'GOOGLE_DRIVE_LINK':
        return 'Google Drive'
      case 'EXTERNAL_LINK':
        return 'External Link'
      default:
        return type
    }
  }

  return (
    <article className="resource-item">
      <div className="resource-header">
        <Link to={`/resources/${resource.id}`}>
          <h3 className="resource-title">{resource.title}</h3>
        </Link>
        <div style={{ display: 'flex', gap: '0.4rem', flexShrink: 0 }}>
          <span className="tag tag-accent">
            {CATEGORY_LABELS[resource.category] || resource.category}
          </span>
          <span className="tag">
            {renderTypeIcon()}
            {formatTypeLabel(resource.type)}
          </span>
        </div>
      </div>

      {resource.description && (
        <p className="resource-desc">{resource.description}</p>
      )}

      <div className="resource-meta">
        <span className="resource-meta-item">
          <User size={13} />
          {resource.uploadedByName}
        </span>
        <span className="resource-meta-item">
          <Calendar size={13} />
          {formattedDate}
        </span>
        {resource.helpfulCount > 0 && (
          <span className="resource-meta-item" style={{ color: 'var(--accent-terracotta)' }}>
            <ThumbsUp size={13} />
            {resource.helpfulCount} helpful
          </span>
        )}
      </div>
    </article>
  )
}
