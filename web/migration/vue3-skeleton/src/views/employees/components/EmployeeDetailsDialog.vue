<!--
  Inline employee details card for table expansion.
-->
<template>
  <v-card flat class="pa-4" v-if="employee">
    <v-row dense>
      <v-col cols="12" md="6">
        <v-list density="comfortable">
          <v-list-item>
            <v-list-item-title>{{ t("Отдел") }}</v-list-item-title>
            <v-list-item-subtitle>
              {{ employee.department?.name ?? t("Не задан") }}
            </v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>{{ t("E-mail") }}</v-list-item-title>
            <v-list-item-subtitle>
              {{ employee.email ?? t("Не задан") }}
            </v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>{{ t("Telegram") }}</v-list-item-title>
            <v-list-item-subtitle>
              {{ employee.telegram ?? t("Не задан") }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-col>
      <v-col cols="12" md="6">
        <v-list density="comfortable">
          <v-list-item>
            <v-list-item-title>{{ t("Текущий проект") }}</v-list-item-title>
            <v-list-item-subtitle>
              {{ employee.currentProject?.name ?? t("Не задан") }}
            </v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>{{ t("Роль на проекте") }}</v-list-item-title>
            <v-list-item-subtitle>
              {{ employee.currentProject?.role ?? t("Не задан") }}
            </v-list-item-subtitle>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>{{ t("Бизнес Аккаунт") }}</v-list-item-title>
            <v-list-item-subtitle>
              {{ employee.ba?.name ?? t("Не задан") }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-col>
    </v-row>
  </v-card>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Employee } from "@/services/employee.service";

const props = defineProps<{
  employee: Employee;
}>();

const { t } = useI18n();
</script>
