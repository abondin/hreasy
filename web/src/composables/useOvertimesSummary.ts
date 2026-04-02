import { computed, onMounted, reactive, ref } from "vue";
import type { ComposerTranslation } from "vue-i18n";
import { listEmployees, type Employee } from "@/services/employee.service";
import {
  closeOvertimePeriod,
  reopenOvertimePeriod,
} from "@/services/admin-overtime.service";
import { fetchProjects, type SimpleDict } from "@/services/projects.service";
import {
  exportOvertimes,
  fetchClosedOvertimes,
  fetchOvertimesSummary,
  ReportPeriod,
  type ClosedOvertimePeriod,
  type CommonApprovalStatus,
  type OvertimeEmployeeSummary,
} from "@/services/overtime.service";
import { extractDataTableRow } from "@/lib/data-table";
import { usePermissions } from "@/lib/permissions";

interface EmployeeRef {
  id: number;
  name: string;
}

export interface OvertimeRow {
  employee: EmployeeRef;
  employeeCurrentProject: string;
  overtimeProjects: string;
  totalHours: number;
  commonApprovalStatus: CommonApprovalStatus;
  overtimeProjectIds: number[];
}

export function useOvertimesSummary(t: ComposerTranslation) {
  const permissions = usePermissions();

  const loading = ref(false);
  const exportCompleted = ref(false);
  const selectedPeriodId = ref(ReportPeriod.currentPeriod().periodId());
  const selectedPeriod = computed(() => ReportPeriod.fromPeriodId(selectedPeriodId.value));

  const closedPeriods = ref<ClosedOvertimePeriod[]>([]);
  const employees = ref<Employee[]>([]);
  const projects = ref<SimpleDict[]>([]);
  const summary = ref<OvertimeEmployeeSummary[]>([]);

  const filter = reactive({
    search: "",
    showEmpty: true,
    selectedDepartments: [] as number[],
    selectedBusinessAccounts: [] as number[],
    selectedProjectsWithOvertimes: [] as number[],
    selectedEmployeeCurrentProjects: [] as number[],
  });

  const selectedEmployee = ref<EmployeeRef | null>(null);
  const employeeDialog = ref(false);

  const canViewAllOvertimes = computed(() => permissions.canViewAllOvertimes());
  const canExportOvertimes = computed(() => permissions.canExportOvertimes());
  const canAdminOvertimes = computed(() => permissions.canAdminOvertimes());

  const headers = computed(() => [
    { title: t("Сотрудник"), key: "employee.name" },
    { title: t("Текущий проект"), key: "employeeCurrentProject" },
    { title: t("Проекты овертаймов"), key: "overtimeProjects" },
    { title: t("Всего"), key: "totalHours" },
    { title: t("Статус согласования"), key: "commonApprovalStatus" },
  ]);

  const activeProjects = computed(() =>
    projects.value.filter((project) => project.active !== false),
  );

  const periodClosed = computed(() =>
    closedPeriods.value.some((period) => period.period === selectedPeriodId.value),
  );

  const baseRows = computed<OvertimeRow[]>(() => {
    const overtimeByEmployee = new Map<number, OvertimeEmployeeSummary>();
    summary.value.forEach((item) => overtimeByEmployee.set(item.employeeId, item));

    return employees.value.map((employee) => {
      const employeeSummary = overtimeByEmployee.get(employee.id);
      const items = employeeSummary?.items ?? [];
      return {
        employee: {
          id: employee.id,
          name: employee.displayName,
        },
        employeeCurrentProject: employee.currentProject?.name ?? t("Без проекта"),
        overtimeProjects: items
          .map((item) => projectNameById(item.projectId))
          .filter((name, index, array) => array.indexOf(name) === index)
          .join(", "),
        totalHours: items.reduce((sum, item) => sum + item.hours, 0),
        commonApprovalStatus: employeeSummary?.commonApprovalStatus ?? "NO_DECISIONS",
        overtimeProjectIds: [...new Set(items.map((item) => item.projectId))],
      };
    });
  });

  const projectsWithOvertimes = computed(() => {
    const ids = new Set<number>();
    baseRows.value.forEach((row) => {
      row.overtimeProjectIds.forEach((id) => ids.add(id));
    });
    return projects.value.filter((project) => ids.has(project.id));
  });

  const filteredOvertimes = computed(() =>
    baseRows.value.filter((row) => {
      if (
        filter.search &&
        !row.employee.name.toLowerCase().includes(filter.search.toLowerCase())
      ) {
        return false;
      }

      const employee = employees.value.find((item) => item.id === row.employee.id);
      if (
        filter.selectedDepartments.length > 0 &&
        !filter.selectedDepartments.includes(employee?.department?.id ?? -1)
      ) {
        return false;
      }

      if (
        filter.selectedBusinessAccounts.length > 0 &&
        !filter.selectedBusinessAccounts.includes(employee?.ba?.id ?? -1)
      ) {
        return false;
      }

      if (
        filter.selectedEmployeeCurrentProjects.length > 0 &&
        !filter.selectedEmployeeCurrentProjects.includes(employee?.currentProject?.id ?? -1)
      ) {
        return false;
      }

      if (filter.selectedProjectsWithOvertimes.length > 0) {
        const hasAnyProject = row.overtimeProjectIds.some((projectId) =>
          filter.selectedProjectsWithOvertimes.includes(projectId),
        );
        if (!hasAnyProject) {
          return false;
        }
      }

      if (!filter.showEmpty && row.totalHours <= 0) {
        return false;
      }

      return true;
    }),
  );

  const totalHours = computed(() =>
    filteredOvertimes.value.reduce((sum, row) => sum + row.totalHours, 0),
  );

  onMounted(() => {
    fetchData().catch(() => undefined);
  });

  async function fetchData(): Promise<void> {
    if (!canViewAllOvertimes.value) {
      return;
    }
    loading.value = true;
    try {
      const [employeesData, projectsData, closedPeriodsData, summaryData] = await Promise.all([
        listEmployees(),
        fetchProjects(),
        fetchClosedOvertimes(),
        fetchOvertimesSummary(selectedPeriodId.value),
      ]);

      employees.value = employeesData;
      projects.value = projectsData;
      closedPeriods.value = closedPeriodsData;
      summary.value = summaryData;
    } finally {
      loading.value = false;
    }
  }

  function shiftPeriod(delta: number): void {
    const period = ReportPeriod.fromPeriodId(selectedPeriodId.value);
    if (delta > 0) {
      period.increment();
    } else {
      period.decrement();
    }
    selectedPeriodId.value = period.periodId();
    fetchData().catch(() => undefined);
  }

  function incrementPeriod(): void {
    shiftPeriod(1);
  }

  function decrementPeriod(): void {
    shiftPeriod(-1);
  }

  function closeEmployeeDialog(): void {
    employeeDialog.value = false;
    selectedEmployee.value = null;
  }

  function onRowClick(_event: Event, payload: unknown): void {
    const row = extractRow(payload);
    if (!row?.employee) {
      return;
    }
    selectedEmployee.value = row.employee;
    employeeDialog.value = true;
  }

  async function exportToExcel(): Promise<void> {
    loading.value = true;
    try {
      await exportOvertimes(selectedPeriodId.value);
      exportCompleted.value = true;
    } finally {
      loading.value = false;
    }
  }

  async function closePeriod(): Promise<void> {
    loading.value = true;
    try {
      await closeOvertimePeriod(selectedPeriodId.value);
      await fetchData();
    } finally {
      loading.value = false;
    }
  }

  async function reopenPeriod(): Promise<void> {
    loading.value = true;
    try {
      await reopenOvertimePeriod(selectedPeriodId.value);
      await fetchData();
    } finally {
      loading.value = false;
    }
  }

  function extractRow(payload: unknown): OvertimeRow | null {
    return extractDataTableRow(payload, isOvertimeRow);
  }

  function isOvertimeRow(value: unknown): value is OvertimeRow {
    if (!value || typeof value !== "object") {
      return false;
    }
    return "employee" in value && "totalHours" in value;
  }

  function projectNameById(projectId: number): string {
    const project = projects.value.find((item) => item.id === projectId);
    if (!project) {
      return `${t("Не найден: ")}${projectId}`;
    }
    return project.name;
  }

  return {
    loading,
    exportCompleted,
    selectedPeriodId,
    selectedPeriod,
    closedPeriods,
    employees,
    filter,
    selectedEmployee,
    employeeDialog,
    canViewAllOvertimes,
    canExportOvertimes,
    canAdminOvertimes,
    headers,
    activeProjects,
    periodClosed,
    projectsWithOvertimes,
    filteredOvertimes,
    totalHours,
    fetchData,
    incrementPeriod,
    decrementPeriod,
    closeEmployeeDialog,
    onRowClick,
    exportToExcel,
    closePeriod,
    reopenPeriod,
  };
}
