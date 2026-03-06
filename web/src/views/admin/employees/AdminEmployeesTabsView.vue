<template>
  <v-container class="py-4">
    <v-tabs color="primary">
      <v-tab :to="{ name: 'admin-employees-list' }">{{ t("Сотрудники") }}</v-tab>
      <v-tab :to="{ name: 'admin-employees-import' }">{{ t("Импорт сотрудников из Excel") }}</v-tab>
      <v-tab :to="{ name: 'admin-employees-kids' }">{{ t("Дети") }}</v-tab>
      <v-tab :to="{ name: 'admin-employees-kids-import' }">{{ t("Импорт детей из Excel") }}</v-tab>
    </v-tabs>
    <router-view />
  </v-container>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";

const { t } = useI18n();
</script>
