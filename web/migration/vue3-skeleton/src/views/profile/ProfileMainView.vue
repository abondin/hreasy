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
          <v-col cols="12" md="6">
            <legacy-feature-card
              :title="t('Навыки')"
              :description="t('Раздел_пока_доступен_в_legacy')"
            />
          </v-col>
          <v-col cols="12" md="6">
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
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { useEmployeeProfile } from "@/composables/useEmployeeProfile";
import LegacyFeatureCard from "@/components/LegacyFeatureCard.vue";
import ProfileSummaryCard from "@/views/profile/components/ProfileSummaryCard.vue";
import ProfileTelegramDialog from "@/views/profile/components/ProfileTelegramDialog.vue";
import ProfileTechProfilesCard from "@/views/profile/components/ProfileTechProfilesCard.vue";

const { t } = useI18n();
const authStore = useAuthStore();

const employeeId = computed(() => authStore.employeeId ?? null);

const {
  employee,
  loading: profileLoading,
  error: profileError,
  reload: reloadEmployeeProfile,
} = useEmployeeProfile(() => employeeId.value);

const isLoading = computed(() => profileLoading.value);
const hasError = computed(() => Boolean(profileError.value));
const telegramDialogOpen = ref(false);

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
</script>
