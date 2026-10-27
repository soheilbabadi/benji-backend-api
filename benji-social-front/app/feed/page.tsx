'use client'

/**
 * Protected demo feed — intentionally minimal verification page.
 *
 * Purpose: prove that route protection + the centralized auth layer work
 * end-to-end. On mount it performs an authenticated API request
 * (GET /api/users/profile/me) through the shared HTTP client, which attaches
 * `Authorization: Bearer <accessToken>` and transparently refreshes the
 * session on 401. It is NOT a real Benji feature.
 */

import { useCallback, useEffect, useState } from 'react'
import Link from 'next/link'
import AuthGate from '@/components/auth/auth-gate'
import { useAuth } from '@/lib/auth/use-auth'
import { fetchMyProfile, type UserProfileDto } from '@/lib/api/profile'
import { isApiError } from '@/lib/api/errors'

function DemoFeedContent() {
  const { userId, logout } = useAuth()
  const [profile, setProfile] = useState<UserProfileDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      // Goes through the shared client with auth: 'authenticated', i.e.
      // Authorization: Bearer <accessToken> (+ single-flight refresh on 401).
      setProfile(await fetchMyProfile())
      setLoading(false)
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Could not load your profile. Please try again.')
      setProfile(null)
      setLoading(false)
    }
  }, [])

  const retry = useCallback(() => {
    setError(null)
    setLoading(true)
    void Promise.resolve().then(load)
  }, [load])

  useEffect(() => {
    // Kick off the request without synchronously setting state in the effect
    // body; the async callback updates state only after the response arrives.
    void Promise.resolve().then(load)
  }, [load])

  return (
    <div className="w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <header className="mb-6 text-center">
        <span aria-hidden className="mb-3 inline-flex h-14 w-14 items-center justify-center rounded-full bg-amber-100 text-3xl dark:bg-amber-950">
          🐾
        </span>
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
          Protected demo
        </h1>
        <p className="mt-2 text-sm leading-6 text-zinc-500 dark:text-zinc-400">
          You are signed in. The data below came from an authenticated API call
          carrying <code className="rounded bg-black/[.06] px-1 py-0.5 font-mono text-[0.85em] dark:bg-white/[.08]">Authorization: Bearer …</code>.
        </p>
      </header>

      <dl className="flex flex-col gap-3 text-sm">
        <div className="flex items-baseline justify-between gap-4">
          <dt className="text-zinc-500 dark:text-zinc-400">User ID</dt>
          <dd className="truncate font-mono text-zinc-900 dark:text-zinc-50">{userId ?? '—'}</dd>
        </div>
        <div className="flex items-baseline justify-between gap-4">
          <dt className="shrink-0 text-zinc-500 dark:text-zinc-400">Name</dt>
          <dd className="truncate text-right text-zinc-900 dark:text-zinc-50">
            {loading ? 'Loading…' : (profile?.name ?? profile?.username ?? 'Not set yet')}
          </dd>
        </div>
      </dl>

      {error && (
        <div className="mt-4" role="alert">
          <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
          <button
            type="button"
            onClick={retry}
            className="mt-2 text-sm font-medium text-amber-600 underline-offset-4 hover:underline dark:text-amber-400"
          >
            Retry
          </button>
        </div>
      )}

      <div className="mt-8 flex flex-col gap-3 sm:flex-row">
        <button
          type="button"
          onClick={() => void logout()}
          className="inline-flex h-11 w-full items-center justify-center rounded-full border border-solid border-zinc-300 px-5 text-sm font-medium text-zinc-700 transition-colors hover:border-transparent hover:bg-zinc-100 dark:border-zinc-700 dark:text-zinc-200 dark:hover:bg-zinc-800"
        >
          Log out
        </button>
        <Link
          href="/"
          className="inline-flex h-11 w-full items-center justify-center rounded-full bg-amber-500 px-5 text-sm font-medium text-white transition-colors hover:bg-amber-600 dark:hover:bg-amber-400"
        >
          Home
        </Link>
      </div>
    </div>
  )
}

export default function FeedPage() {
  return (
    <AuthGate access="protected">
      <div className="flex min-h-dvh items-center justify-center bg-zinc-50 p-4 dark:bg-black">
        <DemoFeedContent />
      </div>
    </AuthGate>
  )
}
