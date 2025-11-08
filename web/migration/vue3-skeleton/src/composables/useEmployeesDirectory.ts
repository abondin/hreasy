import { computed, ref, watch } from "vue";
import type { Employee } from "@/services/employee.service";
import { listEmployees } from "@/services/employee.service";

export interface EmployeesFilter {
  search: string;
  projects: Array<number | null>;
  businessAccounts: number[];
}

const defaultFilter: EmployeesFilter = {
  search: "",
  projects: [],
  businessAccounts: [],
};

export function useEmployeesDirectory(initialFilter?: Partial<EmployeesFilter>) {
  const employees = ref<Employee[]>([]);
  const loading = ref(false);
  const error = ref<unknown>(null);
  const filter = ref<EmployeesFilter>({
    ...defaultFilter,
    ...initialFilter,
  });

  async function reload() {
    loading.value = true;
    error.value = null;
    try {
      const items = await listEmployees();
      employees.value = items;
    } catch (err) {
      error.value = err;
      employees.value = [];
    } finally {
      loading.value = false;
    }
  }

  const filteredEmployees = computed(() => {
    const search = filter.value.search.trim().toLowerCase();
    return employees.value.filter((employee) => {
      if (
        filter.value.projects.length > 0 &&
        !filter.value.projects.includes(employee.currentProject?.id ?? null)
      ) {
        return false;
      }
      if (
        filter.value.businessAccounts.length > 0 &&
        !filter.value.businessAccounts.includes(employee.ba?.id ?? -1)
      ) {
        return false;
      }
      if (!search) {
        return true;
      }
      const haystack = [
        employee.displayName,
        employee.department?.name,
        employee.email,
        employee.currentProject?.name,
        employee.currentProject?.role,
        employee.position?.name,
        employee.telegram,
      ]
        .concat(employee.skills?.map((skill) => skill.name) ?? [])
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
      return haystack.includes(search);
    });
  });

  watch(
    () => filter.value,
    () => {
      // No-op, computed will react
    },
    { deep: true },
  );

  return {
    employees,
    filteredEmployees,
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    filter,
    reload,
  };
}
