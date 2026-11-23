export type Variant = "A" | "B";

// this is actually the experiment key
// the experiment key is used to identify the experiment in the backend and in the tracking events
export const UPGRADE_BUTTON = "upgrade-button";

export type Assignments = Record<string, Variant>;

export type EventName = "button_seen" | "button_clicked";

/**
 * Records an event for the visitor in the visitor_id cookie, which the browser sends on its own.
 * Fire-and-forget: tracking must never break the page.
 * keepalive lets the request finish even if the user navigates away right after clicking.
 */
export function trackEvent(event: EventName, experimentKey: string): void {
  fetch("/api/events", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ event, experimentKey }),
    keepalive: true,
  }).catch((error: unknown) =>
    console.warn(`Could not record ${event}`, error),
  );
}
