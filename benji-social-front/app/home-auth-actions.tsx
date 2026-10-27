'use client'

/**
 * Root-page CTA row. Server components cannot read the browser session
 * (Bearer tokens live in localStorage), so this small client island decides
 * between "Sign in / Create account" and "Open demo feed".
 */

import Link from 'next/link'
import { useAuth } from '@/lib/auth/use-auth'

const primaryClass =
  'flex h-12 w-full items-center justify-center rounded-full bg-amber-500 px-5 font-medium text-white transition-colors hover:bg-amber-600 md:w-[158px] dark:hover:bg-amber-400'
const secondaryClass =
  'flex h-12 w-full items-center justify-center rounded-full border border-solid border-black/[.08] px-5 transition-colors hover:border-transparent hover:bg-black/[.04] dark:border-white/[.145] dark:hover:bg-[#1a1a1a] md:w-[158px]'

export default function HomeAuthActions() {
  const { isAuthenticated } = useAuth()

  // While hydrating, render the public CTAs; they swap instantly if signed in.
  if (isAuthenticated) {
    return (
      <div className="flex flex-col gap-4 text-base font-medium sm:flex-row">
        <Link href="/feed" className={primaryClass}>
          Demo feed
        </Link>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4 text-base font-medium sm:flex-row">
      <Link href="/login" className={primaryClass}>
        Sign in
      </Link>
      <Link href="/register" className={secondaryClass}>
        Create account
      </Link>
    </div>
  )
}
