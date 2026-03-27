<template>
  <v-card class="mt-4 d-flex flex-column flex-grow-1 min-h-0" data-testid="admin-kids-view">
    <HREasyTableBase
      table-class="admin-kids-table text-truncate"
      :headers="headers"
      :items="filteredItems"
      height="fill"
      fixed-header
      :loading="loading"
      :loading-text="t('Загрузка_данных')"
      :no-data-text="t('Отсутствуют данные')"
      density="compact"
      hover
      :sort-by="[{ key: 'displayName', order: 'asc' }]"
      :row-props="rowProps"
      @click:row="onClickRow"
    >
      <template #filters>
        <v-card-text class="pt-4 pb-2">
          <AdaptiveFilterBar :items="filterBarItems" :has-right-actions="permissions.canEditEmployees()">
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
                data-testid="admin-kids-search"
                prepend-inner-icon="mdi-magnify"
                density="compact"
                :label="t('Поиск')"
                variant="outlined"
                hide-details
                clearable
              />
            </template>
            <template #filter-hide-dismissed>
              <v-checkbox
                v-model="hideDismissed"
                data-testid="admin-kids-hide-dismissed"
                density="compact"
                hide-details
                :label="t('Скрыть детей уволенных сотрудников')"
              />
            </template>
            <template #right-actions>
              <table-toolbar-actions
                v-if="permissions.canEditEmployees()"
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

      <template #[`item.birthday`]="{ item }">
        {{ formatDate(item.birthday) }}
      </template>
      <template #[`item.parent.active`]="{ item }">
        {{ item.parent?.active ? t("Нет") : t("Да") }}
      </template>
    </HREasyTableBase>

    <v-dialog v-model="dialog" persistent max-width="760" data-testid="admin-kids-edit-dialog">
      <AdminEmployeeKidForm
        :input="current"
        :employees="employees"
        @close="dialog = false"
        @saved="onSaved"
      />
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import AdminEmployeeKidForm from "@/views/admin/employees/components/AdminEmployeeKidForm.vue";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";
import { extractDataTableRow } from "@/lib/data-table";
import { formatDate } from "@/lib/datetime";
import { listEmployees, type Employee } from "@/services/employee.service";
import {
  listEmployeeKids,
  type EmployeeKid,
} from "@/services/admin/admin-employee.service";

const { t } = useI18n();
const permissions = usePermissions();
const loading = ref(false);
const dialog = ref(false);
const error = ref<string | null>(null);
const search = ref("");
const hideDismissed = ref(true);
const kids = ref<EmployeeKid[]>([]);
const employees = ref<Employee[]>([]);
const current = ref<EmployeeKid | null>(null);

const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "hide-dismissed", minWidth: 280, active: hideDismissed.value !== true },
]);

const headers = computed(() => [
  { title: t("ФИО"), key: "displayName", width: 280 },
  { title: t("День рождения"), key: "birthday", width: 150 },
  { title: t("Возраст (лет)"), key: "age", width: 150 },
  { title: t("Родитель"), key: "parent.name", width: 280 },
  { title: t("Родитель уволен"), key: "parent.active", width: 120 },
]);

const filteredItems = computed(() => {
  const q = search.value.trim().toLowerCase();
  return kids.value.filter((it) => {
    if (hideDismissed.value && !it.parent?.active) {
      return false;
    }
    if (!q) {
      return true;
    }
    return [it.displayName, it.parent?.name]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(q);
  });
});

function rowProps() {
  return permissions.canEditEmployees() ? { class: "cursor-pointer" } : undefined;
}

function onClickRow(_event: Event, payload: unknown): void {
  if (!permissions.canEditEmployees()) {
    return;
  }
  const row = extractRow(payload);
  if (!row) {
    return;
  }
  current.value = row;
  dialog.value = true;
}

function openCreate(): void {
  if (!permissions.canEditEmployees()) {
    return;
  }
  current.value = null;
  dialog.value = true;
}

function extractRow(payload: unknown): EmployeeKid | null {
  return extractDataTableRow(payload, isEmployeeKid);
}

function isEmployeeKid(value: unknown): value is EmployeeKid {
  if (!value || typeof value !== "object") {
    return false;
  }
  return "id" in value && "displayName" in value && "parent" in value;
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = null;
  try {
    const [allKids, allEmployees] = await Promise.all([listEmployeeKids(), listEmployees()]);
    kids.value = allKids;
    employees.value = allEmployees;
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    loading.value = false;
  }
}

async function onSaved(): Promise<void> {
  dialog.value = false;
  await load();
}

load().catch(() => undefined);
</script>
