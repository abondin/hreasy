import {
  JuniorProgressType,
  type AddOrUpdateJuniorReportBody,
  type JuniorReport,
} from "@/services/junior-registry.service";

export const juniorReportRatingFields: (keyof AddOrUpdateJuniorReportBody["ratings"])[] = [
  "overallReadiness",
  "competence",
  "process",
  "teamwork",
  "contribution",
  "motivation",
];

export function getJuniorProgressIcon(
  type: JuniorProgressType,
): { icon: string; color: string } {
  switch (type) {
    case JuniorProgressType.DEGRADATION:
      return { icon: "mdi-arrow-bottom-left", color: "error" };
    case JuniorProgressType.NO_PROGRESS:
      return { icon: "mdi-minus", color: "" };
    case JuniorProgressType.PROGRESS:
      return { icon: "mdi-arrow-top-right", color: "success" };
    case JuniorProgressType.GOOD_PROGRESS:
      return { icon: "mdi-arrow-up-bold", color: "success" };
    default:
      return { icon: "mdi-help", color: "warning" };
  }
}

export function createDefaultJuniorReportForm(): AddOrUpdateJuniorReportBody {
  return {
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
  };
}

export function fillJuniorReportForm(
  target: AddOrUpdateJuniorReportBody,
  source?: JuniorReport | null,
): void {
  const fallback = createDefaultJuniorReportForm();
  target.progress = source?.progress ?? fallback.progress;
  target.comment = source?.comment ?? fallback.comment;
  target.ratings.overallReadiness =
    source?.ratings.overallReadiness ?? fallback.ratings.overallReadiness;
  target.ratings.competence =
    source?.ratings.competence ?? fallback.ratings.competence;
  target.ratings.process =
    source?.ratings.process ?? fallback.ratings.process;
  target.ratings.teamwork =
    source?.ratings.teamwork ?? fallback.ratings.teamwork;
  target.ratings.contribution =
    source?.ratings.contribution ?? fallback.ratings.contribution;
  target.ratings.motivation =
    source?.ratings.motivation ?? fallback.ratings.motivation;
}
