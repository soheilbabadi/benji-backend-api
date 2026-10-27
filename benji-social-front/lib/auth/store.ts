/**
 * Centralized authentication store for the Benji frontend.
 *
 * Single source of truth for session state:
 *  - tokens live in localStorage (the backend is a separate-origin Bearer-token
 *    API; no cookies are involved) and are mirrored into memory so SSR never
 *    touches browser storage,
 *  - concurrent refresh calls are deduplicated into one network request,
 *  - failed refresh clears the session everywhere and notifies listeners,
 *  - cross-tab synchronization via the `storage` event.
 *
 * UI code must go through AuthProvider / useAuth — never this module directly.
 */

import { publicPost } from '@/lib/api/client'

export interface AuthSession {
  accessToken: string
  refreshToken: string
  userId: string
  /** Absolute epoch-ms deadline after which the access token is expired. */
  expiresAt: number
}

const STORAGE_KEY = 'benji.auth.session'

/** Shape returned by all three token endpoints of /api/auth/otp. */
interface OtpAuthResponse {
  accessToken: string
  refreshToken: string
  expiresInSeconds: number
  userId: string
}

function readStorage(): AuthSession | null {
  if (typeof window === 'undefined') return null
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<AuthSession>
    if (
      typeof parsed.accessToken === 'string' &&
      typeof parsed.refreshToken === 'string' &&
      typeof parsed.userId === 'string' &&
      typeof parsed.expiresAt === 'number'
    ) {
      return parsed as AuthSession
    }
    window.localStorage.removeItem(STORAGE_KEY)
    return null
  } catch {
    return null
  }
}

class AuthStore {
  private session: AuthSession | null = null
  private hydrated = false
  private listeners = new Set<() => void>()
  private pendingRefresh: Promise<AuthSession> | null = null

  constructor() {
    if (typeof window !== 'undefined') {
      // Keep tabs in sync when another tab logs in/out/refreshes.
      window.addEventListener('storage', (event) => {
        if (event.key !== STORAGE_KEY) return
        this.session = readStorage()
        this.emit()
      })
    }
  }

  subscribe(listener: () => void): () => void {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }

  private emit(): void {
    for (const listener of this.listeners) listener()
  }

  /** Load the persisted session once on the client. Idempotent. */
  hydrate(): void {
    if (this.hydrated || typeof window === 'undefined') return
    this.hydrated = true
    this.session = readStorage()
    this.emit()
  }

  getSession(): AuthSession | null {
    return this.session
  }

  getAccessToken(): string | null {
    return this.session?.accessToken ?? null
  }

  getRefreshToken(): string | null {
    return this.session?.refreshToken ?? null
  }

  getUserId(): string | null {
    return this.session?.userId ?? null
  }

  /** True while we hold tokens; expiry is handled lazily via refresh-on-401. */
  isAuthenticated(): boolean {
    return this.session !== null
  }

  completeSession(response: OtpAuthResponse): AuthSession {
    const session: AuthSession = {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      userId: String(response.userId),
      expiresAt: Date.now() + Math.max(0, response.expiresInSeconds) * 1000,
    }
    this.persist(session)
    return session
  }

  private persist(session: AuthSession): void {
    this.session = session
    if (typeof window !== 'undefined') {
      try {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
      } catch {
        /* storage unavailable: keep in-memory session */
      }
    }
    this.emit()
  }

  /**
   * Exchange the refresh token for a new session. Concurrent callers share a
   * single in-flight request; on failure the session is cleared so nobody can
   * loop on refresh. Never routed through the auto-retrying API path.
   */
  refreshSession(): Promise<AuthSession> {
    if (this.pendingRefresh) return this.pendingRefresh
    const refreshToken = this.session?.refreshToken
    if (!refreshToken) return Promise.reject(new Error('No refresh token available'))

    this.pendingRefresh = publicPost<OtpAuthResponse>('/api/auth/otp/refresh', { refreshToken })
      .then((response) => {
        const next = this.completeSession(response)
        this.pendingRefresh = null
        return next
      })
      .catch((error: unknown) => {
        this.pendingRefresh = null
        this.clear() // invalid/expired refresh token -> signed out everywhere
        throw error
      })

    return this.pendingRefresh
  }

  clear(): void {
    this.session = null
    if (typeof window !== 'undefined') {
      try {
        window.localStorage.removeItem(STORAGE_KEY)
      } catch {
        /* ignore */
      }
    }
    this.emit()
  }
}

export const authStore = new AuthStore()
