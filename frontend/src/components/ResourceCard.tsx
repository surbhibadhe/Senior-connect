import { Link } from 'react-router-dom'
import {
  FileText,
  FileCode,
  ExternalLink,
  ThumbsUp,
  Calendar,
  User,
  MessageSquare,
  BookOpen,
  FlaskConical,
  ClipboardList,
  HelpCircle,
  Bookmark,
} from 'lucide-react'
import type { ResourceResponse, ResourceCategory } from '../types'
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

  // Pastel styling configuration per category
  const getCategoryTheme = (cat: ResourceCategory) => {
    switch (cat) {
      case 'NOTES':
        return {
          bg: 'var(--pastel-sage)',
          color: 'var(--pastel-sage-text)',
          icon: <BookOpen size={20} />,
        }
      case 'LAB_MANUALS':
        return {
          bg: 'var(--pastel-lavender)',
          color: 'var(--pastel-lavender-text)',
          icon: <FlaskConical size={20} />,
        }
      case 'ASSIGNMENTS':
        return {
          bg: 'var(--pastel-peach)',
          color: 'var(--pastel-peach-text)',
          icon: <ClipboardList size={20} />,
        }
      case 'QUESTION_PAPERS':
        return {
          bg: 'var(--pastel-blue)',
          color: 'var(--pastel-blue-text)',
          icon: <HelpCircle size={20} />,
        }
      case 'REFERENCE_MATERIALS':
        return {
          bg: 'var(--pastel-yellow)',
          color: 'var(--pastel-yellow-text)',
          icon: <Bookmark size={20} />,
        }
      case 'TEXTBOOK_LINKS':
        return {
          bg: 'var(--bg-secondary)',
          color: 'var(--text-main)',
          icon: <ExternalLink size={20} />,
        }
      default:
        return {
          bg: 'var(--pastel-sage)',
          color: 'var(--primary-green)',
          icon: <FileText size={20} />,
        }
    }
  }

  const catTheme = getCategoryTheme(resource.category)

  const formatTypeLabel = (type: string) => {
    switch (type) {
      case 'GOOGLE_DRIVE_LINK':
        return 'Drive Link'
      case 'EXTERNAL_LINK':
        return 'Link'
      case 'TEXT':
        return 'Notes'
      default:
        return type
    }
  }

  return (
    <article className="resource-item">
      {/* Category Pastel Thumbnail */}
      <div
        className="resource-thumbnail"
        style={{
          backgroundColor: catTheme.bg,
          color: catTheme.color,
        }}
      >
        {catTheme.icon}
      </div>

      <div className="resource-body">
        <div className="resource-header">
          <Link to={`/resources/${resource.id}`}>
            <h3 className="resource-title">{resource.title}</h3>
          </Link>
          <div style={{ display: 'flex', gap: '0.4rem', flexShrink: 0 }}>
            <span
              className="tag"
              style={{
                backgroundColor: catTheme.bg,
                color: catTheme.color,
                borderColor: 'transparent',
              }}
            >
              {CATEGORY_LABELS[resource.category] || resource.category}
            </span>
            <span className="tag">
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
            <span className="resource-meta-item" style={{ color: 'var(--primary-green)', fontWeight: 600 }}>
              <ThumbsUp size={13} />
              {resource.helpfulCount} helpful
            </span>
          )}
          <Link
            to={`/resources/${resource.id}#discussion`}
            className="resource-meta-item"
            style={{
              color: (resource.commentCount && resource.commentCount > 0) ? 'var(--primary-green)' : 'var(--text-secondary)',
              fontWeight: (resource.commentCount && resource.commentCount > 0) ? 600 : 400,
            }}
            title="Open community discussion"
          >
            <MessageSquare size={13} />
            {resource.commentCount && resource.commentCount > 0
              ? `${resource.commentCount} ${resource.commentCount === 1 ? 'comment' : 'comments'}`
              : 'Discuss'}
          </Link>
        </div>
      </div>
    </article>
  )
}
