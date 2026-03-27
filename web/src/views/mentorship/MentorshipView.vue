<template>
  <TableFirstPageLayout test-id="mentorship-view">
    <v-alert
      v-if="!canViewMentorship"
      type="warning"
      variant="tonal"
      border="start"
    >
      {{ t("Не достаточно прав") }}
    </v-alert>

        <v-card v-else class="d-flex flex-column h-100" data-testid="mentorship-card">
      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="ma-4 mb-0"
      >
        {{ error }}
      </v-alert>

      <v-card-text class="pt-4 pb-2 d-flex flex-column flex-grow-1 min-h-0">
        <AdaptiveFilterBar
          :items="filterBarItems"
          :has-right-actions="canManageRegistry"
        >
          <template #left-actions>
            <table-toolbar-actions
              :disabled="loading || actionLoading || exportLoading"
              show-refresh
              :show-export="canManageRegistry"
              :refresh-label="t('Обновить данные')"
              :export-label="t('Экспорт в Excel')"
              @refresh="loadJuniors"
              @export="downloadExport"
            />
          </template>

          <template #filter-search>
            <v-text-field
              v-model="filter.search"
              data-testid="mentorship-filter-search"
              density="compact"
              clearable
              :disabled="loading"
              :label="t('Поиск')"
              prepend-inner-icon="mdi-magnify"
              variant="outlined"
              hide-details
            />
          </template>

          <template #filter-ba>
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
              variant="outlined"
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.selectedBas.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-role>
            <v-autocomplete
              v-model="filter.selectedRoles"
              density="compact"
              clearable
              multiple
              :disabled="loading"
              :items="roles"
              :label="t('Роль')"
              variant="outlined"
              hide-details
            >
              <template #selection="{ item, index }">
                <CollapsedSelectionContent
                  :index="index"
                  :total="filter.selectedRoles.length"
                  :label="getFilterSelectionLabel(item)"
                />
              </template>
            </v-autocomplete>
          </template>

          <template #filter-not-graduated>
            <v-checkbox
              v-model="filter.onlyNotGraduated"
              density="compact"
              :disabled="loading"
              :label="t('Завершил обучение')"
              hide-details
              :false-value="true"
              :true-value="false"
            />
          </template>

          <template #right-actions>
            <table-toolbar-actions
              v-if="canManageRegistry"
              :disabled="loading || actionLoading || exportLoading"
              :show-add="canManageRegistry"
              :add-label="t('Добавление в реестр')"
              @add="openAddDialog"
            />
          </template>
        </AdaptiveFilterBar>
      </v-card-text>


      <div class="flex-grow-1 min-h-0">
      <HREasyTableBase
        table-class="mentorship-table"
        fixed-header
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :headers="headers"
        :items="filteredItems"
        density="compact"
        multi-sort
        :sort-by="[{ key: 'juniorEmpl.name', order: 'asc' }]"
        :row-props="buildRowProps"
        height="fill"
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
          <value-with-status-chip :value="item.juniorInCompanyMonths" dense />
        </template>

        <template #[`item.monthsWithoutReport.value`]="{ item }">
          <value-with-status-chip :value="item.monthsWithoutReport" dense />
        </template>

        <template #[`item.juniorEmpl.name`]="{ item }">
          <router-link
            class="mentorship-name-cell"
            :to="{ name: 'mentorship-details', params: { juniorRegistryId: String(item.id) } }"
            @click.stop
          >
            {{ item.juniorEmpl?.name ?? "" }}
          </router-link>
        </template>

        <template #[`item.mentor.name`]="{ item }">
          <span class="mentorship-name-cell">{{ item.mentor?.name ?? "" }}</span>
        </template>

        <template #[`item.latestReport.createdAt`]="{ item }">
          {{ formatDateTime(item.latestReport?.createdAt) }}
        </template>

        <template #[`item.latestReport.comment`]="{ item }">
          <v-tooltip v-if="item.latestReport?.comment" location="bottom" max-width="33vw">
            <template #activator="{ props }">
              <span v-bind="props" class="mentorship-comment-cell">
                {{ item.latestReport.comment }}
              </span>
            </template>
            <span class="mentorship-comment-tooltip">{{ item.latestReport.comment }}</span>
          </v-tooltip>
          <span v-else class="mentorship-comment-cell"></span>
        </template>

        <template #[`item.graduation.graduatedAt`]="{ item }">
          {{ formatDateTime(item.graduation?.graduatedAt) }}
        </template>
      </HREasyTableBase>
      </div>
    </v-card>

    <v-dialog v-model="addDialog" max-width="760">
      <v-card>
        <v-card-title>{{ t("Добавление в реестр") }}</v-card-title>
        <v-card-text>
          <junior-registry-form-fields
            :form="addForm"
            mode="add"
            :employees="employees"
            :business-accounts="allBusinessAccounts"
            :project-roles="projectRoles"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="addDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitAddJunior">{{ t("Создать") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import JuniorRegistryFormFields from "@/components/mentorship/JuniorRegistryFormFields.vue";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";
import { formatDateTime } from "@/lib/datetime";
import { useJuniorRegistry } from "@/composables/useJuniorRegistry";

const { t } = useI18n();
const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: filter.search.trim().length > 0, grow: true },
  { id: "ba", minWidth: 320, active: filter.selectedBas.length > 0 },
  { id: "role", minWidth: 320, active: filter.selectedRoles.length > 0 },
  { id: "not-graduated", minWidth: 220, active: filter.onlyNotGraduated },
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
const {
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
  filteredItems,
  buildRowProps,
  getProgressIcon,
  reportsOrderedAsc,
  openAddDialog,
  loadJuniors,
  submitAddJunior,
  downloadExport,
} = useJuniorRegistry(t);
</script>

<style scoped>
.mentorship-name-cell {
  display: inline-block;
  min-width: 220px;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
}

.mentorship-comment-cell {
  display: inline-block;
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
}

.mentorship-comment-tooltip {
  display: inline-block;
  max-width: 33vw;
  white-space: normal;
  overflow-wrap: anywhere;
}
</style>
