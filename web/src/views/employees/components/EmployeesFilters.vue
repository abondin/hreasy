<!--
  Employees filters block.
  Keeps table filters isolated from list/details orchestration.
-->
<template>
  <AdaptiveFilterBar
    :items="filterBarItems"
    :has-left-actions="false"
    :overflow-menu-min-width="320"
  >
      <template #filter-search>
        <v-text-field
          v-model="searchModel"
          data-testid="employees-filter-search"
          :label="t('Поиск')"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          clearable
          hide-details
        />
      </template>

      <template #filter-department>
        <v-autocomplete
          v-model="departmentModel"
          data-testid="employees-filter-department"
          :items="departmentOptions"
          :label="t('Отдел')"
          variant="outlined"
          density="compact"
          clearable
          multiple
          item-title="title"
          item-value="value"
          hide-details
        >
          <template #selection="{ item, index }">
            <CollapsedSelectionContent
              :index="index"
              :total="departmentModel.length"
              :label="getSelectionLabel(item)"
              :visible-count="2"
            />
          </template>
        </v-autocomplete>
      </template>

      <template #filter-project>
        <v-autocomplete
          v-model="projectModel"
          data-testid="employees-filter-project"
          :items="projectOptions"
          :label="t('Текущий проект')"
          variant="outlined"
          density="compact"
          clearable
          multiple
          item-title="title"
          item-value="value"
          hide-details
        >
          <template #selection="{ item, index }">
            <CollapsedSelectionContent
              :index="index"
              :total="projectModel.length"
              :label="getSelectionLabel(item)"
              :visible-count="2"
            />
          </template>
        </v-autocomplete>
      </template>

      <template #filter-business-account>
        <v-autocomplete
          v-model="businessAccountModel"
          data-testid="employees-filter-ba"
          :items="businessAccountOptions"
          :label="t('Бизнес Аккаунт')"
          variant="outlined"
          density="compact"
          clearable
          multiple
          item-title="title"
          item-value="value"
          hide-details
        >
          <template #selection="{ item, index }">
            <CollapsedSelectionContent
              :index="index"
              :total="businessAccountModel.length"
              :label="getSelectionLabel(item)"
              :visible-count="2"
            />
          </template>
        </v-autocomplete>
      </template>
  </AdaptiveFilterBar>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";

const props = defineProps<{
  departmentOptions: Array<{ title: string; value: number }>;
  projectOptions: Array<{ title: string; value: number | null }>;
  businessAccountOptions: Array<{ title: string; value: number }>;
  search?: string;
  department?: number[];
  project?: Array<number | null>;
  businessAccount?: number[];
}>();

const emit = defineEmits<{
  (event: 'update:search', value: string): void;
  (event: 'update:department', value: number[]): void;
  (event: 'update:project', value: Array<number | null>): void;
  (event: 'update:businessAccount', value: number[]): void;
}>();

const { t } = useI18n();

const searchModel = computed({
  get: () => props.search ?? '',
  set: (value: string) => emit('update:search', value),
});

const departmentModel = computed<number[]>({
  get: () => props.department ?? [],
  set: (value) => emit('update:department', value),
});

const projectModel = computed<Array<number | null>>({
  get: () => props.project ?? [],
  set: (value) => emit('update:project', value),
});

const businessAccountModel = computed<number[]>({
  get: () => props.businessAccount ?? [],
  set: (value) => emit('update:businessAccount', value),
});

const filterBarItems = computed(() => [
  { id: 'search', minWidth: 380, active: searchModel.value.trim().length > 0, grow: true },
  { id: 'department', minWidth: 320, active: departmentModel.value.length > 0 },
  { id: 'business-account', minWidth: 380, active: businessAccountModel.value.length > 0 },
  { id: 'project', minWidth: 380, active: projectModel.value.length > 0 },
]);

function getSelectionLabel(item: unknown): string {
  if (typeof item === 'string') {
    return item;
  }
  if (item && typeof item === 'object' && 'title' in item && typeof item.title === 'string') {
    return item.title;
  }
  return '';
}
</script>
