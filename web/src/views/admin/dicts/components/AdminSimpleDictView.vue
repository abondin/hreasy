<!--
  Reusable CRUD view for simple admin dictionaries with the same table, search,
  archive filter, and create/update dialog behavior.
-->
<template>
  <div class="mt-4" :data-testid="testId">
    <v-card>
      <HREasyTableBase
        table-class="admin-simple-dict-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :hover="editable"
        :sort-by="[{ key: 'name', order: 'asc' }]"
        :row-props="rowProps"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-text class="pt-4 pb-2">
            <AdaptiveFilterBar
              :items="filterBarItems"
              :has-right-actions="editable"
            >
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

              <template #filter-hide-archived>
                <v-checkbox
                  v-model="hideArchived"
                  :label="t('Скрыть архивные')"
                  density="compact"
                  hide-details
                />
              </template>

              <template #right-actions>
                <table-toolbar-actions
                  v-if="editable"
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
          <v-alert
            v-if="error"
            type="error"
            variant="tonal"
            border="start"
            class="ma-4 mb-0"
          >
            {{ error }}
          </v-alert>
        </template>

        <template #[`item.archived`]="{ item }">
          {{ item.archived ? t("Да") : t("Нет") }}
        </template>

        <template
          v-for="slotName in forwardedSlots"
          :key="slotName"
          #[slotName]="slotProps"
        >
          <slot :name="slotName" v-bind="slotProps" />
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="dialog" persistent max-width="640">
      <v-card>
        <v-card-title>
          {{ dialogMode === "create" ? t("Добавить") : t("Изменить") }}: {{ title }}
        </v-card-title>
        <v-card-text>
          <v-form ref="formRef" @submit.prevent="save">
            <v-text-field
              v-model="form.name"
              :label="t('Наименование')"
              :rules="nameRules"
              :counter="255"
              variant="outlined"
              required
            />

            <slot name="additional-fields" :form="form" />

            <v-select
              v-model="form.archived"
              :label="t('Архив')"
              :items="archivedOptions"
              item-title="title"
              item-value="value"
              variant="outlined"
            />
          </v-form>

          <v-alert
            v-if="saveError"
            type="error"
            variant="tonal"
            border="start"
            class="mt-3"
          >
            {{ saveError }}
          </v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="saving" @click="closeDialog">
            {{ t("Отменить") }}
          </v-btn>
          <v-btn color="primary" :loading="saving" :disabled="saving" @click="save">
            {{ t("Сохранить") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts" generic="TItem extends { id: number; name: string; archived: boolean }, TForm extends { name: string; archived: boolean }">
import { computed, ref, useSlots } from "vue";
import { useI18n } from "vue-i18n";
import type { VDataTable, VForm } from "vuetify/components";
import AdaptiveFilterBar from "@/components/shared/AdaptiveFilterBar.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import { errorUtils } from "@/lib/errors";
import { extractDataTableRow } from "@/lib/data-table";

type DataTableHeader = VDataTable["$props"]["headers"];

const props = withDefaults(defineProps<{
  title: string;
  headers: DataTableHeader;
  loadItems: () => Promise<TItem[]>;
  createItem: (payload: TForm) => Promise<unknown>;
  updateItem: (id: number, payload: TForm) => Promise<unknown>;
  createForm: () => TForm;
  itemToForm: (item: TItem) => TForm;
  testId: string;
  editable?: boolean;
}>(), {
  editable: true,
});

const { t } = useI18n();
const slots = useSlots();

const loading = ref(false);
const saving = ref(false);
const error = ref("");
const saveError = ref("");
const items = ref<TItem[]>([]);
const search = ref("");
const hideArchived = ref(true);
const dialog = ref(false);
const dialogMode = ref<"create" | "edit">("create");
const editingId = ref<number | null>(null);
const form = ref<TForm>(props.createForm());
const formRef = ref<VForm | null>(null);

const editable = computed(() => props.editable !== false);
const filterBarItems = computed(() => [
  { id: "search", minWidth: 380, active: search.value.trim().length > 0, grow: true },
  { id: "hide-archived", minWidth: 220, active: hideArchived.value !== true },
]);
const archivedOptions = computed(() => [
  { title: t("Нет"), value: false },
  { title: t("Да"), value: true },
]);
const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase();
  return items.value.filter((item) => {
    if (hideArchived.value && item.archived) {
      return false;
    }
    if (!query) {
      return true;
    }
    return item.name.toLowerCase().includes(query);
  });
});
const forwardedSlots = computed(() =>
  Object.keys(slots).filter((name) => name.startsWith("item.")),
);
const nameRules = computed(() => [
  (value: string) =>
    Boolean(value && value.length <= 255)
    || t("Обязательное поле. Не более N символов", { n: 255 }),
]);

function rowProps() {
  return editable.value ? { class: "cursor-pointer" } : undefined;
}

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    items.value = await props.loadItems();
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function openCreate(): void {
  dialogMode.value = "create";
  editingId.value = null;
  saveError.value = "";
  form.value = props.createForm();
  dialog.value = true;
}

function openEdit(item: TItem): void {
  if (!editable.value) {
    return;
  }
  dialogMode.value = "edit";
  editingId.value = item.id;
  saveError.value = "";
  form.value = props.itemToForm(item);
  dialog.value = true;
}

function closeDialog(): void {
  dialog.value = false;
  saveError.value = "";
}

async function save(): Promise<void> {
  const validation = await formRef.value?.validate();
  if (validation && !validation.valid) {
    return;
  }

  saving.value = true;
  saveError.value = "";
  try {
    if (dialogMode.value === "create") {
      await props.createItem(form.value);
    } else if (editingId.value != null) {
      await props.updateItem(editingId.value, form.value);
    }
    dialog.value = false;
    await load();
  } catch (saveRequestError) {
    saveError.value = errorUtils.shortMessage(saveRequestError);
  } finally {
    saving.value = false;
  }
}

function onClickRow(_event: Event, row: unknown): void {
  if (!editable.value) {
    return;
  }
  const item = extractDataTableRow<TItem>(row);
  if (item) {
    openEdit(item);
  }
}

void load();
</script>
