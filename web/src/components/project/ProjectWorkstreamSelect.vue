<template>
  <v-autocomplete
    v-model="selectedId"
    :items="visibleItems"
    :hide-no-data="false"
    :item-title="title"
    item-value="id"
    :label="t('Направление работ')"
    :placeholder="t('Без направления')"
    persistent-placeholder
    clearable
  >
    <template v-if="items.some(item => item.archivedAt)" #append-item>
      <v-checkbox
        v-model="showArchived"
        :label="t('Показать архивные')"
        class="px-4"
        hide-details
        @click.stop
      />
    </template>
  </v-autocomplete>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { formatDate } from "@/lib/datetime";
import type { ProjectWorkstream } from "@/services/projects.service";

const props = defineProps<{ items: readonly ProjectWorkstream[] }>();
const selectedId = defineModel<number | null>({ default: null });
const { t } = useI18n();
const showArchived = ref(false);
const visibleItems = computed(() => props.items.filter(item =>
  !item.archivedAt || showArchived.value || item.id === selectedId.value));

function title(item: ProjectWorkstream): string {
  return item.archivedAt
    ? `${item.displayName} (${t("Архивировано ДАТА", { date: formatDate(item.archivedAt) })})`
    : item.displayName;
}
</script>
