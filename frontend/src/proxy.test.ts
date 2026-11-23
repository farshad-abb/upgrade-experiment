// @vitest-environment node
import { NextRequest } from "next/server";
import { describe, expect, it } from "vitest";
import { proxy } from "./proxy";

const VISITOR_ID = "3f2b8c1e-9a4d-4e5f-8b6a-1c2d3e4f5a6b";
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

function request(url = "http://localhost:3000/", headers: HeadersInit = {}) {
  return new NextRequest(url, { headers });
}

/** NextResponse.next({ request }) passes the changed request cookies to the page in this header. */
function forwardedCookieHeader(response: Response) {
  return response.headers.get("x-middleware-request-cookie");
}

describe("proxy", () => {
  it("gives a new visitor an id in a cookie", () => {
    const response = proxy(request());

    const cookie = response.cookies.get("visitor_id");
    expect(cookie?.value).toMatch(UUID);
    expect(cookie).toMatchObject({
      httpOnly: true,
      sameSite: "lax",
      path: "/",
      maxAge: 400 * 24 * 60 * 60,
    });
  });

  it("passes the new id to the page being rendered, before the browser has stored it", () => {
    const response = proxy(request());

    const id = response.cookies.get("visitor_id")?.value;
    expect(forwardedCookieHeader(response)).toContain(`visitor_id=${id}`);
  });

  it("keeps the other cookies of the request", () => {
    const response = proxy(request(undefined, { cookie: "theme=dark" }));

    expect(forwardedCookieHeader(response)).toContain("theme=dark");
  });

  it("leaves a visitor with a valid id alone", () => {
    const response = proxy(
      request(undefined, { cookie: `visitor_id=${VISITOR_ID}` }),
    );

    expect(response.cookies.get("visitor_id")).toBeUndefined();
    expect(forwardedCookieHeader(response)).toBeNull();
  });

  it("replaces an id that is not a UUID", () => {
    const response = proxy(request(undefined, { cookie: "visitor_id=garbage" }));

    expect(response.cookies.get("visitor_id")?.value).toMatch(UUID);
  });
});
