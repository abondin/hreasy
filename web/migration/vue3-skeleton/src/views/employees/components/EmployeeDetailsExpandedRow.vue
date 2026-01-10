<!--
  Inline employee details card for table expansion.
  Mirrors the legacy EmployeeCard behaviour where possible.
-->
<template>
  <v-card flat class="pa-4" v-if="employee">
    <v-row no-gutters align="start" class="ga-2">
      <v-col cols="auto">
        <profile-avatar :owner="employee" :read-only="false" />
      </v-col>
      <v-col>
        <div class="text-subtitle-1 font-weight-medium mb-1">
          {{ employee.displayName }}
        </div>
        <div v-if="employee.position" class="text-body-2 text-medium-emphasis">
          {{ employee.position.name }}
        </div>
        <div v-if="employee.officeLocation" class="text-body-2 text-medium-emphasis">
          {{ employee.officeLocation.name }}
          <v-btn
            v-if="employee.officeLocation.mapName"
            icon="mdi-map"
            size="x-small"
            variant="text"
            density="compact"
            class="ml-1"
            @click.stop="openMap"
          />
        </div>
        <div v-if="employee.telegram" class="text-body-2 d-flex align-center">
          <a
            :href="t('telegram_url', { account: employee.telegram })"
            target="_blank"
            rel="noopener"
            class="d-inline-flex align-center text-decoration-none text-info"
          >
            <v-icon size="small" class="mr-1" color="info" icon="fa:fab fa-telegram" />
            {{ employee.telegram }}
          </a>
        </div>

        <div class="d-flex flex-wrap align-center ga-1 text-body-2 text-medium-emphasis mt-1">
          <span class="font-weight-medium">
            {{ t("Текущий проект") }}:
          </span>
          <span class="d-inline-flex align-center ga-1">
            <v-btn
              v-if="canShowProjectInfo"
              icon="mdi-information"
              color="info"
              variant="text"
              density="compact"
              size="small"
              :title="t('Подробная информация по проекту ')"
              @click.stop="openProjectInfo"
            />
            <span>
              {{ employee.currentProject?.name ?? t("Проект не задан") }}
            </span>
            <span v-if="employee.ba" class="ml-1">
              ({{ employee.ba.name }})
            </span>
            <span v-if="employee.currentProject?.role">
              - {{ employee.currentProject.role }}
            </span>
            <v-btn
              v-if="canEditProject"
              icon="mdi-pencil"
              variant="text"
              density="compact"
              size="small"
              class="ml-1"
              :title="t('Обновление текущего проекта')"
              @click.stop="openProjectUpdate"
            />
          </span>
        </div>

        <div class="d-flex flex-wrap align-center ga-1 text-body-2 text-medium-emphasis mt-1">
          <span class="font-weight-medium">
            {{ t("Текущие и планируемые отпуска") }}:
          </span>
          <v-progress-circular
            v-if="vacationsLoading"
            indeterminate
            size="18"
            width="2"
            class="ml-2"
          />
          <div
            v-else-if="employeeVacations.length"
            class="d-flex flex-wrap align-center ga-1"
          >
            <v-chip
              v-for="vacation in employeeVacations"
              :key="vacation.id"
              size="small"
              density="compact"
              class="mr-1 mb-1"
              :color="vacation.current ? 'primary' : undefined"
            >
              {{ formatDate(vacation.startDate) }} -
              {{ formatDate(vacation.endDate) }}
            </v-chip>
          </div>
        </div>

        <div v-if="canViewTechProfiles" class="mt-2">
          <div class="d-flex flex-wrap align-center ga-1 text-body-2 text-medium-emphasis mt-1">
            <span class="font-weight-medium">
              {{ t("Квалификационные карточки") }}:
            </span>
          </div>
          <profile-tech-profiles-card
            :employee-id="employee.id"
            :with-card="false"
            :show-title="false"
            :dense="true"
          />
        </div>

        <div v-if="canViewSkills" class="mt-2">
          <div class="d-flex flex-wrap align-center ga-1 text-body-2 text-medium-emphasis mt-1">
            <span class="font-weight-medium">
              {{ t("Навыки") }}:
            </span>
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
        </div>
      </v-col>
    </v-row>
  </v-card>

  <office-map-preview-dialog
    v-model="mapDialogOpen"
    :map-name="mapName"
    :map-title="mapTitle"
    :workplace="highlightedWorkplace"
  />
  <project-info-dialog
    v-model="projectInfoDialogOpen"
    :project-id="employee.currentProject?.id ?? null"
    :employee-id="employee.id ?? null"
  />
  <project-assignment-dialog
    v-model="projectUpdateDialogOpen"
    :employee-id="employee.id ?? null"
    :employee-name="employee.displayName"
    :current-project="employee.currentProject ?? null"
    @updated="handleProjectUpdated"
  />
</template>

<script setup lang="ts">
import { computed, ref, toRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Employee } from "@/services/employee.service";
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
import ProfileTechProfilesCard from "@/views/profile/components/ProfileTechProfilesCard.vue";
import EmployeeSkillsSection from "@/components/skills/EmployeeSkillsSection.vue";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";
import ProjectInfoDialog from "@/components/project/ProjectInfoDialog.vue";
import ProjectAssignmentDialog from "@/components/project/ProjectAssignmentDialog.vue";
import { usePermissions } from "@/lib/permissions";
import { formatDate } from "@/lib/datetime";
import { useOfficeMapPreview } from "@/composables/useOfficeMapPreview";
import { useEmployeeProjectActions } from "@/composables/useEmployeeProjectActions";
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
import type { GroupedSkillsItem } from "@/composables/useEmployeeSkills";

const props = defineProps<{
  employee: Employee;
}>();

const emit = defineEmits<{
  (event: "employee-updated"): void;
}>();

const { t } = useI18n();
const permissions = usePermissions();
const employeeRef = toRef(props, "employee");

const {
  mapDialogOpen,
  mapName,
  mapTitle,
  highlightedWorkplace,
  openMap,
} = useOfficeMapPreview(employeeRef);

const {
  canShowProjectInfo,
  canEditProject,
  projectInfoDialogOpen,
  projectUpdateDialogOpen,
  openProjectInfo,
  openProjectUpdate,
} = useEmployeeProjectActions(employeeRef);

const vacationsLoading = ref(false);
const employeeVacations = ref<EmployeeVacationShort[]>([]);

const skills = ref<Skill[]>([]);
const skillsLoading = ref(false);
const skillsActionError = ref<unknown>(null);

const canViewTechProfiles = computed(() =>
  permissions.canDownloadTechProfiles(props.employee.id),
);
const canViewSkills = computed(() =>
  permissions.canViewEmplSkills(props.employee.id),
);
const canEditSkills = computed(() =>
  permissions.canEditSkills(props.employee.id),
);
const canAddSkills = computed(() =>
  permissions.canAddSkills(props.employee.id),
);
const canDeleteSkills = computed(() =>
  permissions.canDeleteSkills(props.employee.id),
);
const canRateSkills = computed(() =>
  permissions.canRateSkills(props.employee.id),
);

const skillsSectionLoading = computed(() => skillsLoading.value);
const skillsSectionError = computed(() => skillsActionError.value ?? null);

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
      skills: [...item.skills].sort((a, b) =>
        a.name.localeCompare(b.name, "ru", { sensitivity: "base" }),
      ),
    }))
    .sort((a, b) =>
      a.group.name.localeCompare(b.group.name, "ru", { sensitivity: "base" }),
    );
});

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

function handleProjectUpdated() {
  emit("employee-updated");
}

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
</style>
