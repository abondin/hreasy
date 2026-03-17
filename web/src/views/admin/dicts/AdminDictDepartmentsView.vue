<template>
  <AdminSimpleDictView
    :title="t('Подразделения')"
    :headers="headers"
    :load-items="fetchAdminDepartments"
    :create-item="createAdminDepartment"
    :update-item="updateAdminDepartment"
    :create-form="createForm"
    :item-to-form="itemToForm"
    test-id="admin-dict-departments-view"
  />
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import AdminSimpleDictView from "@/views/admin/dicts/components/AdminSimpleDictView.vue";
import {
  createAdminDepartment,
  fetchAdminDepartments,
  type AdminDepartment,
  type AdminDepartmentPayload,
  updateAdminDepartment,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const headers = computed(() => [
  { title: t("Наименования"), key: "name", width: "420px" },
  { title: t("Архив"), key: "archived", width: "140px" },
]);

function createForm(): AdminDepartmentPayload {
  return {
    name: "",
    archived: false,
  };
}

function itemToForm(item: AdminDepartment): AdminDepartmentPayload {
  return {
    name: item.name,
    archived: item.archived,
  };
}
</script>
