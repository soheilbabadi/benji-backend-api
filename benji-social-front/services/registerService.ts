import { apiPath } from "@/lib/config";
import { saveSession } from "@/lib/authStorage";

/**
 * Service for the new user-management registration flow
 * (mobile-number + OTP, backed by OtpAuthController):
 *
 *   POST /api/auth/otp         -> request an SMS one-time code
 *   POST /api/auth/otp/verify  -> verify the code; first use creates the
 *                                 account and returns JWT tokens, which are
 *                                 persisted to localStorage via saveSession()
 */

export interface OtpSession {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  userId: string;
}

/** Ask the backend to send an OTP via SMS (Kavenegar). Resolves on HTTP 202. */
export async function requestOtp(mobile: string): Promise<void> {
  const res = await fetch(apiPath("/api/auth/otp"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ mobile }),
  });

  if (!res.ok) {
    throw new Error(await extractErrorMessage(res, "Failed to send the code."));
  }
}

/** Verify the OTP. On success the account is created (if new) and returned with tokens. */
export async function verifyOtp(
  mobile: string,
  code: string,
): Promise<OtpSession> {
  const res = await fetch(apiPath("/api/auth/otp/verify"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ mobile, code }),
  });

  if (!res.ok) {
    throw new Error(await extractErrorMessage(res, "Invalid or expired code."));
  }

  const data = (await res.json()) as Partial<OtpSession>;
  if (!data.accessToken || !data.refreshToken) {
    throw new Error("Unexpected response from the server.");
  }
  const session: OtpSession = {
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    expiresInSeconds: data.expiresInSeconds ?? 0,
    userId: String(data.userId ?? ""),
  };
  // Persist the tokens to localStorage so the user stays signed in.
  saveSession(session);
  return session;
}

async function extractErrorMessage(
  res: Response,
  fallback: string,
): Promise<string> {
  try {
    const body = await res.json();
    if (typeof body?.message === "string" && body.message) {
      return body.message;
    }
    if (typeof body?.error === "string" && body.error) {
      return body.error;
    }
  } catch {
    /* non-JSON body — fall through */
  }
  return res.status === 429
    ? "Too many requests. Please wait before trying again."
    : fallback;
}
