<!--
  Employees directory using virtualized data table.
-->
<template>
  <TableFirstPageLayout test-id="employees-view">
    <employees-virtual-table table-height="fill"
      :items="filteredEmployees"
      :all-items="employees"
      :loading="loading"
      :department-options="departmentOptions"
      :project-options="projectOptions"
      :business-account-options="baOptions"
      :search="filter.search"
      :department="filter.departments"
      :project="filter.projects"
      :business-account="filter.businessAccounts"
      @update:search="updateSearch"
      @update:department="updateDepartment"
      @update:project="updateProject"
      @update:ba="updateBa"
      @employee-updated="handleEmployeeUpdated"
    />
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { useI18n } from "vue-i18n";
import { useEmployeesDirectory } from "@/composables/useEmployeesDirectory";
import EmployeesVirtualTable from "@/views/employees/components/EmployeesVirtualTable.vue";

const { filteredEmployees, loading, filter, reload, employees } =
  useEmployeesDirectory();
const { t } = useI18n();

const projectOptions = computed(() => {
  const map = new Map<number | null, string>();
  map.set(null, t("Без проекта"));
  employees.value.forEach((employee) => {
    if (!employee.currentProject) {
      return;
    }
    if (!map.has(employee.currentProject.id)) {
      map.set(employee.currentProject.id, employee.currentProject.name);
    }
  });
  return Array.from(map.entries()).map(([value, title]) => ({ title, value }));
});

const baOptions = computed(() => {
  const map = new Map<number, string>();
  employees.value.forEach((employee) => {
    if (!employee.ba) {
      return;
    }
    if (!map.has(employee.ba.id)) {
      map.set(employee.ba.id, employee.ba.name);
    }
  });
  return Array.from(map.entries()).map(([value, title]) => ({ title, value }));
});

const departmentOptions = computed(() => {
  const map = new Map<number, string>();
  employees.value.forEach((employee) => {
    if (!employee.department) {
      return;
    }
    if (!map.has(employee.department.id)) {
      map.set(employee.department.id, employee.department.name);
    }
  });
  return Array.from(map.entries()).map(([value, title]) => ({ title, value }));
});

onMounted(() => {
  reload().catch(() => undefined);
});

function updateSearch(value: string) {
  filter.value.search = value;
}

function updateDepartment(value: number[]) {
  filter.value.departments = value;
}

function updateProject(value: Array<number | null>) {
  filter.value.projects = value;
}

function updateBa(value: number[]) {
  filter.value.businessAccounts = value;
}

function handleEmployeeUpdated() {
  reload().catch(() => undefined);
}

</script>
