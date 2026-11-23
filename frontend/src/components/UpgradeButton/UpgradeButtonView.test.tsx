import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { UpgradeButtonView } from "./UpgradeButtonView";

function mockEvents() {
  const bodies: unknown[] = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(async (url: string, init?: RequestInit) => {
      if (url === "/api/events") {
        bodies.push(JSON.parse(String(init?.body)));
      }
      return new Response(null, { status: 204 });
    }),
  );
  return () => bodies;
}

describe("UpgradeButtonView", () => {
  it("shows version B", () => {
    mockEvents();
    render(<UpgradeButtonView variant="B" tracked />);

    expect(
      screen.getByRole("button", { name: "Get unlimited polls" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Upgrade" }),
    ).not.toBeInTheDocument();
  });

  it("records that the button was seen", async () => {
    const events = mockEvents();
    render(<UpgradeButtonView variant="B" tracked />);

    await waitFor(() =>
      expect(events()).toEqual([
        { event: "button_seen", experimentKey: "upgrade-button" },
      ]),
    );
  });

  it("records a click", async () => {
    const events = mockEvents();
    const user = userEvent.setup();
    render(<UpgradeButtonView variant="B" tracked />);

    await user.click(
      screen.getByRole("button", { name: "Get unlimited polls" }),
    );

    expect(events()).toContainEqual({
      event: "button_clicked",
      experimentKey: "upgrade-button",
    });
  });

  it("can be clicked with the keyboard", async () => {
    const events = mockEvents();
    const user = userEvent.setup();
    render(<UpgradeButtonView variant="A" tracked />);

    await user.tab();
    expect(screen.getByRole("button", { name: "Upgrade" })).toHaveFocus();
    await user.keyboard("{Enter}");

    expect(events()).toContainEqual({
      event: "button_clicked",
      experimentKey: "upgrade-button",
    });
  });

  it("records nothing for a visitor without an assignment", async () => {
    const events = mockEvents();
    const user = userEvent.setup();
    render(<UpgradeButtonView variant="A" tracked={false} />);

    await user.click(screen.getByRole("button", { name: "Upgrade" }));

    expect(events()).toEqual([]);
  });
});
