<!--
  Employees table orchestrator.
  Keeps page state wiring, URL sync, and responsive details behavior.
-->
<template>
  <v-card class="d-flex flex-column h-100" data-testid="employees-table-card">
    <employees-filters
      :project-options="projectOptions"
      :business-account-options="baOptions"
      :search="search"
      :project="project"
      :business-account="businessAccount"
      @update:search="emit('update:search', $event)"
      @update:project="emit('update:project', $event)"
      @update:business-account="emit('update:ba', $event)"
    />

    <div class="flex-grow-1 min-h-0">
      <employees-table
      :items="items"
      :loading="loading"
      :headers="headers"
      :table-height="tableHeight"
      @select-employee="openEmployeeDetails"
    />
    </div>
  </v-card>

  <employees-details-panel-host
    v-model="detailsOpen"
    :employee="selectedEmployee"
    :use-fullscreen="useFullscreenDetailsPanel"
    :drawer-width="drawerWidth"
    @employee-updated="emit('employee-updated')"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useDisplay } from 'vuetify';
import type { VDataTable } from 'vuetify/components';
import type { Employee } from '@/services/employee.service';
import { usePermissions } from '@/lib/permissions';
import EmployeesFilters from '@/views/employees/components/EmployeesFilters.vue';
import EmployeesTable from '@/views/employees/components/EmployeesTable.vue';
import EmployeesDetailsPanelHost from '@/views/employees/components/EmployeesDetailsPanelHost.vue';

type DataTableHeader = VDataTable['$props']['headers'];
type MutableHeader = {
  title: string;
  key: string;
  width: string;
};

const props = defineProps<{
  items: Employee[];
  allItems: Employee[];
  loading: boolean;
  projectOptions: Array<{ title: string; value: number | null }>;
  businessAccountOptions: Array<{ title: string; value: number }>;
  search?: string;
  project?: Array<number | null>;
  businessAccount?: number[];
  tableHeight?: number | string;
}>();

const emit = defineEmits<{
  (event: 'update:search', value: string): void;
  (event: 'update:project', value: Array<number | null>): void;
  (event: 'update:ba', value: number[]): void;
  (event: 'employee-updated'): void;
}>();

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const permissions = usePermissions();
const display = useDisplay();

const detailsOpen = ref(false);
const selectedEmployee = ref<Employee | null>(null);

const useFullscreenDetailsPanel = computed(() => display.mdAndDown.value);
const headers = computed<DataTableHeader>(() => {
  const items: MutableHeader[] = [
    { title: t('ФИО'), key: 'displayName', width: '240px' },
    { title: t('Отдел'), key: 'department.name', width: '260px' },
    { title: t('E-mail'), key: 'email', width: '172px' },
    { title: t('Текущий проект'), key: 'currentProject.name', width: '192px' },
  ];
  if (permissions.canViewEmplCurrentProjectRole()) {
    items.push({ title: t('Роль на проекте'), key: 'currentProject.role', width: '172px' });
  }
  items.push({ title: t('Бизнес Аккаунт'), key: 'ba.name', width: '152px' });
  return items;
});
const tableHeight = computed(() => props.tableHeight ?? 'fill');
const projectOptions = computed(() => props.projectOptions);
const baOptions = computed(() => props.businessAccountOptions);
const selectedEmployeeIdFromRoute = computed(() => {
  const value = route.query.employeeId;
  if (typeof value !== 'string') {
    return null;
  }
  const parsed = Number(value);
  return Number.isInteger(parsed) ? parsed : null;
});
const drawerWidth = computed(() => {
  if (display.lg.value) {
    return 960;
  }
  if (display.md.value) {
    return 900;
  }
  return 1040;
});

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

watch(
  [selectedEmployeeIdFromRoute, () => props.allItems],
  ([employeeId, allItems]) => {
    if (employeeId == null) {
      selectedEmployee.value = null;
      detailsOpen.value = false;
      return;
    }

    const employee = allItems.find((item) => item.id === employeeId) ?? null;
    selectedEmployee.value = employee;
    detailsOpen.value = Boolean(employee);
  },
  { immediate: true },
);

watch(detailsOpen, (open) => {
  if (open) {
    return;
  }

  const nextQuery = { ...route.query };
  delete nextQuery.employeeId;
  router.replace({ query: nextQuery }).catch(() => undefined);
});

function openEmployeeDetails(row: Employee) {
  selectedEmployee.value = row;
  detailsOpen.value = true;
  router.replace({
    query: {
      ...route.query,
      employeeId: String(row.id),
    },
  }).catch(() => undefined);
}
</script>