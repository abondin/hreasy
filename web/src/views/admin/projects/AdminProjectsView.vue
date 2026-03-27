<template>
  <TableFirstPageLayout test-id="admin-projects-view">
    <v-card class="d-flex flex-column h-100" data-testid="admin-projects-card">
      <HREasyTableBase
        table-class="admin-projects-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="fill"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        hover
        :sort-by="[{ key: 'name', order: 'asc' }]"
        :row-props="rowProps"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-text class="pt-4 pb-2">
            <AdaptiveFilterBar :items="filterBarItems" :has-right-actions="true">
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
                  prepend-inner-icon="mdi-magnify"
                  :label="t('Поиск')"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                  data-testid="admin-projects-search"
                />
              </template>
              <template #filter-ba>
                <v-autocomplete
                  v-model="selectedBas"
                  :items="businessAccounts"
                  item-title="name"
                  item-value="id"
                  :label="t('Бизнес аккаунт')"
                  variant="outlined"
                  density="compact"
                  multiple
                  clearable
                  hide-details
                  data-testid="admin-projects-filter-ba"
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
              <template #filter-departments>
                <v-autocomplete
                  v-model="selectedDepartments"
                  :items="departments"
                  item-title="name"
                  item-value="id"
                  :label="t('Отдел')"
                  variant="outlined"
                  density="compact"
                  multiple
                  clearable
                  hide-details
                  data-testid="admin-projects-filter-departments"
                >
                  <template #selection="{ item, index }">
                    <CollapsedSelectionContent
                      :index="index"
                      :total="selectedDepartments.length"
                      :label="getFilterSelectionLabel(item)"
                    />
                  </template>
                </v-autocomplete>
              </template>
              <template #filter-show-closed>
                <v-checkbox
                  v-model="showClosed"
                  :label="t('Закрытые')"
                  density="compact"
                  hide-details
                  data-testid="admin-projects-show-closed"
                />
              </template>
              <template #right-actions>
                <table-toolbar-actions
                  :disabled="loading"
                  show-add
                  :add-label="t('Добавить')"
                  @add="openCreate"
                />
              </template>
            </AdaptiveFilterBar>
          </v-card-text>
        </template>

        <template #before-table>
          <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-3">
            {{ error }}
          </v-alert>
        </template>

        <template #[`item.businessAccount.name`]="{ item }">
          {{ item.businessAccount?.name ?? "-" }}
        </template>
        <template #[`item.name`]="{ item }">
          <router-link
            :to="{ name: 'admin-project-details', params: { projectId: String(item.id) } }"
            @click.stop
          >
            {{ item.name }}
          </router-link>
        </template>
        <template #[`item.department.name`]="{ item }">
          {{ item.department?.name ?? "-" }}
        </template>
        <template #[`item.planStartDate`]="{ item }">
          {{ formatDate(item.planStartDate) }}
        </template>
        <template #[`item.startDate`]="{ item }">
          {{ formatDate(item.startDate) }}
        </template>
        <template #[`item.planEndDate`]="{ item }">
          {{ formatDate(item.planEndDate) }}
        </template>
        <template #[`item.endDate`]="{ item }">
          {{ formatDate(item.endDate) }}
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="dialog" persistent width="96vw" max-width="960">
      <admin-project-form
        :input="null"
        :departments="departments"
        :business-accounts="businessAccounts"
        @close="dialog = false"
        @saved="onCreated"
      />
    </v-dialog>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import CollapsedSelectionContent from "@/components/shared/CollapsedSelectionContent.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { formatDate } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import type { DictItem } from "@/services/dict.service";
import { fetchBusinessAccounts, fetchDepartments } from "@/services/dict.service";
import { listAdminProjects, type AdminProjectInfo } from "@/services/admin/admin-project.service";
import AdminProjectForm from "@/views/admin/projects/components/AdminProjectForm.vue";

const { t } = useI18n();
const router = useRouter();

const loading = ref(false);
const dialog = ref(false);
const error = ref<string | null>(null);

const search = ref("");
const showClosed = ref(false);
const selectedBas = ref<number[]>([]);
const selectedDepartments = ref<number[]>([]);

const items = ref<AdminProjectInfo[]>([]);
const departments = ref<DictItem[]>([]);
const businessAccounts = ref<DictItem[]>([]);

const headers = computed(() => [
  { title: t("Наименование"), key: "name", width: "240px" },
  { title: t("Бизнес аккаунт"), key: "businessAccount.name", width: "220px" },
  { title: t("Заказчик"), key: "customer", width: "220px" },
  { title: t("Начало (план)"), key: "planStartDate", width: "150px" },
  { title: t("Начало (факт)"), key: "startDate", width: "150px" },
  { title: t("Окончание (план)"), key: "planEndDate", width: "170px" },
  { title: t("Окончание (факт)"), key: "endDate", width: "170px" },
  { title: t("Отдел"), key: "department.name", width: "220px" },
]);

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "ba", minWidth: 320, active: selectedBas.value.length > 0 },
  { id: "departments", minWidth: 320, active: selectedDepartments.value.length > 0 },
  { id: "show-closed", minWidth: 240, active: showClosed.value },
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

const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase();
  return items.value.filter((item) => {
    if (!showClosed.value && !item.active) {
      return false;
    }
    if (selectedBas.value.length > 0) {
      const baId = item.businessAccount?.id;
      if (!baId || !selectedBas.value.includes(baId)) {
        return false;
      }
    }
    if (selectedDepartments.value.length > 0) {
      const departmentId = item.department?.id;
      if (!departmentId || !selectedDepartments.value.includes(departmentId)) {
        return false;
      }
    }
    if (!query) {
      return true;
    }
    return [item.name, item.customer, item.department?.name, item.businessAccount?.name]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(query);
  });
});

function rowProps() {
  return { class: "cursor-pointer" };
}

function openCreate(): void {
  dialog.value = true;
}

function onClickRow(_event: Event, payload: unknown): void {
  const row = extractDataTableRow<AdminProjectInfo>(payload);
  if (!row?.id) {
    return;
  }
  router.push({ name: "admin-project-details", params: { projectId: String(row.id) } }).catch(() => undefined);
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const [projects, departmentItems, baItems] = await Promise.all([
      listAdminProjects(),
      fetchDepartments(),
      fetchBusinessAccounts(),
    ]);
    items.value = projects;
    departments.value = departmentItems;
    businessAccounts.value = baItems;
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function onCreated(projectId: number): void {
  dialog.value = false;
  router.push({ name: "admin-project-details", params: { projectId: String(projectId) } }).catch(() => undefined);
}

void load();
</script>

<style scoped>
.admin-projects-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
