<template>
  <HREasyTableBase
    data-testid="vacations-summary-table"
    class="flex-grow-1 min-h-0 text-truncate"
    height="fill"
    item-key="employee"
    fixed-header
    :loading="loading"
    :loading-text="t('Р—Р°РіСЂСѓР·РєР°_РґР°РЅРЅС‹С…')"
    :no-data-text="t('РћС‚СЃСѓС‚СЃС‚РІСѓСЋС‚ РґР°РЅРЅС‹Рµ')"
    :headers="headers"
    :items="items"
    :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
    density="compact"
    multi-sort
    hover
    @click:row="onRowClick"
  >
    <template #[`item.employeeDisplayName`]="{ item }">
      <span>{{ item.employeeDisplayName }}</span>
    </template>
    <template #[`item.upcomingVacation`]="{ item }">
      <span v-if="item.upcomingVacation">
        {{ formatDate(item.upcomingVacation.startDate) }} -
        {{ formatDate(item.upcomingVacation.endDate) }}
        ({{ t(`VACATION_STATUS_ENUM.${item.upcomingVacation.status}`) }})
      </span>
    </template>
  </HREasyTableBase>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { VDataTable } from "vuetify/components";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import { formatDate } from "@/lib/datetime";
import type { EmployeeVacationSummary } from "@/components/vacations/employeeVacationSummaryService";

defineProps<{
  loading: boolean;
  headers: VDataTable["$props"]["headers"];
  items: EmployeeVacationSummary[];
  onRowClick: (event: Event, payload: unknown) => void;
}>();

const { t } = useI18n();
</script>
