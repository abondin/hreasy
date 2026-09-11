<template>
  <v-menu
    v-model="menuOpen"
    :target="target ?? undefined"
    location="end top"
    :close-on-content-click="false"
    :transition="false"
    max-width="calc(100vw - 24px)"
    offset="8"
  >
    <v-card
      v-if="cell"
      class="resource-allocation-comments"
      elevation="8"
      rounded="lg"
      :ripple="false"
      data-testid="resource-allocation-comments-popover"
    >
      <v-card-title class="resource-allocation-comments__title pb-1">
        {{ formatPeriod(cell.period) }} · {{ cell.employeeName }}
      </v-card-title>
      <v-card-subtitle class="resource-allocation-comments__subtitle pb-3">
        {{ cell.projectName }} · {{ cell.workstreamName ?? t("Без направления") }}
      </v-card-subtitle>
      <v-divider />

      <div v-if="loading" class="resource-allocation-comments__state">
        <v-progress-circular indeterminate size="24" color="primary" />
      </div>
      <div v-else class="resource-allocation-comments__thread">
        <v-alert v-if="error" type="error" variant="tonal" density="compact" class="ma-3 mb-0">
          {{ error }}
        </v-alert>
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="resource-allocation-comments__item"
          :data-testid="`resource-allocation-comment-${comment.id}`"
        >
          <div class="resource-allocation-comments__meta">
            <span class="resource-allocation-comments__author" :title="comment.author.displayName">
              {{ comment.author.displayName }}
            </span>
            <span class="resource-allocation-comments__date text-medium-emphasis">
              {{ formatDateTime(comment.createdAt) }}
              <template v-if="comment.updatedAt"> · {{ t("изменено") }}</template>
            </span>
            <span v-if="comment.mine && editingId !== comment.id" class="resource-allocation-comments__actions">
              <v-btn
                icon
                variant="text"
                size="20"
                density="compact"
                :ripple="false"
                :aria-label="t('Редактировать комментарий')"
                :data-testid="`resource-allocation-comment-edit-${comment.id}`"
                @click="startEdit(comment)"
              >
                <v-icon icon="mdi-pencil-outline" size="14" />
              </v-btn>
              <v-btn
                icon
                variant="text"
                size="20"
                density="compact"
                :ripple="false"
                color="error"
                :aria-label="t('Удалить комментарий')"
                :data-testid="`resource-allocation-comment-delete-${comment.id}`"
                @click="deleteId = comment.id"
              >
                <v-icon icon="mdi-delete-outline" size="14" />
              </v-btn>
            </span>
          </div>

          <template v-if="editingId === comment.id">
            <v-textarea
              v-model="editText"
              rows="3"
              no-resize
              counter
              maxlength="4000"
              hide-details="auto"
              density="compact"
              variant="outlined"
              class="mt-2"
              :disabled="saving"
            />
            <div class="d-flex justify-end ga-2 mt-2">
              <v-btn
                size="small"
                color="primary"
                :loading="saving"
                :disabled="!editText.trim()"
                @click="saveEdit(comment.id)"
              >
                {{ t("Сохранить") }}
              </v-btn>
              <v-btn size="small" variant="text" :disabled="saving" @click="cancelEdit">
                {{ t("Отмена") }}
              </v-btn>
            </div>
          </template>
          <p v-else class="resource-allocation-comments__text text-body-2 mb-0">
            {{ comment.text }}
          </p>

          <div v-if="deleteId === comment.id" class="d-flex align-center justify-end ga-2 mt-2">
            <span class="text-caption text-error">{{ t("Удалить комментарий?") }}</span>
            <v-btn size="small" variant="text" :disabled="saving" @click="deleteId = null">
              {{ t("Нет") }}
            </v-btn>
            <v-btn size="small" color="error" :loading="saving" @click="remove(comment.id)">
              {{ t("Удалить") }}
            </v-btn>
          </div>
        </div>

        <div v-if="comments.length === 0" class="resource-allocation-comments__state text-medium-emphasis">
          {{ t("Комментариев пока нет") }}
        </div>
      </div>

      <v-divider />
      <v-card-text class="pt-3 pb-0">
        <v-textarea
          ref="newCommentInput"
          v-model="newText"
          :label="t('Новый комментарий')"
          rows="3"
          no-resize
          counter
          maxlength="4000"
          hide-details
          density="compact"
          variant="outlined"
          autofocus
          :disabled="saving"
          data-testid="resource-allocation-comment-input"
          @keydown.ctrl.enter.prevent="add"
        />
        <div
          class="resource-allocation-comments__counter text-medium-emphasis"
          data-testid="resource-allocation-comment-counter"
        >
          {{ newText.length }} / 4000
        </div>
      </v-card-text>
      <v-card-actions class="pt-1 pb-3 px-4">
        <v-spacer />
        <v-btn
          color="primary"
          size="small"
          :loading="saving"
          :disabled="loading || !newText.trim()"
          data-testid="resource-allocation-comment-submit"
          @click="add"
        >
          {{ t("Добавить") }}
        </v-btn>
        <v-btn
          variant="text"
          size="small"
          :disabled="saving"
          data-testid="resource-allocation-comments-close"
          @click="emit('update:open', false)"
        >
          {{ t("Закрыть") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { ReportPeriod } from "@/services/overtime.service";
import {
  createResourceAllocationComment,
  deleteResourceAllocationComment,
  fetchResourceAllocationComments,
  updateResourceAllocationComment,
  type ResourceAllocationComment,
  type ResourceAllocationCommentCellKey,
} from "@/services/resource-allocation.service";

export interface ResourceAllocationCommentCellContext extends ResourceAllocationCommentCellKey {
  employeeName: string;
  projectName: string;
  workstreamName: string | null;
}

const props = defineProps<{
  open: boolean;
  target: HTMLElement | null;
  cell: ResourceAllocationCommentCellContext | null;
}>();
const emit = defineEmits<{
  (event: "update:open", value: boolean): void;
  (event: "count", cell: ResourceAllocationCommentCellKey, count: number): void;
}>();
const { t } = useI18n();
const comments = ref<ResourceAllocationComment[]>([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const newText = ref("");
const editingId = ref<number | null>(null);
const editText = ref("");
const deleteId = ref<number | null>(null);
const newCommentInput = ref<{ focus: () => void } | null>(null);
let loadId = 0;

const menuOpen = computed({
  get: () => props.open,
  set: (value: boolean) => emit("update:open", value),
});

watch(
  () => ({ open: props.open, key: cellKey(props.cell) }),
  ({ open, key }, previous) => {
    if (open && props.cell) {
      if (!previous?.open || previous.key !== key) resetDrafts();
      void load(props.cell);
      void nextTick(() => newCommentInput.value?.focus());
    } else {
      resetDrafts();
    }
  },
  { immediate: true },
);

async function load(cell: ResourceAllocationCommentCellContext): Promise<void> {
  const requestId = ++loadId;
  loading.value = true;
  error.value = "";
  comments.value = [];
  try {
    const loaded = await fetchResourceAllocationComments(cell);
    if (requestId === loadId) comments.value = loaded;
  } catch (loadError) {
    if (requestId === loadId) error.value = errorUtils.shortMessage(loadError);
  } finally {
    if (requestId === loadId) loading.value = false;
  }
}

async function add(): Promise<void> {
  if (!props.cell || !newText.value.trim() || saving.value) return;
  const cell = props.cell;
  const requestId = loadId;
  let refocus = false;
  saving.value = true;
  error.value = "";
  try {
    const created = await createResourceAllocationComment(cell, newText.value);
    if (!isCurrent(cell, requestId)) return;
    comments.value.push(created);
    newText.value = "";
    emit("count", cell, comments.value.length);
    refocus = true;
  } catch (saveError) {
    if (isCurrent(cell, requestId)) error.value = errorUtils.shortMessage(saveError);
  } finally {
    saving.value = false;
  }
  if (refocus) {
    await nextTick();
    newCommentInput.value?.focus();
  }
}

function startEdit(comment: ResourceAllocationComment): void {
  editingId.value = comment.id;
  editText.value = comment.text;
  deleteId.value = null;
}

function cancelEdit(): void {
  editingId.value = null;
  editText.value = "";
}

async function saveEdit(commentId: number): Promise<void> {
  if (!props.cell || !editText.value.trim() || saving.value) return;
  const cell = props.cell;
  const requestId = loadId;
  saving.value = true;
  error.value = "";
  try {
    const updated = await updateResourceAllocationComment(commentId, editText.value);
    if (!isCurrent(cell, requestId)) return;
    comments.value = comments.value.map(comment => comment.id === commentId ? updated : comment);
    cancelEdit();
  } catch (saveError) {
    if (isCurrent(cell, requestId)) error.value = errorUtils.shortMessage(saveError);
  } finally {
    saving.value = false;
  }
}

async function remove(commentId: number): Promise<void> {
  if (!props.cell || saving.value) return;
  const cell = props.cell;
  const requestId = loadId;
  saving.value = true;
  error.value = "";
  try {
    await deleteResourceAllocationComment(commentId);
    if (!isCurrent(cell, requestId)) return;
    comments.value = comments.value.filter(comment => comment.id !== commentId);
    deleteId.value = null;
    emit("count", cell, comments.value.length);
  } catch (deleteError) {
    if (isCurrent(cell, requestId)) error.value = errorUtils.shortMessage(deleteError);
  } finally {
    saving.value = false;
  }
}

function resetDrafts(): void {
  loadId += 1;
  error.value = "";
  newText.value = "";
  cancelEdit();
  deleteId.value = null;
}

function cellKey(cell: ResourceAllocationCommentCellContext | null): string {
  return cell == null
    ? ""
    : `${cell.period}:${cell.employeeId}:${cell.projectId}:${cell.workstreamId ?? "project"}`;
}

function isCurrent(cell: ResourceAllocationCommentCellContext, requestId: number): boolean {
  return requestId === loadId && cellKey(props.cell) === cellKey(cell);
}

function formatPeriod(period: number): string {
  return ReportPeriod.fromPeriodId(period).toString();
}
</script>

<style scoped>
.resource-allocation-comments {
  display: flex;
  max-height: calc(100vh - 24px);
  flex-direction: column;
  overflow: hidden;
  width: 440px;
}

.resource-allocation-comments__title,
.resource-allocation-comments__subtitle {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-allocation-comments__title {
  font-size: 14px;
  line-height: 20px;
}

.resource-allocation-comments__subtitle {
  font-size: 11px;
  line-height: 16px;
}

.resource-allocation-comments__thread {
  min-height: 0;
  max-height: min(420px, 50vh);
  flex: 1 1 auto;
  overflow-y: auto;
}

.resource-allocation-comments__item {
  padding: 10px 16px;
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

.resource-allocation-comments__meta {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 6px;
  min-height: 24px;
}

.resource-allocation-comments__author {
  overflow: hidden;
  font-size: 12px;
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-allocation-comments__date {
  font-size: 10px;
  white-space: nowrap;
}

.resource-allocation-comments__counter {
  height: 20px;
  padding-top: 3px;
  font-size: 10px;
  line-height: 16px;
  text-align: right;
}

.resource-allocation-comments__actions {
  display: flex;
}

.resource-allocation-comments__state {
  display: flex;
  min-height: 88px;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.resource-allocation-comments__text {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
</style>
