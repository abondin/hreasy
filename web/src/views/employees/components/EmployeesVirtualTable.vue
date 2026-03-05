<!--
  Virtualized employees table with filters and row actions.
-->
<template>
  <v-card>
    <v-card-title class="d-flex flex-wrap ga-3 align-center employees-filters">
      <v-text-field
        v-model="localSearch"
        :label="t('Поиск')"
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="compact"
        class="employees-filters__item"
        clearable
      />
      <v-autocomplete
        v-model="selectedProject"
        :items="projectOptions"
        :label="t('Текущий проект')"
        variant="outlined"
        density="compact"
        clearable
        multiple
        chips
        item-title="title"
        item-value="value"
        class="employees-filters__item"
      />
      <v-autocomplete
        v-model="selectedBa"
        :items="baOptions"
        :label="t('Бизнес Аккаунт')"
        variant="outlined"
        density="compact"
        clearable
        multiple
        chips
        item-title="title"
        item-value="value"
        class="employees-filters__item"
      />
    </v-card-title>

    <v-card-text>
      <v-data-table
        :headers="headers"
        :items="items"
        item-key="id"
        :height="tableHeight"
        fixed-header
        fixed-footer
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :items-per-page="-1"
        hide-default-footer
        hover
      >
        <template #item="{ columns, internalItem, item, props }">
          <tr
            v-bind="props"
            class="cursor-pointer"
            @click="toggleRow(resolveItem(item, internalItem))"
          >
            <td
              v-for="column in columns"
              :key="column.key ?? column.title"
            >
              {{ toDisplay(column.key ?? "", resolveItem(item, internalItem)) }}
            </td>
          </tr>
          <tr v-if="isExpanded(resolveItem(item, internalItem))">
            <td :colspan="columns.length">
              <employee-details-expanded-row
                :employee="resolveItem(item, internalItem)"
                @employee-updated="emitEmployeeUpdated"
              />
            </td>
          </tr>
        </template>
      </v-data-table>

    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { Employee } from "@/services/employee.service";
import EmployeeDetailsExpandedRow from "@/views/employees/components/EmployeeDetailsExpandedRow.vue";
import { usePermissions } from "@/lib/permissions";

const props = defineProps<{
  items: Employee[];
  loading: boolean;
  projectOptions: Array<{ title: string; value: number | null }>;
  businessAccountOptions: Array<{ title: string; value: number }>;
  search?: string;
  project?: Array<number | null>;
  businessAccount?: number[];
  tableHeight?: number | string;
}>();

const emit = defineEmits<{
  (event: "update:search", value: string): void;
  (event: "update:project", value: Array<number | null>): void;
  (event: "update:ba", value: number[]): void;
  (event: "employee-updated"): void;
}>();

const { t } = useI18n();
const permissions = usePermissions();

const localSearch = ref(props.search ?? "");
const selectedProject = ref<Array<number | null>>(props.project ?? []);
const selectedBa = ref<number[]>(props.businessAccount ?? []);
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
    const nextValue = value ?? [];
    if (nextValue !== selectedProject.value) {
      selectedProject.value = nextValue;
    }
  },
);

watch(
  () => props.businessAccount,
  (value) => {
    const nextValue = value ?? [];
    if (nextValue !== selectedBa.value) {
      selectedBa.value = nextValue;
    }
  },
);

const headers = computed(() => {
  const items = [
    { title: t("ФИО"), key: "displayName", width: "240px" },
    { title: t("Отдел"), key: "department.name", width: "260px" },
    { title: t("E-mail"), key: "email", width: "172px" },
    { title: t("Текущий проект"), key: "currentProject.name", width: "192px" },
  ];
  if (permissions.canViewEmplCurrentProjectRole()) {
    items.push({ title: t("Роль на проекте"), key: "currentProject.role", width: "172px" });
  }
  items.push({ title: t("Бизнес Аккаунт"), key: "ba.name", width: "152px" });
  return items;
});

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

function resolveItem(item: unknown, internalItem?: { raw?: Employee }): Employee {
  if (internalItem?.raw) {
    return internalItem.raw;
  }
  return item as Employee;
}

function toggleRow(employee: Employee) {
  expandedId.value = expandedId.value === employee.id ? null : employee.id;
}

function isExpanded(employee: Employee): boolean {
  return expandedId.value === employee.id;
}

function emitEmployeeUpdated() {
  emit("employee-updated");
}
</script>

<style scoped>
.employees-filters__item {
  flex: 1 1 0;
  min-width: 0;
}
</style>
