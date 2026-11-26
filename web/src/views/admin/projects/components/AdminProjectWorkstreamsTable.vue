<template>
  <v-card class="pa-6" data-testid="admin-project-workstreams">
    <div class="d-flex align-center flex-wrap ga-3 mb-3">
      <div class="text-h6">{{ t("Направления работ") }}</div>
      <v-spacer />
      <SearchTextField v-model="search" v-model:settings="searchSettings" :label="t('Поиск')" max-width="360" />
      <v-btn icon="mdi-refresh" variant="text" :loading="loading" :aria-label="t('Обновить')" @click="load" />
      <v-btn v-if="editable" prepend-icon="mdi-plus" variant="text" data-testid="workstream-add" @click="openEditor()">
        {{ t("Добавить") }}
      </v-btn>
    </div>
    <v-alert v-if="error" type="error" class="mb-3">{{ error }}</v-alert>
    <HREasyTableBase :headers="headers" :items="filteredItems" :height="360" :loading="loading"
      :loading-text="t('Загрузка_данных')" :no-data-text="t('Отсутствуют данные')" density="compact"
      :row-props="editable ? rowProps : undefined" hover @click:row="onClickRow">
      <template #[`item.displayName`]="{ item }">
        <div class="d-flex align-center ga-2 min-width-0">
          <span class="text-truncate" :class="{ 'text-decoration-line-through': Boolean(item.archivedAt) }">{{ item.displayName }}</span>
          <div v-if="editable" class="workstream-row-delete-slot d-inline-flex align-center justify-center flex-shrink-0">
            <v-btn icon="mdi-delete" variant="text" size="x-small" color="error" class="workstream-row-delete"
              :aria-label="t('Удалить')" @click.stop="openDelete(item)" />
          </div>
        </div>
      </template>
      <template #[`item.archivedAt`]="{ item }">
        {{ formatDate(item.archivedAt) }}
      </template>
    </HREasyTableBase>

    <v-dialog v-model="dialog" ref="editor" persistent max-width="720" @after-enter="focusDialog(editor)">
      <v-card>
        <v-card-title>{{ current ? t("Изменить направление работ") : t("Добавить направление работ") }}</v-card-title>
        <v-card-text>
          <v-form ref="formRef" :disabled="saving" @submit.prevent="save">
            <v-text-field v-model="form.displayName" :label="t('Наименование')" :counter="255"
              :rules="[nameRule]" autofocus data-testid="workstream-name" />
            <v-text-field v-model="form.externalId" :label="t('Внешний идентификатор')" :counter="255"
              :rules="[externalIdRule]" />
            <v-textarea v-model="form.description" :label="t('Описание')" rows="3" />
            <v-checkbox v-model="form.archived" :label="t('Архив')" hide-details />
          </v-form>
          <v-alert v-if="saveError" type="error" class="mt-3">{{ saveError }}</v-alert>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn :disabled="saving" @click="dialog = false">{{ t("Отменить") }}</v-btn>
          <v-btn color="primary" :loading="saving" :disabled="saving" data-testid="workstream-save" @click="save">
            {{ t("Сохранить") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <ConfirmDeleteDialog :open="deleteTarget != null" :title="t('Удалить')"
      :message="t('Удалить направление работ?')" :item-label="deleteTarget?.displayName"
      :confirm-label="t('Удалить')" :cancel-label="t('Отменить')" :loading="saving" :error-message="saveError"
      @close="deleteTarget = null" @confirm="remove" />
  </v-card>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import { focusDialog } from "@/lib/dialog-focus";
import { extractDataTableRow } from "@/lib/data-table";
import { formatDate } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { createSearchSettings, matchesSearch } from "@/lib/search";
import type { ProjectWorkstream } from "@/services/projects.service";
import {
  listProjectWorkstreams, createProjectWorkstream, updateProjectWorkstream, deleteProjectWorkstream,
} from "@/services/admin/admin-project.service";

const props = defineProps<{ projectId: number; editable: boolean }>();
const { t } = useI18n();
const items = ref<ProjectWorkstream[]>([]);
const search = ref("");
const searchSettings = ref(createSearchSettings());
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const saveError = ref("");
const dialog = ref(false);
const editor = ref();
const formRef = ref<InstanceType<typeof VForm>>();
const current = ref<ProjectWorkstream | null>(null);
const deleteTarget = ref<ProjectWorkstream | null>(null);
const form = reactive({ displayName: "", externalId: "", description: "", archived: false });
const headers = computed(() => [
  { title: t("Наименование"), key: "displayName" },
  { title: t("Внешний идентификатор"), key: "externalId" },
  { title: t("Описание"), key: "description" },
  { title: t("Архив"), key: "archivedAt" },
]);
const filteredItems = computed(() => {
  return items.value.filter(item => matchesSearch(search.value,
    [item.displayName, item.externalId, item.description], searchSettings.value));
});
const nameRule = (value: string) => Boolean(value?.trim() && value.length <= 255)
  || t("Обязательное поле. Не более N символов", { n: 255 });
const externalIdRule = (value: string) => !value || value.length <= 255 || t("Не более N символов", { n: 255 });

function rowProps() {
  return { class: "cursor-pointer" };
}

function onClickRow(_event: Event, row: unknown) {
  if (!props.editable) return;
  const item = extractDataTableRow<ProjectWorkstream>(row);
  if (item) void openEditor(item);
}

async function load() {
  loading.value = true;
  error.value = "";
  try {
    items.value = await listProjectWorkstreams(props.projectId);
  } catch (err) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}

async function openEditor(item?: ProjectWorkstream) {
  current.value = item ?? null;
  Object.assign(form, {
    displayName: item?.displayName ?? "", externalId: item?.externalId ?? "",
    description: item?.description ?? "", archived: Boolean(item?.archivedAt),
  });
  saveError.value = "";
  dialog.value = true;
  await nextTick();
  formRef.value?.resetValidation();
}

async function save() {
  if (saving.value || !(await formRef.value?.validate())?.valid) return;
  saving.value = true;
  saveError.value = "";
  try {
    if (current.value?.id != null) {
      await updateProjectWorkstream(props.projectId, current.value.id, { ...form });
    } else {
      await createProjectWorkstream(props.projectId, { ...form });
    }
    dialog.value = false;
    await load();
  } catch (err) {
    saveError.value = errorUtils.shortMessage(err);
  } finally {
    saving.value = false;
  }
}

function openDelete(item: ProjectWorkstream) {
  deleteTarget.value = item;
  saveError.value = "";
}

async function remove() {
  if (saving.value || deleteTarget.value?.id == null) return;
  saving.value = true;
  saveError.value = "";
  try {
    await deleteProjectWorkstream(props.projectId, deleteTarget.value.id);
    deleteTarget.value = null;
    await load();
  } catch (err) {
    saveError.value = errorUtils.shortMessage(err);
  } finally {
    saving.value = false;
  }
}

watch(() => props.projectId, load, { immediate: true });
</script>

<style scoped>
.workstream-row-delete-slot {
  width: 24px;
}

:deep(tbody tr .workstream-row-delete) {
  opacity: 0;
  pointer-events: none;
  transition: opacity 120ms ease-in-out;
}

:deep(tbody tr:hover .workstream-row-delete),
:deep(tbody tr:focus-within .workstream-row-delete) {
  opacity: 1;
  pointer-events: auto;
}
</style>
