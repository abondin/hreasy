<template>
  <v-app>
    <v-app-bar elevation="1" density="comfortable">
      <v-app-bar-title class="font-weight-medium">
        HREasy Vue 3 Skeleton
      </v-app-bar-title>
      <v-spacer />
      <v-btn variant="text" :to="{ name: 'home' }">
        {{ t('Главная') }}
      </v-btn>
      <v-btn variant="text" :to="{ name: 'legacy-status' }">
        Статус миграции
      </v-btn>
      <template v-if="!isAuthenticated">
        <v-btn
          variant="tonal"
          color="primary"
          :to="{ name: 'login' }"
          class="ml-4"
        >
          {{ t('Вход') }}
        </v-btn>
      </template>
      <template v-else>
        <v-chip class="ml-4" color="primary" variant="outlined">
          {{ displayName }}
        </v-chip>
        <v-btn
          variant="text"
          color="primary"
          class="ml-2"
          @click="logout"
        >
          {{ t('Выход') }}
        </v-btn>
      </template>
    </v-app-bar>

    <v-main class="pa-4">
      <RouterView />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import {RouterView, useRouter} from 'vue-router';
import {computed, onMounted} from 'vue';
import {useI18n} from 'vue-i18n';
import {useAuthStore} from '@/stores/auth';

const authStore = useAuthStore();
const router = useRouter();
const {t} = useI18n();

const displayName = computed(() => authStore.displayName);
const isAuthenticated = computed(() => authStore.isAuthenticated);

onMounted(() => {
  authStore.fetchCurrentUser().catch(() => undefined);
});

async function logout() {
  await authStore.logout();
  await router.push({name: 'login'});
}
</script>
