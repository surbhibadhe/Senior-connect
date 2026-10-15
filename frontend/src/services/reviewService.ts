import { api } from './apiClient'
import type { ReviewResponse, ReviewCreateRequest } from '../types'

/**
 * Fetch all student reviews (Public - anyone can view without sign-in)
 */
export async function getReviews(): Promise<ReviewResponse[]> {
  return api<ReviewResponse[]>('/api/reviews')
}

/**
 * Submit a student community review (Sign-in required)
 */
export async function createReview(data: ReviewCreateRequest): Promise<ReviewResponse> {
  return api<ReviewResponse>('/api/reviews', {
    method: 'POST',
    body: JSON.stringify(data),
  })
}

/**
 * Delete a review (Author or Admin only)
 */
export async function deleteReview(id: number): Promise<void> {
  return api<void>(`/api/reviews/${id}`, {
    method: 'DELETE',
  })
}
