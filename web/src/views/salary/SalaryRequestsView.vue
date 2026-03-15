<template>
  <v-container class="py-6" data-testid="salary-requests-view">
    <v-alert
      v-if="!canViewSalaryRequests"
      type="warning"
      variant="tonal"
      class="mb-4"
      data-testid="salary-requests-no-access"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

    <v-alert
      v-else-if="error"
      type="error"
      variant="tonal"
      class="mb-4"
      data-testid="salary-requests-error"
    >
      {{ error }}
    </v-alert>

    <v-card v-else data-testid="salary-requests-card">
      <v-card-title>
        <v-row align="center" class="w-100">
          <v-col cols="12" md="8" class="d-flex align-center ga-2">
            <table-toolbar-actions
              :disabled="loading"
              show-refresh
              :refresh-label="t('Обновить данные')"
              @refresh="fetchData"
            />
            <v-btn
              icon="mdi-chevron-left"
              variant="text"
              :disabled="loading"
              @click="decrementPeriod"
              data-testid="salary-requests-period-prev"
            />
            <span data-testid="salary-requests-period-label">{{ selectedPeriod.toString() }}</span>
            <v-icon
              v-if="periodClosed"
              color="primary"
              icon="mdi-lock"
              :title="t('Период закрыт для внесения изменений')"
            />
            <v-btn
              icon="mdi-chevron-right"
              variant="text"
              :disabled="loading"
              @click="incrementPeriod"
              data-testid="salary-requests-period-next"
            />
          </v-col>
          <v-col cols="12" md="4" class="d-flex justify-end ga-2">
            <table-toolbar-actions
              v-if="canAdminSalaryRequests"
              :disabled="loading"
              show-export
              :export-label="t('Экспорт в Excel')"
              @export="exportToExcel"
            />
            <v-tooltip location="bottom" v-if="canAdminSalaryRequests">
              <template #activator="{ props }">
                <v-btn
                  v-bind="props"
                  :icon="periodClosed ? 'mdi-lock-open' : 'mdi-lock'"
                  variant="text"
                  :disabled="loading"
                  data-testid="salary-requests-period-toggle"
                  @click="periodClosed ? reopenPeriod() : closePeriod()"
                />
              </template>
              <span>
                {{
                  t(
                    periodClosed
                      ? "Переоткрыть период. Вернуть возможность вносить изменения"
                      : "Закрыть период. Запретить внесение изменений.",
                  )
                }}
              </span>
            </v-tooltip>
          </v-col>
        </v-row>
      </v-card-title>

      <v-card-text class="pt-0">
        <v-row class="mb-2" align="center">
          <v-col cols="12" md="4" class="pb-0">
            <v-btn-toggle
              v-model="filter.type"
              mandatory
              color="primary"
              data-testid="salary-requests-type-toggle"
            >
              <v-btn :value="1">
                {{ t("Повышения") }} ({{ increaseImplementedCount }}/{{ increaseTotalCount }})
              </v-btn>
              <v-btn :value="2">
                {{ t("Бонусы") }} ({{ bonusImplementedCount }}/{{ bonusTotalCount }})
              </v-btn>
            </v-btn-toggle>
          </v-col>
          <v-col cols="12" md="3" class="pb-0">
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
          </v-col>
          <v-col cols="12" md="3" class="pb-0">
            <v-autocomplete
              v-model="filter.budgetBusinessAccounts"
              :items="bas"
              item-title="name"
              item-value="id"
              :label="t('Бюджет из бизнес аккаунта')"
              multiple
              chips
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="salary-requests-filter-ba"
            />
          </v-col>
          <v-col cols="12" md="2" class="pb-0">
            <v-select
              v-model="filter.implemented"
              :items="implementedOptions"
              item-title="title"
              item-value="value"
              :label="t('Завершён')"
              multiple
              chips
              clearable
              variant="outlined"
              density="compact"
              hide-details
              data-testid="salary-requests-filter-implemented"
            />
          </v-col>
        </v-row>

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
          height="calc(100vh - 340px)"
          :row-props="rowProps"
          data-testid="salary-requests-table"
          @click:row="onRowClick"
        >
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
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import { extractDataTableRow } from "@/lib/data-table";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { useSalaryRequests } from "@/composables/useSalaryRequests";
import type { SalaryApprovalState, SalaryRequestApproval } from "@/services/salary.service";

const { t } = useI18n();
const {
  loading,
  error,
  bas,
  filter,
  selectedPeriod,
  headers,
  filteredItems,
  periodClosed,
  canViewSalaryRequests,
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
} = useSalaryRequests(t);
const router = useRouter();

const implementedOptions = computed(() => [
  { title: t("Нет"), value: false },
  { title: t("Да"), value: true },
]);

function formatMoney(value: number | null | undefined): string {
  if (value == null) {
    return "";
  }
  return Number(value).toLocaleString();
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
  return {
    class: "cursor-pointer",
  };
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
  }).catch(() => undefined);
}
</script>
