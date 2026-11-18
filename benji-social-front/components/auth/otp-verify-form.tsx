'use client'

/**
 * OTP verification screen — the second (shared) half of the login/register
 * flow. Handles: verify, resend with cooldown (backend Retry-After aware),
 * change-number, and every backend error class (invalid/expired/rate-limit/
 * validation/network).
 */

import { useCallback, useEffect, useRef, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { requestOtp, verifyOtp } from '@/lib/auth/auth-service'
import { isApiError } from '@/lib/api/errors'
import { clearPendingMobile, readPendingMobile, takePendingNext } from './pending-mobile'

const CODE_MIN_DIGITS = 4
const CODE_MAX_DIGITS = 8

/**
 * Fallback resend cooldown used only when the backend does not tell us the
 * exact wait via `retryAfterSeconds` / `Retry-After`. Matches the backend's
 * default OTP resend cooldown (benji.security.otp.resend-cooldown-seconds).
 */
const DEFAULT_RESEND_COOLDOWN_SECONDS = 180

interface OtpVerifyFormProps {
  mobile: string
  /** Called after a successful verification (session already stored). */
  onVerified: () => void
}

export default function OtpVerifyForm({ mobile, onVerified }: OtpVerifyFormProps) {
  const [code, setCode] = useState('')
  const [verifying, setVerifying] = useState(false)
  const [resending, setResending] = useState(false)
  const [cooldownLeft, setCooldownLeft] = useState(DEFAULT_RESEND_COOLDOWN_SECONDS)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const cooldownTimer = useRef<ReturnType<typeof setInterval> | null>(null)

  const startCooldown = useCallback((seconds: number) => {
    if (cooldownTimer.current) clearInterval(cooldownTimer.current)
    setCooldownLeft(seconds)
    cooldownTimer.current = setInterval(() => {
      setCooldownLeft((previous) => {
        if (previous <= 1 && cooldownTimer.current) {
          clearInterval(cooldownTimer.current)
          cooldownTimer.current = null
        }
        return Math.max(0, previous - 1)
      })
    }, 1000)
  }, [])

  useEffect(() => {
    // A freshly sent code just arrived by SMS; the initial cooldown value was
    // seeded via useState above — here we only start the countdown ticking on
    // mount and clean up the timer afterwards.
    cooldownTimer.current = setInterval(() => {
      setCooldownLeft((previous) => {
        if (previous <= 1 && cooldownTimer.current) {
          clearInterval(cooldownTimer.current)
          cooldownTimer.current = null
        }
        return Math.max(0, previous - 1)
      })
    }, 1000)
    return () => {
      if (cooldownTimer.current) clearInterval(cooldownTimer.current)
    }
  }, [])

  function handleCodeChange(raw: string) {
    // Digits only, capped at the backend maximum.
    setCode(raw.replace(/\D/g, '').slice(0, CODE_MAX_DIGITS))
    setError(null)
  }

  async function handleVerify(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (verifying) return
    if (code.length < CODE_MIN_DIGITS) {
      setError(`Enter the ${CODE_MIN_DIGITS}–${CODE_MAX_DIGITS}-digit code from the SMS.`)
      return
    }

    setVerifying(true)
    setError(null)
    try {
      await verifyOtp(mobile, code)
      clearPendingMobile()
      onVerified()
    } catch (err) {
      setError(
        isApiError(err) ? err.message : 'Verification failed. Please try again.',
      )
      setVerifying(false)
    }
  }

  async function handleResend() {
    if (resending || cooldownLeft > 0) return
    setResending(true)
    setError(null)
    setNotice(null)
    try {
      await requestOtp(mobile)
      setNotice('A new code has been sent by SMS.')
      setCode('')
      startCooldown(DEFAULT_RESEND_COOLDOWN_SECONDS)
    } catch (err) {
      if (isApiError(err)) {
        setError(err.message)
        // Honor the backend's precise backoff hint when it provides one.
        if (err.retryAfterSeconds && err.retryAfterSeconds > 0) {
          startCooldown(err.retryAfterSeconds)
        }
      } else {
        setError('Could not send a new code. Please try again.')
      }
    } finally {
      setResending(false)
    }
  }

  const canSubmit = code.length >= CODE_MIN_DIGITS && !verifying

  return (
    <div className="w-full max-w-sm rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm dark:border-zinc-800 dark:bg-zinc-900">
      <header className="mb-8 text-center">
        <span aria-hidden className="mb-3 inline-flex h-14 w-14 items-center justify-center rounded-full bg-amber-100 text-3xl dark:bg-amber-950">
          📩
        </span>
        <h1 className="text-2xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
          Enter your code
        </h1>
        <p className="mt-2 text-sm leading-6 text-zinc-500 dark:text-zinc-400">
          We sent a {CODE_MIN_DIGITS}–{CODE_MAX_DIGITS}-digit code by SMS to{' '}
          <span className="font-medium text-zinc-700 dark:text-zinc-200">{mobile}</span>
        </p>
      </header>

      <form onSubmit={handleVerify} noValidate className="flex flex-col gap-4">
        <div>
          <label htmlFor="otp-code" className="block text-sm font-medium text-zinc-700 dark:text-zinc-300">
            Verification code
          </label>
          <input
            id="otp-code"
            name="one-time-code"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            autoFocus
            required
            pattern="\d{4,8}"
            maxLength={CODE_MAX_DIGITS}
            placeholder="••••••"
            value={code}
            onChange={(event) => handleCodeChange(event.target.value)}
            onPaste={(event) => {
              event.preventDefault()
              handleCodeChange(event.clipboardData.getData('text'))
            }}
            disabled={verifying}
            aria-invalid={error ? true : undefined}
            aria-describedby={error ? 'otp-error' : 'otp-hint'}
            className="mt-2 block w-full rounded-lg border border-zinc-300 bg-white px-4 py-3 text-center font-mono text-2xl tracking-[0.5em] text-zinc-900 outline-none transition-colors placeholder:tracking-[0.5em] placeholder:text-zinc-300 focus:border-amber-500 focus:ring-2 focus:ring-amber-500/30 disabled:cursor-not-allowed disabled:opacity-60 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-50 dark:placeholder:text-zinc-600"
          />
          {error ? (
            <p id="otp-error" role="alert" className="mt-2 text-sm text-red-600 dark:text-red-400">
              {error}
            </p>
          ) : notice ? (
            <p className="mt-2 text-sm text-emerald-600 dark:text-emerald-400" role="status">
              {notice}
            </p>
          ) : (
            <p id="otp-hint" className="mt-2 text-xs text-zinc-400 dark:text-zinc-500">
              The code contains digits only. You can paste it directly.
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={!canSubmit}
          className="mt-2 inline-flex h-12 w-full items-center justify-center gap-2 rounded-full bg-amber-500 px-5 text-base font-medium text-white transition-colors hover:bg-amber-600 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-500 disabled:cursor-not-allowed disabled:opacity-60 dark:hover:bg-amber-400"
        >
          {verifying && (
            <span aria-hidden className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
          )}
          {verifying ? 'Verifying…' : 'Verify'}
        </button>
      </form>

      <div className="mt-6 flex items-center justify-between text-sm">
        <button
          type="button"
          onClick={handleResend}
          disabled={resending || cooldownLeft > 0}
          className="inline-flex items-center gap-2 font-medium text-amber-600 underline-offset-4 hover:underline disabled:cursor-not-allowed disabled:opacity-60 dark:text-amber-400"
        >
          {resending && (
            <span aria-hidden className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-amber-300 border-t-amber-600 dark:border-amber-700 dark:border-t-amber-300" />
          )}
          {cooldownLeft > 0
            ? `Resend code in ${cooldownLeft}s`
            : resending
              ? 'Sending…'
              : 'Resend code'}
        </button>

        <Link
          href="/login"
          className="font-medium text-zinc-500 underline-offset-4 hover:underline dark:text-zinc-400"
        >
          Change number
        </Link>
      </div>
    </div>
  )
}

/**
 * Page-level wrapper: resolves the pending mobile number from sessionStorage
 * (saved by MobileEntryForm before navigating here). Kept separate so the
 * /verify-otp page file stays a thin composition. No searchParams usage — the
 * route must remain prerenderable under cacheComponents.
 */
export function OtpVerifyScreen() {
  const router = useRouter()
  const [mobile] = useState<string | null>(() => readPendingMobile())

  // Deep-link without a known number: nothing to verify — go back to entry.
  useEffect(() => {
    if (!mobile) router.replace('/login')
  }, [mobile, router])

  if (!mobile) return null

  return (
    <OtpVerifyForm
      mobile={mobile}
      onVerified={() => {
        // Return to where the user was headed before sign-in (validated
        // internal path only; consumed exactly once, defaults to '/').
        router.replace(takePendingNext())
      }}
    />
  )
}
