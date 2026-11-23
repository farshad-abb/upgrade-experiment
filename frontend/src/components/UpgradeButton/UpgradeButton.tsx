import { cookies } from "next/headers";
import { unstable_rethrow } from "next/navigation";
import { UPGRADE_BUTTON, type Variant } from "@/lib/event";
import { fetchAssignments } from "@/lib/serverApi";
import { isVisitorId, VISITOR_COOKIE } from "@/lib/visitorCookie";
import { UpgradeButtonView } from "./UpgradeButtonView";

/**
 * Server component: picks the visitor's variant while rendering, so the right label is in the first HTML.
 * Reads a cookie, so it renders per request; wrap it in <Suspense> to keep the rest of the page static.
 */
export async function UpgradeButton() {
  const visitorId = (await cookies()).get(VISITOR_COOKIE)?.value;
  const variant = isVisitorId(visitorId) ? await getVariant(visitorId) : null;

  // Without a variant we still show the default version, but record nothing:
  // a visitor without an assignment must not end up in the results.
  return (
    <UpgradeButtonView variant={variant ?? "A"} tracked={variant !== null} />
  );
}

async function getVariant(visitorId: string): Promise<Variant | null> {
  try {
    const assignments = await fetchAssignments(visitorId);
    return assignments[UPGRADE_BUTTON] ?? null;
  } catch (error) {
    // Next.js aborts in-flight fetches when a prerender finishes; that is not a backend failure.
    unstable_rethrow(error);
    // should be logged in  production inside a logging system for further investigation
    console.warn("Could not load the experiment variant", error);
    return null;
  }
}

/** Keeps the button's space while it streams in, to prevent layout shifts. */
export function UpgradeButtonPlaceholder() {
  return <span className="inline-block min-h-11 min-w-30" aria-hidden="true" />;
}
