<template>
  <AdminSimpleDictView
    :title="t('Уровни')"
    :headers="headers"
    :load-items="fetchAdminLevels"
    :create-item="createAdminLevel"
    :update-item="updateAdminLevel"
    :create-form="createForm"
    :item-to-form="itemToForm"
    test-id="admin-dict-levels-view"
  >
    <template #additional-fields="{ form }">
      <v-text-field
        v-model.number="form.weight"
        :label="t('Вес')"
        :rules="weightRules"
        type="number"
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
  createAdminLevel,
  fetchAdminLevels,
  type AdminLevel,
  type AdminLevelPayload,
  updateAdminLevel,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const headers = computed(() => [
  { title: t("Наименования"), key: "name", width: "320px" },
  { title: t("Вес"), key: "weight", width: "160px" },
  { title: t("Архив"), key: "archived", width: "140px" },
]);

const weightRules = computed(() => [
  (value?: number | null) =>
    value == null || (value >= 0 && value <= 100) || t("Число от до", { min: 0, max: 100 }),
]);

function createForm(): AdminLevelPayload {
  return {
    name: "",
    archived: false,
    weight: null,
  };
}

function itemToForm(item: AdminLevel): AdminLevelPayload {
  return {
    name: item.name,
    archived: item.archived,
    weight: item.weight ?? null,
  };
}
</script>
