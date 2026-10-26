import { apiPath } from "@/lib/config";
import { loadSession, saveSession, clearSession } from "@/lib/authStorage";

/**
 * Service for the login cycle against the Spring backend
 * (OtpAuthController under /api/auth/otp):
 *
 *   POST /api/auth/otp          -> send the SMS code (same endpoint as register)
 *   POST /api/auth/otp/verify   -> exchange code for JWT tokens
 *   POST /api/auth/otp/refresh  -> renew an expired access token
 *   POST /api/auth/otp/logout   -> revoke the current refresh token
 */

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  userId: string;
}

/** Request an OTP by SMS for an existing account's mobile number. */
export async function requestLoginOtp(mobile: string): Promise<void> {
  const res = await fetch(apiPath("/api/auth/otp"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ mobile }),
  });

  if (!res.ok) {
    throw new Error(
      await extractErrorMessage(res, "Could not send a code to this number."),
    );
  }
}

/** Verify the code, persist the session, and return the tokens. */
export async function loginWithOtp(
  mobile: string,
  code: string,
): Promise<AuthTokens> {
  const res = await fetch(apiPath("/api/auth/otp/verify"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ mobile, code }),
  });

  if (!res.ok) {
    throw new Error(await extractErrorMessage(res, "Invalid or expired code."));
  }

  const data = (await res.json()) as Partial<AuthTokens>;
  if (!data.accessToken || !data.refreshToken) {
    throw new Error("Unexpected response from the server.");
  }

  const tokens: AuthTokens = {
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    expiresInSeconds: data.expiresInSeconds ?? 0,
    userId: String(data.userId ?? ""),
  };
  saveSession(tokens);
  return tokens;
}

/** Exchange the stored refresh token for a fresh access token. */
export async function refreshSession(): Promise<AuthTokens | null> {
  const { refreshToken } = loadSession();
  if (!refreshToken) return null;

  const res = await fetch(apiPath("/api/auth/otp/refresh"), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (!res.ok) {
    clearSession();
    return null;
  }

  const data = (await res.json()) as Partial<AuthTokens>;
  if (!data.accessToken || !data.refreshToken) {
    clearSession();
    return null;
  }

  const tokens: AuthTokens = {
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    expiresInSeconds: data.expiresInSeconds ?? 0,
    userId: String(data.userId ?? ""),
  };
  saveSession(tokens);
  return tokens;
}

/** Revoke the current refresh token server-side and clear the local session. */
export async function logout(): Promise<void> {
  const { refreshToken } = loadSession();
  try {
    if (refreshToken) {
      await fetch(apiPath("/api/auth/otp/logout"), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });
    }
  } finally {
    clearSession();
  }
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
