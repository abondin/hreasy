<template>
  <v-container class="py-6" data-testid="admin-projects-view">
    <v-card data-testid="admin-projects-card">
      <HREasyTableBase
        table-class="admin-projects-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430_\u0434\u0430\u043d\u043d\u044b\u0445')"
        :no-data-text="t('\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442 \u0434\u0430\u043d\u043d\u044b\u0435')"
        hover
        :sort-by="[{ key: 'name', order: 'asc' }]"
        :row-props="rowProps"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex ga-2 align-center flex-wrap">
            <v-btn
              icon="mdi-refresh"
              variant="text"
              :loading="loading"
              data-testid="admin-projects-refresh"
              @click="load"
            />

            <v-tooltip location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-plus"
                  color="primary"
                  variant="text"
                  :disabled="loading"
                  data-testid="admin-projects-add"
                  @click="openCreate"
                />
              </template>
              <span>{{ t("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043d\u043e\u0432\u044b\u0439 \u043f\u0440\u043e\u0435\u043a\u0442") }}</span>
            </v-tooltip>
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row dense>
              <v-col cols="12" lg="3">
                <v-text-field
                  v-model="search"
                  append-inner-icon="mdi-magnify"
                  :label="t('\u041f\u043e\u0438\u0441\u043a')"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                  data-testid="admin-projects-search"
                />
              </v-col>
              <v-col cols="12" lg="3">
                <v-autocomplete
                  v-model="selectedBas"
                  :items="businessAccounts"
                  item-title="name"
                  item-value="id"
                  :label="t('\u0411\u0438\u0437\u043d\u0435\u0441 \u0430\u043a\u043a\u0430\u0443\u043d\u0442')"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                  hide-details
                  data-testid="admin-projects-filter-ba"
                />
              </v-col>
              <v-col cols="12" lg="3">
                <v-autocomplete
                  v-model="selectedDepartments"
                  :items="departments"
                  item-title="name"
                  item-value="id"
                  :label="t('\u041e\u0442\u0434\u0435\u043b')"
                  variant="outlined"
                  density="compact"
                  multiple
                  chips
                  clearable
                  hide-details
                  data-testid="admin-projects-filter-departments"
                />
              </v-col>
              <v-col cols="12" lg="3" class="d-flex align-center">
                <v-checkbox
                  v-model="showClosed"
                  :label="t('\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0437\u0430\u043a\u0440\u044b\u0442\u044b\u0435 \u043f\u0440\u043e\u0435\u043a\u0442\u044b')"
                  density="compact"
                  hide-details
                  data-testid="admin-projects-show-closed"
                />
              </v-col>
            </v-row>
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
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
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
  { title: t("\u041d\u0430\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d\u0438\u0435"), key: "name", width: "240px" },
  { title: t("\u0411\u0438\u0437\u043d\u0435\u0441 \u0430\u043a\u043a\u0430\u0443\u043d\u0442"), key: "businessAccount.name", width: "220px" },
  { title: t("\u0417\u0430\u043a\u0430\u0437\u0447\u0438\u043a"), key: "customer", width: "220px" },
  { title: t("\u041d\u0430\u0447\u0430\u043b\u043e (\u043f\u043b\u0430\u043d)"), key: "planStartDate", width: "150px" },
  { title: t("\u041d\u0430\u0447\u0430\u043b\u043e (\u0444\u0430\u043a\u0442)"), key: "startDate", width: "150px" },
  { title: t("\u041e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435 (\u043f\u043b\u0430\u043d)"), key: "planEndDate", width: "170px" },
  { title: t("\u041e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u0435 (\u0444\u0430\u043a\u0442)"), key: "endDate", width: "170px" },
  { title: t("\u041e\u0442\u0434\u0435\u043b"), key: "department.name", width: "220px" },
]);

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
