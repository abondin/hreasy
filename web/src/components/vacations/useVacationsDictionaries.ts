import { ref } from "vue";
import { listEmployees, type Dict } from "@/services/employee.service";
import {
  fetchCurrentProjectRoles,
  fetchProjects,
  type ProjectDictDto,
} from "@/services/projects.service";
import { fetchDaysNotIncludedInVacations } from "@/services/dict.service";

const projectOptions = ref<ProjectDictDto[]>([]);
const projectRoles = ref<string[]>([]);
const employees = ref<Dict[]>([]);
const daysNotIncludedInVacations = ref<string[]>([]);

let projectsAndRolesPromise: Promise<void> | null = null;
let employeesPromise: Promise<void> | null = null;
const loadedDaysYears = new Set<number>();

async function loadProjectsAndRoles() {
  if (projectOptions.value.length && projectRoles.value.length) {
    return;
  }
  if (projectsAndRolesPromise) {
    return projectsAndRolesPromise;
  }
  projectsAndRolesPromise = Promise.all([
    fetchProjects(),
    fetchCurrentProjectRoles(),
  ])
    .then(([projects, roles]) => {
      projectOptions.value = projects.filter(
        (project) => project.active !== false,
      );
      projectRoles.value = roles.map((role) => role.value).sort();
    })
    .finally(() => {
      projectsAndRolesPromise = null;
    });

  return projectsAndRolesPromise;
}

async function loadEmployees() {
  if (employees.value.length) {
    return;
  }
  if (employeesPromise) {
    return employeesPromise;
  }
  employeesPromise = listEmployees()
    .then((result) => {
      employees.value = result.map((employee) => ({
        id: employee.id,
        name: employee.displayName,
      }));
    })
    .finally(() => {
      employeesPromise = null;
    });

  return employeesPromise;
}

async function loadDaysNotIncluded(years: number[]) {
  const missingYears = years.filter((year) => !loadedDaysYears.has(year));
  if (!missingYears.length) {
    return;
  }
  const fetched = await fetchDaysNotIncludedInVacations(missingYears);
  fetched.forEach((date) => {
    const year = Number(date.slice(0, 4));
    if (!Number.isNaN(year)) {
      loadedDaysYears.add(year);
    }
  });
  daysNotIncludedInVacations.value = Array.from(
    new Set([...daysNotIncludedInVacations.value, ...fetched]),
  );
}

export function useVacationsDictionaries() {
  return {
    projectOptions,
    projectRoles,
    employees,
    daysNotIncludedInVacations,
    loadProjectsAndRoles,
    loadEmployees,
    loadDaysNotIncluded,
  };
}
