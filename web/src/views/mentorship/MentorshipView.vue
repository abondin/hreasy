<template>
  <v-container class="py-6">
    <v-alert
      v-if="!canViewMentorship"
      type="warning"
      variant="tonal"
      border="start"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

    <v-card v-else>
      <v-container class="pt-4">
        <v-row align="center">
          <v-col cols="12" sm="4" class="pb-0">
            <v-text-field
              v-model="filter.search"
              density="compact"
              clearable
              :disabled="loading"
              :label="t('Поиск')"
            />
          </v-col>

          <v-col cols="12" sm="3" class="pb-0">
            <v-autocomplete
              v-model="filter.selectedBas"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="baOptions"
              item-title="name"
              item-value="id"
              :label="t('Бюджет')"
            />
          </v-col>

          <v-col cols="12" sm="3" class="pb-0">
            <v-autocomplete
              v-model="filter.selectedRoles"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="roles"
              :label="t('Роль')"
            />
          </v-col>

          <v-col cols="12" sm="2" class="pb-0">
            <v-checkbox
              v-model="filter.onlyNotGraduated"
              density="compact"
              :disabled="loading"
              :label="t('Завершил обучение')"
              hide-details
              :false-value="true"
              :true-value="false"
            />
          </v-col>
        </v-row>

        <v-row>
          <v-col cols="auto">
            <v-btn
              color="primary"
              variant="tonal"
              prepend-icon="mdi-refresh"
              :loading="loading"
              @click="loadJuniors"
            >
              {{ t("Обновить данные") }}
            </v-btn>
          </v-col>
        </v-row>
      </v-container>

      <v-data-table
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :headers="headers"
        :items="filteredItems"
        density="compact"
        multi-sort
        :sort-by="[{ key: 'juniorEmpl.name', order: 'asc' }]"
        :items-per-page="25"
        :row-props="buildRowProps"
      >
        <template #[`item.progress`]="{ item }">
          <div class="d-flex ga-1 flex-wrap">
            <v-tooltip
              v-for="report in reportsOrderedAsc(item.reports)"
              :key="report.id"
              location="bottom"
            >
              <template #activator="{ props }">
                <v-icon
                  v-bind="props"
                  :icon="getProgressIcon(report.progress).icon"
                  :color="getProgressIcon(report.progress).color"
                />
              </template>
              <p>{{ report.createdBy.name }}</p>
              <p>{{ formatDateTime(report.createdAt) }}</p>
              <p>{{ report.comment }}</p>
            </v-tooltip>
          </div>
        </template>

        <template #[`item.juniorInCompanyMonths.value`]="{ item }">
          {{ item.juniorInCompanyMonths?.value ?? "" }}
        </template>

        <template #[`item.monthsWithoutReport.value`]="{ item }">
          {{ item.monthsWithoutReport?.value ?? "" }}
        </template>

        <template #[`item.latestReport.createdAt`]="{ item }">
          {{ formatDateTime(item.latestReport?.createdAt) }}
        </template>

        <template #[`item.graduation.graduatedAt`]="{ item }">
          {{ formatDateTime(item.graduation?.graduatedAt) }}
        </template>
      </v-data-table>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";
import { formatDateTime } from "@/lib/datetime";
import {
  fetchJuniorsRegistry,
  JuniorProgressType,
  type JuniorDto,
  type JuniorReport,
  type SimpleDict,
} from "@/services/junior-registry.service";

interface JuniorFilter {
  search: string;
  onlyNotGraduated: boolean;
  selectedBas: number[];
  selectedRoles: string[];
}

const { t } = useI18n();
const permissions = usePermissions();
const router = useRouter();
const loading = ref(false);
const juniors = ref<JuniorDto[]>([]);

const filter = reactive<JuniorFilter>({
  search: "",
  onlyNotGraduated: true,
  selectedBas: [],
  selectedRoles: [],
});

const canViewMentorship = computed(
  () => permissions.canAccessJuniorsRegistry() || permissions.canAdminJuniorRegistry(),
);

const headers = computed(() => [
  { title: t("Молодой специалист"), key: "juniorEmpl.name" },
  { title: t("Ментор"), key: "mentor.name" },
  { title: t("Роль"), key: "role" },
  { title: t("Бюджет"), key: "budgetingAccount.name" },
  { title: t("Текущий проект"), key: "currentProject.name" },
  { title: t("Месяцев в компании"), key: "juniorInCompanyMonths.value" },
  { title: t("Месяцев без отчёта"), key: "monthsWithoutReport.value" },
  { title: t("Прогресс"), key: "progress", sortable: false },
  { title: t("Последний срез (Когда)"), key: "latestReport.createdAt" },
  { title: t("Последний срез (Кто)"), key: "latestReport.createdBy.name" },
  { title: t("Последний срез (Комментарий)"), key: "latestReport.comment" },
  { title: t("Завершил обучение"), key: "graduation.graduatedAt" },
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

    if (!search) {
      return true;
    }

    const searchText = [
      item.juniorEmpl?.name,
      item.mentor?.name,
      item.latestReport?.createdBy?.name,
      item.role,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();

    return searchText.includes(search);
  });
});


function buildRowProps({ item }: { item: JuniorDto }): Record<string, unknown> {
  return {
    class: "cursor-pointer",
    onClick: () => {
      router.push({ name: "mentorship-details", params: { juniorRegistryId: item.id } }).catch(() => {});
    },
  };
}
function getProgressIcon(type: JuniorProgressType): { icon: string; color: string } {
  switch (type) {
    case JuniorProgressType.DEGRADATION:
      return { icon: "mdi-arrow-bottom-left", color: "error" };
    case JuniorProgressType.NO_PROGRESS:
      return { icon: "mdi-minus", color: "" };
    case JuniorProgressType.PROGRESS:
      return { icon: "mdi-arrow-top-right", color: "success" };
    case JuniorProgressType.GOOD_PROGRESS:
      return { icon: "mdi-arrow-up-bold", color: "success" };
    default:
      return { icon: "mdi-help", color: "warning" };
  }
}

function reportsOrderedAsc(reports: JuniorReport[]): JuniorReport[] {
  return [...(reports ?? [])].sort((a, b) => a.createdAt.localeCompare(b.createdAt));
}

async function loadJuniors(): Promise<void> {
  if (!canViewMentorship.value) {
    return;
  }
  loading.value = true;
  try {
    juniors.value = await fetchJuniorsRegistry();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadJuniors().catch((error: unknown) => {
    console.error(error);
  });
});
</script>
