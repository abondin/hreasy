import type { Skill, SkillGroup } from "@/services/skills.service";

export interface GroupedSkillsItem {
  group: SkillGroup;
  skills: Skill[];
}

function sortByName(lhs: string, rhs: string): number {
  return lhs.localeCompare(rhs, "ru", { sensitivity: "base" });
}

export function groupSkillsByGroup(skills: Skill[]): GroupedSkillsItem[] {
  const groups = new Map<number, GroupedSkillsItem>();

  skills.forEach((skill) => {
    const key = skill.group.id;
    const entry = groups.get(key);
    if (entry) {
      entry.skills.push(skill);
    } else {
      groups.set(key, {
        group: skill.group,
        skills: [skill],
      });
    }
  });

  return Array.from(groups.values())
    .map((item) => ({
      group: item.group,
      skills: [...item.skills].sort((a, b) => sortByName(a.name, b.name)),
    }))
    .sort((a, b) => sortByName(a.group.name, b.group.name));
}
