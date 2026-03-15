<template>
  <v-container class="py-6" data-testid="salary-request-details-view">
    <div class="mx-auto" style="max-width: 1360px;">
      <div class="d-flex align-center ga-2">
        <v-btn
          variant="text"
          prepend-icon="mdi-arrow-left"
          :to="{ name: 'salary-requests' }"
          data-testid="salary-request-details-back"
        >
          {{ t("Повышения и бонусы") }}
        </v-btn>
      </div>

      <v-skeleton-loader
        v-if="loading"
        type="heading, paragraph, paragraph"
        class="mt-4"
        data-testid="salary-request-details-loading"
      />

      <v-alert
        v-else-if="error"
        type="error"
        variant="tonal"
        class="mt-4"
        data-testid="salary-request-details-error"
      >
        {{ error }}
      </v-alert>

      <template v-else-if="request">
        <v-card class="mt-4" data-testid="salary-request-details-main-card">
          <v-card-title class="d-flex flex-wrap align-center ga-2">
            <span>
              {{ request.type === 1 ? t("Запрос на повышение") : t("Запрос на бонус") }}
            </span>
            <span class="text-subtitle-1">
              (+{{ formatMoney(request.req.increaseAmount) }})
            </span>
            <v-chip
              size="small"
              :color="stateChip.color"
              variant="tonal"
            >
              {{ stateChip.label }}
            </v-chip>
            <v-chip v-if="periodClosed" color="warning" variant="tonal" size="small">
              {{ t("Период закрыт для внесения изменений") }}
            </v-chip>
          </v-card-title>
          <v-card-subtitle>
            {{ formatPeriod(request.req.increaseStartPeriod) }}
          </v-card-subtitle>
          <v-card-text class="pa-6">
            <v-row>
              <v-col cols="12" md="12" lg="auto">
                <profile-summary
                  v-if="employee"
                  :employee="employee"
                  :read-only="true"
                />
                <div
                  v-else
                  class="pa-6 text-body-2 text-medium-emphasis"
                >
                  {{ t("Отсутствуют данные") }}
                </div>
              </v-col>
              <v-col cols="12" md="12" lg class="pt-0 pt-lg-3">
                <div class="text-subtitle-2 mb-2">{{ t("Информация о сотруднике") }}</div>
                <profile-summary-item :label="t('Дата трудоустройства')">
                  {{ formatDate(request.employeeInfo.dateOfEmployment) || "-" }}
                </profile-summary-item>
                <profile-summary-item v-if="request.type === 1" :label="t('Предыдущий реализованный пересмотр')">
                  {{ formatDate(request.employeeInfo.previousSalaryIncreaseDate) || "-" }}
                  <span v-if="request.employeeInfo.previousSalaryIncreaseText">
                    ({{ request.employeeInfo.previousSalaryIncreaseText }})
                  </span>
                </profile-summary-item>
                <profile-summary-item v-if="request.type === 1" :label="t('Текущая заработная плата')">
                  {{ formatMoney(request.employeeInfo.currentSalaryAmount) || "-" }}
                </profile-summary-item>
                <profile-summary-item v-if="request.type === 1" :label="t('Ассессмент')">
                  <router-link
                    v-if="request.assessment"
                    target="_blank"
                    :to="`/assessments/${request.employee.id}/${request.assessment.id}`"
                  >
                    {{ request.assessment.name }}
                  </router-link>
                  <span v-else>-</span>
                </profile-summary-item>

                <v-divider class="my-3" />

                <div class="text-subtitle-2 mb-2">{{ t("Запрос") }}</div>
                <profile-summary-item :label="t('Инициатор')">
                  {{ request.createdBy.name }} ({{ formatDateTime(request.createdAt) }})
                </profile-summary-item>
                <profile-summary-item :label="t('Бюджет из бизнес аккаунта')">
                  {{ request.budgetBusinessAccount?.name ?? "-" }}
                </profile-summary-item>
                <profile-summary-item v-if="request.type === 1" :label="t('Перспективы биллинга')">
                  {{ formatDate(request.budgetExpectedFundingUntil) || "-" }}
                </profile-summary-item>
                <profile-summary-item :label="request.type === 1 ? t('Изменение на') : t('Сумма бонуса')">
                  {{ formatMoney(request.req.increaseAmount) }}
                </profile-summary-item>
                <profile-summary-item v-if="request.type === 1" :label="t('Заработная плата после повышения')">
                  {{ formatMoney(request.req.plannedSalaryAmount) || "-" }}
                </profile-summary-item>
                <profile-summary-item v-if="request.type === 1" :label="t('Запрошенная позиция')">
                  {{ request.req.newPosition?.name || "-" }}
                </profile-summary-item>
                <profile-summary-item :label="t('Обоснование')">
                  {{ request.req.reason || "-" }}
                </profile-summary-item>
                <profile-summary-item :label="t('Примечание')">
                  {{ request.req.comment || "-" }}
                </profile-summary-item>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-card class="mt-5" data-testid="salary-request-details-implementation-card">
          <v-card-title class="text-h6">{{ t("Реализация") }}</v-card-title>
          <v-card-text class="pa-6" v-if="request.impl">
            <profile-summary-item :label="t('Результат')">
              <v-chip size="small" :color="request.impl.state === 2 ? 'error' : 'success'" variant="tonal">
                {{ t(`SALARY_REQUEST_STAT.${request.impl.state}`) }}
              </v-chip>
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.implementedBy" :label="t('Принял решение')">
              {{ request.impl.implementedBy.name }} ({{ formatDateTime(request.impl.implementedAt) }})
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.increaseAmount != null" :label="request.type === 1 ? t('Изменение на') : t('Сумма бонуса')">
              {{ formatMoney(request.impl.increaseAmount) }}
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.salaryAmount != null" :label="t('Заработная плата после повышения')">
              {{ formatMoney(request.impl.salaryAmount) }}
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.newPosition" :label="t('Новая позиция')">
              {{ request.impl.newPosition.name }}
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.rejectReason" :label="t('Обоснование отказа')">
              {{ request.impl.rejectReason }}
            </profile-summary-item>
            <profile-summary-item :label="t('Месяц старта изменений')">
              {{ formatPeriod(request.impl.increaseStartPeriod) }}
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.comment" :label="t('Примечание')">
              {{ request.impl.comment }}
            </profile-summary-item>
            <profile-summary-item v-if="request.impl.increaseText" :label="t('Сообщение об изменениях')">
              {{ request.impl.increaseText }}
            </profile-summary-item>
          </v-card-text>
          <v-card-text class="pa-6" v-else>
            {{ t("На рассмотрении") }}
          </v-card-text>
        </v-card>

        <v-card class="mt-5" data-testid="salary-request-details-approvals-card">
          <v-card-title class="text-h6">{{ t("Согласования и комментарии") }}</v-card-title>
          <v-card-text class="pa-4" v-if="request.approvals?.length">
            <v-list>
              <template v-for="(approval, index) in approvalsOrderedAsc" :key="approval.id">
                <v-list-item>
                  <template #prepend>
                    <v-icon :icon="approvalIcon(approval.state).icon" :color="approvalIcon(approval.state).color" />
                  </template>
                  <v-list-item-title class="text-body-2 font-weight-medium">
                    {{ approval.createdBy.name }} • {{ formatDateTime(approval.createdAt) }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-body-2">
                    {{ approval.comment || "-" }}
                  </v-list-item-subtitle>
                </v-list-item>
                <v-divider v-if="index < approvalsOrderedAsc.length - 1" />
              </template>
            </v-list>
          </v-card-text>
          <v-card-text class="pa-6" v-else>
            {{ t("Добавьте первый комментарий или согласование") }}
          </v-card-text>
        </v-card>
      </template>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { errorUtils } from "@/lib/errors";
import { formatDate, formatDateTime } from "@/lib/datetime";
import { ReportPeriod } from "@/services/overtime.service";
import { findEmployee, type Employee } from "@/services/employee.service";
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";
import {
  fetchClosedSalaryRequestPeriods,
  fetchSalaryRequest,
  type SalaryApprovalState,
  type SalaryIncreaseRequest,
  type SalaryRequestApproval,
} from "@/services/salary.service";

const { t } = useI18n();
const route = useRoute();

const loading = ref(false);
const error = ref("");
const request = ref<SalaryIncreaseRequest | null>(null);
const employee = ref<Employee | null>(null);
const periodClosed = ref(false);

const stateChip = computed(() => {
  if (!request.value?.impl) {
    return { label: t("На рассмотрении"), color: "default" };
  }
  if (request.value.impl.state === 2) {
    return { label: t("SALARY_REQUEST_STAT.2"), color: "error" };
  }
  return { label: t("SALARY_REQUEST_STAT.1"), color: "success" };
});

const approvalsOrderedAsc = computed<SalaryRequestApproval[]>(() =>
  [...(request.value?.approvals ?? [])].sort((a, b) => a.createdAt.localeCompare(b.createdAt)),
);

watch(
  () => [route.params.period, route.params.requestId],
  () => {
    load().catch(() => undefined);
  },
);

onMounted(() => {
  load().catch(() => undefined);
});

async function load(): Promise<void> {
  const period = Number(route.params.period);
  const requestId = Number(route.params.requestId);

  if (!Number.isInteger(period) || !Number.isInteger(requestId)) {
    error.value = t("Ошибка");
    request.value = null;
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const [closedPeriods, details] = await Promise.all([
      fetchClosedSalaryRequestPeriods(),
      fetchSalaryRequest(period, requestId),
    ]);
    periodClosed.value = closedPeriods.some((item) => item.period === period);
    request.value = details;
    employee.value = await findEmployee(details.employee.id).catch(() => null);
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
    request.value = null;
    employee.value = null;
  } finally {
    loading.value = false;
  }
}

function formatMoney(value: number | null | undefined): string {
  if (value == null) {
    return "";
  }
  return `${Number(value).toLocaleString("ru-RU")} ₽`;
}

function formatPeriod(period: number): string {
  return ReportPeriod.fromPeriodId(period).toString();
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
</script>
