<template>
  <v-container class="py-6" fluid data-testid="salary-latest-view">
    <v-alert
      v-if="!canAccess"
      type="warning"
      variant="tonal"
      class="mb-4"
      data-testid="salary-latest-no-access"
    >
      {{ t("Недостаточно прав") }}
    </v-alert>

        <v-card v-else data-testid="salary-latest-card">
      <v-card-text class="pt-4 pb-2">
        <AdaptiveFilterBar
          :items="filterBarItems"
          :has-left-actions="true"
          :has-right-actions="false"
        >
          <template #left-actions>
            <table-toolbar-actions
              :disabled="loading"
              show-refresh
              :refresh-label="t('Обновить данные')"
              @refresh="load"
            />
          </template>

          <template #filter-search>
            <v-text-field
              v-model="search"
              :label="t('Поиск')"
              prepend-inner-icon="mdi-magnify"
              clearable
              density="compact"
              variant="outlined"
              hide-details
              data-testid="salary-latest-filter-search"
            />
          </template>

          <template #filter-ba>
            <v-autocomplete
              v-model="selectedBas"
              :items="bas"
              item-title="name"
              item-value="id"
              :label="t('Бизнес аккаунт')"
              multiple
              clearable
              density="compact"
              variant="outlined"
              hide-details
              data-testid="salary-latest-filter-ba"
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="selectedBas.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-only-with-requests>
            <v-select
              v-model="onlyWithRequests"
              :items="yesNoOptions"
              item-title="title"
              item-value="value"
              :label="t('Только с запросами')"
              density="compact"
              variant="outlined"
              hide-details
              data-testid="salary-latest-filter-only-with-requests"
            />
          </template>
        </AdaptiveFilterBar>


        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          class="mb-3"
          data-testid="salary-latest-error"
        >
          {{ error }}
        </v-alert>

        <HREasyTableBase
          table-class="salary-latest-table text-truncate"
          :headers="headers"
          :items="filteredItems"
          :loading="loading"
          :loading-text="t('Загрузка_данных')"
          :no-data-text="t('Отсутствуют данные')"
          fixed-header
          hover
          density="compact"
          height="calc(100vh - 320px)"
          :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
          data-testid="salary-latest-table"
        >
          <template #[`item.employeeDisplayName`]="{ item }">
            <router-link
              v-if="item.requestId && item.requestStartPeriod"
              :to="{
                name: 'salary-request-details',
                params: { period: String(item.requestStartPeriod), requestId: String(item.requestId) },
                query: { source: 'salary-latest' },
              }"
            >
              {{ item.employeeDisplayName }}
            </router-link>
            <span v-else>{{ item.employeeDisplayName }}</span>
          </template>
          <template #[`item.employeeCurrentProject`]="{ item }">
            {{ item.employeeCurrentProject?.name ?? "" }}
            <span v-if="item.employeeCurrentProject?.role">({{ item.employeeCurrentProject.role }})</span>
          </template>
          <template #[`item.requestStartPeriod`]="{ item }">
            {{ formatPeriod(item.requestStartPeriod) }}
          </template>
          <template #[`item.requestReqIncreaseAmount`]="{ item }">
            {{ formatMoney(item.requestReqIncreaseAmount) }}
          </template>
          <template #[`item.requestImplIncreaseAmount`]="{ item }">
            {{ formatMoney(item.requestImplIncreaseAmount) }}
          </template>
          <template #[`item.requestImplSalaryAmount`]="{ item }">
            {{ formatMoney(item.requestImplSalaryAmount) }}
          </template>
          <template #[`item.requestImplState`]="{ item }">
            <span :class="item.requestImplState === 2 ? 'text-error' : 'text-success'">
              {{ item.requestImplState ? t(`SALARY_REQUEST_STAT.${item.requestImplState}`) : "" }}
            </span>
          </template>
        </HREasyTableBase>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { errorUtils } from "@/lib/errors";
import { usePermissions } from "@/lib/permissions";
import { fetchBusinessAccounts, type DictItem } from "@/services/dict.service";
import {
  fetchEmployeesWithLatestSalaryRequest,
  type EmployeeWithLatestSalaryRequest,
} from "@/services/salary.service";
import { ReportPeriod } from "@/services/overtime.service";

const { t } = useI18n();
const permissions = usePermissions();

const loading = ref(false);
const error = ref("");
const items = ref<EmployeeWithLatestSalaryRequest[]>([]);
const bas = ref<DictItem[]>([]);
const search = ref("");
const selectedBas = ref<number[]>([]);
const onlyWithRequests = ref(true);

const canAccess = computed(() => permissions.canAdminSalaryRequests());

const headers = computed(() => [
  { title: t("Сотрудник"), key: "employeeDisplayName", width: "260px" },
  { title: t("Бизнес аккаунт"), key: "employeeBusinessAccount.name", width: "220px" },
  { title: t("Проект"), key: "employeeCurrentProject", width: "220px" },
  { title: t("Период"), key: "requestStartPeriod", width: "140px" },
  { title: t("Запрошенное повышение"), key: "requestReqIncreaseAmount", width: "170px" },
  { title: t("Итоговое повышение"), key: "requestImplIncreaseAmount", width: "170px" },
  { title: t("Зарплата после повышения"), key: "requestImplSalaryAmount", width: "190px" },
  { title: t("Статус"), key: "requestImplState", width: "140px" },
]);

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "ba", minWidth: 320, active: selectedBas.value.length > 0 },
  { id: "only-with-requests", minWidth: 240, active: onlyWithRequests.value !== true },
]);

function getFilterSelectionLabel(item: unknown): string {
  if (typeof item === "string") {
    return item;
  }
  if (item && typeof item === "object" && "name" in item && typeof item.name === "string") {
    return item.name;
  }
  return "";
}

const yesNoOptions = computed(() => [
  { title: t("Да"), value: true },
  { title: t("Нет"), value: false },
]);

const filteredItems = computed(() => {
  const normalizedSearch = search.value.toLowerCase().trim();

  return items.value.filter((item) => {
    if (onlyWithRequests.value && !item.requestId) {
      return false;
    }

    if (selectedBas.value.length > 0) {
      const baId = item.employeeBusinessAccount?.id;
      if (!baId || !selectedBas.value.includes(baId)) {
        return false;
      }
    }

    if (!normalizedSearch) {
      return true;
    }

    return [
      item.employeeDisplayName,
      item.employeeEmail,
      item.employeeCurrentProject?.name,
      item.employeeCurrentProject?.role,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(normalizedSearch);
  });
});

onMounted(() => {
  load().catch(() => undefined);
});

async function load(): Promise<void> {
  if (!canAccess.value) {
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const [latestRequests, businessAccounts] = await Promise.all([
      fetchEmployeesWithLatestSalaryRequest(),
      fetchBusinessAccounts(),
    ]);
    items.value = latestRequests;
    bas.value = businessAccounts;
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}

function formatPeriod(period: number | null): string {
  if (!period) {
    return "";
  }
  return ReportPeriod.fromPeriodId(period).toString();
}

function formatMoney(value: number | null): string {
  if (value == null) {
    return "";
  }
  return Number(value).toLocaleString("ru-RU");
}
</script>
