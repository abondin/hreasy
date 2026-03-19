<template>
  <v-container class="py-6" data-testid="profile-view">
    <v-skeleton-loader
      v-if="isLoading"
      type="card, list-item-two-line, actions"
      class="mt-6"
    />

    <template v-else-if="employee">
        <section data-testid="profile-summary-section">
          <profile-summary-card
            :employee="employee"
            :read-only="false"
            @avatar-updated="handleEmployeeUpdated"
            @edit-telegram="openTelegramDialog"
            @update-project="handleEmployeeUpdated"
          >
            <div class="profile-tech-profiles-block">
              <div class="profile-summary__label mb-1">{{ t("Квалификационные карточки") }}:</div>
              <div class="profile-summary-tech-profiles">
                <profile-tech-profiles-card
                  :employee-id="employee.id"
                  :with-card="false"
                  :show-title="false"
                  :dense="true"
                  @updated="handleEmployeeUpdated"
                />
              </div>
            </div>
          </profile-summary-card>
        </section>

        <section class="mt-5 profile-secondary-section" data-testid="profile-secondary-section">
          <v-row align="stretch">
            <v-col cols="12">
              <employee-overtime-card
                v-if="canViewMyOvertimes && employee"
                :employee-id="employee.id"
              />
              <legacy-feature-card
                v-else
                :title="t('Овертаймы')"
                :description="t('Раздел_пока_доступен_в_legacy')"
              />
            </v-col>
            <v-col cols="12">
              <my-vacations />
            </v-col>
            <v-col cols="12">
              <shared-articles-card />
            </v-col>
            <v-col cols="12" v-if="canViewSkills">
              <v-card class="h-100">
                <v-card-title class="d-flex align-center justify-space-between">
                  <span>{{ t("Навыки") }}</span>
                </v-card-title>
                <v-card-text>
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
            </v-col>
          </v-row>
        </section>

        <profile-telegram-dialog
          :open="telegramDialogOpen"
          :employee-id="employee.id"
          :display-name="employee.displayName"
          :initial-telegram="employee.telegram"
          :telegram-confirmed-at="employee.telegramConfirmedAt"
          @close="telegramDialogOpen = false"
          @updated="handleTelegramUpdated"
        />
    </template>

    <v-alert
      v-else-if="hasError"
      type="error"
      variant="tonal"
      border="start"
    >
      {{ t("Не_удалось_загрузить_профиль") }}
    </v-alert>
    <v-alert
      v-else
      type="info"
      variant="tonal"
      border="start"
    >
      {{ t("Профиль_недоступен") }}
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { useEmployeeProfile } from "@/composables/useEmployeeProfile";
import { useEmployeeSkills } from "@/composables/useEmployeeSkills";
import { useEmployeeSkillPermissions } from "@/composables/useEmployeeSkillPermissions";
import { usePermissions } from "@/lib/permissions";
import SharedArticlesCard from "@/components/article/SharedArticlesCard.vue";
import LegacyFeatureCard from "@/components/LegacyFeatureCard.vue";
import MyVacations from "@/components/vacations/MyVacations.vue";
import EmployeeOvertimeCard from "@/components/overtimes/EmployeeOvertimeCard.vue";
import ProfileSummaryCard from "@/views/profile/components/ProfileSummaryCard.vue";
import ProfileTelegramDialog from "@/views/profile/components/ProfileTelegramDialog.vue";
import ProfileTechProfilesCard from "@/views/profile/components/ProfileTechProfilesCard.vue";
import EmployeeSkillsSection from "@/components/skills/EmployeeSkillsSection.vue";
import type { Skill, AddSkillBody } from "@/services/skills.service";

const { t } = useI18n();
const authStore = useAuthStore();
const permissions = usePermissions();

const employeeId = computed(() => authStore.employeeId ?? null);

const {
  employee,
  loading: profileLoading,
  error: profileError,
  reload: reloadEmployeeProfile,
} = useEmployeeProfile(() => employeeId.value);

const {
  groupedSkills,
  loading: skillsLoading,
  error: skillsError,
  addSkill,
  updateSkillRating,
  deleteSkill,
} = useEmployeeSkills(() => employeeId.value);

const isLoading = computed(() => profileLoading.value);
const hasError = computed(() => Boolean(profileError.value));
const canViewMyOvertimes = computed(() => {
  if (!employeeId.value) {
    return false;
  }
  return permissions.canViewOvertimes(employeeId.value);
});
const telegramDialogOpen = ref(false);
const actionError = ref<unknown>(null);

const skillsSectionLoading = computed(() => skillsLoading.value);

const skillsSectionError = computed(() => {
  return (
    actionError.value ??
    skillsError.value ??
    null
  );
});

const {
  canViewSkills,
  canEditSkills,
  canAddSkills,
  canDeleteSkills,
  canRateSkills,
} = useEmployeeSkillPermissions(() => employeeId.value);

function handleEmployeeUpdated() {
  reloadEmployeeProfile().catch(() => undefined);
}

function handleTelegramUpdated() {
  telegramDialogOpen.value = false;
  handleEmployeeUpdated();
}

function openTelegramDialog() {
  telegramDialogOpen.value = true;
}

async function submitNewSkill(payload: AddSkillBody) {
  actionError.value = null;
  return addSkill(payload);
}

async function handleRateSkill({
  skill,
  rating,
}: {
  skill: Skill;
  rating: number;
}) {
  if (!canRateSkills.value || rating <= 0) {
    return;
  }
  actionError.value = null;
  try {
    await updateSkillRating(skill.id, { rating });
  } catch (error) {
    actionError.value = error;
  }
}

async function confirmDeleteSkill(skill: Skill) {
  await deleteSkill(skill.id);
}

function handleSkillDeleted() {}
</script>

<style scoped>
.profile-summary-tech-profiles {
  min-width: 0;
}

.profile-summary-tech-profiles :deep(.d-flex.flex-column.ga-4) {
  gap: 0 !important;
}

.profile-summary-tech-profiles :deep(.d-flex.flex-wrap.align-center.ga-3) {
  gap: 8px !important;
}

.profile-tech-profiles-block {
  font-size: 0.92rem;
  color: rgba(0, 0, 0, 0.72);
}

@media (min-width: 1280px) {
  .profile-tech-profiles-block {
    padding-top: 34px;
  }
}

.profile-tech-profiles-block :deep(.v-btn) {
  margin-top: 2px;
}

.profile-secondary-section :deep(.v-card-title) {
  padding: 18px 20px 10px;
}

.profile-secondary-section :deep(.v-card-text) {
  padding: 0 20px 18px;
}

.profile-secondary-section :deep(.v-card > .v-card-title + .v-card-text) {
  padding-top: 0;
}
</style>
