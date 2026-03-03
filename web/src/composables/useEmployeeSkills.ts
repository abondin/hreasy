import { computed, ref, watch } from "vue";
import {
  addSkill as addSkillApi,
  deleteSkill as deleteSkillApi,
  fetchCurrentEmployeeSkills,
  type AddSkillBody,
  type Skill,
  type UpdateRatingBody,
  updateSkillRating as updateSkillRatingApi,
} from "@/services/skills.service";
import { groupSkillsByGroup, type GroupedSkillsItem } from "@/lib/skills";

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

  const groupedSkills = computed<GroupedSkillsItem[]>(() =>
    groupSkillsByGroup(skills.value),
  );

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
