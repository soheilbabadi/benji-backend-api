/**
 * Minimal typed access to the authenticated profile endpoint. Used by the
 * protected demo page to prove that Bearer-token requests reach the backend.
 */

import { apiRequest } from './client'

export interface UserProfileDto {
  userId: string
  name: string | null
  username: string | null
  email: string | null
  city: string | null
  country: string | null
  bio: string | null
  phone: string | null
  website: string | null
  birthDate: string | null
  gender: string | null
  avatarUrl: string | null
  createdAt: string
  updatedAt: string
}

/** GET /api/users/profile/me — requires Authorization: Bearer <accessToken>. */
export function fetchMyProfile(): Promise<UserProfileDto> {
  return apiRequest<UserProfileDto>('/api/users/profile/me', { auth: 'authenticated' })
}
