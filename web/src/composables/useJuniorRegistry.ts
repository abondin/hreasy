import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import type { ComposerTranslation } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import { getJuniorProgressIcon } from "@/lib/mentorship";
import {
  addJuniorToRegistry,
  exportJuniorsRegistry,
  fetchBusinessAccounts,
  fetchCurrentProjectRoles,
  fetchEmployeesForRegistry,
  fetchJuniorsRegistry,
  type AddJuniorRegistryBody,
  type CurrentProjectRole,
  type JuniorDto,
  type JuniorReport,
  type SimpleDict,
} from "@/services/junior-registry.service";
import type { Employee } from "@/services/employee.service";

interface JuniorFilter {
  search: string;
  onlyNotGraduated: boolean;
  selectedBas: number[];
  selectedRoles: string[];
  selectedCurrentProjects: number[];
}

export function useJuniorRegistry(t: ComposerTranslation) {
  const router = useRouter();
  const permissions = usePermissions();

  const loading = ref(false);
  const actionLoading = ref(false);
  const exportLoading = ref(false);
  const error = ref("");
  const juniors = ref<JuniorDto[]>([]);
  const addDialog = ref(false);
  const allBusinessAccounts = ref<SimpleDict[]>([]);
  const projectRoles = ref<CurrentProjectRole[]>([]);
  const employees = ref<Employee[]>([]);

  const addForm = reactive<AddJuniorRegistryBody>({
    juniorEmplId: null,
    mentorId: null,
    budgetingAccount: null,
    role: "",
  });

  const filter = reactive<JuniorFilter>({
    search: "",
    onlyNotGraduated: true,
    selectedBas: [],
    selectedRoles: [],
    selectedCurrentProjects: [],
  });

  const canViewMentorship = computed(
    () => permissions.canAccessJuniorsRegistry() || permissions.canAdminJuniorRegistry(),
  );
  const canManageRegistry = computed(() => permissions.canAdminJuniorRegistry());

  const headers = computed(() => [
    { title: t("Молодой специалист"), key: "juniorEmpl.name", width: "280px" },
    { title: t("Ментор"), key: "mentor.name", width: "240px" },
    { title: t("Роль"), key: "role", width: "150px" },
    { title: t("Бюджет"), key: "budgetingAccount.name", width: "120px" },
    { title: t("Текущий проект"), key: "currentProject.name", width: "180px" },
    { title: t("Месяцев в компании"), key: "juniorInCompanyMonths.value", width: "110px" },
    { title: t("Месяцев без отчёта"), key: "monthsWithoutReport.value", width: "130px" },
    { title: t("Прогресс"), key: "progress", sortable: false, width: "100px" },
    { title: t("Последний срез (Когда)"), key: "latestReport.createdAt", width: "160px" },
    { title: t("Последний срез (Кто)"), key: "latestReport.createdBy.name", width: "220px" },
    { title: t("Последний срез (Комментарий)"), key: "latestReport.comment", width: "360px" },
    { title: t("Завершил обучение"), key: "graduation.graduatedAt", width: "150px" },
  ]);

  const baOptions = computed<SimpleDict[]>(() => {
    const map = new Map<number, SimpleDict>();
    juniors.value.forEach((item) => {
      if (item.budgetingAccount?.id && item.budgetingAccount?.name) {
        map.set(item.budgetingAccount.id, item.budgetingAccount);
      }
    });
    return [...map.values()];
  });

  const roles = computed<string[]>(() =>
    [...new Set(juniors.value.map((item) => item.role).filter(Boolean))].sort(),
  );
  const currentProjectOptions = computed<SimpleDict[]>(() => {
    const map = new Map<number, SimpleDict>();
    juniors.value.forEach((item) => {
      if (item.currentProject?.id && item.currentProject?.name) {
        map.set(item.currentProject.id, item.currentProject);
      }
    });
    return [...map.values()].sort((a, b) => a.name.localeCompare(b.name));
  });

  const filteredItems = computed(() => {
    const search = filter.search.trim().toLowerCase();
    return juniors.value.filter((item) => {
      if (filter.onlyNotGraduated && item.graduation) {
        return false;
      }
      if (
        filter.selectedBas.length > 0
        && (!item.budgetingAccount || !filter.selectedBas.includes(item.budgetingAccount.id))
      ) {
        return false;
      }
      if (filter.selectedRoles.length > 0 && !filter.selectedRoles.includes(item.role)) {
        return false;
      }
      if (
        filter.selectedCurrentProjects.length > 0
        && (!item.currentProject || !filter.selectedCurrentProjects.includes(item.currentProject.id))
      ) {
        return false;
      }
      if (!search) {
        return true;
      }

      return [
        item.juniorEmpl?.name,
        item.mentor?.name,
        item.latestReport?.createdBy?.name,
        item.role,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(search);
    });
  });

  watch(
    () => addForm.juniorEmplId,
    (juniorId) => {
      const employee = employees.value.find((item) => item.id === juniorId);
      if (!employee) {
        return;
      }
      addForm.budgetingAccount = employee.ba?.id ?? null;
      addForm.role = employee.currentProject?.role ?? "";
      addForm.mentorId = null;
    },
  );

  function buildRowProps({ item }: { item: JuniorDto }): Record<string, unknown> {
    return {
      class: "cursor-pointer",
      onClick: () => {
        router
          .push({ name: "mentorship-details", params: { juniorRegistryId: item.id } })
          .catch(() => undefined);
      },
    };
  }

  function reportsOrderedAsc(reports: JuniorReport[]): JuniorReport[] {
    return [...(reports ?? [])].sort((a, b) => a.createdAt.localeCompare(b.createdAt));
  }

  function openAddDialog(): void {
    addDialog.value = true;
  }

  function resetAddForm(): void {
    addForm.juniorEmplId = null;
    addForm.mentorId = null;
    addForm.budgetingAccount = null;
    addForm.role = "";
  }

  async function loadDictionaries(): Promise<void> {
    const [bas, rolesList, employeesList] = await Promise.all([
      fetchBusinessAccounts(),
      fetchCurrentProjectRoles(),
      fetchEmployeesForRegistry(),
    ]);
    allBusinessAccounts.value = bas;
    projectRoles.value = rolesList;
    employees.value = employeesList;
  }

  async function loadJuniors(): Promise<void> {
    if (!canViewMentorship.value) {
      return;
    }
    loading.value = true;
    error.value = "";
    try {
      juniors.value = await fetchJuniorsRegistry();
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      loading.value = false;
    }
  }

  async function submitAddJunior(): Promise<void> {
    if (!addForm.juniorEmplId || !canManageRegistry.value) {
      return;
    }
    actionLoading.value = true;
    try {
      await addJuniorToRegistry(addForm);
      addDialog.value = false;
      resetAddForm();
      await loadJuniors();
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      actionLoading.value = false;
    }
  }

  async function downloadExport(): Promise<void> {
    if (!canManageRegistry.value) {
      return;
    }
    exportLoading.value = true;
    error.value = "";
    try {
      const blob = await exportJuniorsRegistry(!filter.onlyNotGraduated);
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = `JuniorsRegistry_${new Date().toISOString().slice(0, 10)}.xlsx`;
      link.click();
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      exportLoading.value = false;
    }
  }

  onMounted(() => {
    Promise.all([loadJuniors(), loadDictionaries()]).catch((err: unknown) => {
      error.value = errorUtils.shortMessage(err);
    });
  });

  return {
    loading,
    actionLoading,
    exportLoading,
    error,
    addDialog,
    allBusinessAccounts,
    projectRoles,
    employees,
    addForm,
    filter,
    canViewMentorship,
    canManageRegistry,
    headers,
    baOptions,
    roles,
    currentProjectOptions,
    filteredItems,
    buildRowProps,
    getProgressIcon: getJuniorProgressIcon,
    reportsOrderedAsc,
    openAddDialog,
    loadJuniors,
    submitAddJunior,
    downloadExport,
  };
}
