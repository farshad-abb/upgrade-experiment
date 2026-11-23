import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { UpgradeButtonView } from "./UpgradeButtonView";

describe("UpgradeButtonView", () => {
  it("shows version B", () => {
    render(<UpgradeButtonView variant="B" />);

    expect(
      screen.getByRole("button", { name: "Get unlimited polls" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Upgrade" }),
    ).not.toBeInTheDocument();
  });
});
