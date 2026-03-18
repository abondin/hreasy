<template>
  <v-container class="py-6" data-testid="admin-dicts-tabs-view">
    <v-tabs color="primary" data-testid="admin-dicts-tabs">
      <v-tab
        v-for="tab in tabs"
        :key="tab.key"
        :to="tab.to"
      >
        {{ tab.label }}
      </v-tab>
    </v-tabs>
    <router-view />
  </v-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";

const { t } = useI18n();
const permissions = usePermissions();

const tabs = computed(() => {
  const result: Array<{ key: string; label: string; to: { name: string } }> = [];

  if (permissions.canAdminDictOrganizations()) {
    result.push({
      key: "organizations",
      label: t("Организации"),
      to: { name: "admin-dicts-organizations" },
    });
  }

  if (permissions.canAdminDictDepartments()) {
    result.push({
      key: "departments",
      label: t("Подразделения"),
      to: { name: "admin-dicts-departments" },
    });
  }

  if (permissions.canAdminDictPositions()) {
    result.push({
      key: "positions",
      label: t("Позиции"),
      to: { name: "admin-dicts-positions" },
    });
  }

  if (permissions.canAdminDictLevels()) {
    result.push({
      key: "levels",
      label: t("Уровни"),
      to: { name: "admin-dicts-levels" },
    });
  }

  if (permissions.canAdminDictOffices()) {
    result.push({
      key: "offices",
      label: t("Офисы"),
      to: { name: "admin-dicts-offices" },
    });
  }

  if (permissions.canAdminDictOfficeLocations()) {
    result.push({
      key: "office_locations",
      label: t("Кабинеты"),
      to: { name: "admin-dicts-office-locations" },
    });
    result.push({
      key: "office_maps",
      label: t("Карты офисов и кабинетов"),
      to: { name: "admin-dicts-office-maps" },
    });
  }

  return result;
});
</script>
