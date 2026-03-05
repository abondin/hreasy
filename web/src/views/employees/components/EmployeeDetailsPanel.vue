<!--
  Employee details drawer panel.
-->
<template>
  <div v-if="employee" class="employee-panel">
    <section class="employee-panel__primary">
      <div class="employee-panel__avatar">
        <profile-avatar :owner="employee" :read-only="false" />
      </div>

      <div class="employee-panel__summary">
        <div v-if="employee.position" class="text-body-1 font-weight-medium">
          {{ employee.position.name }}
        </div>

        <div class="text-body-2 text-medium-emphasis">
          {{ t("Отдел") }}: {{ employee.department?.name ?? t("Не задан") }}
        </div>

        <div class="text-body-2 text-medium-emphasis">
          {{ t("E-mail") }}: {{ employee.email ?? t("Не задан") }}
        </div>

        <div v-if="employee.officeLocation" class="text-body-2 text-medium-emphasis d-flex align-center ga-1">
          <span>
            {{ employee.officeLocation.name }}
          </span>
          <v-btn
            v-if="employee.officeLocation.mapName"
            icon="mdi-map"
            size="x-small"
            variant="text"
            density="compact"
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

        <div class="text-body-2 d-flex align-center ga-1 flex-wrap">
          <span class="font-weight-medium">{{ t("Текущий проект") }}:</span>
          <span>{{ employee.currentProject?.name ?? t("Не задан") }}</span>
          <span v-if="employee.ba">({{ employee.ba.name }})</span>
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
          <v-btn
            v-if="canEditProject"
            icon="mdi-pencil"
            variant="text"
            density="compact"
            size="small"
            :title="t('Обновление текущего проекта')"
            @click.stop="openProjectUpdate"
          />
        </div>

        <div v-if="permissions.canViewEmplCurrentProjectRole()" class="text-body-2 text-medium-emphasis">
          {{ t("Роль на проекте") }}: {{ employee.currentProject?.role ?? t("Не задан") }}
        </div>

        <div class="text-body-2 text-medium-emphasis">
          {{ t("Бизнес Аккаунт") }}: {{ employee.ba?.name ?? t("Не задан") }}
        </div>
      </div>
    </section>

    <v-divider class="my-3" />

    <section class="employee-panel__section">
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
    </section>

    <section v-if="canViewTechProfiles" class="employee-panel__section mt-4">
      <div class="text-body-2 font-weight-medium mb-2">
        {{ t("Квалификационные карточки") }}
      </div>
      <profile-tech-profiles-card
        :employee-id="employee.id"
        :with-card="false"
        :show-title="false"
        :dense="true"
      />
    </section>

    <section v-if="canViewSkills" class="employee-panel__section mt-4">
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
    </section>

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
  </div>
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
import { useEmployeeSkillPermissions } from "@/composables/useEmployeeSkillPermissions";
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
import { groupSkillsByGroup, type GroupedSkillsItem } from "@/lib/skills";

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
.employee-panel {
  padding: 12px;
}

.employee-panel__primary {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.employee-panel__avatar {
  flex: 0 0 auto;
}

.employee-panel__summary {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

@media (max-width: 600px) {
  .employee-panel__primary {
    flex-direction: column;
  }
}
</style>
