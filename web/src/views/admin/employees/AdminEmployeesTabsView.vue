<template>
  <v-container class="py-6" fluid data-testid="admin-employees-tabs-view">
    <v-tabs color="primary" data-testid="admin-employees-tabs">
      <v-tab :to="{ name: 'admin-employees-list' }" data-testid="admin-tab-list">{{ t("Сотрудники") }}</v-tab>
      <v-tab :to="{ name: 'admin-employees-import' }" data-testid="admin-tab-import">{{ t("Импорт сотрудников из Excel") }}</v-tab>
      <v-tab :to="{ name: 'admin-employees-kids' }" data-testid="admin-tab-kids">{{ t("Дети") }}</v-tab>
      <v-tab :to="{ name: 'admin-employees-kids-import' }" data-testid="admin-tab-kids-import">{{ t("Импорт детей из Excel") }}</v-tab>
    </v-tabs>
    <router-view />
  </v-container>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>
