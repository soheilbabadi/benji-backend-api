/**
 * Authentication service — the only place that knows the OTP auth endpoints.
 *
 * Login and Register share this implementation: requesting an OTP is the same
 * call for both; verification completes sign-in AND registration server-side.
 */

import { apiRequest, publicPost } from '@/lib/api/client'
import { authStore, type AuthSession } from './store'

export interface OtpAuthResponse {
  accessToken: string
  refreshToken: string
  expiresInSeconds: number
  userId: string
}

/** Ask the backend to SMS a one-time code to `mobile` (202 Accepted). */
export function requestOtp(mobile: string): Promise<void> {
  return publicPost<void>('/api/auth/otp', { mobile })
}

/** Verify the code; on success the returned tokens complete the session. */
export async function verifyOtp(mobile: string, code: string): Promise<AuthSession> {
  const response = await publicPost<OtpAuthResponse>('/api/auth/otp/verify', { mobile, code })
  return authStore.completeSession(response)
}

/** Revoke the current refresh token server-side (idempotent, 204). */
export function logout(refreshToken: string): Promise<void> {
  return publicPost<void>('/api/auth/otp/logout', { refreshToken })
}

/** Revoke every live session of the signed-in user (requires Bearer token). */
export function logoutAllDevices(): Promise<void> {
  return apiRequest<void>('/api/auth/otp/logout/all', { method: 'POST', auth: 'authenticated' })
}
