import { useEffect, useState, useCallback } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Bookmark, Compass, AlertCircle, RefreshCw } from 'lucide-react'
import { getBookmarkedResources } from '../services/resourceService'
import ResourceCard from '../components/ResourceCard'
import type { PageResponse, ResourceResponse } from '../types'

export default function Bookmarks() {
  const [searchParams, setSearchParams] = useSearchParams()
  const page = parseInt(searchParams.get('page') ?? '0', 10)

  const [data, setData] = useState<PageResponse<ResourceResponse>>({
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

  const loadBookmarks = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getBookmarkedResources(page, 10, 'newest')
      setData(res)
    } catch (err: any) {
      setError(err?.message || 'Failed to load saved resources.')
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    loadBookmarks()
  }, [loadBookmarks])

  return (
    <div className="page wrap">
      <div className="page-header">
        <h1 className="page-title">Saved Resources</h1>
        <p className="page-subtitle">
          Your personal library of bookmarked notes, reference guides, and experiment manuals.
        </p>
      </div>

      {loading && (
        <div className="resource-list">
          {[1, 2, 3].map((i) => (
            <div key={i} className="card" style={{ padding: '1.5rem' }}>
              <div className="skeleton" style={{ height: '20px', width: '60%', marginBottom: '0.75rem' }} />
              <div className="skeleton" style={{ height: '14px', width: '90%', marginBottom: '0.5rem' }} />
              <div className="skeleton" style={{ height: '14px', width: '35%' }} />
            </div>
          ))}
        </div>
      )}

      {error && !loading && (
        <div className="alert alert-danger" role="alert">
          <AlertCircle size={20} style={{ flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            <p style={{ margin: '0 0 0.4rem', fontWeight: 600 }}>Failed to load saved resources</p>
            <p style={{ margin: 0, fontSize: '0.86rem' }}>{error}</p>
          </div>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={loadBookmarks}
          >
            <RefreshCw size={13} /> Retry
          </button>
        </div>
      )}

      {!loading && !error && data.content.length === 0 && (
        <div className="empty-state">
          <Bookmark
            size={36}
            color="var(--accent-terracotta)"
            style={{ marginBottom: '1rem' }}
          />
          <h3 className="empty-state-title">No saved resources yet</h3>
          <p className="empty-state-desc">
            As you discover helpful notes or question papers, click "Save for Later" on any resource to bookmark it here for quick study access.
          </p>
          <Link to="/resources" className="btn btn-primary">
            <Compass size={15} /> Discover Resources
          </Link>
        </div>
      )}

      {!loading && !error && data.content.length > 0 && (
        <>
          <div
            style={{
              fontSize: '0.86rem',
              color: 'var(--text-secondary)',
              marginBottom: '1rem',
            }}
          >
            {data.totalElements} saved resource{data.totalElements === 1 ? '' : 's'}
          </div>

          <div className="resource-list">
            {data.content.map((res) => (
              <ResourceCard key={res.id} resource={res} />
            ))}
          </div>

          {data.totalPages > 1 && (
            <nav className="pagination" aria-label="Bookmarks Pagination">
              <span className="pagination-info">
                Page {data.page + 1} of {data.totalPages}
              </span>
              <div className="pagination-controls">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  disabled={data.first}
                  onClick={() => setSearchParams({ page: String(data.page - 1) })}
                >
                  Previous
                </button>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  disabled={data.last}
                  onClick={() => setSearchParams({ page: String(data.page + 1) })}
                >
                  Next
                </button>
              </div>
            </nav>
          )}
        </>
      )}
    </div>
  )
}
