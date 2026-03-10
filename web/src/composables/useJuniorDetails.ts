import { computed, onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import type { ComposerTranslation } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import { juniorReportRatingFields, fillJuniorReportForm, getJuniorProgressIcon } from "@/lib/mentorship";
import { usePermissions } from "@/lib/permissions";
import {
  cancelJuniorGraduation,
  createJuniorReport,
  deleteJuniorFromRegistry,
  deleteJuniorReport,
  fetchBusinessAccounts,
  fetchCurrentProjectRoles,
  fetchEmployeesForRegistry,
  fetchJuniorDetails,
  graduateJunior,
  JuniorProgressType,
  juniorProgressTypes,
  type AddOrUpdateJuniorReportBody,
  type CurrentProjectRole,
  type JuniorDto,
  type SimpleDict,
  type UpdateJuniorRegistryBody,
  updateJuniorRegistry,
  updateJuniorReport,
} from "@/services/junior-registry.service";
import { findEmployee, type Employee } from "@/services/employee.service";

export function useJuniorDetails(t: ComposerTranslation) {
  const route = useRoute();
  const router = useRouter();
  const permissions = usePermissions();

  const loading = ref(false);
  const actionLoading = ref(false);
  const error = ref("");
  const junior = ref<JuniorDto | null>(null);
  const employee = ref<Employee | null>(null);
  const deleteDialog = ref(false);
  const graduateDialog = ref(false);
  const deleteReportDialog = ref(false);
  const reportDialog = ref(false);
  const editDialog = ref(false);
  const reportToDelete = ref<number | null>(null);
  const editingReportId = ref<number | null>(null);
  const graduationComment = ref("");
  const employees = ref<Employee[]>([]);
  const allBusinessAccounts = ref<SimpleDict[]>([]);
  const projectRoles = ref<CurrentProjectRole[]>([]);

  const reportForm = reactive<AddOrUpdateJuniorReportBody>({
    progress: JuniorProgressType.NO_PROGRESS,
    comment: "",
    ratings: {
      overallReadiness: 3,
      competence: 3,
      process: 3,
      teamwork: 3,
      contribution: 3,
      motivation: 3,
    },
  });

  const updateForm = reactive<UpdateJuniorRegistryBody>({
    mentorId: null,
    role: "",
    budgetingAccount: null,
  });

  const canViewMentorship = computed(
    () => permissions.canAccessJuniorsRegistry() || permissions.canAdminJuniorRegistry(),
  );

  const canEditRegistry = computed(() => {
    if (!junior.value) {
      return false;
    }
    return permissions.canUpdateJuniorRegistryInfo(junior.value.createdBy.id);
  });

  const sortedReports = computed(() =>
    [...(junior.value?.reports ?? [])].sort((a, b) => b.createdAt.localeCompare(a.createdAt)),
  );

  const progressOptions = computed(() =>
    juniorProgressTypes.map((type) => ({
      title: t(`JUNIOR_PROGRESS_TYPE.${type}`),
      value: type,
    })),
  );

  function canEditReport(reportCreatorId: number): boolean {
    return permissions.canUpdateJuniorReport(reportCreatorId);
  }

  async function loadJunior(): Promise<void> {
    if (!canViewMentorship.value) {
      return;
    }
    loading.value = true;
    error.value = "";
    try {
      const juniorId = Number(route.params.juniorRegistryId);
      const juniorDetails = await fetchJuniorDetails(juniorId);
      junior.value = juniorDetails;
      employee.value = await findEmployee(juniorDetails.juniorEmpl.id);
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
      employee.value = null;
    } finally {
      loading.value = false;
    }
  }

  async function loadDictionaries(): Promise<void> {
    const [employeesList, baList, rolesList] = await Promise.all([
      fetchEmployeesForRegistry(),
      fetchBusinessAccounts(),
      fetchCurrentProjectRoles(),
    ]);
    employees.value = employeesList;
    allBusinessAccounts.value = baList;
    projectRoles.value = rolesList;
  }

  function openCreateReport() {
    editingReportId.value = null;
    fillJuniorReportForm(reportForm);
    reportDialog.value = true;
  }

  function openEditReport(reportId: number): void {
    const report = junior.value?.reports.find((item) => item.id === reportId);
    if (!report) {
      return;
    }
    editingReportId.value = report.id;
    fillJuniorReportForm(reportForm, report);
    reportDialog.value = true;
  }

  function openEditDialog(): void {
    if (!junior.value) {
      return;
    }
    updateForm.mentorId = junior.value.mentor?.id ?? null;
    updateForm.role = junior.value.role ?? "";
    updateForm.budgetingAccount = junior.value.budgetingAccount?.id ?? null;
    editDialog.value = true;
  }

  async function submitUpdateJunior(): Promise<void> {
    if (!junior.value) {
      return;
    }
    actionLoading.value = true;
    try {
      await updateJuniorRegistry(junior.value.id, updateForm);
      editDialog.value = false;
      await loadJunior();
    } finally {
      actionLoading.value = false;
    }
  }

  async function submitReport() {
    if (!junior.value) {
      return;
    }
    actionLoading.value = true;
    try {
      if (editingReportId.value) {
        await updateJuniorReport(junior.value.id, editingReportId.value, reportForm);
      } else {
        await createJuniorReport(junior.value.id, reportForm);
      }
      reportDialog.value = false;
      editingReportId.value = null;
      await loadJunior();
    } finally {
      actionLoading.value = false;
    }
  }

  async function deleteJunior() {
    if (!junior.value) {
      return;
    }
    actionLoading.value = true;
    try {
      await deleteJuniorFromRegistry(junior.value.id);
      deleteDialog.value = false;
      await router.push({ name: "mentorship" });
    } finally {
      actionLoading.value = false;
    }
  }

  function openDeleteReport(reportId: number) {
    reportToDelete.value = reportId;
    deleteReportDialog.value = true;
  }

  function closeDeleteReport() {
    deleteReportDialog.value = false;
    reportToDelete.value = null;
  }

  async function confirmDeleteReport() {
    if (!junior.value || !reportToDelete.value) {
      return;
    }
    actionLoading.value = true;
    try {
      await deleteJuniorReport(junior.value.id, reportToDelete.value);
      closeDeleteReport();
      await loadJunior();
    } finally {
      actionLoading.value = false;
    }
  }

  async function submitGraduation() {
    if (!junior.value) {
      return;
    }
    actionLoading.value = true;
    try {
      if (junior.value.graduation) {
        await cancelJuniorGraduation(junior.value.id);
      } else {
        await graduateJunior(junior.value.id, { comment: graduationComment.value || null });
      }
      graduateDialog.value = false;
      graduationComment.value = "";
      await loadJunior();
    } finally {
      actionLoading.value = false;
    }
  }

  onMounted(() => {
    Promise.all([loadJunior(), loadDictionaries()]).catch((err: unknown) => {
      error.value = errorUtils.shortMessage(err);
    });
  });

  return {
    loading,
    actionLoading,
    error,
    junior,
    employee,
    deleteDialog,
    graduateDialog,
    deleteReportDialog,
    reportDialog,
    editDialog,
    editingReportId,
    graduationComment,
    employees,
    allBusinessAccounts,
    projectRoles,
    ratingFields: juniorReportRatingFields,
    reportForm,
    updateForm,
    canViewMentorship,
    canEditRegistry,
    sortedReports,
    progressOptions,
    getProgressIcon: getJuniorProgressIcon,
    canEditReport,
    openCreateReport,
    openEditReport,
    openEditDialog,
    submitUpdateJunior,
    submitReport,
    deleteJunior,
    openDeleteReport,
    closeDeleteReport,
    confirmDeleteReport,
    submitGraduation,
  };
}
