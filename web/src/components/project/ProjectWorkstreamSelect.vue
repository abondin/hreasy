<template>
  <ArchivedItemsSelect
    v-model="selectedId"
    :items="items"
    :item-title="item => item.displayName"
    :archive-date="item => item.archivedAt"
    :archived-label="date => t('Архивировано ДАТА', { date })"
    :show-archived-label="t('Показать архивные')"
    :label="t('Направление работ')"
    :placeholder="t('Без направления')"
    persistent-placeholder
    clearable
  />
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import ArchivedItemsSelect from "@/components/shared/ArchivedItemsSelect.vue";
import type { ProjectWorkstream } from "@/services/projects.service";

defineProps<{ items: readonly ProjectWorkstream[] }>();
const selectedId = defineModel<number | null>({ default: null });
const { t } = useI18n();
</script>
