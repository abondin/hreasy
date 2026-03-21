import { describe, expect, it } from "vitest";
import {
  createDefaultJuniorReportForm,
  fillJuniorReportForm,
  getJuniorProgressIcon,
} from "@/lib/mentorship";
import { JuniorProgressType } from "@/services/junior-registry.service";

describe("mentorship helpers", () => {
  it("returns stable icon mapping for progress types", () => {
    expect(getJuniorProgressIcon(JuniorProgressType.DEGRADATION)).toEqual({
      icon: "mdi-arrow-bottom-left",
      color: "error",
    });
    expect(getJuniorProgressIcon(JuniorProgressType.GOOD_PROGRESS)).toEqual({
      icon: "mdi-arrow-up-bold",
      color: "success",
    });
  });

  it("creates default report form with neutral values", () => {
    expect(createDefaultJuniorReportForm()).toEqual({
      progress: JuniorProgressType.NO_PROGRESS,
      comment: "",
      ratings: {
        overallReadiness: 3,
        competence: 3,
        process: 3,
        teamwork: 3,
        contribution: 3,
        motivation: 3,
      },
    });
  });

  it("fills report form from source report", () => {
    const form = createDefaultJuniorReportForm();

    fillJuniorReportForm(form, {
      id: 1,
      progress: JuniorProgressType.PROGRESS,
      createdAt: "2025-01-01T00:00:00Z",
      createdBy: { id: 7, name: "Mentor" },
      comment: "steady growth",
      ratings: {
        overallReadiness: 4,
        competence: 5,
        process: 3,
        teamwork: 4,
        contribution: 5,
        motivation: 4,
      },
    });

    expect(form).toEqual({
      progress: JuniorProgressType.PROGRESS,
      comment: "steady growth",
      ratings: {
        overallReadiness: 4,
        competence: 5,
        process: 3,
        teamwork: 4,
        contribution: 5,
        motivation: 4,
      },
    });
  });
});
