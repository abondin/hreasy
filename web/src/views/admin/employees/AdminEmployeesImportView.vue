<template>
  <div data-testid="admin-employees-import-view">
    <admin-import-workflow
      :title="t('Импорт сотрудников из Excel')"
      :service="adminEmployeeImportService"
      :config="defaultConfig"
      :preview-headers-loader="previewHeadersLoader"
      :preview-filter-function="previewFilterFunction"
      :on-complete-action="onCompleteAction"
    />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import AdminImportWorkflow from "@/views/admin/employees/components/AdminImportWorkflow.vue";
import {
  adminEmployeeImportService,
  type ImportEmployeeExcelRow,
  type EmployeeImportConfig,
} from "@/services/admin/import/admin-employee-import.service";

const { t } = useI18n();
const router = useRouter();

const defaultConfig: EmployeeImportConfig = {
  sheetNumber: 1,
  tableStartRow: 11,
  columns: {
    displayName: "B",
    externalErpId: "H",
    email: "R",
    phone: "AI",
    department: "AJ",
    position: "AK",
    dateOfEmployment: "AL",
    dateOfDismissal: "AM",
    birthday: "AO",
    sex: "AP",
    documentSeries: "AR",
    documentNumber: "AS",
    documentIssuedDate: "AT",
    documentIssuedBy: "AU",
    registrationAddress: "AW",
  },
};

function onCompleteAction() {
  router.push({ name: "admin-employees-list" }).catch(() => undefined);
}

function previewHeadersLoader() {
  return [
    { title: t("Строка"), key: "rowNumber", width: 110 },
    { title: t("Email"), key: "email", width: 240 },
    { title: t("ФИО"), key: "displayName", width: 260, format: "string" as const },
    { title: t("Телефон"), key: "phone", width: 170, format: "string" as const },
    {
      title: t("Дата трудоустройства"),
      key: "dateOfEmployment",
      width: 160,
      format: "date" as const,
    },
    {
      title: t("Дата увольнения"),
      key: "dateOfDismissal",
      width: 160,
      format: "date" as const,
    },
    { title: t("День рождения"), key: "birthday", width: 150, format: "date" as const },
    { title: t("Позиция"), key: "position", width: 220, format: "dict" as const },
    { title: t("Подразделение"), key: "department", width: 260, format: "dict" as const },
    { title: t("Организация"), key: "organization", width: 260, format: "dict" as const },
    {
      title: t("IMPORT_CONFIG.documentSeries"),
      key: "documentSeries",
      width: 170,
      format: "string" as const,
    },
    {
      title: t("IMPORT_CONFIG.documentNumber"),
      key: "documentNumber",
      width: 170,
      format: "string" as const,
    },
    {
      title: t("IMPORT_CONFIG.documentIssuedDate"),
      key: "documentIssuedDate",
      width: 170,
      format: "date" as const,
    },
    {
      title: t("IMPORT_CONFIG.documentIssuedBy"),
      key: "documentIssuedBy",
      width: 260,
      format: "string" as const,
    },
    {
      title: t("Адрес по регистрации"),
      key: "registrationAddress",
      width: 360,
      format: "string" as const,
    },
    {
      title: t("Идентификатор во внешней ERP системе"),
      key: "externalErpId",
      width: 180,
      format: "string" as const,
    },
    { title: t("Пол"), key: "sex", width: 120, format: "string" as const },
  ];
}

function previewFilterFunction(
  items: ImportEmployeeExcelRow[],
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
    const name = String(item.displayName?.importedValue ?? "").toLowerCase();
    const email = String(item.email ?? "").toLowerCase();
    return name.includes(search) || email.includes(search);
  });
}
</script>
