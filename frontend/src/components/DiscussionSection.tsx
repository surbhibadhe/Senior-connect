import { useState, useEffect, useCallback, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import {
  MessageSquare,
  ArrowBigUp,
  Plus,
  Trash2,
  CornerDownRight,
  Send,
  AlertCircle,
  Sparkles,
  ChevronDown,
  ChevronRight,
  User as UserIcon,
} from 'lucide-react'
import {
  getComments,
  createComment,
  toggleCommentUpvote,
  deleteComment,
} from '../services/commentService'
import { useAuth } from '../hooks/useAuth'
import type { CommentResponse, ResourceResponse } from '../types'

interface DiscussionSectionProps {
  resource: ResourceResponse
  onCommentCountChange?: (count: number) => void
}

function formatTimeAgo(dateStr: string): string {
  try {
    const date = new Date(dateStr)
    const now = new Date()
    const diffSeconds = Math.floor((now.getTime() - date.getTime()) / 1000)

    if (diffSeconds < 45) return 'just now'
    const diffMinutes = Math.floor(diffSeconds / 60)
    if (diffMinutes < 60) return `${diffMinutes}m ago`
    const diffHours = Math.floor(diffMinutes / 60)
    if (diffHours < 24) return `${diffHours}h ago`
    const diffDays = Math.floor(diffHours / 24)
    if (diffDays < 7) return `${diffDays}d ago`
    return date.toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
    })
  } catch {
    return 'recently'
  }
}

// Single Comment Thread Component with recursive replies
interface CommentItemProps {
  comment: CommentResponse
  depth: number
  onReply: (parentId: number, content: string) => Promise<void>
  onDelete: (commentId: number) => Promise<void>
  onToggleUpvote: (commentId: number) => Promise<void>
  resourceUploaderName: string
}

