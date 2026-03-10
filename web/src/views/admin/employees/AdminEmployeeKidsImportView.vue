<template>
  <admin-import-workflow
    :title="t('Импорт детей из Excel')"
    :service="adminEmployeeKidImportService"
    :config="defaultConfig"
    :stats-labels="statsLabels"
    :preview-headers-loader="previewHeadersLoader"
    :preview-filter-function="previewFilterFunction"
    :on-complete-action="onCompleteAction"
  />
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import AdminImportWorkflow from "@/views/admin/employees/components/AdminImportWorkflow.vue";
import {
  adminEmployeeKidImportService,
  type EmployeeKidImportConfig,
  type ImportEmployeeKidExcelRow,
} from "@/services/admin/import/admin-employee-kid-import.service";

const { t } = useI18n();
const router = useRouter();

const defaultConfig: EmployeeKidImportConfig = {
  sheetNumber: 1,
  tableStartRow: 8,
  columns: {
    displayName: "G",
    birthday: "I",
    parentEmail: "K",
  },
};

const statsLabels = {
  newItems: t("Новых детей"),
  updatedItems: t("Детей с изменениями"),
};

function onCompleteAction() {
  router.push({ name: "admin-employees-kids" }).catch(() => undefined);
}

function previewHeadersLoader() {
  return [
    { title: t("Строка"), key: "rowNumber", width: 110 },
    { title: t("ФИО"), key: "displayName", width: 280 },
    { title: t("День рождения"), key: "birthday", width: 160, format: "date" as const },
    { title: t("Сотрудник"), key: "parent", width: 260, format: "dict" as const },
  ];
}

function previewFilterFunction(
  items: ImportEmployeeKidExcelRow[],
  filter: { search: string; hideNotUpdatedWithoutErrors: boolean },
) {
  const search = filter.search.trim().toLowerCase();
  return items.filter((item) => {
    if (filter.hideNotUpdatedWithoutErrors && item.updatedCellsCount <= 0 && item.errorCount <= 0) {
      return false;
    }
    if (!search) {
      return true;
    }
    return (
      item.displayName?.toLowerCase().includes(search)
      || item.parentEmail?.toLowerCase().includes(search)
      || item.parent?.name?.toLowerCase().includes(search)
    );
  });
}
</script>
