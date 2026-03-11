<!--
  Employee details drawer panel.
-->
<template>
  <div v-if="employee" class="employee-panel d-flex flex-column ga-3">
    <div class="employee-panel__block">
      <profile-summary-card
        :employee="employee"
        :read-only="true"
        :avatar-read-only="false"
        :project-read-only="false"
        @avatar-updated="emit('employee-updated')"
        @update-project="emit('employee-updated')"
      />
    </div>

    <v-card class="employee-panel__block">
      <v-card-text class="pa-6">
        <div class="text-body-2 font-weight-medium mb-2">
          {{ t("Текущие и планируемые отпуска") }}
        </div>
        <v-progress-circular
          v-if="vacationsLoading"
          indeterminate
          size="18"
          width="2"
        />
        <div v-else-if="employeeVacations.length" class="d-flex flex-wrap align-center ga-1">
          <v-chip
            v-for="vacation in employeeVacations"
            :key="vacation.id"
            size="small"
            density="compact"
            :color="vacation.current ? 'primary' : undefined"
          >
            {{ formatDate(vacation.startDate) }} -
            {{ formatDate(vacation.endDate) }}
          </v-chip>
        </div>
        <div v-else class="text-body-2 text-medium-emphasis">
          {{ t("Отсутствуют данные") }}
        </div>
      </v-card-text>
    </v-card>

    <v-card v-if="canViewTechProfiles" class="employee-panel__block">
      <v-card-text class="pa-6">
        <div class="text-body-2 font-weight-medium mb-2">
          {{ t("Квалификационные карточки") }}
        </div>
        <profile-tech-profiles-card
          :employee-id="employee.id"
          :with-card="false"
          :show-title="false"
          :dense="true"
        />
      </v-card-text>
    </v-card>

    <v-card v-if="canViewSkills" class="employee-panel__block">
      <v-card-text class="pa-6">
        <div class="text-body-2 font-weight-medium mb-2">
          {{ t("Навыки") }}
        </div>
        <employee-skills-section
          :grouped-skills="groupedSkills"
          :loading="skillsSectionLoading"
          :error="skillsSectionError"
          :can-edit="canEditSkills"
          :can-add="canAddSkills"
          :can-delete="canDeleteSkills"
          :can-rate="canRateSkills"
          :dense="true"
          :submit-skill="submitNewSkill"
          :rate-skill="handleRateSkill"
          :delete-skill="confirmDeleteSkill"
          @deleted="handleSkillDeleted"
        />
      </v-card-text>
    </v-card>

  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Employee } from "@/services/employee.service";
import ProfileSummaryCard from "@/views/profile/components/ProfileSummaryCard.vue";
import ProfileTechProfilesCard from "@/views/profile/components/ProfileTechProfilesCard.vue";
import EmployeeSkillsSection from "@/components/skills/EmployeeSkillsSection.vue";
import { usePermissions } from "@/lib/permissions";
import { useEmployeeSkillPermissions } from "@/composables/useEmployeeSkillPermissions";
import { formatDate } from "@/lib/datetime";
import {
  fetchCurrentOrFutureVacations,
  type EmployeeVacationShort,
} from "@/services/vacation.service";
import {
  addSkill,
  deleteSkill,
  updateSkillRating,
  type AddSkillBody,
  type Skill,
} from "@/services/skills.service";
import { groupSkillsByGroup, type GroupedSkillsItem } from "@/lib/skills";

const props = defineProps<{
  employee: Employee;
}>();

const emit = defineEmits<{
  (event: "employee-updated"): void;
}>();

const { t } = useI18n();
const permissions = usePermissions();

const vacationsLoading = ref(false);
const employeeVacations = ref<EmployeeVacationShort[]>([]);

const skills = ref<Skill[]>([]);
const skillsLoading = ref(false);
const skillsActionError = ref<unknown>(null);

const canViewTechProfiles = computed(() =>
  permissions.canDownloadTechProfiles(props.employee.id),
);
const {
  canViewSkills,
  canEditSkills,
  canAddSkills,
  canDeleteSkills,
  canRateSkills,
} = useEmployeeSkillPermissions(() => props.employee.id);

const skillsSectionLoading = computed(() => skillsLoading.value);
const skillsSectionError = computed(() => skillsActionError.value ?? null);

const groupedSkills = computed<GroupedSkillsItem[]>(() =>
  groupSkillsByGroup(skills.value),
);

watch(
  () => props.employee.id,
  (id) => {
    if (typeof id !== "number") {
      employeeVacations.value = [];
      vacationsLoading.value = false;
      skills.value = [];
      skillsActionError.value = null;
      return;
    }
    vacationsLoading.value = true;
    fetchCurrentOrFutureVacations(id)
      .then((items) => {
        employeeVacations.value = items;
      })
      .catch(() => {
        employeeVacations.value = [];
      })
      .finally(() => {
        vacationsLoading.value = false;
      });
  },
  { immediate: true },
);

watch(
  () => props.employee.skills,
  (next) => {
    skills.value = next ? [...next] : [];
  },
  { immediate: true },
);

async function submitNewSkill(payload: AddSkillBody) {
  skillsActionError.value = null;
  skillsLoading.value = true;
  try {
    const created = await addSkill(props.employee.id, payload);
    skills.value = [...skills.value, created];
    return created;
  } catch (error) {
    skillsActionError.value = error;
    throw error;
  } finally {
    skillsLoading.value = false;
  }
}

async function handleRateSkill({
  skill,
  rating,
}: {
  skill: Skill;
  rating: number;
}) {
  skillsActionError.value = null;
  skillsLoading.value = true;
  try {
    const updated = await updateSkillRating(skill.id, { rating });
    skills.value = skills.value.map((item) =>
      item.id === updated.id ? updated : item,
    );
  } catch (error) {
    skillsActionError.value = error;
  } finally {
    skillsLoading.value = false;
  }
}

async function confirmDeleteSkill(skill: Skill) {
  skillsLoading.value = true;
  try {
    await deleteSkill(props.employee.id, skill.id);
  } finally {
    skillsLoading.value = false;
  }
}

function handleSkillDeleted(skill: Skill) {
  skills.value = skills.value.filter((item) => item.id !== skill.id);
}
</script>

<style scoped>
.employee-panel {
  padding: 12px;
}
</style>
