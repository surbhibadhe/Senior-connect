import { api } from './apiClient'
import { API_BASE } from './config'
import type {
  UserProfileResponse,
  ContributorProfileResponse,
  ProfileUpdateRequest,
  PageResponse,
  ResourceResponse,
  SortOption,
} from '../types'

/**
 * Fetch current authenticated student profile.
 * Returns null if the user is unauthenticated or session has expired.
 */
export async function currentUser(): Promise<UserProfileResponse | null> {
  try {
    return await api<UserProfileResponse>('/api/users/me')
  } catch (err: any) {
    if (err?.status === 401 || err?.status === 403) {
      return null
    }
    // Network or server error on initial load
    return null
  }
}

/**
 * Initiates institutional Google OAuth2 sign-in flow.
 * Spring Security will authenticate and verify @despu.edu.in domain.
 */
export function signIn(): void {
  const target = `${API_BASE}/oauth2/authorization/google`
  window.location.href = target
}

/**
 * Logs out the current user via Spring Security's /logout endpoint.
 */
export async function signOut(): Promise<void> {
  try {
    await api('/logout', { method: 'POST' })
  } catch {
    // Proceed to refresh state even if request failed
  }
}

/**
 * Updates current student's profile (bio, department).
 */
export async function updateProfile(
  data: ProfileUpdateRequest
): Promise<UserProfileResponse> {
  return await api<UserProfileResponse>('/api/users/me', {
    method: 'PUT',
    body: JSON.stringify(data),
  })
}

/**
 * Retrieves public contributor profile by student ID.
 */
export async function getContributorProfile(
  id: number
): Promise<ContributorProfileResponse> {
  return await api<ContributorProfileResponse>(`/api/users/${id}`)
}

/**
 * Retrieves resources published by a specific contributor.
 */
export async function getContributorResources(
  id: number,
  page: number = 0,
  size: number = 10,
  sort: SortOption = 'newest'
): Promise<PageResponse<ResourceResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort,
  })
  return await api<PageResponse<ResourceResponse>>(
    `/api/users/${id}/resources?${params.toString()}`
  )
}
