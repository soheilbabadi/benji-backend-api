/**
 * Normalized API errors for the Benji backend.
 *
 * The backend exposes two error shapes:
 *  - RFC 7807 ProblemDetail with a stable machine-readable `code` property
 *    (user-management domain errors such as OTP_INVALID / OTP_RATE_LIMITED),
 *    optionally carrying `retryAfterSeconds` + a `Retry-After` header.
 *  - A legacy ErrorResponse `{ timestamp, status, message, path }`.
 *
 * This module maps both onto a single ApiError and translates technical
 * codes into user-friendly copy. Internal details never leak to the UI.
 */

export class ApiError extends Error {
  readonly status: number
  /** Stable backend code (e.g. "OTP_INVALID"), when present. */
  readonly code?: string
  /** Rate-limit backoff hint in seconds, when the backend exposed one. */
  readonly retryAfterSeconds?: number

  constructor(
    message: string,
    options: { status: number; code?: string; retryAfterSeconds?: number },
  ) {
    super(message)
    this.name = 'ApiError'
    this.status = options.status
    this.code = options.code
    this.retryAfterSeconds = options.retryAfterSeconds
  }
}

interface ParsedBody {
  code?: string
  detail?: string
  message?: string
  title?: string
  retryAfterSeconds?: number
}

function parseBody(text: string): ParsedBody | null {
  if (!text) return null
  try {
    const json = JSON.parse(text) as Record<string, unknown>
    return {
      // RFC 7807 fields
      code: typeof json.code === 'string' ? json.code : undefined,
      detail: typeof json.detail === 'string' ? json.detail : undefined,
      title: typeof json.title === 'string' ? json.title : undefined,
      // Legacy ErrorResponse field
      message: typeof json.message === 'string' ? json.message : undefined,
      retryAfterSeconds:
        typeof json.retryAfterSeconds === 'number' ? json.retryAfterSeconds : undefined,
    }
  } catch {
    return null
  }
}

const FRIENDLY_MESSAGES: Record<string, string> = {
  INVALID_MOBILE_NUMBER: 'This mobile number does not look valid. Please check it and try again.',
  OTP_INVALID: 'The verification code is incorrect. Please try again.',
  OTP_EXPIRED: 'This verification code has expired. Request a new one.',
  OTP_TOO_MANY_ATTEMPTS: 'Too many incorrect attempts. Please request a new code.',
  OTP_RATE_LIMITED: 'You are requesting codes too quickly. Please wait a moment.',
  USER_BLOCKED: 'This account is not allowed to sign in.',
  AUTHENTICATION_FAILED: 'Your session is no longer valid. Please sign in again.',
  INVALID_REFRESH_TOKEN: 'Your session has expired. Please sign in again.',
}

function fallbackMessage(status: number, body: ParsedBody | null): string {
  if (status >= 400 && status < 500 && status !== 429) {
    // Validation / domain rejection: the backend detail/message is already user-safe here.
    return body?.detail ?? body?.message ?? 'The request could not be completed. Please try again.'
  }
  if (status === 429) {
    return 'Too many requests. Please try again later.'
  }
  if (status >= 500) {
    return 'Something went wrong on our side. Please try again in a moment.'
  }
  return 'An unexpected error occurred. Please try again.'
}

/**
 * Build a normalized ApiError from a non-ok fetch Response. Never throws.
 */
export async function toApiError(response: Response): Promise<ApiError> {
  let text = ''
  try {
    text = await response.text()
  } catch {
    /* ignore unreadable bodies */
  }
  const body = parseBody(text)

  let retryAfterSeconds = body?.retryAfterSeconds
  if (retryAfterSeconds === undefined) {
    const header = response.headers.get('Retry-After')
    if (header) {
      const parsed = Number.parseInt(header, 10)
      if (!Number.isNaN(parsed) && parsed > 0) retryAfterSeconds = parsed
    }
  }

  const code = body?.code
  const message = (code && FRIENDLY_MESSAGES[code]) || fallbackMessage(response.status, body)

  return new ApiError(message, {
    status: response.status,
    code,
    retryAfterSeconds,
  })
}

/** Network-level failure (fetch rejected) mapped to friendly copy. */
export function networkError(): ApiError {
  return new ApiError('Cannot reach the Benji servers. Check your connection and try again.', {
    status: 0,
  })
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError
}
