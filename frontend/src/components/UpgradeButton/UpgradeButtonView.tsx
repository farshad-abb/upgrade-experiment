"use client";

import { useEffect } from "react";
import { trackEvent, UPGRADE_BUTTON, type Variant } from "@/lib/event";

/** Version B changes only the text */
export const UPGRADE_LABELS: Record<Variant, string> = {
  A: "Upgrade",
  B: "Get unlimited polls",
};

type Props = {
  variant: Variant;
  /** tracked is False when the visitor has no assignment (e.g. backend down): show the button, dont send tracking events. */
  tracked: boolean;
};

/** The button as the visitor sees it. The variant is chosen on the server and arrives in the HTML. */
export function UpgradeButtonView({ variant, tracked }: Props) {
  // "Seen" = the button is on screen in a real browser. Sent after hydration, not during server rendering,
  // so prefetches and bots that never run JavaScript are not counted.
  // Repeats (reloads, repeat clicks, StrictMode in dev) are harmless: the backend stores each event once per visitor.
  useEffect(() => {
    if (tracked) {
      trackEvent("button_seen", UPGRADE_BUTTON);
    }
  }, [tracked]);

  return (
    <button
      type="button"
      className="inline-block min-h-11 min-w-30 cursor-pointer rounded-full bg-black px-5 py-2.5 font-semibold whitespace-nowrap text-white hover:bg-gray-800 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-amber-500"
      data-variant={variant}
      onClick={() => {
        if (tracked) {
          trackEvent("button_clicked", UPGRADE_BUTTON);
        }
      }}
    >
      {UPGRADE_LABELS[variant]}
    </button>
  );
}
