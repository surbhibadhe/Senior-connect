import { api } from './apiClient'
import type { CommentResponse, CommentCreateRequest } from '../types'

export async function getComments(resourceId: number): Promise<CommentResponse[]> {
  return api<CommentResponse[]>(`/api/resources/${resourceId}/comments`)
}

export async function createComment(
  resourceId: number,
  content: string,
  parentCommentId?: number
): Promise<CommentResponse> {
  const payload: CommentCreateRequest = {
    content,
    parentCommentId,
  }
  return api<CommentResponse>(`/api/resources/${resourceId}/comments`, {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export async function toggleCommentUpvote(
  commentId: number
): Promise<{ commentId: number; upvoteCount: number; upvoted: boolean }> {
  return api<{ commentId: number; upvoteCount: number; upvoted: boolean }>(
    `/api/comments/${commentId}/upvote`,
    {
      method: 'POST',
    }
  )
}

export async function deleteComment(commentId: number): Promise<void> {
  return api<void>(`/api/comments/${commentId}`, {
    method: 'DELETE',
  })
}
