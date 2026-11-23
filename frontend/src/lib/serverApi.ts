import "server-only";

/**
 * Server-side only: these calls go to the backend directly, not through the /api proxy.
 */

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export type Poll = {
  id: string;
  title: string;
  /** Creation time, Unix milliseconds. */
  initiated: number;
};

async function getJson<T>(
  path: string,
  label: string,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(`${BACKEND_URL}${path}`, init);
  if (!response.ok) {
    throw new Error(`${label} request failed with ${response.status}`);
  }
  return response.json();
}

export function fetchPolls(): Promise<Poll[]> {
  return getJson("/api/polls", "Polls");
}
