import { api, downloadFile } from './apiClient'
import type {
  PageResponse,
  ResourceResponse,
  ResourceCategory,
  ResourceCreateRequest,
  BookmarkStatusResponse,
  HelpfulStatusResponse,
  ResourceReportRequest,
  ResourceReportResponse,
  ReportStatus,
  ReportStatusUpdateRequest,
  SortOption,
} from '../types'

export interface ResourceQueryParams {
  category?: ResourceCategory
  search?: string
  page?: number
  size?: number
  sort?: SortOption
}

/**
 * Fetch paginated resources with optional search, category filter, and sorting.
 * Note: Backend uses 0-indexed page numbers.
 */
export async function getResources(
  params: ResourceQueryParams = {}
): Promise<PageResponse<ResourceResponse>> {
  const query = new URLSearchParams()
  if (params.category) query.set('category', params.category)
  if (params.search && params.search.trim()) query.set('search', params.search.trim())
  query.set('page', String(params.page ?? 0))
  query.set('size', String(params.size ?? 10))
  query.set('sort', params.sort ?? 'newest')

  return await api<PageResponse<ResourceResponse>>(`/api/resources?${query.toString()}`)
}

/**
 * Get detailed information for a single resource by ID.
 */
export async function getResourceById(id: number): Promise<ResourceResponse> {
  return await api<ResourceResponse>(`/api/resources/${id}`)
}

/**
 * Create a link or text resource.
 */
export async function createResource(
  request: ResourceCreateRequest
): Promise<ResourceResponse> {
  return await api<ResourceResponse>('/api/resources', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

/**
 * Upload a document file resource (PDF, DOC, DOCX up to 10MB).
 */
export async function uploadResourceFile(
  title: string,
  description: string | undefined,
  category: ResourceCategory,
  file: File
): Promise<ResourceResponse> {
  const formData = new FormData()
  formData.append('title', title)
  if (description) formData.append('description', description)
  formData.append('category', category)
  formData.append('file', file)

  return await api<ResourceResponse>('/api/resources/upload', {
    method: 'POST',
    body: formData,
  })
}

/**
 * Download a document file attachment.
 */
export async function downloadResource(
  id: number,
  suggestedFilename?: string
): Promise<void> {
  await downloadFile(`/api/resources/${id}/download`, suggestedFilename)
}

/**
 * Delete a resource (contributor or admin only).
 */
export async function deleteResource(id: number): Promise<void> {
  await api<void>(`/api/resources/${id}`, {
    method: 'DELETE',
  })
}

/**
 * Update a resource title, description, category, or content.
 */
export async function updateResource(
  id: number,
  request: ResourceCreateRequest
): Promise<ResourceResponse> {
  return await api<ResourceResponse>(`/api/resources/${id}`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

/* ========================================================================
   BOOKMARKS
   ======================================================================== */

export async function bookmarkResource(id: number): Promise<BookmarkStatusResponse> {
  return await api<BookmarkStatusResponse>(`/api/resources/${id}/bookmark`, {
    method: 'POST',
  })
}

export async function removeBookmark(id: number): Promise<BookmarkStatusResponse> {
  return await api<BookmarkStatusResponse>(`/api/resources/${id}/bookmark`, {
    method: 'DELETE',
  })
}

export async function getBookmarkStatus(id: number): Promise<BookmarkStatusResponse> {
  return await api<BookmarkStatusResponse>(`/api/resources/${id}/bookmark`)
}

export async function getBookmarkedResources(
  page: number = 0,
  size: number = 10,
  sort: SortOption = 'newest'
): Promise<PageResponse<ResourceResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort,
  })
  return await api<PageResponse<ResourceResponse>>(`/api/bookmarks?${params.toString()}`)
}

/* ========================================================================
   HELPFUL FEEDBACK
   ======================================================================== */

export async function markHelpful(id: number): Promise<HelpfulStatusResponse> {
  return await api<HelpfulStatusResponse>(`/api/resources/${id}/helpful`, {
    method: 'POST',
  })
}

export async function removeHelpful(id: number): Promise<HelpfulStatusResponse> {
  return await api<HelpfulStatusResponse>(`/api/resources/${id}/helpful`, {
    method: 'DELETE',
  })
}

export async function getHelpfulStatus(id: number): Promise<HelpfulStatusResponse> {
  return await api<HelpfulStatusResponse>(`/api/resources/${id}/helpful`)
}

/* ========================================================================
   MODERATION & REPORTS
   ======================================================================== */

export async function reportResource(
  id: number,
  request: ResourceReportRequest
): Promise<ResourceReportResponse> {
  return await api<ResourceReportResponse>(`/api/resources/${id}/report`, {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export async function getAdminReports(
  status?: ReportStatus,
  page: number = 0,
  size: number = 10
): Promise<PageResponse<ResourceReportResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })
  if (status) params.set('status', status)
  return await api<PageResponse<ResourceReportResponse>>(
    `/api/admin/reports?${params.toString()}`
  )
}

export async function getAdminReportById(id: number): Promise<ResourceReportResponse> {
  return await api<ResourceReportResponse>(`/api/admin/reports/${id}`)
}

export async function updateAdminReportStatus(
  id: number,
  request: ReportStatusUpdateRequest
): Promise<ResourceReportResponse> {
  return await api<ResourceReportResponse>(`/api/admin/reports/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify(request),
  })
}

export async function removeResourceByReport(
  id: number
): Promise<ResourceReportResponse> {
  return await api<ResourceReportResponse>(`/api/admin/reports/${id}/resource`, {
    method: 'DELETE',
  })
}
