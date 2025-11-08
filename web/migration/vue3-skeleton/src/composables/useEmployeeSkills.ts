import { computed, ref, watch } from "vue";
import {
  addSkill as addSkillApi,
  deleteSkill as deleteSkillApi,
  fetchCurrentEmployeeSkills,
  type AddSkillBody,
  type Skill,
  type SkillGroup,
  type UpdateRatingBody,
  updateSkillRating as updateSkillRatingApi,
} from "@/services/skills.service";

export interface GroupedSkillsItem {
  group: SkillGroup;
  skills: Skill[];
}

function sortByName(lhs: string, rhs: string): number {
  return lhs.localeCompare(rhs, "ru", { sensitivity: "base" });
}

export function useEmployeeSkills(
  employeeIdGetter: () => number | null | undefined,
) {
  const skills = ref<Skill[]>([]);
  const loading = ref(false);
  const error = ref<unknown>(null);

  async function loadSkills(id: number | null | undefined) {
    if (typeof id !== "number") {
      skills.value = [];
      return;
    }
    loading.value = true;
    error.value = null;
    try {
      const response = await fetchCurrentEmployeeSkills();
      skills.value = response;
    } catch (err) {
      error.value = err;
      skills.value = [];
    } finally {
      loading.value = false;
    }
  }

  async function reload() {
    await loadSkills(employeeIdGetter());
  }

  async function addSkill(payload: AddSkillBody) {
    const employeeId = employeeIdGetter();
    if (typeof employeeId !== "number") {
      throw new Error("Employee ID is not available");
    }
    const created = await addSkillApi(employeeId, payload);
    skills.value = [...skills.value, created];
    return created;
  }

  async function updateSkillRating(
    skillId: number,
    payload: UpdateRatingBody,
  ) {
    const updated = await updateSkillRatingApi(skillId, payload);
    skills.value = skills.value.map((skill) =>
      skill.id === updated.id ? updated : skill,
    );
    return updated;
  }

  async function deleteSkill(skillId: number) {
    const employeeId = employeeIdGetter();
    if (typeof employeeId !== "number") {
      throw new Error("Employee ID is not available");
    }
    await deleteSkillApi(employeeId, skillId);
    skills.value = skills.value.filter((skill) => skill.id !== skillId);
  }

  watch(
    employeeIdGetter,
    (id) => {
      void loadSkills(id);
    },
    { immediate: true },
  );

  const groupedSkills = computed<GroupedSkillsItem[]>(() => {
    const groups = new Map<number, GroupedSkillsItem>();
    skills.value.forEach((skill) => {
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
  });

  return {
    skills: computed(() => skills.value),
    groupedSkills,
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    addSkill,
    updateSkillRating,
    deleteSkill,
    reload,
  };
}
