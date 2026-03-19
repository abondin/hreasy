<template>
  <v-card class="mt-4" data-testid="admin-kids-view">
    <HREasyTableBase
      table-class="admin-kids-table text-truncate"
      :headers="headers"
      :items="filteredItems"
      height="70vh"
      fixed-header
      :loading="loading"
      :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430_\u0434\u0430\u043D\u043D\u044B\u0445')"
      :no-data-text="t('\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435')"
      density="compact"
      hover
      :sort-by="[{ key: 'displayName', order: 'asc' }]"
      :row-props="rowProps"
      @click:row="onClickRow"
    >
      <template #filters>
        <v-card-title class="d-flex ga-3 align-center flex-wrap">
          <v-btn icon="mdi-refresh" variant="text" :loading="loading" @click="load" data-testid="admin-kids-refresh" />
          <v-tooltip v-if="permissions.canEditEmployees()" location="bottom">
            <template #activator="{ props: tooltipProps }">
              <v-btn
                v-bind="tooltipProps"
                icon="mdi-plus"
                data-testid="admin-kids-add"
                color="primary"
                variant="text"
                :disabled="loading"
                @click="openCreate"
              />
            </template>
            <span>{{ t("\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C \u0438\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044E \u043E \u0440\u0435\u0431\u0451\u043D\u043A\u0435") }}</span>
          </v-tooltip>
        </v-card-title>

        <v-card-text class="pb-0">
          <v-row density="comfortable">
            <v-col cols="12" md="6" lg="4">
              <v-text-field
                v-model="search"
                data-testid="admin-kids-search"
                append-inner-icon="mdi-magnify"
                density="compact"
                :label="t('\u041F\u043E\u0438\u0441\u043A')"
                variant="outlined"
                hide-details
                clearable
              />
            </v-col>
            <v-col cols="12" md="6" lg="4" class="d-flex align-center">
              <v-checkbox
                v-model="hideDismissed"
                data-testid="admin-kids-hide-dismissed"
                density="compact"
                hide-details
                :label="t('\u0421\u043A\u0440\u044B\u0442\u044C \u0434\u0435\u0442\u0435\u0439 \u0443\u0432\u043E\u043B\u0435\u043D\u043D\u044B\u0445 \u0441\u043E\u0442\u0440\u0443\u0434\u043D\u0438\u043A\u043E\u0432')"
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

      <template #[`item.birthday`]="{ item }">
        {{ formatDate(item.birthday) }}
      </template>
      <template #[`item.parent.active`]="{ item }">
        {{ item.parent?.active ? t("\u041D\u0435\u0442") : t("\u0414\u0430") }}
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
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
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

const headers = computed(() => [
  { title: t("\u0424\u0418\u041E"), key: "displayName", width: 280 },
  { title: t("\u0414\u0435\u043D\u044C \u0440\u043E\u0436\u0434\u0435\u043D\u0438\u044F"), key: "birthday", width: 150 },
  { title: t("\u0412\u043E\u0437\u0440\u0430\u0441\u0442 (\u043B\u0435\u0442)"), key: "age", width: 150 },
  { title: t("\u0420\u043E\u0434\u0438\u0442\u0435\u043B\u044C"), key: "parent.name", width: 280 },
  { title: t("\u0420\u043E\u0434\u0438\u0442\u0435\u043B\u044C \u0443\u0432\u043E\u043B\u0435\u043D"), key: "parent.active", width: 120 },
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
