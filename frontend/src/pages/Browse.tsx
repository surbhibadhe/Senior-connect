import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  Search,
  Filter,
  PlusCircle,
  AlertCircle,
  RefreshCw,
  SlidersHorizontal,
} from 'lucide-react'
import { getResources } from '../services/resourceService'
import ResourceCard from '../components/ResourceCard'
import UploadModal from '../components/UploadModal'
import type {
  PageResponse,
  ResourceResponse,
  ResourceCategory,
  SortOption,
} from '../types'
import { CATEGORY_LABELS } from '../types'

export default function Browse() {
  const [searchParams, setSearchParams] = useSearchParams()

  const search = searchParams.get('search') ?? ''
  const category = (searchParams.get('category') as ResourceCategory | null) ?? undefined
  const sort = (searchParams.get('sort') as SortOption | null) ?? 'newest'
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
  const [isUploadOpen, setIsUploadOpen] = useState(false)

  const updateParam = (key: string, value: string | undefined) => {
    const next = new URLSearchParams(searchParams)
    if (value) {
      next.set(key, value)
    } else {
      next.delete(key)
    }
    // Reset page to 0 when filter/search/sort changes
    if (key !== 'page') {
      next.delete('page')
    }
    setSearchParams(next)
  }

  const loadResources = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const res = await getResources({
        category,
        search,
        page,
        size: 10,
        sort,
      })
      setData(res)
    } catch (err: any) {
      setError(err?.message || 'Failed to load resources from SeniorConnect server.')
    } finally {
      setLoading(false)
    }
  }, [category, search, page, sort])

  useEffect(() => {
    loadResources()
  }, [loadResources])

  const hasActiveFilters = Boolean(search || category || sort !== 'newest')

  const clearAllFilters = () => {
    setSearchParams({})
  }

  return (
    <div className="page wrap">
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-start',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '2rem',
        }}
      >
        <div>
          <h1 className="page-title">Browse Resources</h1>
          <p className="page-subtitle">
            Explore verified study materials, manuals, and exam papers uploaded by university peers.
          </p>
        </div>

        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setIsUploadOpen(true)}
        >
          <PlusCircle size={16} /> Share Material
        </button>
      </div>

      {/* Filter Bar */}
      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={18} className="search-icon" />
          <input
            type="search"
            className="input-field"
            placeholder="Search by title or topic..."
            value={search}
            onChange={(e) => updateParam('search', e.target.value.trim() || undefined)}
          />
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <select
            className="select-field"
            aria-label="Filter by category"
            value={category ?? ''}
            onChange={(e) => updateParam('category', e.target.value || undefined)}
          >
            <option value="">All Categories</option>
            {Object.entries(CATEGORY_LABELS).map(([k, label]) => (
              <option key={k} value={k}>
                {label}
              </option>
            ))}
          </select>

          <select
            className="select-field"
            aria-label="Sort resources"
            value={sort}
            onChange={(e) => updateParam('sort', e.target.value)}
          >
            <option value="newest">Newest First</option>
            <option value="oldest">Oldest First</option>
            <option value="title_asc">Title A → Z</option>
            <option value="title_desc">Title Z → A</option>
          </select>
        </div>
      </div>

      {/* Loading Skeletons */}
      {loading && (
        <div className="resource-list">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="card" style={{ padding: '1.5rem' }}>
              <div
                className="skeleton"
                style={{ height: '22px', width: '55%', marginBottom: '0.85rem' }}
              />
              <div
                className="skeleton"
                style={{ height: '14px', width: '92%', marginBottom: '0.5rem' }}
              />
              <div
                className="skeleton"
                style={{ height: '14px', width: '40%' }}
              />
            </div>
          ))}
        </div>
      )}

      {/* Error state */}
      {error && !loading && (
        <div className="alert alert-danger" role="alert">
          <AlertCircle size={20} style={{ flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            <p style={{ margin: '0 0 0.4rem', fontWeight: 600 }}>
              Error loading resources
            </p>
            <p style={{ margin: 0, fontSize: '0.86rem' }}>{error}</p>
          </div>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={loadResources}
          >
            <RefreshCw size={13} /> Retry
          </button>
        </div>
      )}

      {/* Empty State */}
      {!loading && !error && data.content.length === 0 && (
        <div className="empty-state">
          <h3 className="empty-state-title">
            {hasActiveFilters ? 'No matching resources found' : 'No resources available yet'}
          </h3>
          <p className="empty-state-desc">
            {hasActiveFilters
              ? 'Try adjusting your search keywords, clearing category filters, or switching sort order.'
              : 'Be the first student to upload a lecture note or past paper for this discipline.'}
          </p>
          <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
            {hasActiveFilters && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={clearAllFilters}
              >
                Clear Filters
              </button>
            )}
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => setIsUploadOpen(true)}
            >
              <PlusCircle size={15} /> Upload Resource
            </button>
          </div>
        </div>
      )}

      {/* Results List */}
      {!loading && !error && data.content.length > 0 && (
        <>
          <div
            style={{
              fontSize: '0.86rem',
              color: 'var(--text-secondary)',
              marginBottom: '1rem',
            }}
          >
            Showing {data.content.length} of {data.totalElements} resource
            {data.totalElements === 1 ? '' : 's'}
          </div>

          <div className="resource-list">
            {data.content.map((res) => (
              <ResourceCard key={res.id} resource={res} />
            ))}
          </div>

          {/* Pagination Controls */}
          {data.totalPages > 1 && (
            <nav className="pagination" aria-label="Resource Pagination">
              <span className="pagination-info">
                Page {data.page + 1} of {data.totalPages}
              </span>
              <div className="pagination-controls">
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  disabled={data.first}
                  onClick={() => updateParam('page', String(data.page - 1))}
                >
                  Previous
                </button>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  disabled={data.last}
                  onClick={() => updateParam('page', String(data.page + 1))}
                >
                  Next
                </button>
              </div>
            </nav>
          )}
        </>
      )}

      <UploadModal
        isOpen={isUploadOpen}
        onClose={() => setIsUploadOpen(false)}
        onSuccess={() => loadResources()}
      />
    </div>
  )
}
