<!--
  Employees directory using virtualized data table.
-->
<template>
  <v-container class="py-6">
    <employees-virtual-table
      :items="filteredEmployees"
      :loading="loading"
      :project-options="projectOptions"
      :business-account-options="baOptions"
      :search="filter.search"
      :project="filter.projects"
      :business-account="filter.businessAccounts"
      @update:search="updateSearch"
      @update:project="updateProject"
      @update:ba="updateBa"
      @employee-updated="handleEmployeeUpdated"
    />
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
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

onMounted(() => {
  reload().catch(() => undefined);
});

function updateSearch(value: string) {
  filter.value.search = value;
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
