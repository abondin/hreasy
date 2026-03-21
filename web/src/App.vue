<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" temporary class="app-navigation">
      <v-list density="comfortable" nav>
        <v-list-item
          v-for="item in profileNavigationItems"
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
          v-for="item in primaryNavigationItems"
          :key="item.key"
          :prepend-icon="item.icon"
          :to="item.to"
          link
          @click="drawer = false"
        >
          <v-list-item-title>{{ item.label }}</v-list-item-title>
        </v-list-item>

        <v-list-group v-if="salaryNavigationItems.length" value="salary-navigation-group">
          <template #activator="{ props }">
            <v-list-item v-bind="props" prepend-icon="mdi-currency-rub">
              <v-list-item-title>{{ t("Повышения") }}</v-list-item-title>
            </v-list-item>
          </template>
          <v-list-item
            v-for="item in salaryNavigationItems"
            :key="item.key"
            :to="item.to"
            link
            @click="drawer = false"
          >
            <v-list-item-title>{{ item.label }}</v-list-item-title>
          </v-list-item>
        </v-list-group>

        <v-list-item
          v-for="item in postSalaryNavigationItems"
          :key="item.key"
          :prepend-icon="item.icon"
          :to="item.to"
          link
          @click="drawer = false"
        >
          <v-list-item-title>{{ item.label }}</v-list-item-title>
        </v-list-item>

        <v-list-group v-if="adminNavigationItems.length" value="admin-navigation-group">
          <template #activator="{ props }">
            <v-list-item v-bind="props" prepend-icon="mdi-cogs">
              <v-list-item-title>{{ t("Админка") }}</v-list-item-title>
            </v-list-item>
          </template>
          <v-list-item
            v-for="item in adminNavigationItems"
            :key="item.key"
            :to="item.to"
            link
            @click="drawer = false"
          >
            <v-list-item-title>{{ item.label }}</v-list-item-title>
          </v-list-item>
        </v-list-group>

        <v-divider v-if="isAuthenticated" class="my-2" />
        <v-list-item
          v-if="isAuthenticated"
          prepend-icon="mdi-logout"
          data-testid="logout-button"
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
      <v-app-bar-title class="font-weight-medium" data-testid="app-title">
        HR Easy
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
        <v-btn variant="text" color="primary" data-testid="logout-button" @click="logout">
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
            {{ currentYear }} - <strong>Alexander Bondin</strong>
            <a :href="vue2Url" class="footer-link ml-4">
              {{ t("Перейти_в_старую_версию_интерфейса") }}
            </a>
          </v-col>
        </v-row>
      </v-container>
    </v-footer>
  </v-app>
</template>

<script setup lang="ts">
import { RouterView, useRouter } from "vue-router";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { usePermissions } from "@/lib/permissions";

const authStore = useAuthStore();
const permissions = usePermissions();
const router = useRouter();
const { t } = useI18n();

const displayName = computed(() => authStore.displayName);
const isAuthenticated = computed(() => authStore.isAuthenticated);
const drawer = ref(false);
const currentYear = new Date().getFullYear();
const vue2Url = import.meta.env.VITE_VUE2_UI_URL ?? "/old/";

const profileNavigationItems = computed(() => {
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

const primaryNavigationItems = computed(() => {
  if (!isAuthenticated.value) {
    return [];
  }

  const items = [
    {
      key: "employees",
      label: t("Сотрудники"),
      icon: "mdi-account-group",
      to: { name: "employees" },
    },
  ];

  if (permissions.canViewAllOvertimes()) {
    items.push({
      key: "overtimes",
      label: t("Овертаймы"),
      icon: "mdi-briefcase-clock",
      to: { name: "overtimes" },
    });
  }

  if (permissions.canViewAllVacations()) {
    items.push({
      key: "vacations",
      label: t("Отпуска"),
      icon: "mdi-calendar-range",
      to: { name: "vacations" },
    });
  }

  if (permissions.canCreateAssessments()) {
    items.push({
      key: "assessments",
      label: t("Ассессменты"),
      icon: "mdi-book-check-outline",
      to: { name: "assessments" },
    });
  }

  return items;
});

const salaryNavigationItems = computed(() => {
  if (!isAuthenticated.value) {
    return [];
  }

  const items = [];

  if (permissions.canReportSalaryRequest()) {
    items.push({
      key: "salary-requests",
      label: t("Повышения и бонусы"),
      to: { name: "salary-requests" },
    });
  }

  if (permissions.canAdminSalaryRequests()) {
    items.push({
      key: "salary-latest",
      label: t("Последние повышения"),
      to: { name: "salary-latest" },
    });
  }

  return items;
});

const postSalaryNavigationItems = computed(() => {
  if (!isAuthenticated.value) {
    return [];
  }

  const items = [];

  if (permissions.canAccessJuniorsRegistry() || permissions.canAdminJuniorRegistry()) {
    items.push({
      key: "mentorship",
      label: t("Менторство"),
      icon: "mdi-account-school",
      to: { name: "mentorship" },
    });
  }

  return items;
});

const adminNavigationItems = computed(() => {
  if (!isAuthenticated.value) {
    return [];
  }

  const items = [];

  if (permissions.canAdminEmployees()) {
    items.push({
      key: "admin-employees",
      label: t("Админка сотрудников"),
      icon: "mdi-account-cog",
      to: { name: "admin-employees-list" },
    });
  }

  if (
    permissions.canAdminDictOrganizations()
    || permissions.canAdminDictDepartments()
    || permissions.canAdminDictPositions()
    || permissions.canAdminDictLevels()
    || permissions.canAdminDictOffices()
    || permissions.canAdminDictOfficeLocations()
  ) {
    items.push({
      key: "admin-dicts",
      label: t("Справочники"),
      icon: "mdi-book-open-variant",
      to: "/admin/dicts",
    });
  }

  if (permissions.canAdminProjects()) {
    items.push({
      key: "admin-projects",
      label: t("Все проекты"),
      icon: "mdi-briefcase-edit-outline",
      to: { name: "admin-projects" },
    });
  }

  if (permissions.canAdminBusinessAccounts()) {
    items.push({
      key: "admin-business-accounts",
      label: t("Бизнес Аккаунты"),
      icon: "mdi-domain",
      to: { name: "admin-business-accounts" },
    });
  }

  if (permissions.canAdminUsers()) {
    items.push({
      key: "admin-users",
      label: t("Пользователи и роли"),
      icon: "mdi-shield-account",
      to: { name: "admin-users" },
    });
  }

  if (permissions.canAdminArticles()) {
    items.push({
      key: "admin-articles",
      label: t("Статьи и новости"),
      icon: "mdi-newspaper-variant-outline",
      to: { name: "admin-articles" },
    });
  }

  if (permissions.canAdminManagers()) {
    items.push({
      key: "admin-managers",
      label: t("Все менеджеры"),
      icon: "mdi-account-tie",
      to: { name: "admin-managers" },
    });
  }

  return items;
});

onMounted(() => {
  authStore.fetchCurrentUser().catch(() => undefined);
});

async function logout() {
  drawer.value = false;
  await authStore.logout();
  await router.push({ name: "login" });
}
</script>

<style scoped>
.footer-link {
  color: inherit;
}
</style>
