<template>
  <div class="d-flex flex-column h-100 min-h-0">
    <HREasyTableBase
      class="flex-grow-1 min-h-0 vacations-list-table text-truncate"
      height="fill"
      fixed-header
      :loading="loading"
      :loading-text="t('Загрузка_данных')"
      :no-data-text="t('Отсутствуют данные')"
      :headers="headers"
      :items="items"
      :sort-by="[{ key: 'employeeDisplayName', order: 'asc' }]"
      density="compact"
      multi-sort
      hover
      @click:row="onRowClick"
    >
      <template #[`item.employeeDisplayName`]="{ item }">
        <v-hover v-slot="{ isHovering, props: hoverProps }">
          <div
            v-bind="hoverProps"
            class="d-inline-flex align-center ga-1"
          >
            <span>{{ item.employeeDisplayName }}</span>
            <v-fade-transition>
              <v-tooltip
                v-if="isHovering || smAndDown"
                location="bottom"
              >
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon="mdi-content-copy"
                    size="x-small"
                    variant="text"
                    @click.stop="copyToClipboard(item)"
                  />
                </template>
                <span>{{ t("Скопировать в буфер обмена") }}</span>
              </v-tooltip>
            </v-fade-transition>
          </div>
        </v-hover>
      </template>
      <template #[`item.startDate`]="{ item }">
        {{ formatDate(item.startDate) }}
      </template>
      <template #[`item.endDate`]="{ item }">
        {{ formatDate(item.endDate) }}
      </template>
      <template #[`item.plannedStartDate`]="{ item }">
        {{ formatDate(item.plannedStartDate) }}
      </template>
      <template #[`item.plannedEndDate`]="{ item }">
        {{ formatDate(item.plannedEndDate) }}
      </template>
      <template #[`item.status`]="{ item }">
        {{ t(`VACATION_STATUS_ENUM.${item.status}`) }}
      </template>
    </HREasyTableBase>
  </div>
</template>

<script setup lang="ts">
import { useDisplay } from "vuetify";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import { formatDate } from "@/lib/datetime";
import type { Vacation } from "@/services/vacation.service";
import type { VDataTable } from "vuetify/components";

defineProps<{
  loading: boolean;
  headers: VDataTable["$props"]["headers"];
  items: Vacation[];
  onRowClick: (event: Event, payload: unknown) => void;
  copyToClipboard: (vacation: Vacation | null) => Promise<void>;
}>();

const { t } = useI18n();
const { smAndDown } = useDisplay();
</script>
