/**
 * Central service configuration for the Benji frontend.
 *
 * The backend API base URL is read from NEXT_PUBLIC_API_BASE_URL
 * (see .env.local). It defaults to the local Spring Boot service at
 * http://localhost:8080 so the app works out of the box in development.
 */
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

/** Build an absolute API URL, e.g. apiPath("/api/admin/users/roles"). */
export function apiPath(path: string): string {
  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}
