'use client'

/**
 * Route-protection gate for authenticated areas.
 *
 * Client-side because the session lives in browser storage (Bearer tokens, no
 * cookies): renders nothing until hydrated, redirects unauthenticated visitors
 * to /login (preserving the intended destination), and bounces authenticated
 * users away from public auth pages (login/register) consistently.
 */

import { useEffect } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuth } from '@/lib/auth/use-auth'

interface ProtectedRouteProps {
  /** 'protected' requires a session; 'public' redirects signed-in users to /. */
  access: 'protected' | 'public'
  redirectTo?: string
  children: React.ReactNode
}

export default function AuthGate({ access, redirectTo = '/', children }: ProtectedRouteProps) {
  const router = useRouter()
  const pathname = usePathname()
  const { ready, isAuthenticated } = useAuth()

  useEffect(() => {
    if (!ready) return
    if (access === 'protected' && !isAuthenticated) {
      // Preserve the intended destination so /verify-otp can return to it.
      const target = encodeURIComponent(pathname ?? '/')
      router.replace(`/login?next=${target}`)
    } else if (access === 'public' && isAuthenticated) {
      router.replace(redirectTo)
    }
  }, [ready, isAuthenticated, access, pathname, redirectTo, router])

  if (!ready || (access === 'protected' && !isAuthenticated)) {
    return (
      <div className="flex min-h-dvh items-center justify-center bg-zinc-50 dark:bg-black">
        <span
          role="status"
          aria-live="polite"
          className="h-8 w-8 animate-spin rounded-full border-2 border-zinc-300 border-t-amber-500 dark:border-zinc-700 dark:border-t-amber-400"
        />
      </div>
    )
  }

  return <>{children}</>
}
