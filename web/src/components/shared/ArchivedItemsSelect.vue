<template>
  <v-autocomplete v-model="selectedId" :items="visibleItems" item-title="title" item-value="id" :hide-no-data="false">
    <template #item="{ props: itemProps, item }">
      <v-list-item v-bind="itemProps" :subtitle="item.archiveDate ? archivedLabel(formatDate(item.archiveDate)) : undefined">
        <template #title>
          <span :class="{ 'text-decoration-line-through': Boolean(item.archiveDate) }">{{ item.title }}</span>
        </template>
      </v-list-item>
    </template>
    <template v-if="items.some(item => archiveDate(item))" #menu-header>
      <v-checkbox v-model="showArchived" :label="showArchivedLabel" class="px-4" hide-details @click.stop />
      <v-divider />
    </template>
  </v-autocomplete>
</template>

<script setup lang="ts" generic="T extends { id?: number }">
import { computed, ref } from "vue";
import { formatDate } from "@/lib/datetime";

const props = defineProps<{
  items: readonly T[];
  itemTitle: (item: T) => string;
  archiveDate: (item: T) => string | null | undefined;
  archivedLabel: (date: string) => string;
  showArchivedLabel: string;
}>();
const selectedId = defineModel<number | null>({ default: null });
const showArchived = ref(false);
const visibleItems = computed(() => props.items.filter(item =>
  !props.archiveDate(item) || showArchived.value || item.id === selectedId.value)
  .map(item => ({ id: item.id, title: props.itemTitle(item), archiveDate: props.archiveDate(item) })));
</script>
