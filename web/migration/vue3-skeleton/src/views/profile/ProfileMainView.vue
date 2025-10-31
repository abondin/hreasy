<template>
  <v-container class="profile-view py-6">
    <v-alert
      v-if="!isAuthenticated"
      type="info"
      variant="tonal"
      border="start"
      class="mb-6"
    >
      {{ t('Для_просмотра_профиля_нужно_войти') }}
    </v-alert>

    <v-skeleton-loader
      v-else-if="isLoading"
      type="card, list-item-two-line, actions"
      class="mt-6"
    />

    <template v-else-if="employee">
      <profile-summary-card
        :display-name="employee.displayName"
        :avatar-url="avatarUrl"
        :items="summaryItems"
      />

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
            <legacy-feature-card
              :title="t('Квалификационные_карточки')"
              :description="t('Раздел_пока_доступен_в_legacy')"
            />
          </v-col>
        </v-row>
      </section>
    </template>

    <v-alert
      v-else-if="hasError"
      type="error"
      variant="tonal"
      border="start"
    >
      {{ t('Не_удалось_загрузить_профиль') }}
    </v-alert>
    <v-alert
      v-else
      type="info"
      variant="tonal"
      border="start"
    >
      {{ t('Профиль_недоступен') }}
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import {computed} from 'vue';
import {useI18n} from 'vue-i18n';
import {useAuthStore} from '@/stores/auth';
import {useEmployeeProfile} from '@/composables/useEmployeeProfile';
import {getEmployeeAvatarUrl} from '@/services/employee.service';
import LegacyFeatureCard from '@/components/LegacyFeatureCard.vue';
import ProfileSummaryCard from '@/views/profile/components/ProfileSummaryCard.vue';

const {t} = useI18n();
const authStore = useAuthStore();

const employeeId = computed(() => authStore.employeeId ?? null);
const isAuthenticated = computed(() => authStore.isAuthenticated);

const {
  employee,
  loading: profileLoading,
  error: profileError
} = useEmployeeProfile(() => employeeId.value);

const isLoading = computed(() => profileLoading.value);

const avatarUrl = computed(() => {
  const id = employee.value?.id;
  if (typeof id === 'number') {
    return getEmployeeAvatarUrl(id);
  }
  return undefined;
});

const summaryItems = computed(() => {
  const profile = employee.value;
  if (!profile) {
    return [];
  }
  return [
    {
      key: 'department',
      label: t('Отдел'),
      value: profile.department?.name ?? t('Не задан')
    },
    {
      key: 'project',
      label: t('Текущий проект'),
      value: profile.currentProject?.name ?? t('Не задан')
    },
    {
      key: 'role',
      label: t('Роль на текущем проекте'),
      value: profile.currentProject?.role ?? t('Не задана')
    },
    {
      key: 'ba',
      label: t('Бизнес Аккаунт'),
      value: profile.ba?.name ?? t('Не задан')
    },
    {
      key: 'email',
      label: t('Почтовый адрес'),
      value: profile.email ?? t('Не задан')
    },
    {
      key: 'position',
      label: t('Позиция'),
      value: profile.position?.name ?? t('Не задана')
    },
    {
      key: 'office',
      label: t('Кабинет'),
      value: profile.officeLocation?.name ?? t('Не задан')
    },
    {
      key: 'telegram',
      label: t('Телеграм'),
      value: profile.telegram ?? t('Не задан')
    }
  ];
});

const hasError = computed(() => Boolean(profileError.value));
</script>
