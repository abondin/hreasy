<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" temporary class="app-navigation">
      <v-list density="comfortable" nav>
        <v-list-item
          v-for="item in navigationItems"
          :key="item.key"
          :prepend-icon="item.icon"
          :to="item.to"
          link
          @click="drawer = false"
        >
          <v-list-item-title>{{ item.label }}</v-list-item-title>
        </v-list-item>

        <v-divider v-if="isAuthenticated" class="my-2" />

        <v-list-item
          v-if="isAuthenticated"
          prepend-icon="mdi-logout"
          @click="logout"
        >
          <v-list-item-title>{{ t("Выход") }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar app elevation="1" density="comfortable">
      <v-app-bar-nav-icon
        :aria-label="t('Открыть_меню')"
        @click="drawer = !drawer"
      />
      <v-app-bar-title class="font-weight-medium">
        HREasy Vue 3 Skeleton
      </v-app-bar-title>
      <v-spacer />
      <template v-if="!isAuthenticated">
        <v-btn variant="tonal" color="primary" :to="{ name: 'login' }">
          {{ t("Вход") }}
        </v-btn>
      </template>
      <template v-else>
        <v-chip class="mr-2" color="primary" variant="outlined">
          {{ displayName }}
        </v-chip>
        <v-btn variant="text" color="primary" @click="logout">
          {{ t("Выход") }}
        </v-btn>
      </template>
    </v-app-bar>

    <v-main>
      <RouterView />
    </v-main>

    <v-footer app color="grey-lighten-4" border="top" height="60">
      <v-container>
        <v-row>
          <v-col cols="12" class="text-end text-body-2">
            {{ currentYear }} — <strong>Alexander Bondin</strong>
          </v-col>
        </v-row>
      </v-container>
    </v-footer>
  </v-app>
</template>

<script setup lang="ts">
import { RouterView } from "vue-router";
import { computed, getCurrentInstance, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();
const instance = getCurrentInstance();
const globalProperties = instance?.appContext.config.globalProperties as {
  $router?: { push: (location: unknown) => Promise<unknown> };
};
const router = globalProperties?.$router;
const { t } = useI18n();

const displayName = computed(() => authStore.displayName);
const isAuthenticated = computed(() => authStore.isAuthenticated);
const drawer = ref(false);
const currentYear = new Date().getFullYear();

const navigationItems = computed(() => {
  if (isAuthenticated.value) {
    return [
      {
        key: "profile-main",
        label: t("Профиль"),
        icon: "mdi-account",
        to: { name: "profile-main" },
      },
    ];
  }

  return [
    {
      key: "login",
      label: t("Вход"),
      icon: "mdi-login",
      to: { name: "login" },
    },
  ];
});

onMounted(() => {
  authStore.fetchCurrentUser().catch(() => undefined);
});

async function logout() {
  drawer.value = false;
  if (!router) {
    return;
  }
  await authStore.logout();
  await router.push({ name: "login" });
}
</script>
