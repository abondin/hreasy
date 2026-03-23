<!--
  Employees table with filters and details drawer.
-->
<template>
  <v-card data-testid="employees-table-card">
    <HREasyTableBase data-testid="employees-table"
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
        <v-card-title class="pb-0">
          <v-row>
            <v-col cols="12" :md="showExtendedFilters ? 4 : 12">
              <v-text-field
                v-model="localSearch"
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
                  v-model="selectedProject"
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
                  v-model="selectedBa"
                  data-testid="employees-filter-ba"
                  :items="baOptions"
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

  <v-dialog
    v-if="useFullscreenDetailsPanel"
    v-model="detailsOpen"
    fullscreen
    scrollable
  >
    <v-card>
      <v-toolbar density="comfortable" border="b">
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" @click="detailsOpen = false" />
      </v-toolbar>

      <v-card-text class="pa-2">
        <employee-details-panel
          v-if="selectedEmployee"
          :employee="selectedEmployee"
          @employee-updated="emitEmployeeUpdated"
        />
      </v-card-text>
    </v-card>
  </v-dialog>

  <v-navigation-drawer
    v-else
    v-model="detailsOpen"
    data-testid="employees-details-drawer"
    location="right"
    temporary
    :width="drawerWidth"
  >
    <v-toolbar density="comfortable" border="b">
      <v-spacer />
      <v-btn icon="mdi-close" variant="text" @click="detailsOpen = false" />
    </v-toolbar>

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
const showExtendedFilters = computed(() => !display.smAndDown.value);
const useFullscreenDetailsPanel = computed(() => display.mdAndDown.value);

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
const projectOptions = computed(() => props.projectOptions);
const baOptions = computed(() => props.businessAccountOptions);
const drawerWidth = computed(() => {
  if (display.lg.value) {
    return 960;
  }
  if (display.md.value) {
    return 900;
  }
  return 1040;
});

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
