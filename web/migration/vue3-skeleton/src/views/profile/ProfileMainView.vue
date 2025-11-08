<template>
  <v-container class="profile-view py-6">
    <v-skeleton-loader
      v-if="isLoading"
      type="card, list-item-two-line, actions"
      class="mt-6"
    />

    <template v-else-if="employee">
      <section>
        <v-row dense>
          <v-col cols="12">
            <profile-summary-card
              :employee="employee"
              :read-only="false"
              @avatar-updated="handleEmployeeUpdated"
              @edit-telegram="openTelegramDialog"
              @update-project="handleEmployeeUpdated"
            />
          </v-col>
        </v-row>
      </section>

      <section class="mt-6">
        <v-row dense>
          <v-col cols="12" md="6">
            <legacy-feature-card
              :title="t('Отпуска')"
              :description="t('Раздел_пока_доступен_в_legacy')"
            />
          </v-col>
          <v-col cols="12" md="6">
            <legacy-feature-card
              :title="t('Овертаймы')"
              :description="t('Раздел_пока_доступен_в_legacy')"
            />
          </v-col>
          <v-col cols="12" v-if="canViewSkills">
            <v-card>
              <v-card-title class="d-flex align-center justify-space-between">
                <span>{{ t("Навыки") }}</span>
              </v-card-title>
              <v-card-text>
                <profile-skills-card
                  :grouped-skills="groupedSkills"
                  :loading="skillsSectionLoading"
                  :error="skillsSectionError"
                  :can-edit="canEditSkills"
                  @add="handleAddSkillRequested"
                  @rate="handleRateSkill"
                  @delete="handleDeleteSkillRequested"
                />
              </v-card-text>
            </v-card>
          </v-col>
          <v-col cols="12">
            <profile-tech-profiles-card
              :employee-id="employee.id"
              @updated="handleEmployeeUpdated"
            />
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
      <skill-create-dialog
        :open="createSkillDialogOpen"
        :skill-groups="skillGroups"
        :shared-skill-names="sharedSkillNames"
        :submit-skill="submitNewSkill"
        @close="createSkillDialogOpen = false"
      />
      <skill-delete-dialog
        :open="deleteSkillDialogOpen"
        :skill="skillPendingDeletion"
        :delete-skill="confirmDeleteSkill"
        @close="closeDeleteDialog"
        @deleted="handleSkillDeleted"
      />
    </template>

    <v-alert v-else-if="hasError" type="error" variant="tonal" border="start">
      {{ t("Не_удалось_загрузить_профиль") }}
    </v-alert>
    <v-alert v-else type="info" variant="tonal" border="start">
      {{ t("Профиль_недоступен") }}
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { useEmployeeProfile } from "@/composables/useEmployeeProfile";
import { useEmployeeSkills } from "@/composables/useEmployeeSkills";
import { useSkillsDictionaryStore } from "@/stores/skills-dictionary";
import { usePermissions } from "@/lib/permissions";
import LegacyFeatureCard from "@/components/LegacyFeatureCard.vue";
import ProfileSummaryCard from "@/views/profile/components/ProfileSummaryCard.vue";
import ProfileTelegramDialog from "@/views/profile/components/ProfileTelegramDialog.vue";
import ProfileTechProfilesCard from "@/views/profile/components/ProfileTechProfilesCard.vue";
import ProfileSkillsCard from "@/views/profile/components/ProfileSkillsCard.vue";
import SkillCreateDialog from "@/views/profile/components/SkillCreateDialog.vue";
import SkillDeleteDialog from "@/views/profile/components/SkillDeleteDialog.vue";
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

const skillsDictionaryStore = useSkillsDictionaryStore();
const {
  skillGroups,
  sharedSkillNames,
  loading: skillsMetadataLoading,
  error: skillsMetadataError,
} = storeToRefs(skillsDictionaryStore);

const isLoading = computed(() => profileLoading.value);
const hasError = computed(() => Boolean(profileError.value));
const telegramDialogOpen = ref(false);
const createSkillDialogOpen = ref(false);
const deleteSkillDialogOpen = ref(false);
const skillPendingDeletion = ref<Skill | null>(null);
const actionError = ref<unknown>(null);

const skillsSectionLoading = computed(
  () => skillsLoading.value || skillsMetadataLoading.value,
);

const skillsSectionError = computed(() => {
  return (
    actionError.value ??
    skillsError.value ??
    skillsMetadataError.value ??
    null
  );
});

const canEditSkills = computed(() => {
  const id = employeeId.value;
  if (typeof id !== "number") {
    return false;
  }
  return permissions.canEditSkills(id);
});

const canViewSkills = computed(() => {
  const id = employeeId.value;
  if (typeof id !== "number") {
    return false;
  }
  return permissions.canViewEmplSkills(id);
});

onMounted(() => {
  void skillsDictionaryStore.loadSkillMetadata();
});

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

async function handleAddSkillRequested() {
  if (!canEditSkills.value) {
    return;
  }
  actionError.value = null;
  try {
    await skillsDictionaryStore.loadSkillMetadata();
    createSkillDialogOpen.value = true;
  } catch (error) {
    actionError.value = error;
  }
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
  if (!canEditSkills.value || rating <= 0) {
    return;
  }
  actionError.value = null;
  try {
    await updateSkillRating(skill.id, { rating });
  } catch (error) {
    actionError.value = error;
  }
}

function handleDeleteSkillRequested(skill: Skill) {
  if (!canEditSkills.value) {
    return;
  }
  skillPendingDeletion.value = skill;
  deleteSkillDialogOpen.value = true;
}

function closeDeleteDialog() {
  deleteSkillDialogOpen.value = false;
  skillPendingDeletion.value = null;
}

async function confirmDeleteSkill(skill: Skill) {
  await deleteSkill(skill.id);
}

function handleSkillDeleted() {
  closeDeleteDialog();
}
</script>
