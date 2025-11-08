<!--
  Virtualized employees table with filters and row actions.
-->
<template>
  <v-card>
    <v-card-title class="d-flex flex-wrap ga-3 align-center">
      <v-text-field
        v-model="localSearch"
        :label="t('Поиск')"
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="comfortable"
        class="flex-grow-1 min-w-0"
        clearable
      />
      <v-select
        v-model="selectedProject"
        :items="projectOptions"
        :label="t('Текущий проект')"
        variant="outlined"
        density="comfortable"
        clearable
      />
      <v-select
        v-model="selectedBa"
        :items="baOptions"
        :label="t('Бизнес Аккаунт')"
        variant="outlined"
        density="comfortable"
        clearable
      />
    </v-card-title>

    <v-card-text>
      <v-data-table-virtual
        :headers="headers"
        :items="items"
        item-key="id"
        :height="tableHeight"
        fixed-header
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
      >
        <template #item="{ columns, internalItem, props, itemRef }">
          <tr
            v-bind="props"
            :ref="itemRef"
            class="cursor-pointer"
            @click="toggleRow(internalItem.raw)"
          >
            <td
              v-for="column in columns"
              :key="column.key ?? column.title"
            >
              {{ toDisplay(column.key ?? "", internalItem.raw) }}
            </td>
          </tr>
          <tr v-if="isExpanded(internalItem.raw)">
            <td :colspan="columns.length">
              <employee-details-card :employee="internalItem.raw" />
            </td>
          </tr>
        </template>
      </v-data-table-virtual>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Employee } from "@/services/employee.service";
import EmployeeDetailsCard from "@/views/employees/components/EmployeeDetailsDialog.vue";

const props = defineProps<{
  items: Employee[];
  loading: boolean;
  projectOptions: Array<{ title: string; value: number | null }>;
  businessAccountOptions: Array<{ title: string; value: number | null }>;
  search?: string;
  project?: number | null;
  businessAccount?: number | null;
  tableHeight?: number | string;
}>();

const emit = defineEmits<{
  (event: "update:search", value: string): void;
  (event: "update:project", value: number | null): void;
  (event: "update:ba", value: number | null): void;
}>();

const { t } = useI18n();

const localSearch = ref(props.search ?? "");
const selectedProject = ref<number | null>(props.project ?? null);
const selectedBa = ref<number | null>(props.businessAccount ?? null);
const expandedId = ref<number | null>(null);

watch(localSearch, (value) => emit("update:search", value));
watch(selectedProject, (value) => emit("update:project", value));
watch(selectedBa, (value) => emit("update:ba", value));

watch(
  () => props.search,
  (value) => {
    if (value !== undefined && value !== localSearch.value) {
      localSearch.value = value;
    }
  },
);

watch(
  () => props.project,
  (value) => {
    if (value !== selectedProject.value) {
      selectedProject.value = value ?? null;
    }
  },
);

watch(
  () => props.businessAccount,
  (value) => {
    if (value !== selectedBa.value) {
      selectedBa.value = value ?? null;
    }
  },
);

const headers = computed(() => [
  { title: t("ФИО"), key: "displayName" },
  { title: t("Отдел"), key: "department.name" },
  { title: t("E-mail"), key: "email" },
  { title: t("Текущий проект"), key: "currentProject.name" },
  { title: t("Роль на проекте"), key: "currentProject.role" },
  { title: t("Бизнес Аккаунт"), key: "ba.name" },
]);

const tableHeight = computed(() => props.tableHeight ?? "70vh");

const projectOptions = computed(() => props.projectOptions);
const baOptions = computed(() => props.businessAccountOptions);

function toDisplay(key: string, item: Employee): string {
  switch (key) {
    case "department.name":
      return item.department?.name ?? t("Не задан");
    case "currentProject.name":
      return item.currentProject?.name ?? t("Не задан");
    case "currentProject.role":
      return item.currentProject?.role ?? t("Не задан");
    case "ba.name":
      return item.ba?.name ?? t("Не задан");
    default:
      return String(resolveKey(item, key) ?? "");
  }
}

function resolveKey(item: Employee, key: string): unknown {
  return key.split(".").reduce<unknown>((value, part) => {
    if (value && typeof value === "object") {
      return (value as Record<string, unknown>)[part];
    }
    return undefined;
  }, item);
}

function toggleRow(employee: Employee) {
  expandedId.value = expandedId.value === employee.id ? null : employee.id;
}

function isExpanded(employee: Employee): boolean {
  return expandedId.value === employee.id;
}
</script>
