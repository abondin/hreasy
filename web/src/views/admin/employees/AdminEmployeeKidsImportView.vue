<template>
  <admin-import-workflow
    :title="t('Импорт детей из Excel')"
    :service="adminEmployeeKidImportService"
    :config="defaultConfig"
  />
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import AdminImportWorkflow from "@/views/admin/employees/components/AdminImportWorkflow.vue";
import {
  adminEmployeeKidImportService,
  type EmployeeKidImportConfig,
} from "@/services/admin/import/admin-employee-kid-import.service";

const { t } = useI18n();

const defaultConfig: EmployeeKidImportConfig = {
  sheetNumber: 1,
  tableStartRow: 8,
  columns: {
    displayName: "G",
    birthday: "I",
    parentEmail: "K",
  },
};
</script>
