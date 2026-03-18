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
        :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430_\u0434\u0430\u043D\u043D\u044B\u0445')"
        :no-data-text="t('\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435')"
        :hover="editable"
        :sort-by="[{ key: 'name', order: 'asc' }]"
        :row-props="rowProps"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex align-center ga-2 flex-wrap">
            <span>{{ title }}</span>
            <v-spacer />
            <v-btn
              data-testid="toolbar-refresh"
              icon="mdi-refresh"
              variant="text"
              :disabled="loading"
              @click="load"
            />
            <v-btn
              v-if="editable"
              data-testid="toolbar-add"
              icon="mdi-plus"
              color="primary"
              variant="text"
              :disabled="loading"
              @click="openCreate"
            />
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row dense>
              <v-col cols="12" md="6" lg="4">
                <v-text-field
                  v-model="search"
                  :label="t('\u041F\u043E\u0438\u0441\u043A')"
                  append-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3" class="d-flex align-center">
                <v-checkbox
                  v-model="hideArchived"
                  :label="t('\u0421\u043A\u0440\u044B\u0442\u044C \u0430\u0440\u0445\u0438\u0432\u043D\u044B\u0435')"
                  density="compact"
                  hide-details
                />
              </v-col>
            </v-row>
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
          {{ item.archived ? t("\u0414\u0430") : t("\u041D\u0435\u0442") }}
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
          {{ dialogMode === "create" ? t("\u0414\u043E\u0431\u0430\u0432\u0438\u0442\u044C") : t("\u0418\u0437\u043C\u0435\u043D\u0438\u0442\u044C") }}: {{ title }}
        </v-card-title>
        <v-card-text>
          <v-form ref="formRef" @submit.prevent="save">
            <v-text-field
              v-model="form.name"
              :label="t('\u041D\u0430\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435')"
              :rules="nameRules"
              :counter="255"
              variant="outlined"
              required
            />

            <slot name="additional-fields" :form="form" />

            <v-select
              v-model="form.archived"
              :label="t('\u0410\u0440\u0445\u0438\u0432')"
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
            {{ t("\u041E\u0442\u043C\u0435\u043D\u0438\u0442\u044C") }}
          </v-btn>
          <v-btn color="primary" :loading="saving" :disabled="saving" @click="save">
            {{ t("\u0421\u043E\u0445\u0440\u0430\u043D\u0438\u0442\u044C") }}
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
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
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
const archivedOptions = computed(() => [
  { title: t("\u041D\u0435\u0442"), value: false },
  { title: t("\u0414\u0430"), value: true },
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
    || t("\u041E\u0431\u044F\u0437\u0430\u0442\u0435\u043B\u044C\u043D\u043E\u0435 \u043F\u043E\u043B\u0435. \u041D\u0435 \u0431\u043E\u043B\u0435\u0435 N \u0441\u0438\u043C\u0432\u043E\u043B\u043E\u0432", { n: 255 }),
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
