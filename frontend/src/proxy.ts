import { NextResponse, type NextRequest } from "next/server";
import {
  isVisitorId,
  VISITOR_COOKIE,
  VISITOR_COOKIE_MAX_AGE,
} from "@/lib/visitorCookie";

/**
 * Gives every visitor an visitorId cookie before the page renders, so server
 * components can pick the visitor's variant. Cookie based A/B testing is seleted for this project.
 */
export function proxy(request: NextRequest) {
  const existing = request.cookies.get(VISITOR_COOKIE)?.value;
  if (isVisitorId(existing)) {
    return NextResponse.next();
  }

  const visitorId = crypto.randomUUID();

  request.cookies.set(VISITOR_COOKIE, visitorId);
  const response = NextResponse.next({ request: { headers: request.headers } });

  response.cookies.set(VISITOR_COOKIE, visitorId, {
    httpOnly: true, // page scripts never need it, so it could be httpOnly
    sameSite: "lax", // not sent on cross-site POSTs, which blocks CSRF on /api/events
    path: "/",
    maxAge: VISITOR_COOKIE_MAX_AGE,
  });
  return response;
}

export const config = {
  // Every page, so new pages (e.g. /upgrade) get a visitor id without changes here.
  // API calls, static files and images never need a new id.
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
};
