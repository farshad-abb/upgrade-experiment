export type Variant = "A" | "B";

// this is actually the experiment key
// the experiment key is used to identify the experiment in the backend and in the tracking events
export const UPGRADE_BUTTON = "upgrade-button";

export type Assignments = Record<string, Variant>;
