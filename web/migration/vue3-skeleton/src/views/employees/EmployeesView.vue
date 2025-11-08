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
      :project="projectFilter"
      :business-account="baFilter"
      @update:search="updateSearch"
      @update:project="updateProject"
      @update:ba="updateBa"
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
  map.set(null, t("Все"));
  employees.value.forEach((employee) => {
    const id = employee.currentProject?.id ?? null;
    const label = employee.currentProject?.name ?? t("Без проекта");
    if (!map.has(id)) {
      map.set(id, label);
    }
  });
  return Array.from(map.entries()).map(([value, title]) => ({ title, value }));
});

const baOptions = computed(() => {
  const map = new Map<number | null, string>();
  map.set(null, t("Все"));
  employees.value.forEach((employee) => {
    const id = employee.ba?.id ?? null;
    const label = employee.ba?.name ?? t("Без БА");
    if (!map.has(id)) {
      map.set(id, label);
    }
  });
  return Array.from(map.entries()).map(([value, title]) => ({ title, value }));
});

const projectFilter = computed(() => filter.value.projects[0] ?? null);
const baFilter = computed(() => filter.value.businessAccounts[0] ?? null);

onMounted(() => {
  reload().catch(() => undefined);
});

function updateSearch(value: string) {
  filter.value.search = value;
}

function updateProject(value: number | null) {
  filter.value.projects = typeof value === "number" ? [value] : [];
}

function updateBa(value: number | null) {
  filter.value.businessAccounts = typeof value === "number" ? [value] : [];
}

</script>
