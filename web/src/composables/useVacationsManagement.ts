import { computed, reactive, ref, watch } from "vue";
import type { ComposerTranslation } from "vue-i18n";
import {
  employeeVacationSummaryMapper,
  type EmployeeVacationSummary,
} from "@/components/vacations/employeeVacationSummaryService";
import { errorUtils } from "@/lib/errors";
import { formatDate } from "@/lib/datetime";
import {
  formatDateOnly,
  getDefaultYears,
  getEndOfYear,
  getStartOfYear,
  isDateInRange,
} from "@/lib/vacation-dates";
import { usePermissions } from "@/lib/permissions";
import {
  exportVacations,
  fetchVacations,
  type Vacation,
  type VacationStatus,
  vacationStatuses,
} from "@/services/vacation.service";
import { listEmployees, type Employee } from "@/services/employee.service";
import { useVacationsDictionaries } from "@/components/vacations/useVacationsDictionaries";

export function useVacationsManagement(t: ComposerTranslation) {
  const permissions = usePermissions();

  const canViewVacations = computed(() => permissions.canViewAllVacations());
  const canEditVacations = computed(() => permissions.canEditAllVacations());
  const canExportVacations = computed(() => permissions.canExportAllVacations());

  const selectedTab = ref(0);
  const allYears = getDefaultYears();
  const selectedYear = ref(new Date().getFullYear());

  const filter = reactive({
    selectedStatuses: ["PLANNED", "TAKEN", "REQUESTED"] as VacationStatus[],
    search: "",
    selectedProjects: [] as number[],
    selectedDepartments: [] as number[],
    selectedBusinessAccounts: [] as number[],
    selectedProjectRoles: [] as string[],
    selectedDates: [] as string[],
  });

  const loading = ref(false);
  const vacations = ref<Vacation[]>([]);
  const employeeDirectory = ref<Employee[]>([]);
  const snackbarNotification = ref(false);
  const snackbarMessage = ref("");

  const {
    projectOptions,
    projectRoles,
    employees: allEmployees,
    daysNotIncludedInVacations,
    loadProjectsAndRoles,
    loadEmployees,
    loadDaysNotIncluded,
  } = useVacationsDictionaries();

  const headers = computed(() => [
    { title: t("ФИО"), key: "employeeDisplayName" },
    { title: t("Текущий проект"), key: "employeeCurrentProject.name" },
    { title: t("Роль на проекте"), key: "employeeCurrentProject.role" },
    { title: t("Год"), key: "year" },
    { title: t("Начало"), key: "startDate" },
    { title: t("Окончание"), key: "endDate" },
    { title: t("Кол-во дней"), key: "daysNumber" },
    { title: t("Статус"), key: "status" },
    { title: t("Документ"), key: "documents" },
    { title: t("Примечание"), key: "notes" },
  ]);

  const summaryHeaders = computed(() => [
    { title: t("ФИО"), key: "employeeDisplayName" },
    { title: t("Текущий проект"), key: "employeeCurrentProject.name" },
    { title: t("Роль на проекте"), key: "employeeCurrentProject.role" },
    { title: t("Год"), key: "year" },
    { title: t("Количество отпусков"), key: "vacationsNumber" },
    { title: t("Общее количество дней"), key: "vacationsTotalDays" },
    { title: t("Текущий или ближайший отпуск"), key: "upcomingVacation" },
  ]);

  const allStatuses = computed(() =>
    vacationStatuses.map((status) => ({
      value: status,
      title: t(`VACATION_STATUS_ENUM.${status}`),
    })),
  );

  const filteredItems = computed(() => vacations.value.filter(filterItem));
  const filteredSummaryItems = computed(() =>
    employeeVacationSummaryMapper.map(vacations.value).filter(filterItem),
  );

  watch(selectedYear, () => {
    fetchData(true).catch(() => undefined);
  });

  watch(canViewVacations, (value) => {
    if (!value || vacations.value.length) {
      return;
    }
    bootstrapData().catch(() => undefined);
  });

  async function bootstrapData() {
    loading.value = true;
    try {
      await Promise.all([
        loadProjectsAndRoles(),
        loadEmployees(),
        listEmployees().then((items) => {
          employeeDirectory.value = items;
        }),
        loadDaysNotIncluded(allYears),
      ]);
      await fetchData(true);
    } catch (error) {
      snackbarMessage.value = errorUtils.shortMessage(error);
      snackbarNotification.value = true;
    } finally {
      loading.value = false;
    }
  }

  async function fetchData(resetFilter: boolean) {
    loading.value = true;
    try {
      const data = await fetchVacations([selectedYear.value]);
      vacations.value = data.filter(
        (item) => item.startDate && item.endDate && item.employeeDisplayName,
      );
      if (resetFilter) {
        resetSelectedDatesFilterToDefault();
      }
    } finally {
      loading.value = false;
    }
  }

  function resetSelectedDatesFilterToDefault() {
    const start = formatDateOnly(getStartOfYear(selectedYear.value));
    const end = formatDateOnly(getEndOfYear(selectedYear.value));
    filter.selectedDates = [start, end];
  }

  function filterItem(item: Vacation | EmployeeVacationSummary) {
    const employee = employeeDirectory.value.find((employeeItem) => employeeItem.id === item.employee);
    let filtered = true;
    filtered =
      filtered &&
      (filter.selectedProjects.length === 0 ||
        Boolean(
          item.employeeCurrentProject &&
            filter.selectedProjects.includes(item.employeeCurrentProject.id),
        ));
    filtered =
      filtered &&
      (filter.selectedDepartments.length === 0 ||
        Boolean(
          employee?.department?.id &&
            filter.selectedDepartments.includes(employee.department.id),
        ));
    filtered =
      filtered &&
      (filter.selectedBusinessAccounts.length === 0 ||
        Boolean(
          employee?.ba?.id &&
            filter.selectedBusinessAccounts.includes(employee.ba.id),
        ));
    filtered =
      filtered &&
      (filter.selectedProjectRoles.length === 0 ||
        Boolean(
          item.employeeCurrentProject?.role &&
            filter.selectedProjectRoles.includes(item.employeeCurrentProject.role),
        ));

    if ("status" in item) {
      filtered =
        filtered &&
        (filter.selectedStatuses.length === 0 ||
          filter.selectedStatuses.includes(item.status));
      filtered = filtered && isDateInRange(item.startDate, filter.selectedDates);
    }

    if (filter.search) {
      const search = filter.search.toLowerCase();
      const combined = `${item.employeeDisplayName ?? ""} ${
        item.employeeCurrentProject?.role ?? ""
      }`.toLowerCase();
      filtered = filtered && combined.includes(search);
    }

    return filtered;
  }

  async function exportToExcel() {
    loading.value = true;
    try {
      await exportVacations([selectedYear.value]);
      snackbarMessage.value = t("Экспорт успешно завершён. Файл скачен.");
      snackbarNotification.value = true;
    } finally {
      loading.value = false;
    }
  }

  function selectEmployee(item: { employeeDisplayName: string }) {
    filter.search = item.employeeDisplayName;
    selectedTab.value = 0;
  }

  function statusesForForm(vacation?: Vacation | null) {
    if (vacation && vacation.status === "REQUESTED") {
      return allStatuses.value;
    }
    return allStatuses.value.filter((status) => status.value !== "REQUESTED");
  }

  function timeLineYearChanged(year: number) {
    selectedYear.value = year;
    fetchData(true).catch(() => undefined);
  }

  async function copyToClipboard(vacation: Vacation | null) {
    if (!vacation) {
      return;
    }
    const text = `${vacation.employeeDisplayName} ${
      vacation.employeeCurrentProject?.name ?? ""
    } ${formatDate(vacation.startDate)}-${formatDate(vacation.endDate)} ${
      vacation.daysNumber
    } ${t(`VACATION_STATUS_ENUM.${vacation.status}`)}`.replace(/\s\s+/g, " ");

    try {
      await navigator.clipboard.writeText(text);
      snackbarMessage.value = t("Скопировано в буфер обмена");
      snackbarNotification.value = true;
    } catch (error) {
      snackbarMessage.value = errorUtils.shortMessage(error);
      snackbarNotification.value = true;
    }
  }

  function init() {
    if (!canViewVacations.value) {
      return;
    }
    bootstrapData().catch(() => undefined);
  }

  return {
    canViewVacations,
    canEditVacations,
    canExportVacations,
    selectedTab,
    headers,
    summaryHeaders,
    allStatuses,
    allYears,
    selectedYear,
    filter,
    loading,
    projectOptions,
    projectRoles,
    allEmployees,
    employeeDirectory,
    daysNotIncludedInVacations,
    snackbarNotification,
    snackbarMessage,
    filteredItems,
    filteredSummaryItems,
    fetchData,
    exportToExcel,
    selectEmployee,
    statusesForForm,
    timeLineYearChanged,
    copyToClipboard,
    init,
  };
}
