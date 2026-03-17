<template>
  <AdminSimpleDictView
    :title="t('Организации')"
    :headers="headers"
    :load-items="fetchAdminOrganizations"
    :create-item="createAdminOrganization"
    :update-item="updateAdminOrganization"
    :create-form="createForm"
    :item-to-form="itemToForm"
    test-id="admin-dict-organizations-view"
  >
    <template #additional-fields="{ form }">
      <v-textarea
        v-model="form.description"
        :label="t('Описание')"
        :rules="descriptionRules"
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
  createAdminOrganization,
  fetchAdminOrganizations,
  type AdminOrganization,
  type AdminOrganizationPayload,
  updateAdminOrganization,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const headers = computed(() => [
  { title: t("Наименования"), key: "name", width: "320px" },
  { title: t("Описание"), key: "description", width: "420px" },
  { title: t("Архив"), key: "archived", width: "140px" },
]);

const descriptionRules = computed(() => [
  (value?: string | null) =>
    !value || value.length <= 1024 || t("Не более N символов", { n: 1024 }),
]);

function createForm(): AdminOrganizationPayload {
  return {
    name: "",
    archived: false,
    description: "",
  };
}

function itemToForm(item: AdminOrganization): AdminOrganizationPayload {
  return {
    name: item.name,
    archived: item.archived,
    description: item.description ?? "",
  };
}
</script>
