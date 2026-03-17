<template>
  <AdminSimpleDictView
    :title="t('Позиции')"
    :headers="headers"
    :load-items="fetchAdminPositions"
    :create-item="createAdminPosition"
    :update-item="updateAdminPosition"
    :create-form="createForm"
    :item-to-form="itemToForm"
    test-id="admin-dict-positions-view"
  >
    <template #additional-fields="{ form }">
      <v-text-field
        v-model="form.category"
        :label="t('Категория')"
        :rules="categoryRules"
        variant="outlined"
      />
    </template>
  </AdminSimpleDictView>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import AdminSimpleDictView from "@/views/admin/dicts/components/AdminSimpleDictView.vue";
import {
  createAdminPosition,
  fetchAdminPositions,
  type AdminPosition,
  type AdminPositionPayload,
  updateAdminPosition,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const headers = computed(() => [
  { title: t("Наименования по штатному расписанию"), key: "name", width: "420px" },
  { title: t("Категория"), key: "category", width: "260px" },
  { title: t("Архив"), key: "archived", width: "140px" },
]);

const categoryRules = computed(() => [
  (value?: string | null) =>
    !value || value.length <= 255 || t("Не более N символов", { n: 255 }),
]);

function createForm(): AdminPositionPayload {
  return {
    name: "",
    archived: false,
    category: "",
  };
}

function itemToForm(item: AdminPosition): AdminPositionPayload {
  return {
    name: item.name,
    archived: item.archived,
    category: item.category ?? "",
  };
}
</script>
