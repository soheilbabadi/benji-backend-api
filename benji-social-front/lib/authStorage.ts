"use client";

/**
 * Client-side storage helpers for the JWT session issued by
 * POST /api/auth/otp/verify (accessToken + refreshToken + userId).
 */

const ACCESS_TOKEN_KEY = "benji.accessToken";
const REFRESH_TOKEN_KEY = "benji.refreshToken";
const USER_ID_KEY = "benji.userId";

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

export interface StoredSession {
  accessToken: string | null;
  refreshToken: string | null;
  userId: string | null;
}

export function saveSession(session: {
  accessToken: string;
  refreshToken: string;
  userId: string;
  expiresInSeconds?: number;
}): void {
  if (!isBrowser()) return;
  localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken);
  localStorage.setItem(USER_ID_KEY, session.userId);
}

/**
 * Store only the tokens (no user id) — used by the refresh flow when the
 * backend response omits userId.
 */
export function saveTokens(tokens: {
  accessToken: string;
  refreshToken: string;
}): void {
  if (!isBrowser()) return;
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function loadSession(): StoredSession {
  if (!isBrowser()) {
    return { accessToken: null, refreshToken: null, userId: null };
  }
  return {
    accessToken: localStorage.getItem(ACCESS_TOKEN_KEY),
    refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
    userId: localStorage.getItem(USER_ID_KEY),
  };
}

export function getAccessToken(): string | null {
  return isBrowser() ? localStorage.getItem(ACCESS_TOKEN_KEY) : null;
}

export function clearSession(): void {
  if (!isBrowser()) return;
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_ID_KEY);
}

export function isAuthenticated(): boolean {
  return getAccessToken() != null;
}
