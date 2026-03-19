<template>
  <v-container class="py-6" data-testid="admin-business-accounts-view">
    <v-card data-testid="admin-business-accounts-card">
      <HREasyTableBase
        table-class="admin-business-accounts-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430_\u0434\u0430\u043d\u043d\u044b\u0445')"
        :no-data-text="t('\u041e\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044e\u0442 \u0434\u0430\u043d\u043d\u044b\u0435')"
        :row-props="rowProps"
        hover
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex ga-2 align-center flex-wrap">
            <v-btn
              icon="mdi-refresh"
              variant="text"
              :loading="loading"
              data-testid="admin-business-accounts-refresh"
              @click="load"
            />
            <v-btn
              icon="mdi-plus"
              color="primary"
              variant="text"
              :disabled="loading"
              data-testid="admin-business-accounts-add"
              @click="openCreate"
            />
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row density="comfortable">
              <v-col cols="12" lg="4">
                <v-text-field
                  v-model="search"
                  :label="t('\u041f\u043e\u0438\u0441\u043a')"
                  append-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </v-col>
              <v-col cols="12" lg="4" class="d-flex align-center">
                <v-checkbox
                  v-model="showArchived"
                  :label="t('\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0430\u0440\u0445\u0438\u0432\u043d\u044b\u0435 \u0431\u0438\u0437\u043d\u0435\u0441 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u044b')"
                  density="compact"
                  hide-details
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
    </v-card>

    <v-dialog v-model="dialog" persistent max-width="720">
      <admin-business-account-form
        :input="null"
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
  { title: t("\u041d\u0430\u0438\u043c\u0435\u043d\u043e\u0432\u0430\u043d\u0438\u0435"), key: "name", width: "280px" },
  { title: t("\u041e\u043f\u0438\u0441\u0430\u043d\u0438\u0435"), key: "description" },
  { title: t("\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440\u044b"), key: "managers", width: "360px" },
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
