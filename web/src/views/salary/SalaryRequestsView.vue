<template>
  <TableFirstPageLayout test-id="salary-requests-view">
    <TableFirstPageState
      v-if="!canViewSalaryRequests"
      test-id="salary-requests-no-access"
      :title="t('Недостаточно прав')"
      :action-text="t('На главную')"
      :to="{ name: 'profile-main' }"
      icon="mdi-alert-outline"
      color="warning"
    />

    <TableFirstPageState
      v-else-if="error"
      test-id="salary-requests-error"
      :title="t('Не удалось загрузить данные')"
      :text="error"
      icon="mdi-alert-circle-outline"
      color="error"
    />

        <v-card v-else class="d-flex flex-column h-100" data-testid="salary-requests-card">
      <v-card-text class="pt-4 pb-2 d-flex flex-column flex-grow-1 min-h-0">
        <AdaptiveFilterBar
          :items="filterBarItems"
          :has-right-actions="canCreateSalaryRequest || canAdminSalaryRequests"
        >
          <template #left-actions>
            <div class="d-flex align-center ga-2 flex-wrap">
              <table-toolbar-actions

                show-refresh
                :refresh-label="t('Обновить данные')"
                @refresh="fetchData"
              />
              <v-btn-toggle
                v-model="filter.type"
                mandatory
                color="primary"
                data-testid="salary-requests-type-toggle"
              >
                <v-btn :value="1" min-width="180">
                  {{ t("Повышения") }} ({{ increaseImplementedCount }}/{{ increaseTotalCount }})
                </v-btn>
                <v-btn :value="2" min-width="180">
                  {{ t("Бонусы") }} ({{ bonusImplementedCount }}/{{ bonusTotalCount }})
                </v-btn>
              </v-btn-toggle>
              <table-toolbar-actions
                v-if="canAdminSalaryRequests"

                show-export
                :export-label="t('Экспорт в Excel')"
                @export="exportToExcel"
              />
            </div>
          </template>

          <template #filter-search>
            <v-text-field
              v-model="filter.search"
              :label="t('Поиск')"
              prepend-inner-icon="mdi-magnify"
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="salary-requests-filter-search"
            />
          </template>

          <template #filter-period>
            <period-switcher-control
              :label="selectedPeriod.toString()"
              :is-current="isCurrentPeriod"
              :width="272"
              :disabled="loading"
              :period-closed="periodClosed"

              prev-test-id="salary-requests-period-prev"
              next-test-id="salary-requests-period-next"
              label-test-id="salary-requests-period-label"
              @prev="decrementPeriod"
              @next="incrementPeriod"
              @go-current="goToCurrentPeriod"
            />
          </template>


          <template #filter-ba>
            <v-autocomplete
              v-model="filter.budgetBusinessAccounts"
              :items="bas"
              item-title="name"
              item-value="id"
              :label="t('Бюджет из бизнес аккаунта')"
              multiple
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="salary-requests-filter-ba"
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.budgetBusinessAccounts.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-current-project>
            <v-autocomplete
              v-model="filter.currentProjects"
              :items="currentProjectOptions"
              item-title="name"
              item-value="id"
              :label="t('Текущий проект')"
              multiple
              clearable
              variant="outlined"
              density="compact"
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.currentProjects.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-implemented>
            <v-select
              v-model="filter.implemented"
              :items="implementedOptions"
              item-title="title"
              item-value="value"
              :label="t('Завершён')"
              multiple
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="salary-requests-filter-implemented"
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.implemented.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-select>
          </template>

          <template #right-actions>
            <div class="d-flex align-center ga-2">
              <table-toolbar-actions

                :show-add="canReportSalaryRequest"
                :add-label="createNewTitle"
                @add="openCreateDialog"
              />
              <v-tooltip location="bottom" v-if="canAdminSalaryRequests">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    :icon="periodClosed ? 'mdi-lock-open' : 'mdi-lock'"
                    variant="text"

                    data-testid="salary-requests-period-toggle"
                    @click="openPeriodToggleDialog"
                  />
                </template>
                <span>
                  {{
                    t(
                      periodClosed
                        ? 'Переоткрыть период. Вернуть возможность вносить изменения'
                        : 'Закрыть период. Запретить внесение изменений.',
                    )
                  }}
                </span>
              </v-tooltip>
            </div>
          </template>
        </AdaptiveFilterBar>

        <div class="flex-grow-1 min-h-0">
        <HREasyTableBase
          table-class="salary-requests-table text-truncate"
          :headers="headers"
          :items="filteredItems"
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          fixed-header
          hover
          density="compact"
          :sort-by="[{ key: 'createdAt', order: 'desc' }]"
          :row-props="rowProps"
          data-testid="salary-requests-table"
          @click:row="onRowClick"
          height="fill"
        >
          <template #[`item.employee.name`]="{ item }">
            <router-link
              :to="{
                name: 'salary-request-details',
                params: {
                  period: String(item.req?.increaseStartPeriod ?? selectedPeriodId),
                  requestId: String(item.id),
                },
                query: {
                  source: 'salary-requests',
                  tab: activeTabQuery,
                },
              }"
              @click.stop
            >
              {{ item.employee?.name ?? "" }}
            </router-link>
          </template>
          <template #[`item.req.increaseAmount`]="{ item }">
            {{ formatMoney(item.req?.increaseAmount) }}
          </template>
          <template #[`item.req.plannedSalaryAmount`]="{ item }">
            {{ formatMoney(item.req?.plannedSalaryAmount) }}
          </template>
          <template #[`item.impl.increaseAmount`]="{ item }">
            {{ formatMoney(item.impl?.increaseAmount) }}
          </template>
          <template #[`item.impl.salaryAmount`]="{ item }">
            {{ formatMoney(item.impl?.salaryAmount) }}
          </template>
          <template #[`item.impl.state`]="{ item }">
            <span
              :class="item.impl?.state === 2 ? 'text-error' : 'text-success'"
              data-testid="salary-requests-impl-state"
            >
              {{ item.impl?.state ? t(`SALARY_REQUEST_STAT.${item.impl.state}`) : "" }}
            </span>
          </template>
          <template #[`item.approvals`]="{ item }">
            <div class="d-flex ga-1">
              <v-tooltip
                v-for="approval in orderedApprovals(item.approvals ?? [])"
                :key="approval.id"
                location="bottom"
                max-width="480"
              >
                <template #activator="{ props }">
                  <v-icon
                    v-bind="props"
                    :icon="approvalIcon(approval.state).icon"
                    :color="approvalIcon(approval.state).color"
                  />
                </template>
                <div class="text-body-2">{{ approval.createdBy.name }}</div>
                <div class="text-body-2">{{ formatDateTime(approval.createdAt) }}</div>
                <div class="text-body-2">{{ approval.comment ?? "" }}</div>
              </v-tooltip>
            </div>
          </template>
          <template #[`item.createdAt`]="{ item }">
            {{ formatDateTime(item.createdAt) }}
          </template>
        </HREasyTableBase>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="createDialog" max-width="920" data-testid="salary-requests-create-dialog">
      <v-card>
        <v-card-title>{{ createNewTitle }}</v-card-title>
        <v-card-text>
          <v-alert v-if="createError" type="error" variant="tonal" class="mb-4">
            {{ createError }}
          </v-alert>
          <v-form ref="createFormRef">
            <v-autocomplete
              v-model="createForm.employeeId"
              :items="employees"
              item-title="displayName"
              item-value="id"
              :label="t('Сотрудник')"
              :rules="[requiredRule]"
              clearable
              data-testid="salary-requests-create-employee"
            />
            <v-autocomplete
              v-model="createForm.budgetBusinessAccount"
              :items="bas"
              item-title="name"
              item-value="id"
              :label="t('Бюджет из бизнес аккаунта')"
              :rules="[requiredRule]"
              clearable
              data-testid="salary-requests-create-ba"
            />
            <v-autocomplete
              v-if="filter.type === 1 && employeeAssessments.length > 0"
              v-model="createForm.assessmentId"
              :items="employeeAssessments"
              item-title="plannedDate"
              item-value="id"
              :label="t('Ассессмент')"
              clearable
              data-testid="salary-requests-create-assessment"
            />
            <my-date-form-component
              v-if="filter.type === 1"
              v-model="createForm.budgetExpectedFundingUntil"
              :label="t('Планируемая дата окончания финансирования')"
            />
            <v-text-field
              v-if="filter.type === 1"
              v-model.number="createForm.currentSalaryAmount"
              type="number"
              hide-spin-buttons
              :label="t('Текущая заработная плата')"
              data-testid="salary-requests-create-current-salary"
            />
            <v-text-field
              v-model.number="createForm.increaseAmount"
              type="number"
              hide-spin-buttons
              :rules="[requiredNumberRule]"
              :label="filter.type === 1 ? t('Предполагаемое изменение на') : t('Сумма бонуса')"
              data-testid="salary-requests-create-increase"
            />
            <v-text-field
              v-if="filter.type === 1"
              v-model.number="createForm.plannedSalaryAmount"
              type="number"
              hide-spin-buttons
              :rules="[plannedSalaryRule]"
              :label="t('Предполагаемая заработная плата после повышения')"
              data-testid="salary-requests-create-planned-salary"
            />
            <v-text-field
              v-model="createForm.reason"
              counter="256"
              :rules="[requiredReasonRule]"
              :label="t('Обоснование')"
              data-testid="salary-requests-create-reason"
            />
            <v-textarea
              v-model="createForm.comment"
              :rules="[maxCommentRule]"
              :label="t('Примечание')"
              data-testid="salary-requests-create-comment"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="createLoading" @click="closeCreateDialog">
            {{ t("Отмена") }}
          </v-btn>
          <v-btn color="primary" :loading="createLoading" @click="submitCreate">
            {{ t("Создать") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="periodToggleDialog" max-width="560" data-testid="salary-requests-period-toggle-dialog">
      <v-card>
        <v-card-title>
          {{ periodClosed ? t("Переоткрыть период") : t("Закрыть период") }}
        </v-card-title>
        <v-card-text>
          {{
            periodClosed
              ? t("Переоткрыть период. Вернуть возможность вносить изменения")
              : t("Закрыть период. Запретить внесение изменений.")
          }}
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="loading" @click="periodToggleDialog = false">
            {{ t("Отмена") }}
          </v-btn>
          <v-btn color="primary" :loading="loading" @click="confirmPeriodToggle">
            {{ t("Применить") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import TableFirstPageState from "@/components/shared/TableFirstPageState.vue";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import { formatDateTime } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { extractDataTableRow } from "@/lib/data-table";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import PeriodSwitcherControl from "@/components/shared/PeriodSwitcherControl.vue";
import { useSalaryRequests } from "@/composables/useSalaryRequests";
import { ReportPeriod } from "@/services/overtime.service";
import { listEmployees, type Employee } from "@/services/employee.service";
import { fetchProjectInfo } from "@/services/projects.service";
import { fetchEmployeeAssessments } from "@/services/assessment.service";
import {
  reportSalaryRequest,
  type SalaryApprovalState,
  type SalaryRequestApproval,
  type SalaryRequestReportBody,
} from "@/services/salary.service";

type VFormInstance = InstanceType<typeof VForm>;

interface SalaryCreateForm {
  employeeId: number | null;
  budgetBusinessAccount: number | null;
  budgetExpectedFundingUntil: string;
  increaseAmount: number | null;
  currentSalaryAmount: number | null;
  previousSalaryIncreaseDate: string;
  previousSalaryIncreaseText: string | null;
  plannedSalaryAmount: number | null;
  assessmentId: number | null;
  reason: string;
  comment: string | null;
}

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const {
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
  canCreateSalaryRequest,
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
} = useSalaryRequests(t);

const filterBarItems = computed(() => [
  { id: "period", minWidth: 272, active: false },
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "ba", minWidth: 340, active: filter.budgetBusinessAccounts.length > 0 },
  { id: "current-project", minWidth: 320, active: filter.currentProjects.length > 0 },
  { id: "implemented", minWidth: 240, active: filter.implemented.length > 0 },
]);

const currentProjectOptions = computed(() => {
  const map = new Map<number, string>();
  items.value
    .filter((item) => item.type === filter.type)
    .forEach((item) => {
    const project = item.employeeInfo.currentProject;
    if (project?.id && project.name) {
      map.set(project.id, project.name);
    }
    });
  return Array.from(map.entries()).map(([id, name]) => ({ id, name }));
});

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object") {
    if ("name" in item && typeof item.name === "string") {
      return item.name;
    }
    if ("title" in item && typeof item.title === "string") {
      return item.title;
    }
  }
  return "";
}

const implementedOptions = computed(() => [
  { title: t("Нет"), value: false },
  { title: t("Да"), value: true },
]);

const createDialog = ref(false);
const periodToggleDialog = ref(false);
const createLoading = ref(false);
const createError = ref("");
const createFormRef = ref<VFormInstance | null>(null);
const employees = ref<Employee[]>([]);
const employeeAssessments = ref<{ id: number; plannedDate: string }[]>([]);

const createForm = reactive<SalaryCreateForm>({
  employeeId: null,
  budgetBusinessAccount: null,
  budgetExpectedFundingUntil: "",
  increaseAmount: null,
  currentSalaryAmount: null,
  previousSalaryIncreaseDate: "",
  previousSalaryIncreaseText: null,
  plannedSalaryAmount: null,
  assessmentId: null,
  reason: "",
  comment: null,
});

const createNewTitle = computed(() =>
  filter.type === 1 ? t("Создание запроса на индексацию ЗП") : t("Создание запроса на бонус"),
);
const isCurrentPeriod = computed(() => selectedPeriodId.value === ReportPeriod.currentPeriod().periodId());
const activeTabQuery = computed(() => (filter.type === 2 ? "bonuses" : "requests"));

const requiredRule = (value: unknown) => Boolean(value) || t("Обязательное поле");
const requiredNumberRule = (value: unknown) =>
  (value != null && value !== "" && !Number.isNaN(Number(value))) || t("Обязательное числовое поле");
const requiredReasonRule = (value: string) =>
  (Boolean(value?.trim()) && value.length <= 256) || t("Обязательное поле. Не более N символов", { n: 256 });
const maxCommentRule = (value: string | null) =>
  (!value || value.length <= 4096) || t("Не более N символов", { n: 4096 });
const plannedSalaryRule = () =>
  validateIncreaseAndSalary() || t("Запланированная заработная плата должна совпадать с суммой текущей и изменением");

watch(
  () => createForm.employeeId,
  async (employeeId) => {
    if (!employeeId) {
      employeeAssessments.value = [];
      return;
    }

    const employee = employees.value.find((item) => item.id === employeeId);
    if (!employee) {
      return;
    }

    createForm.budgetBusinessAccount = employee.ba?.id ?? null;
    createForm.increaseAmount = null;
    createForm.plannedSalaryAmount = null;
    createForm.currentSalaryAmount = null;
    createForm.reason = "";
    createForm.comment = null;
    createForm.assessmentId = null;
    createForm.budgetExpectedFundingUntil = "";

    try {
      const assessments = await fetchEmployeeAssessments(employee.id);
      employeeAssessments.value = assessments
        .filter((item) => !item.canceledAt)
        .map((item) => ({
          id: item.id,
          plannedDate: item.plannedDate ?? "",
        }));
    } catch {
      employeeAssessments.value = [];
    }

    if (employee.currentProject?.id) {
      try {
        const project = await fetchProjectInfo(employee.currentProject.id);
        createForm.budgetExpectedFundingUntil = project.endDate ?? project.planEndDate ?? "";
      } catch {
        createForm.budgetExpectedFundingUntil = "";
      }
    }
  },
);

watch(
  () => route.query.tab,
  (tab) => {
    if (tab === "bonuses" && filter.type !== 2) {
      filter.type = 2;
      return;
    }
    if (tab === "requests" && filter.type !== 1) {
      filter.type = 1;
    }
  },
  { immediate: true },
);

watch(
  () => filter.type,
  (type) => {
    const nextTab = type === 2 ? "bonuses" : "requests";
    if (route.query.tab === nextTab) {
      return;
    }
    router.replace({
      query: {
        ...route.query,
        tab: nextTab,
      },
    }).catch(() => undefined);
  },
);

function formatMoney(value: number | null | undefined): string {
  if (value == null) {
    return "";
  }
  return Number(value).toLocaleString("ru-RU");
}

function orderedApprovals(approvals: SalaryRequestApproval[]): SalaryRequestApproval[] {
  return approvals
    .filter((approval) => approval.state !== 1)
    .sort((a, b) => a.createdAt.localeCompare(b.createdAt));
}

function approvalIcon(state: SalaryApprovalState): { icon: string; color: string } {
  if (state === 3) {
    return { icon: "mdi-alert-circle", color: "error" };
  }
  if (state === 2) {
    return { icon: "mdi-checkbox-marked-circle", color: "success" };
  }
  return { icon: "mdi-comment", color: "" };
}

function rowProps() {
  return { class: "cursor-pointer" };
}

function onRowClick(_event: Event, payload: unknown): void {
  const row = extractDataTableRow<{ id: number; req: { increaseStartPeriod: number } }>(payload);
  if (!row?.id || !row.req?.increaseStartPeriod) {
    return;
  }
  router.push({
    name: "salary-request-details",
    params: {
      period: String(row.req.increaseStartPeriod),
      requestId: String(row.id),
    },
    query: {
      source: "salary-requests",
      tab: activeTabQuery.value,
    },
  }).catch(() => undefined);
}

async function openCreateDialog(): Promise<void> {
  if (!canCreateSalaryRequest.value) {
    return;
  }

  if (employees.value.length === 0) {
    createLoading.value = true;
    createError.value = "";
    try {
      employees.value = await listEmployees();
    } catch (err: unknown) {
      createError.value = errorUtils.shortMessage(err);
      createLoading.value = false;
      return;
    } finally {
      createLoading.value = false;
    }
  }

  resetCreateForm();
  createDialog.value = true;
}

function openPeriodToggleDialog(): void {
  periodToggleDialog.value = true;
}

async function confirmPeriodToggle(): Promise<void> {
  periodToggleDialog.value = false;
  if (periodClosed.value) {
    await reopenPeriod();
    return;
  }
  await closePeriod();
}

async function goToCurrentPeriod(): Promise<void> {
  const currentPeriodId = ReportPeriod.currentPeriod().periodId();
  if (selectedPeriodId.value === currentPeriodId) {
    return;
  }
  selectedPeriodId.value = currentPeriodId;
  await fetchData();
}

function closeCreateDialog(): void {
  createDialog.value = false;
  createError.value = "";
  createFormRef.value?.resetValidation();
}

function resetCreateForm(): void {
  createForm.employeeId = null;
  createForm.budgetBusinessAccount = null;
  createForm.budgetExpectedFundingUntil = "";
  createForm.increaseAmount = null;
  createForm.currentSalaryAmount = null;
  createForm.previousSalaryIncreaseDate = "";
  createForm.previousSalaryIncreaseText = null;
  createForm.plannedSalaryAmount = null;
  createForm.assessmentId = null;
  createForm.reason = "";
  createForm.comment = null;
  employeeAssessments.value = [];
}

function validateIncreaseAndSalary(): boolean {
  if (filter.type !== 1) {
    return true;
  }
  if (
    createForm.currentSalaryAmount == null
    || createForm.increaseAmount == null
    || createForm.plannedSalaryAmount == null
  ) {
    return true;
  }
  return Number(createForm.plannedSalaryAmount)
    === Number(createForm.currentSalaryAmount) + Number(createForm.increaseAmount);
}

async function submitCreate(): Promise<void> {
  if (!createFormRef.value) {
    return;
  }

  const validation = await createFormRef.value.validate();
  if (!validation.valid || !validateIncreaseAndSalary()) {
    return;
  }

  createLoading.value = true;
  createError.value = "";
  try {
    const payload: SalaryRequestReportBody = {
      employeeId: Number(createForm.employeeId),
      type: filter.type,
      budgetBusinessAccount: createForm.budgetBusinessAccount,
      budgetExpectedFundingUntil: toNullableDate(createForm.budgetExpectedFundingUntil),
      increaseAmount: createForm.increaseAmount,
      currentSalaryAmount: filter.type === 1 ? createForm.currentSalaryAmount : null,
      previousSalaryIncreaseDate: toNullableDate(createForm.previousSalaryIncreaseDate),
      previousSalaryIncreaseText: createForm.previousSalaryIncreaseText,
      plannedSalaryAmount: filter.type === 1 ? createForm.plannedSalaryAmount : null,
      increaseStartPeriod: selectedPeriodId.value,
      assessmentId: filter.type === 1 ? createForm.assessmentId : null,
      reason: createForm.reason.trim(),
      comment: createForm.comment,
    };
    await reportSalaryRequest(payload);
    closeCreateDialog();
    await fetchData();
  } catch (err: unknown) {
    createError.value = errorUtils.shortMessage(err);
  } finally {
    createLoading.value = false;
  }
}

function toNullableDate(value: string): string | null {
  return value ? value : null;
}
</script>

