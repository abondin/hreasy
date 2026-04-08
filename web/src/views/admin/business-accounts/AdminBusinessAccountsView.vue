<template>
  <TableFirstPageLayout test-id="admin-business-accounts-view">
    <TablePageCard test-id="admin-business-accounts-card" content-class="pa-0 d-flex flex-column flex-grow-1 min-h-0 overflow-hidden">
      <HREasyTableBase
        table-class="admin-business-accounts-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="fill"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :row-props="rowProps"
        hover
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
                  :label="t('Поиск')"
                  prepend-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </template>

              <template #filter-show-archived>
                <v-checkbox
                  v-model="showArchived"
                  :label="t('Архивные')"
                  density="compact"
                  hide-details
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

        <template #[`item.name`]="{ item }">
          <router-link
            :to="{ name: 'admin-business-account-details', params: { businessAccountId: String(item.id) } }"
            @click.stop
          >
            <span :class="{ 'text-decoration-line-through': item.archived }">{{ item.name }}</span>
          </router-link>
        </template>
        <template #[`item.managers`]="{ item }">
          <div class="d-flex flex-wrap ga-1">
            <v-chip
              v-for="manager in item.managers"
              :key="manager.id"
              size="small"
              variant="outlined"
            >
              {{ manager.employeeName }}
            </v-chip>
          </div>
        </template>
      </HREasyTableBase>
    </TablePageCard>

    <v-dialog v-model="dialog" persistent max-width="720">
      <admin-business-account-form
        :input="null"
        @close="dialog = false"
        @saved="onCreated"
      />
    </v-dialog>
  </TableFirstPageLayout>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import TablePageCard from "@/components/shared/TablePageCard.vue";
import TableFirstPageLayout from "@/components/shared/TableFirstPageLayout.vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { extractDataTableRow } from "@/lib/data-table";
import { errorUtils } from "@/lib/errors";
import {
  listAdminBusinessAccounts,
  type BusinessAccountInfo,
} from "@/services/admin/admin-business-account.service";
import AdminBusinessAccountForm from "@/views/admin/business-accounts/components/AdminBusinessAccountForm.vue";

const { t } = useI18n();
const router = useRouter();

const loading = ref(false);
const dialog = ref(false);
const error = ref("");
const search = ref("");
const showArchived = ref(false);
const items = ref<BusinessAccountInfo[]>([]);

const headers = computed(() => [
  { title: t("Наименование"), key: "name", width: "280px" },
  { title: t("Описание"), key: "description" },
  { title: t("Менеджеры"), key: "managers", width: "360px" },
]);
const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "show-archived", minWidth: 280, active: showArchived.value },
]);

const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase();
  return items.value.filter((item) => {
    if (!showArchived.value && item.archived) {
      return false;
    }
    if (!query) {
      return true;
    }
    return [item.name, item.description]
      .filter(Boolean)
      .join(" ")
      .toLowerCase()
      .includes(query);
  });
});

function rowProps() {
  return { class: "cursor-pointer" };
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    items.value = await listAdminBusinessAccounts();
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  dialog.value = true;
}

function onCreated(id: number): void {
  dialog.value = false;
  router.push({ name: "admin-business-account-details", params: { businessAccountId: String(id) } }).catch(() => undefined);
}

function onClickRow(_event: Event, row: unknown): void {
  const item = extractDataTableRow<BusinessAccountInfo>(row);
  if (!item?.id) {
    return;
  }
  router.push({ name: "admin-business-account-details", params: { businessAccountId: String(item.id) } }).catch(() => undefined);
}

void load();
</script>

<style scoped>
.admin-business-accounts-table :deep(tbody tr:hover) {
  cursor: pointer;
}
</style>
