/**
 * Tiny sessionStorage helper that carries the mobile number between the entry
 * page (/login or /register) and /verify-otp. sessionStorage keeps it scoped to
 * the tab and automatically drops it when the tab closes.
 */

const PENDING_MOBILE_KEY = 'benji.auth.pendingMobile'
const PENDING_NEXT_KEY = 'benji.auth.pendingNext'

/** Only same-origin relative paths are accepted as post-login destinations. */
export function isSafeRedirectPath(value: string | null | undefined): boolean {
  if (!value) return false
  try {
    const url = new URL(value, 'http://internal.local')
    // Must resolve on the synthetic internal origin (rejects absolute URLs,
    // data:/javascript: schemes) and must not be protocol-relative (//evil).
    return url.origin === 'http://internal.local' && !value.startsWith('//')
  } catch {
    return false
  }
}

/**
 * Remember where an unauthenticated user was headed so the OTP flow can return
 * them there after verification. Invalid/external destinations are dropped —
 * never stored, never redirected to (open-redirect protection).
 */
export function savePendingNext(next: string | null | undefined): void {
  if (typeof window === 'undefined') return
  try {
    if (next != null && isSafeRedirectPath(next)) {
      window.sessionStorage.setItem(PENDING_NEXT_KEY, next)
    } else {
      window.sessionStorage.removeItem(PENDING_NEXT_KEY)
    }
  } catch {
    /* storage unavailable */
  }
}

/** Read the pending destination exactly once; falls back to '/' when absent or unsafe. */
export function takePendingNext(): string {
  if (typeof window === 'undefined') return '/'
  let value: string | null = null
  try {
    value = window.sessionStorage.getItem(PENDING_NEXT_KEY)
    window.sessionStorage.removeItem(PENDING_NEXT_KEY)
  } catch {
    return '/'
  }
  return isSafeRedirectPath(value) ? (value as string) : '/'
}

export function savePendingMobile(mobile: string): void {
  if (typeof window === 'undefined') return
  try {
    window.sessionStorage.setItem(PENDING_MOBILE_KEY, mobile)
  } catch {
    /* storage unavailable */
  }
}

export function readPendingMobile(): string | null {
  if (typeof window === 'undefined') return null
  try {
    return window.sessionStorage.getItem(PENDING_MOBILE_KEY)
  } catch {
    return null
  }
}

export function clearPendingMobile(): void {
  if (typeof window === 'undefined') return
  try {
    window.sessionStorage.removeItem(PENDING_MOBILE_KEY)
  } catch {
    /* ignore */
  }
}
