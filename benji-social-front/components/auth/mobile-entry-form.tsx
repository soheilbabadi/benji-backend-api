'use client'

/**
 * Shared OTP entry form used by BOTH /login and /register.
 *
 * The backend treats login and registration as one flow (OTP verification
 * creates the account if it does not exist), so this component holds all the
 * logic: mobile validation, request-OTP call, loading/error states, and
 * navigation to /verify-otp with the number preserved in sessionStorage.
 */

import { use, useState, type FormEvent } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { requestOtp } from '@/lib/auth/auth-service'
import { savePendingMobile, savePendingNext } from './pending-mobile'
import { isApiError } from '@/lib/api/errors'

/** Loose pre-flight check; the backend remains the authority (E.164 normalization). */
const MOBILE_ALLOWED_CHARS = /^\+?[\d\s().-]+$/
const MOBILE_DIGITS_MIN = 8
const MOBILE_DIGITS_MAX = 15

interface MobileEntryFormProps {
  mode: 'login' | 'register'
  /**
   * Server-component searchParams promise, forwarded unawaited so this route
   * stays prerenderable under cacheComponents. Resolved here with use().
   */
  searchParams: Promise<{ next?: string }>
}

export default function MobileEntryForm({ mode, searchParams }: MobileEntryFormProps) {
  const router = useRouter()
  // Intended post-login destination (?next=, set by AuthGate when redirecting
  // an unauthenticated visitor away from a protected page).
  const { next } = use(searchParams)
  const [mobile, setMobile] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const isLogin = mode === 'login'

  function validate(raw: string): string | null {
    const value = raw.trim()
    if (!value) return 'Please enter your mobile number.'
    if (!MOBILE_ALLOWED_CHARS.test(value)) return 'The mobile number contains invalid characters.'
    const digits = value.replace(/\D/g, '')
    if (digits.length < MOBILE_DIGITS_MIN || digits.length > MOBILE_DIGITS_MAX) {
      return 'The mobile number must have between 8 and 15 digits.'
    }
    return null
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (submitting) return

    const validationMessage = validate(mobile)
    if (validationMessage) {
      setError(validationMessage)
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      await requestOtp(mobile.trim())
      savePendingMobile(mobile.trim())
      // Persist the intended post-login destination (validated internally —
      // unsafe values are dropped) so /verify-otp can consume it once.
      savePendingNext(next)
      router.push('/verify-otp')
    } catch (err) {
      setError(
        isApiError(err)
          ? err.message
          : 'Something went wrong while sending the code. Please try again.',
      )
      setSubmitting(false)
    }
  }

  return (
    <div className="w-full max-w-sm rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <header className="mb-8 text-center">
        <span aria-hidden className="mb-3 inline-flex h-14 w-14 items-center justify-center rounded-full bg-amber-100 text-3xl dark:bg-amber-950">
          🐾
        </span>
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
          {isLogin ? 'Welcome back to Benji' : 'Join Benji'}
        </h1>
        <p className="mt-2 text-sm leading-6 text-zinc-500 dark:text-zinc-400">
          {isLogin
            ? 'Sign in with your mobile number. We will text you a verification code.'
            : 'Create your account with just your mobile number — no password needed.'}
        </p>
      </header>

      <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
        <div>
          <label htmlFor="mobile" className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Mobile number
          </label>
          <input
            id="mobile"
            name="mobile"
            type="tel"
            inputMode="tel"
            autoComplete="tel"
            autoFocus
            required
            placeholder="+98 912 345 6789"
            value={mobile}
            onChange={(event) => setMobile(event.target.value)}
            disabled={submitting}
            aria-invalid={error ? true : undefined}
            aria-describedby={error ? 'mobile-error' : 'mobile-hint'}
            className="mt-2 block w-full rounded-lg border border-zinc-300 bg-white px-4 py-3 text-base text-zinc-900 outline-none transition-colors placeholder:text-zinc-400 focus:border-amber-500 focus:ring-2 focus:ring-amber-500/30 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-50 dark:placeholder:text-zinc-500"
          />
          {error ? (
            <p id="mobile-error" role="alert" className="mt-2 text-sm text-red-600 dark:text-red-400">
              {error}
            </p>
          ) : (
            <p id="mobile-hint" className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
              Include your country code, or we will use the default one.
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="mt-2 inline-flex h-12 w-full items-center justify-center gap-2 rounded-full bg-amber-500 px-5 text-base font-medium text-white transition-colors hover:bg-amber-600 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-500 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-amber-500 dark:hover:bg-amber-400"
        >
          {submitting && (
            <span
              aria-hidden
              className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white"
            />
          )}
          {submitting ? 'Sending code…' : 'Send code'}
        </button>
      </form>

      <nav aria-label="Authentication" className="mt-6 text-center text-sm text-zinc-500 dark:text-zinc-400">
        {isLogin ? (
          <>
            New to Benji?{' '}
            <Link href="/register" className="font-medium text-amber-600 underline-offset-4 hover:underline dark:text-amber-400">
              Create an account
            </Link>
          </>
        ) : (
          <>
            Already have an account?{' '}
            <Link href="/login" className="font-medium text-amber-600 underline-offset-4 hover:underline dark:text-amber-400">
              Sign in
            </Link>
          </>
        )}
      </nav>
    </div>
  )
}