function CommentItem({
  comment,
  depth,
  onReply,
  onDelete,
  onToggleUpvote,
  resourceUploaderName,
}: CommentItemProps) {
  const { user } = useAuth()
  const [isCollapsed, setIsCollapsed] = useState(false)
  const [isReplying, setIsReplying] = useState(false)
  const [replyText, setReplyText] = useState('')
  const [submittingReply, setSubmittingReply] = useState(false)
  const [upvoting, setUpvoting] = useState(false)
  const [deleting, setDeleting] = useState(false)

  // Local state for instant upvote UI response
  const [upvotes, setUpvotes] = useState(comment.upvotes)
  const [userUpvoted, setUserUpvoted] = useState(comment.userUpvoted)

  useEffect(() => {
    setUpvotes(comment.upvotes)
    setUserUpvoted(comment.userUpvoted)
  }, [comment.upvotes, comment.userUpvoted])

  const handleUpvote = async () => {
    if (upvoting || !user || comment.deleted) return
    try {
      setUpvoting(true)
      // Optimistic update
      const nextUpvoted = !userUpvoted
      setUserUpvoted(nextUpvoted)
      setUpvotes((prev) => (nextUpvoted ? prev + 1 : Math.max(0, prev - 1)))
      await onToggleUpvote(comment.id)
    } catch {
      // Revert if error
      setUserUpvoted(comment.userUpvoted)
      setUpvotes(comment.upvotes)
    } finally {
      setUpvoting(false)
    }
  }

  const handleReplySubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (!replyText.trim() || submittingReply) return
    try {
      setSubmittingReply(true)
      await onReply(comment.id, replyText.trim())
      setReplyText('')
      setIsReplying(false)
    } finally {
      setSubmittingReply(false)
    }
  }

  const handleDelete = async () => {
    if (deleting || !window.confirm('Are you sure you want to delete this comment?')) return
    try {
      setDeleting(true)
      await onDelete(comment.id)
    } finally {
      setDeleting(false)
    }
  }

  // Count total nested replies
  const countAllReplies = (c: CommentResponse): number => {
    let count = c.replies.length
    for (const r of c.replies) {
      count += countAllReplies(r)
    }
    return count
  }
  const totalReplies = countAllReplies(comment)

  // Collapsed header view
  if (isCollapsed) {
    return (
      <div className="reddit-comment-collapsed">
        <button
          type="button"
          className="collapse-toggle-btn"
          onClick={() => setIsCollapsed(false)}
          aria-label="Expand thread"
        >
          <ChevronRight size={14} />
        </button>
        <span className="comment-author-name">{comment.userName}</span>
        <span className="comment-collapsed-meta">
          {totalReplies > 0
            ? `(${totalReplies + 1} comments hidden, click to expand)`
            : '(comment hidden)'}
        </span>
      </div>
    )
  }

  return (
    <div className={`reddit-comment-wrapper depth-${Math.min(depth, 5)}`}>
      {/* Left Guideline & Collapse click area */}
      <div
        className="reddit-thread-line"
        onClick={() => setIsCollapsed(true)}
        title="Click to collapse thread"
      />

      <div className="reddit-comment-content">
        {/* Comment Header */}
        <div className="reddit-comment-header">
          {/* Collapse button */}
          <button
            type="button"
            className="collapse-toggle-btn"
            onClick={() => setIsCollapsed(true)}
            aria-label="Collapse thread"
          >
            <ChevronDown size={14} />
          </button>

          {/* User Avatar */}
          <div className="reddit-avatar">
            {comment.userProfilePicture ? (
              <img src={comment.userProfilePicture} alt={comment.userName} />
            ) : (
              <div className="avatar-placeholder">
                {comment.userName ? comment.userName.charAt(0).toUpperCase() : 'U'}
              </div>
            )}
          </div>

          {/* Author Name */}
          {comment.userId ? (
            <Link to={`/users/${comment.userId}`} className="comment-author-name">
              {comment.userName}
            </Link>
          ) : (
            <span className="comment-author-name">{comment.userName}</span>
          )}

          {/* OP / Author Badge */}
          {comment.isAuthor && (
            <span className="reddit-badge-op" title="Resource Uploader">
              OP
            </span>
          )}

          {/* Department or Role Badge */}
          {comment.userDepartment && (
            <span className="reddit-badge-dept">{comment.userDepartment}</span>
          )}

          {/* Timestamp */}
          <span className="comment-timestamp">{formatTimeAgo(comment.createdAt)}</span>
        </div>

        {/* Comment Body */}
        <div className={`reddit-comment-body ${comment.deleted ? 'comment-deleted' : ''}`}>
          {comment.deleted ? (
            <em>[This comment was deleted]</em>
          ) : (
            <p>{comment.content}</p>
          )}
        </div>

        {/* Comment Action Bar */}
        {!comment.deleted && (
          <div className="reddit-comment-actions">
            {/* Upvote button */}
            <button
              type="button"
              className={`reddit-action-btn upvote-btn ${userUpvoted ? 'active' : ''}`}
              onClick={handleUpvote}
              disabled={upvoting || !user}
              title={user ? 'Upvote comment' : 'Sign in to upvote'}
            >
              <ArrowBigUp
                size={18}
                className={userUpvoted ? 'fill-terracotta' : ''}
              />
              <span className="upvote-count">{upvotes}</span>
            </button>

            {/* Reply Button with Plus Sign */}
            <button
              type="button"
              className={`reddit-action-btn reply-btn ${isReplying ? 'active' : ''}`}
              onClick={() => setIsReplying((prev) => !prev)}
            >
              <Plus size={14} className="action-icon" />
              <span>Reply</span>
            </button>

            {/* Delete button (if allowed) */}
            {comment.canDelete && (
              <button
                type="button"
                className="reddit-action-btn delete-btn"
                onClick={handleDelete}
                disabled={deleting}
                title="Delete comment"
              >
                <Trash2 size={13} />
                <span>Delete</span>
              </button>
            )}
          </div>
        )}

        {/* Inline Reply Box */}
        {isReplying && (
          <form onSubmit={handleReplySubmit} className="reddit-inline-composer">
            <div className="composer-header">
              <CornerDownRight size={14} />
              <span>Replying to <strong>{comment.userName}</strong></span>
            </div>
            <textarea
              className="input-field composer-textarea"
              placeholder="What are your thoughts? Keep it constructive..."
              rows={3}
              value={replyText}
              onChange={(e) => setReplyText(e.target.value)}
              autoFocus
              required
            />
            <div className="composer-footer">
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={() => {
                  setIsReplying(false)
                  setReplyText('')
                }}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-sm btn-primary"
                disabled={submittingReply || !replyText.trim()}
              >
                <Send size={13} />
                <span>{submittingReply ? 'Posting…' : 'Post Reply'}</span>
              </button>
            </div>
          </form>
        )}

        {/* Nested Child Replies */}
        {comment.replies && comment.replies.length > 0 && (
          <div className="reddit-replies-list">
            {comment.replies.map((child) => (
              <CommentItem
                key={child.id}
                comment={child}
                depth={depth + 1}
                onReply={onReply}
                onDelete={onDelete}
                onToggleUpvote={onToggleUpvote}
                resourceUploaderName={resourceUploaderName}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

export default function DiscussionSection({
  resource,
  onCommentCountChange,
}: DiscussionSectionProps) {
  const { user } = useAuth()
  const [comments, setComments] = useState<CommentResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Top level composer state
  const [isComposerOpen, setIsComposerOpen] = useState(false)
  const [newCommentText, setNewCommentText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [sortBy, setSortBy] = useState<'top' | 'newest' | 'oldest'>('top')

  const fetchDiscussion = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getComments(resource.id)
      setComments(data)

      // Count total comments
      const countTotal = (items: CommentResponse[]): number => {
        let count = 0
        for (const item of items) {
          if (!item.deleted) count++
          if (item.replies) count += countTotal(item.replies)
        }
        return count
      }
      const totalCount = countTotal(data)
      if (onCommentCountChange) {
        onCommentCountChange(totalCount)
      }
    } catch (err: any) {
      setError(err?.message || 'Could not load discussion comments.')
    } finally {
      setLoading(false)
    }
  }, [resource.id, onCommentCountChange])

  useEffect(() => {
    fetchDiscussion()
  }, [fetchDiscussion])

  const handlePostTopComment = async (e: FormEvent) => {
    e.preventDefault()
    if (!newCommentText.trim() || submitting || !user) return

    try {
      setSubmitting(true)
      await createComment(resource.id, newCommentText.trim())
      setNewCommentText('')
      setIsComposerOpen(false)
      await fetchDiscussion()
    } catch (err: any) {
      alert(err?.message || 'Failed to post comment.')
    } finally {
      setSubmitting(false)
    }
  }

  const handlePostReply = async (parentId: number, content: string) => {
    try {
      await createComment(resource.id, content, parentId)
      await fetchDiscussion()
    } catch (err: any) {
      alert(err?.message || 'Failed to post reply.')
    }
  }

  const handleDeleteComment = async (commentId: number) => {
    try {
      await deleteComment(commentId)
      await fetchDiscussion()
    } catch (err: any) {
      alert(err?.message || 'Failed to delete comment.')
    }
  }

  const handleToggleUpvote = async (commentId: number) => {
    await toggleCommentUpvote(commentId)
  }

  // Count active comments
  const countActiveComments = (list: CommentResponse[]): number => {
    let count = 0
    for (const c of list) {
      if (!c.deleted) count++
      if (c.replies) count += countActiveComments(c.replies)
    }
    return count
  }
  const totalCommentsCount = countActiveComments(comments)

  // Sort top-level comments
  const sortedComments = [...comments].sort((a, b) => {
    if (sortBy === 'top') {
      return b.upvotes - a.upvotes
    }
    if (sortBy === 'newest') {
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    }
    // oldest
    return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
  })

  return (
    <section id="discussion" className="reddit-discussion-container">
      {/* Discussion Header */}
      <div className="reddit-discussion-header">
        <div className="discussion-title-wrap">
          <div className="discussion-icon-circle">
            <MessageSquare size={20} color="var(--accent-terracotta)" />
          </div>
          <div>
            <h2 className="discussion-title">
              Community Discussion
            </h2>
            <p className="discussion-subtitle">
              {totalCommentsCount} {totalCommentsCount === 1 ? 'thought' : 'thoughts'} shared by students on this resource
            </p>
          </div>
        </div>

        {/* Plus Button to Open Comment Composer */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          {/* Sort Selector */}
          {comments.length > 1 && (
            <div className="discussion-sort-selector">
              <span className="sort-label">Sort:</span>
              <select
                className="select-field"
                style={{ padding: '0.35rem 0.65rem', fontSize: '0.84rem' }}
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as any)}
              >
                <option value="top">Top Upvoted</option>
                <option value="newest">Newest First</option>
                <option value="oldest">Oldest First</option>
              </select>
            </div>
          )}

          {/* Prominent Plus Sign Comment Button */}
          <button
            type="button"
            className="btn btn-primary add-comment-btn"
            onClick={() => setIsComposerOpen((prev) => !prev)}
            aria-expanded={isComposerOpen}
          >
            <Plus size={16} className="plus-icon" />
            <span>{isComposerOpen ? 'Close Editor' : 'Add Comment'}</span>
          </button>
        </div>
      </div>

      {/* Top-Level Comment Composer (Reddit Style) */}
      {isComposerOpen && (
        <form onSubmit={handlePostTopComment} className="reddit-top-composer">
          <div className="composer-user-indicator">
            <div className="reddit-avatar-sm">
              {user?.profilePicture ? (
                <img src={user.profilePicture} alt={user.name} />
              ) : (
                <div className="avatar-placeholder-sm">
                  {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
                </div>
              )}
            </div>
            <span>
              Commenting as <strong style={{ color: 'var(--text-main)' }}>{user?.name}</strong>
              {user?.department ? ` • ${user.department}` : ''}
            </span>
          </div>

          <textarea
            className="input-field composer-textarea-large"
            placeholder="Share notes, clarify doubts, or give helpful context on this material..."
            rows={4}
            value={newCommentText}
            onChange={(e) => setNewCommentText(e.target.value)}
            required
            autoFocus
          />

          <div className="composer-footer">
            <div className="composer-tips">
              <Sparkles size={14} color="var(--accent-terracotta)" />
              <span>Markdown and code snippets supported</span>
            </div>

            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                type="button"
                className="btn btn-sm btn-outline"
                onClick={() => {
                  setIsComposerOpen(false)
                  setNewCommentText('')
                }}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn btn-sm btn-primary"
                disabled={submitting || !newCommentText.trim()}
              >
                <Send size={14} />
                <span>{submitting ? 'Posting…' : 'Publish Comment'}</span>
              </button>
            </div>
          </div>
        </form>
      )}

      {/* Loading Skeleton */}
      {loading && (
        <div className="discussion-loading">
          {[1, 2, 3].map((i) => (
            <div key={i} className="comment-skeleton">
              <div className="skeleton-avatar skeleton" />
              <div style={{ flex: 1 }}>
                <div className="skeleton" style={{ height: '14px', width: '30%', marginBottom: '0.5rem' }} />
                <div className="skeleton" style={{ height: '18px', width: '90%', marginBottom: '0.4rem' }} />
                <div className="skeleton" style={{ height: '14px', width: '60%' }} />
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Error Notice */}
      {error && (
        <div className="alert alert-danger" style={{ margin: '1.5rem 0' }}>
          <AlertCircle size={16} />
          <span>{error}</span>
          <button
            type="button"
            className="btn btn-xs btn-outline"
            onClick={fetchDiscussion}
            style={{ marginLeft: 'auto' }}
          >
            Retry
          </button>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && comments.length === 0 && (
        <div className="reddit-empty-discussion">
          <div className="empty-bubble-icon">
            <MessageSquare size={32} color="var(--accent-terracotta)" />
          </div>
          <h3 className="empty-title">No comments yet</h3>
          <p className="empty-desc">
            Be the first student to start the conversation! Ask questions, point out important exam topics, or leave a review.
          </p>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setIsComposerOpen(true)}
          >
            <Plus size={16} /> Start the Discussion
          </button>
        </div>
      )}

      {/* Comment Threads Tree */}
      {!loading && !error && comments.length > 0 && (
        <div className="reddit-threads-container">
          {sortedComments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              depth={0}
              onReply={handlePostReply}
              onDelete={handleDeleteComment}
              onToggleUpvote={handleToggleUpvote}
              resourceUploaderName={resource.uploadedByName}
            />
          ))}
        </div>
      )}
    </section>
  )
}
