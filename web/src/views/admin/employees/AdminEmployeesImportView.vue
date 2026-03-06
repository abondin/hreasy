<template>
  <admin-import-workflow
    :title="t('Импорт сотрудников из Excel')"
    :service="adminEmployeeImportService"
    :config="defaultConfig"
  />
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import AdminImportWorkflow from "@/views/admin/employees/components/AdminImportWorkflow.vue";
import {
  adminEmployeeImportService,
  type EmployeeImportConfig,
} from "@/services/admin/import/admin-employee-import.service";

const { t } = useI18n();

const defaultConfig: EmployeeImportConfig = {
  sheetNumber: 1,
  tableStartRow: 10,
  columns: {
    displayName: "E",
    externalErpId: "G",
    email: "C",
    phone: "L",
    department: "N",
    position: "M",
    dateOfEmployment: "D",
    dateOfDismissal: "T",
    birthday: "H",
    sex: "I",
    documentSeries: "Y",
    documentNumber: "Z",
    documentIssuedDate: "AA",
    documentIssuedBy: "AB",
    registrationAddress: "X",
  },
};
</script>
