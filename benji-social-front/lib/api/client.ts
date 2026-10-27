/**
 * Benji API client — the single HTTP layer for talking to the backend.
 *
 * Responsibilities:
 *  - Base URL from NEXT_PUBLIC_BENJI_API_URL (falls back to localhost:8080).
 *  - Automatic `Authorization: Bearer <accessToken>` on authenticated calls.
 *  - Transparent refresh-on-401: when an authenticated call fails with 401,
 *    exactly ONE refresh attempt is made and the original request is retried
 *    once. The refresh endpoint itself never goes through this retry logic,
 *    which prevents infinite refresh loops / recursive refreshes.
 */

import { ApiError, networkError, toApiError } from './errors'
import { authStore } from '@/lib/auth/store'

export const API_BASE_URL: string =
  process.env.NEXT_PUBLIC_BENJI_API_URL?.replace(/\/+$/, '') ?? 'http://localhost:8080'

export type AuthMode = 'public' | 'authenticated'

export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  /** 'public' (default) sends no Authorization header; 'authenticated' attaches the Bearer token. */
  auth?: AuthMode
  signal?: AbortSignal
}

async function rawRequest(path: string, options: RequestOptions): Promise<Response> {
  const headers: Record<string, string> = {}
  if (options.body !== undefined) headers['Content-Type'] = 'application/json'

  if (options.auth === 'authenticated') {
    const accessToken = authStore.getAccessToken()
    if (accessToken) headers.Authorization = `Bearer ${accessToken}`
  }

  try {
    return await fetch(`${API_BASE_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
      signal: options.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw networkError()
  }
}

/**
 * Perform an API request. Returns the parsed JSON body (or null for 204/empty).
 * Throws ApiError for non-ok responses and network failures.
 */
export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const mode: AuthMode = options.auth ?? 'public'
  let response = await rawRequest(path, { ...options, auth: mode })

  // Auto-refresh once for authenticated requests that fail with 401.
  if (response.status === 401 && mode === 'authenticated' && authStore.getRefreshToken()) {
    try {
      await authStore.refreshSession()
    } catch {
      // Refresh failed: session already cleared by the store; surface the
      // original unauthorized response to the caller.
      throw await toApiError(response)
    }
    response = await rawRequest(path, { ...options, auth: 'authenticated' })
  }

  return parseResponse<T>(response)
}

async function parseResponse<T>(response: Response): Promise<T> {
  if (!response.ok) throw await toApiError(response)
  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null as T
  }
  return (await response.json()) as T
}

// NOTE: exported helper so the auth store can perform the refresh POST
// without re-entering the auto-retry path above.
export async function publicPost<T>(path: string, body: unknown): Promise<T> {
  return apiRequest<T>(path, { method: 'POST', body, auth: 'public' })
}

export { ApiError }
