import type { Variant } from "@/lib/event";

/** Version B changes only the text */
export const UPGRADE_LABELS: Record<Variant, string> = {
  A: "Upgrade",
  B: "Get unlimited polls",
};

type Props = {
  variant: Variant;
};

/** The button as the visitor sees it. The variant is chosen on the server and arrives in the HTML. */
export function UpgradeButtonView({ variant }: Props) {
  return (
    <button
      type="button"
      className="inline-block min-h-11 min-w-30 cursor-pointer rounded-full bg-black px-5 py-2.5 font-semibold whitespace-nowrap text-white hover:bg-gray-800 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-amber-500"
      data-variant={variant}
    >
      {UPGRADE_LABELS[variant]}
    </button>
  );
}
