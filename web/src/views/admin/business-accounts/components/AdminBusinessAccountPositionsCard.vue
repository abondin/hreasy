<template>
  <v-card data-testid="admin-business-account-positions-card">
    <v-card-item>
      <template #title>
        <span>{{ t("Позиции бизнес аккаунта") }}</span>
      </template>
      <template #append>
        <v-btn
          icon="mdi-refresh"
          variant="text"
          :disabled="loading"
          data-testid="admin-business-account-positions-refresh"
          @click="load"
        />
        <v-btn
          icon="mdi-plus"
          color="primary"
          variant="text"
          :disabled="loading"
          data-testid="admin-business-account-positions-add"
          @click="openCreate"
        />
      </template>
    </v-card-item>

    <v-card-text class="px-6 pb-5 pt-0">
      <v-row density="comfortable" class="mb-2">
        <v-col cols="12" md="6">
          <v-text-field
            v-model="search"
            :label="t('Поиск')"
            append-inner-icon="mdi-magnify"
            variant="outlined"
            density="compact"
            hide-details
            clearable
          />
        </v-col>
        <v-col cols="12" md="6" class="d-flex align-center">
          <v-checkbox
            v-model="showArchived"
            :label="t('Показать архивные позиции')"
            density="compact"
            hide-details
          />
        </v-col>
      </v-row>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mb-4"
      >
        {{ error }}
      </v-alert>

      <HREasyTableBase
        :headers="headers"
        :items="filteredItems"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :row-props="rowProps"
        hover
        @click:row="onClickRow"
      >
        <template #[`item.name`]="{ item }">
          <span :class="{ 'text-decoration-line-through': item.archived }">{{ item.name }}</span>
        </template>
      </HREasyTableBase>
    </v-card-text>

    <v-dialog v-model="dialog" persistent max-width="720">
      <admin-business-account-position-form
        :business-account-id="businessAccountId"
        :input="current"
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
import { extractDataTableRow } from "@/lib/data-table";
import { errorUtils } from "@/lib/errors";
import {
  listBusinessAccountPositions,
  type BusinessAccountPosition,
} from "@/services/admin/admin-business-account.service";
import AdminBusinessAccountPositionForm from "@/views/admin/business-accounts/components/AdminBusinessAccountPositionForm.vue";

const props = defineProps<{
  businessAccountId: number;
}>();

const { t } = useI18n();

const loading = ref(false);
const dialog = ref(false);
const error = ref("");
const search = ref("");
const showArchived = ref(false);
const items = ref<BusinessAccountPosition[]>([]);
const current = ref<BusinessAccountPosition | null>(null);

const headers = computed(() => [
  { title: t("Наименование"), key: "name" },
  { title: t("Описание"), key: "description" },
  { title: t("Ставка (с НДС)"), key: "rate", width: "160px" },
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
    items.value = await listBusinessAccountPositions(props.businessAccountId);
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  current.value = null;
  dialog.value = true;
}

function onClickRow(_event: Event, row: unknown): void {
  const item = extractDataTableRow<BusinessAccountPosition>(row);
  if (item) {
    current.value = item;
    dialog.value = true;
  }
}

function onSaved(): void {
  dialog.value = false;
  void load();
}

void load();
</script>
