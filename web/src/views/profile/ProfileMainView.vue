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
          <detail-section-block
            :title="t('Квалификационные карточки')"
            class="min-w-0 pt-xl-8"
          >
            <profile-tech-profiles-card
              :employee-id="employee.id"
              :with-card="false"
              :show-title="false"
              :dense="true"
              @updated="handleEmployeeUpdated"
            />
          </detail-section-block>
        </profile-summary-card>
      </section>

      <section class="mt-5" data-testid="profile-secondary-section">
        <v-row align="stretch">
          <v-col cols="12">
            <employee-overtime-card
              v-if="canViewMyOvertimes && employee"
              :employee-id="employee.id"
            />
          </v-col>
          <v-col cols="12">
            <my-vacations />
          </v-col>
          <v-col cols="12">
            <shared-articles-card />
          </v-col>
          <v-col cols="12" v-if="canViewSkills">
            <detail-section-card class="h-100" :title="t('Навыки')">
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
            </detail-section-card>
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
import SharedArticlesCard from "@/components/article/SharedArticlesCard.vue";
import DetailSectionBlock from "@/components/shared/DetailSectionBlock.vue";
import DetailSectionCard from "@/components/shared/DetailSectionCard.vue";
import EmployeeSkillsSection from "@/components/skills/EmployeeSkillsSection.vue";
import EmployeeOvertimeCard from "@/components/overtimes/EmployeeOvertimeCard.vue";
import MyVacations from "@/components/vacations/MyVacations.vue";
import { useEmployeeProfile } from "@/composables/useEmployeeProfile";
import { useEmployeeSkills } from "@/composables/useEmployeeSkills";
import { useEmployeeSkillPermissions } from "@/composables/useEmployeeSkillPermissions";
import { usePermissions } from "@/lib/permissions";
import type { AddSkillBody, Skill } from "@/services/skills.service";
import { useAuthStore } from "@/stores/auth";
import ProfileSummaryCard from "@/views/profile/components/ProfileSummaryCard.vue";
import ProfileTechProfilesCard from "@/views/profile/components/ProfileTechProfilesCard.vue";
import ProfileTelegramDialog from "@/views/profile/components/ProfileTelegramDialog.vue";

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
  return actionError.value ?? skillsError.value ?? null;
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