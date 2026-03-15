import { computed, onMounted, reactive, ref } from "vue";
import type { ComposerTranslation } from "vue-i18n";
import { fetchBusinessAccounts, type DictItem } from "@/services/dict.service";
import {
  closeSalaryRequestPeriod,
  exportSalaryRequests,
  fetchAllSalaryRequests,
  reopenSalaryRequestPeriod,
} from "@/services/admin-salary.service";
import {
  fetchClosedSalaryRequestPeriods,
  fetchSalaryRequests,
  type SalaryIncreaseRequest,
  type SalaryRequestImplementationState,
  type SalaryRequestType,
} from "@/services/salary.service";
import { ReportPeriod } from "@/services/overtime.service";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";

interface SalaryFilter {
  search: string;
  type: SalaryRequestType;
  budgetBusinessAccounts: number[];
  implemented: boolean[];
  implementationStates: SalaryRequestImplementationState[];
}

/**
 * Salary requests state and business filtering for list page migration.
 */
export function useSalaryRequests(t: ComposerTranslation) {
  const permissions = usePermissions();

  const loading = ref(false);
  const error = ref("");
  const items = ref<SalaryIncreaseRequest[]>([]);
  const bas = ref<DictItem[]>([]);
  const closedPeriods = ref<number[]>([]);
  const selectedPeriodId = ref(ReportPeriod.currentPeriod().periodId());
  const selectedPeriod = computed(() => ReportPeriod.fromPeriodId(selectedPeriodId.value));

  const filter = reactive<SalaryFilter>({
    search: "",
    type: 1,
    budgetBusinessAccounts: [],
    implemented: [],
    implementationStates: [],
  });

  const canAdminSalaryRequests = computed(() => permissions.canAdminSalaryRequests());
  const canReportSalaryRequest = computed(() => permissions.canReportSalaryRequest());
  const canViewSalaryRequests = computed(
    () => canReportSalaryRequest.value || canAdminSalaryRequests.value,
  );

  const periodClosed = computed(() => closedPeriods.value.includes(selectedPeriodId.value));

  const headers = computed(() => {
    const wrapped = (title: string, key: string, width: string) => ({
      title,
      key,
      width,
      headerProps: {
        class: "text-wrap",
      },
    });

    const requestSpecificHeaders = filter.type === 1
      ? [
        wrapped(t("Предполагаемое изменение на"), "req.increaseAmount", "110px"),
        wrapped(
          t("Предполагаемая заработная плата после повышения"),
          "req.plannedSalaryAmount",
          "130px",
        ),
      ]
      : [wrapped(t("Предполагаемая сумма бонуса"), "req.increaseAmount", "120px")];

    const resultSpecificHeaders = filter.type === 1
      ? [
        wrapped(t("Итоговое изменение на"), "impl.increaseAmount", "110px"),
        wrapped(t("Итоговая сумма"), "impl.salaryAmount", "120px"),
        wrapped(t("Сообщение об изменениях"), "impl.increaseText", "180px"),
      ]
      : [wrapped(t("Итоговая сумма бонуса"), "impl.increaseAmount", "120px")];

    return [
      wrapped(t("Сотрудник"), "employee.name", "320px"),
      ...requestSpecificHeaders,
      wrapped(t("Решение"), "impl.state", "120px"),
      ...resultSpecificHeaders,
      wrapped(t("Бюджет из бизнес аккаунта"), "budgetBusinessAccount.name", "220px"),
      wrapped(t("Инициатор"), "createdBy.name", "240px"),
      wrapped(t("Согласования"), "approvals", "120px"),
      wrapped(t("Создано (время)"), "createdAt", "180px"),
    ];
  });

  const filteredItems = computed(() => {
    const search = filter.search.toLowerCase().trim();
    return items.value.filter((item) => {
      if (item.type !== filter.type) {
        return false;
      }

      if (filter.budgetBusinessAccounts.length > 0) {
        const baId = item.budgetBusinessAccount?.id;
        if (!baId || !filter.budgetBusinessAccounts.includes(baId)) {
          return false;
        }
      }

      const implemented = Boolean(item.impl?.implementedAt);
      if (filter.implemented.length > 0 && !filter.implemented.includes(implemented)) {
        return false;
      }

      const implState = item.impl?.state;
      if (filter.implementationStates.length > 0) {
        if (!implState || !filter.implementationStates.includes(implState)) {
          return false;
        }
      }

      if (!search) {
        return true;
      }

      return [
        item.employee?.name,
        item.createdBy?.name,
        item.budgetBusinessAccount?.name,
        item.req?.reason,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase()
        .includes(search);
    });
  });

  const increaseImplementedCount = computed(
    () =>
      items.value.filter((item) => item.type === 1 && Boolean(item.impl?.implementedAt)).length,
  );
  const increaseTotalCount = computed(() => items.value.filter((item) => item.type === 1).length);
  const bonusImplementedCount = computed(
    () =>
      items.value.filter((item) => item.type === 2 && Boolean(item.impl?.implementedAt)).length,
  );
  const bonusTotalCount = computed(() => items.value.filter((item) => item.type === 2).length);

  async function fetchData(): Promise<void> {
    if (!canViewSalaryRequests.value) {
      return;
    }

    loading.value = true;
    error.value = "";
    try {
      const [businessAccounts, closed, requests] = await Promise.all([
        fetchBusinessAccounts(),
        fetchClosedSalaryRequestPeriods(),
        canAdminSalaryRequests.value
          ? fetchAllSalaryRequests(selectedPeriodId.value)
          : fetchSalaryRequests(selectedPeriodId.value),
      ]);
      bas.value = businessAccounts;
      closedPeriods.value = closed.map((period) => period.period);
      items.value = requests;
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
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

  async function closePeriod(): Promise<void> {
    if (!canAdminSalaryRequests.value) {
      return;
    }
    loading.value = true;
    error.value = "";
    try {
      await closeSalaryRequestPeriod(selectedPeriodId.value);
      await fetchData();
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      loading.value = false;
    }
  }

  async function reopenPeriod(): Promise<void> {
    if (!canAdminSalaryRequests.value) {
      return;
    }
    loading.value = true;
    error.value = "";
    try {
      await reopenSalaryRequestPeriod(selectedPeriodId.value);
      await fetchData();
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      loading.value = false;
    }
  }

  async function exportToExcel(): Promise<void> {
    if (!canAdminSalaryRequests.value) {
      return;
    }
    loading.value = true;
    error.value = "";
    try {
      await exportSalaryRequests(selectedPeriodId.value);
    } catch (err: unknown) {
      error.value = errorUtils.shortMessage(err);
    } finally {
      loading.value = false;
    }
  }

  onMounted(() => {
    fetchData().catch(() => undefined);
  });

  return {
    loading,
    error,
    items,
    bas,
    filter,
    selectedPeriod,
    selectedPeriodId,
    headers,
    filteredItems,
    periodClosed,
    canViewSalaryRequests,
    canReportSalaryRequest,
    canAdminSalaryRequests,
    increaseImplementedCount,
    increaseTotalCount,
    bonusImplementedCount,
    bonusTotalCount,
    fetchData,
    incrementPeriod,
    decrementPeriod,
    closePeriod,
    reopenPeriod,
    exportToExcel,
  };
}
