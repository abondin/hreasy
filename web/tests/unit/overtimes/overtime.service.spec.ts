import { beforeEach, describe, expect, it, vi } from "vitest";
import { addOvertimeItem } from "@/services/overtime.service";
import http from "@/lib/http";

vi.mock("@/lib/http", () => ({
  default: {
    post: vi.fn(),
  },
}));

describe("overtime service", () => {
  beforeEach(() => {
    vi.mocked(http.post).mockReset();
    vi.mocked(http.post).mockResolvedValue({
      data: {
        employeeId: 42,
        period: 202604,
        items: [],
        approvals: [],
        lastUpdate: null,
      },
    });
  });

  it("sends only new overtime item fields when creating item", async () => {
    await addOvertimeItem(42, 202604, {
      id: 10,
      date: "2026-06-04",
      projectId: 1162,
      hours: 4,
      notes: "Release support",
      createdAt: "2026-06-04T14:17:18.537Z",
      deletedAt: null,
    });

    expect(http.post).toHaveBeenCalledWith("v1/overtimes/42/report/202604/item", {
      date: "2026-06-04",
      projectId: 1162,
      hours: 4,
      notes: "Release support",
    });
  });
});
