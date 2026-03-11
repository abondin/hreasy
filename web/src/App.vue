<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" temporary class="app-navigation">
      <v-list density="comfortable" nav>
        <v-list-item
          v-for="item in mainNavigationItems"
          :key="item.key"
          :prepend-icon="item.icon"
          :to="item.to"
          link
          @click="drawer = false"
        >
          <v-list-item-title>{{ item.label }}</v-list-item-title>
        </v-list-item>

        <template v-if="adminNavigationItems.length">
          <v-divider class="my-2" />
          <v-list-subheader>{{ t("РђРґРјРёРЅРєР°") }}</v-list-subheader>
          <v-list-item
            v-for="item in adminNavigationItems"
            :key="item.key"
            :prepend-icon="item.icon"
            :to="item.to"
            link
            @click="drawer = false"
          >
            <v-list-item-title>{{ item.label }}</v-list-item-title>
          </v-list-item>
        </template>

        <v-divider v-if="isAuthenticated" class="my-2" />

        <v-list-item
          v-if="isAuthenticated"
          prepend-icon="mdi-logout"
          data-testid="logout-button"
          @click="logout"
        >
          <v-list-item-title>{{ t("Р’С‹С…РѕРґ") }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar app elevation="1" density="comfortable">
      <v-app-bar-nav-icon
        :aria-label="t('РћС‚РєСЂС‹С‚СЊ_РјРµРЅСЋ')"
        @click="drawer = !drawer"
      />
      <v-app-bar-title class="font-weight-medium" data-testid="app-title">
        HR Easy
      </v-app-bar-title>
      <v-spacer />
      <template v-if="!isAuthenticated">
        <v-btn variant="tonal" color="primary" :to="{ name: 'login' }">
          {{ t("Р’С…РѕРґ") }}
        </v-btn>
      </template>
      <template v-else>
        <v-chip class="mr-2" color="primary" variant="outlined">
          {{ displayName }}
        </v-chip>
        <v-btn variant="text" color="primary" data-testid="logout-button" @click="logout">
          {{ t("Р’С‹С…РѕРґ") }}
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
            {{ currentYear }} вЂ” <strong>Alexander Bondin</strong>
            <a href="/" class="footer-link ml-4">
              {{ t("РџРµСЂРµР№С‚Рё_РІ_СЃС‚Р°СЂСѓСЋ_РІРµСЂСЃРёСЋ_РёРЅС‚РµСЂС„РµР№СЃР°") }}
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

const mainNavigationItems = computed(() => {
  if (isAuthenticated.value) {
    const items = [
      {
        key: "profile-main",
        label: t("РџСЂРѕС„РёР»СЊ"),
        icon: "mdi-account",
        to: { name: "profile-main" },
      },
      {
        key: "employees",
        label: t("РЎРѕС‚СЂСѓРґРЅРёРєРё"),
        icon: "mdi-account-group",
        to: { name: "employees" },
      },
    ];
    if (permissions.canViewAllVacations()) {
      items.push({
        key: "vacations",
        label: t("РћС‚РїСѓСЃРєР°"),
        icon: "mdi-calendar-range",
        to: { name: "vacations" },
      });
    }
    if (permissions.canViewAllOvertimes()) {
      items.push({
        key: "overtimes",
        label: t("РћРІРµСЂС‚Р°Р№РјС‹"),
        icon: "mdi-briefcase-clock",
        to: { name: "overtimes" },
      });
    }
    if (permissions.canAccessJuniorsRegistry() || permissions.canAdminJuniorRegistry()) {
      items.push({
        key: "mentorship",
        label: t("РњРµРЅС‚РѕСЂСЃС‚РІРѕ"),
        icon: "mdi-account-school",
        to: { name: "mentorship" },
      });
    }
    return items;
  }

  return [
    {
      key: "login",
      label: t("Р’С…РѕРґ"),
      icon: "mdi-login",
      to: { name: "login" },
    },
  ];
});

const adminNavigationItems = computed(() => {
  if (!isAuthenticated.value) {
    return [];
  }

  const items = [];

  if (permissions.canAdminEmployees()) {
    items.push({
      key: "admin-employees",
      label: t("РђРґРјРёРЅРєР° СЃРѕС‚СЂСѓРґРЅРёРєРѕРІ"),
      icon: "mdi-account-cog",
      to: { name: "admin-employees-list" },
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

