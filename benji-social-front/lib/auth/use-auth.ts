'use client'

/**
 * React binding for the centralized auth store (useSyncExternalStore).
 *
 * Provides `isAuthenticated`, `userId`, `logout` and the pending state used by
 * protected-route gates. All pages/components consume auth through this hook —
 * authentication logic never lives inside page components.
 */

import { useCallback, useSyncExternalStore } from 'react'
import { useRouter } from 'next/navigation'
import { authStore, type AuthSession } from './store'
import { logout as logoutService } from './auth-service'

export interface AuthContextValue {
  /** False until the persisted session has been loaded on the client. */
  ready: boolean
  isAuthenticated: boolean
  userId: string | null
  session: AuthSession | null
  /** True while a logout round-trip is in flight. */
  pending: boolean
  logout: () => Promise<void>
}

export function useAuth(): AuthContextValue {
  const router = useRouter()

  const session = useSyncExternalStore<AuthSession | null>(
    useCallback((onChange) => authStore.subscribe(onChange), []),
    () => authStore.getSession(),
    () => null,
  )

  // Hydrate from localStorage on first client render (no-ops during SSR).
  if (typeof window !== 'undefined') authStore.hydrate()

  const ready = typeof window !== 'undefined'

  const logout = useCallback(async () => {
    const refreshToken = authStore.getRefreshToken()
    // Always clear local state; even if the API call fails because the token
    // is already invalid, the user must end up signed out.
    authStore.clear()
    if (refreshToken) {
      try {
        await logoutService(refreshToken)
      } catch {
        /* server-side revocation best-effort only */
      }
    }
    router.replace('/login')
  }, [router])

  return {
    ready,
    isAuthenticated: session !== null,
    userId: session?.userId ?? null,
    session,
    pending: false,
    logout,
  }
}
