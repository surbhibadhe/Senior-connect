export type Role = 'JUNIOR' | 'SENIOR' | 'ADMIN'

export type ResourceCategory =
  | 'NOTES'
  | 'LAB_MANUALS'
  | 'ASSIGNMENTS'
  | 'QUESTION_PAPERS'
  | 'REFERENCE_MATERIALS'
  | 'TEXTBOOK_LINKS'

export type ResourceType =
  | 'PDF'
  | 'DOC'
  | 'DOCX'
  | 'TEXT'
  | 'GOOGLE_DRIVE_LINK'
  | 'EXTERNAL_LINK'

export type ReportReason =
  | 'SPAM'
  | 'INCORRECT_CONTENT'
  | 'COPYRIGHT'
  | 'INAPPROPRIATE'
  | 'OTHER'

export type ReportStatus = 'OPEN' | 'REVIEWED' | 'DISMISSED'

export type SortOption = 'newest' | 'oldest' | 'title_asc' | 'title_desc'

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface ResourceResponse {
  id: number
  title: string
  description: string | null
  category: ResourceCategory
  type: ResourceType
  textContent: string | null
  resourceUrl: string | null
  originalFileName: string | null
  uploadedByName: string
  createdAt: string
  updatedAt: string | null
  helpfulCount: number
  commentCount?: number
}

export interface CommentResponse {
  id: number
  resourceId: number
  userId: number | null
  userName: string
  userEmail: string
  userProfilePicture: string | null
  userRole: string
  userDepartment: string | null
  content: string
  parentCommentId: number | null
  createdAt: string
  updatedAt: string
  upvotes: number
  userUpvoted: boolean
  canDelete: boolean
  isAuthor: boolean
  deleted: boolean
  replies: CommentResponse[]
}

export interface CommentCreateRequest {
  content: string
  parentCommentId?: number
}

export interface UserProfileResponse {
  id: number
  name: string
  email: string
  profilePicture: string | null
  role: Role
  bio: string | null
  department: string | null
  profileComplete: boolean
  createdAt: string
}

export interface ContributorProfileResponse {
  id: number
  name: string
  profilePicture: string | null
  bio: string | null
  department: string | null
  totalResources: number
  createdAt: string
}

export interface BookmarkStatusResponse {
  resourceId: number
  bookmarked: boolean
  message: string
}

export interface HelpfulStatusResponse {
  resourceId: number
  helpfulCount: number
  markedHelpful: boolean
  message: string
}

export interface ResourceReportResponse {
  id: number
  resourceId: number
  resourceTitle: string
  reporterId: number
  reporterName: string
  reason: ReportReason
  explanation: string | null
  status: ReportStatus
  moderatorNotes: string | null
  resolvedByName: string | null
  createdAt: string
  resolvedAt: string | null
}

export interface ResourceCreateRequest {
  title: string
  description?: string
  category: ResourceCategory
  type: ResourceType
  textContent?: string
  resourceUrl?: string
}

export interface ProfileUpdateRequest {
  bio?: string
  department?: string
}

export interface ResourceReportRequest {
  reason: ReportReason
  explanation?: string
}

export interface ReportStatusUpdateRequest {
  status: ReportStatus
  moderatorNotes?: string
}

export const CATEGORY_LABELS: Record<ResourceCategory, string> = {
  NOTES: 'Lecture Notes',
  LAB_MANUALS: 'Lab Manuals',
  ASSIGNMENTS: 'Assignments',
  QUESTION_PAPERS: 'Question Papers (PYQ)',
  REFERENCE_MATERIALS: 'Reference Materials',
  TEXTBOOK_LINKS: 'Textbook Links',
}

export const REPORT_REASON_LABELS: Record<ReportReason, string> = {
  SPAM: 'Spam or misleading',
  INCORRECT_CONTENT: 'Incorrect or outdated material',
  COPYRIGHT: 'Copyright violation',
  INAPPROPRIATE: 'Inappropriate or harmful content',
  OTHER: 'Other issue',
}

export interface ReviewResponse {
  id: number
  userId: number | null
  authorName: string
  authorPicture: string | null
  authorSubtitle: string | null
  perspectiveTag: string | null
  content: string
  rating: number
  createdAt: string
  canDelete: boolean
}

export interface ReviewCreateRequest {
  content: string
  rating?: number
  perspectiveTag?: string
  authorSubtitle?: string
}
