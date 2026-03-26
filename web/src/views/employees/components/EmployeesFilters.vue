<!--
  Employees filters block.
  Keeps table filters isolated from list/details orchestration.
-->
<template>
  <v-card-title class="pb-0">
    <v-row>
      <v-col cols="12" :md="showExtendedFilters ? 4 : 12">
        <v-text-field
          v-model="searchModel"
          data-testid="employees-filter-search"
          :label="t('Поиск')"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          clearable
        />
      </v-col>
      <template v-if="showExtendedFilters">
        <v-col cols="12" md="4">
          <v-autocomplete
            v-model="projectModel"
            data-testid="employees-filter-project"
            :items="projectOptions"
            :label="t('Текущий проект')"
            variant="outlined"
            density="compact"
            clearable
            multiple
            chips
            item-title="title"
            item-value="value"
          />
        </v-col>
        <v-col cols="12" md="4">
          <v-autocomplete
            v-model="businessAccountModel"
            data-testid="employees-filter-ba"
            :items="businessAccountOptions"
            :label="t('Бизнес Аккаунт')"
            variant="outlined"
            density="compact"
            clearable
            multiple
            chips
            item-title="title"
            item-value="value"
          />
        </v-col>
      </template>
    </v-row>
  </v-card-title>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const props = defineProps<{
  showExtendedFilters: boolean;
  projectOptions: Array<{ title: string; value: number | null }>;
  businessAccountOptions: Array<{ title: string; value: number }>;
  search?: string;
  project?: Array<number | null>;
  businessAccount?: number[];
}>();

const emit = defineEmits<{
  (event: 'update:search', value: string): void;
  (event: 'update:project', value: Array<number | null>): void;
  (event: 'update:businessAccount', value: number[]): void;
}>();

const { t } = useI18n();

const searchModel = computed({
  get: () => props.search ?? '',
  set: (value: string) => emit('update:search', value),
});

const projectModel = computed<Array<number | null>>({
  get: () => props.project ?? [],
  set: (value) => emit('update:project', value),
});

const businessAccountModel = computed<number[]>({
  get: () => props.businessAccount ?? [],
  set: (value) => emit('update:businessAccount', value),
});
</script>
