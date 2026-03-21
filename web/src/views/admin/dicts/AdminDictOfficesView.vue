<template>
  <AdminSimpleDictView
    :title="t('Офисы')"
    :headers="headers"
    :load-items="fetchAdminOffices"
    :create-item="createAdminOffice"
    :update-item="updateAdminOffice"
    :create-form="createForm"
    :item-to-form="itemToForm"
    test-id="admin-dict-offices-view"
  >
    <template #additional-fields="{ form }">
      <v-text-field
        v-model="form.address"
        :label="t('Адрес')"
        :rules="longTextRules"
        variant="outlined"
      />
      <v-textarea
        v-model="form.description"
        :label="t('Описание')"
        :rules="longTextRules"
        variant="outlined"
        rows="3"
      />
    </template>
  </AdminSimpleDictView>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import AdminSimpleDictView from "@/views/admin/dicts/components/AdminSimpleDictView.vue";
import {
  createAdminOffice,
  fetchAdminOffices,
  type AdminOffice,
  type AdminOfficePayload,
  updateAdminOffice,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const headers = computed(() => [
  { title: t("Наименования"), key: "name", width: "300px" },
  { title: t("Адрес"), key: "address", width: "320px" },
  { title: t("Описание"), key: "description", width: "360px" },
  { title: t("Архив"), key: "archived", width: "140px" },
]);

const longTextRules = computed(() => [
  (value?: string | null) =>
    !value || value.length <= 1024 || t("Не более N символов", { n: 1024 }),
]);

function createForm(): AdminOfficePayload {
  return {
    name: "",
    archived: false,
    address: "",
    description: "",
  };
}

function itemToForm(item: AdminOffice): AdminOfficePayload {
  return {
    name: item.name,
    archived: item.archived,
    address: item.address ?? "",
    description: item.description ?? "",
  };
}
</script>
