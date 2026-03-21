import http from "@/lib/http";
import type { Dict } from "@/services/employee.service";

export interface SkillGroup extends Dict {
  active?: boolean;
}

export interface Ratings {
  averageRating?: number;
  ratingsCount?: number;
  myRating?: number;
}

export interface Skill {
  id: number;
  group: SkillGroup;
  name: string;
  ratings: Ratings;
}

export interface SharedSkillName {
  groupId: number;
  name: string;
}

export interface SkillRatingBody {
  rating: number;
  notes: string;
}

export interface AddSkillBody {
  groupId: number;
  name: string;
  rating: SkillRatingBody | null;
}

export interface UpdateRatingBody {
  rating: number;
}

export async function fetchCurrentEmployeeSkills(): Promise<Skill[]> {
  const response = await http.get<Skill[]>("v1/employee/skills");
  return response.data;
}

export async function addSkill(
  employeeId: number,
  payload: AddSkillBody,
): Promise<Skill> {
  const response = await http.post<Skill>(
    `v1/employee/skills/${employeeId}`,
    payload,
  );
  return response.data;
}

export async function updateSkillRating(
  skillId: number,
  payload: UpdateRatingBody,
): Promise<Skill> {
  const response = await http.put<Skill>(
    `v1/employee/skills/${skillId}/rating`,
    payload,
  );
  return response.data;
}

export async function deleteSkill(
  employeeId: number,
  skillId: number,
): Promise<void> {
  await http.delete(`v1/employee/skills/${employeeId}/${skillId}`);
}

export async function fetchSkillGroups(): Promise<SkillGroup[]> {
  const response = await http.get<SkillGroup[]>(
    "/v1/employee/skills/groups",
  );
  return response.data;
}

export async function fetchSharedSkillNames(): Promise<SharedSkillName[]> {
  const response = await http.get<SharedSkillName[]>(
    "/v1/employee/skills/shared/names",
  );
  return response.data;
}
