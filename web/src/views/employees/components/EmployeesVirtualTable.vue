<!--
  Employees table with filters and details drawer.
-->
<template>
  <v-card>
    <HREasyTableBase
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
      @click:row="openEmployeeDetails"
    >
      <template #filters>
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
      </template>

      <template #[`item.department.name`]="{ item }">
        {{ item.department?.name ?? t("Не задан") }}
      </template>
      <template #[`item.currentProject.name`]="{ item }">
        {{ item.currentProject?.name ?? t("Не задан") }}
      </template>
      <template #[`item.currentProject.role`]="{ item }">
        {{ item.currentProject?.role ?? t("Не задан") }}
      </template>
      <template #[`item.ba.name`]="{ item }">
        {{ item.ba?.name ?? t("Не задан") }}
      </template>
    </HREasyTableBase>
  </v-card>

  <v-navigation-drawer
    v-model="detailsOpen"
    location="right"
    temporary
    :width="drawerWidth"
  >
    <div class="d-flex align-center justify-end px-4 py-3 border-b">
      <v-btn icon="mdi-close" variant="text" @click="detailsOpen = false" />
    </div>

    <div class="pa-2">
      <employee-details-panel
        v-if="selectedEmployee"
        :employee="selectedEmployee"
        @employee-updated="emitEmployeeUpdated"
      />
    </div>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify";
import type { Employee } from "@/services/employee.service";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import EmployeeDetailsPanel from "@/views/employees/components/EmployeeDetailsPanel.vue";
import { usePermissions } from "@/lib/permissions";
import { extractDataTableRow } from "@/lib/data-table";

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
const display = useDisplay();

const localSearch = ref(props.search ?? "");
const selectedProject = ref<Array<number | null>>(props.project ?? []);
const selectedBa = ref<number[]>(props.businessAccount ?? []);

const detailsOpen = ref(false);
const selectedEmployee = ref<Employee | null>(null);

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

watch(
  () => props.items,
  (nextItems) => {
    if (!selectedEmployee.value) {
      return;
    }
    const updated = nextItems.find((item) => item.id === selectedEmployee.value?.id);
    if (updated) {
      selectedEmployee.value = updated;
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
const drawerWidth = computed(() => {
  const width = Number(display.width.value);
  if (display.smAndDown.value) {
    return width;
  }
  if (display.xlAndUp.value) {
    return Math.round(width / 3);
  }
  return Math.round(width / 2);
});

const projectOptions = computed(() => props.projectOptions);
const baOptions = computed(() => props.businessAccountOptions);

function rowProps() {
  return {
    class: "cursor-pointer",
  };
}

function openEmployeeDetails(
  _event: Event,
  payload: unknown,
) {
  const row = extractRow(payload);
  if (!row) {
    return;
  }
  selectedEmployee.value = row;
  detailsOpen.value = true;
}

function extractRow(payload: unknown): Employee | null {
  return extractDataTableRow<Employee>(payload);
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
