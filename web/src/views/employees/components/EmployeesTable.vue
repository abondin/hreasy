<!--
  Employees data table.
  Owns headers rendering and row selection only.
-->
<template>
  <HREasyTableBase
    data-testid="employees-table"
    table-class="employees-table"
    :headers="headers"
    :items="items"
    :height="tableHeight"
    :fixed-header="true"
    density="compact"
    :loading="loading"
    :loading-text="t('Загрузка_данных')"
    :no-data-text="t('Отсутствуют данные')"
    :hover="true"
    :row-props="rowProps"
    @click:row="onClickRow"
  >
    <template v-if="$slots.filters" #filters>
      <slot name="filters" />
    </template>
    <template #[`item.department.name`]="{ item }">
      {{ item.department?.name ?? t('Не задан') }}
    </template>
    <template #[`item.currentProject.name`]="{ item }">
      {{ item.currentProject?.name ?? t('Не задан') }}
    </template>
    <template #[`item.currentProject.role`]="{ item }">
      {{ item.currentProject?.role ?? t('Не задан') }}
    </template>
    <template #[`item.ba.name`]="{ item }">
      {{ item.ba?.name ?? t('Не задан') }}
    </template>
  </HREasyTableBase>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { VDataTable } from "vuetify/components";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import { extractDataTableRow } from "@/lib/data-table";
import type { Employee } from "@/services/employee.service";

type DataTableHeader = VDataTable['$props']['headers'];

defineProps<{
  items: Employee[];
  loading: boolean;
  headers: DataTableHeader;
  tableHeight: number | string;
}>();

const emit = defineEmits<{
  (event: 'select-employee', value: Employee): void;
}>();

const { t } = useI18n();

function rowProps() {
  return {
    class: 'cursor-pointer',
  };
}

function onClickRow(_event: Event, payload: unknown) {
  const row = extractDataTableRow<Employee>(payload);
  if (!row) {
    return;
  }
  emit('select-employee', row);
}
</script>
