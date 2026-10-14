import { API_BASE } from './config'

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public details?: unknown
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

function getCsrfFromCookie(): string | undefined {
  if (typeof document === 'undefined') return undefined
  const match = document.cookie
    .split('; ')
    .find((row) => row.startsWith('XSRF-TOKEN='))
  return match ? decodeURIComponent(match.split('=')[1]) : undefined
}

let cachedCsrfToken: string | undefined = undefined

export async function fetchCsrfToken(): Promise<string | undefined> {
  // First check document cookie
  const cookieToken = getCsrfFromCookie()
  if (cookieToken) {
    cachedCsrfToken = cookieToken
    return cookieToken
  }

  // Otherwise, request from Spring Boot CsrfController
  try {
    const res = await fetch(`${API_BASE}/api/csrf`, {
      method: 'GET',
      credentials: 'include',
    })
    if (res.ok) {
      const data = (await res.json()) as { token?: string }
      if (data?.token) {
        cachedCsrfToken = data.token
        return data.token
      }
    }
  } catch {
    // If not reachable or unauthenticated, ignore
  }

  return getCsrfFromCookie() || cachedCsrfToken
}

export async function api<T>(
  path: string,
  init: RequestInit = {}
): Promise<T> {
  const url = path.startsWith('http') ? path : `${API_BASE}${path}`
  const method = (init.method || 'GET').toUpperCase()
  const isMutating = ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)

  const headers = new Headers(init.headers)

  // Only set application/json if not sending FormData
  if (!(init.body instanceof FormData) && !headers.has('Content-Type') && isMutating) {
    headers.set('Content-Type', 'application/json')
  }

  // Attach CSRF header for state-changing HTTP requests
  if (isMutating) {
    let token = getCsrfFromCookie() || cachedCsrfToken
    if (!token) {
      token = await fetchCsrfToken()
    }
    if (token) {
      headers.set('X-XSRF-TOKEN', token)
    }
  }

  let res: Response
  try {
    res = await fetch(url, {
      ...init,
      headers,
      credentials: 'include', // essential for Spring session cookies
    })
  } catch (err) {
    throw new ApiError(
      0,
      'Unable to connect to SeniorConnect server. Please verify backend is running.'
    )
  }

  // Update cached CSRF token if new cookie was received
  const refreshedToken = getCsrfFromCookie()
  if (refreshedToken) {
    cachedCsrfToken = refreshedToken
  }

  if (res.status === 401) {
    throw new ApiError(401, 'Please sign in with your @despu.edu.in account to continue.')
  }

  if (res.status === 403) {
    throw new ApiError(403, 'You do not have permission to perform this action.')
  }

  if (!res.ok) {
    let errorMsg = `Request failed with status ${res.status}`
    let errorDetails: unknown = null
    try {
      const errorJson = await res.json()
      errorDetails = errorJson
      if (errorJson?.message) {
        errorMsg = errorJson.message
      } else if (typeof errorJson === 'string') {
        errorMsg = errorJson
      }
    } catch {
      // Body is not JSON
    }
    throw new ApiError(res.status, errorMsg, errorDetails)
  }

  if (res.status === 204) {
    return undefined as T
  }

  // For responses with no content length or empty body
  const contentType = res.headers.get('content-type')
  if (contentType && contentType.includes('application/json')) {
    return (await res.json()) as T
  }

  return (await res.text()) as unknown as T
}

export async function downloadFile(
  path: string,
  suggestedFilename?: string
): Promise<void> {
  const url = path.startsWith('http') ? path : `${API_BASE}${path}`
  let res: Response
  try {
    res = await fetch(url, {
      method: 'GET',
      credentials: 'include',
    })
  } catch {
    throw new ApiError(0, 'Failed to download file. Please check server connection.')
  }

  if (!res.ok) {
    throw new ApiError(res.status, `Download failed with status ${res.status}`)
  }

  // Try extracting filename from Content-Disposition header
  let filename = suggestedFilename || 'download'
  const disposition = res.headers.get('content-disposition')
  if (disposition && disposition.includes('filename=')) {
    const filenameMatch = disposition.match(/filename\*?=['"]?(?:UTF-8'')?([^'";\n]+)['"]?/i)
    if (filenameMatch?.[1]) {
      filename = decodeURIComponent(filenameMatch[1])
    }
  }

  const blob = await res.blob()
  const blobUrl = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = blobUrl
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(blobUrl)
}
