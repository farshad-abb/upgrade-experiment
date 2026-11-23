/** Keep in sync with VisitorIds.COOKIE in the backend. */
export const VISITOR_COOKIE = "visitor_id";

/** 400 days: the longest lifetime Chrome accepts for a cookie. */
export const VISITOR_COOKIE_MAX_AGE = 400 * 24 * 60 * 60;

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function isVisitorId(value: string | undefined): value is string {
  return value !== undefined && UUID_PATTERN.test(value);
}
