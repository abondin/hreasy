<!--
  Generic card for displaying downloadable attachments with built-in upload and delete flows.
-->
<template>
  <!--<editor-fold desc="File attachments inline">-->
  <div class="d-flex flex-column ga-4">
    <div class="d-flex flex-wrap align-center ga-3">
      <div class="flex-grow-1 min-w-0">
        <template v-if="!loading && !permissionDenied && !loadError">
          <div v-if="displayFiles.length" class="d-flex flex-wrap ga-2 align-center">
            <v-tooltip
                v-for="file in displayFiles"
                :key="file.key"
                :text="file.tooltip"
                location="bottom"
            >
              <template #activator="{ props: tooltipProps }">
                <v-chip v-bind="tooltipProps" variant="outlined" class="px-3">
                  <span class="text-truncate" style="max-width: 200px">
                    <template v-if="isDownloadAllowed(file.source)">
                      <a
                          :href="file.downloadUrl"
                          class="text-truncate text-decoration-none"
                          download>
                        {{ file.filename }}
                      </a>
                    </template>
                    <template v-else>
                      {{ file.filename }}
                    </template>
                  </span>
                  <template v-if="canDelete" #append>
                    <v-btn
                        icon="mdi-close"
                        size="x-small"
                        variant="text"
                        density="comfortable"
                        @click.stop.prevent="requestDelete(file.source)"
                    />
                  </template>
                </v-chip>
              </template>
            </v-tooltip>
          </div>
          <div v-else class="text-medium-emphasis">
            {{ emptyMessageComputed }}
          </div>
        </template>
        <v-progress-circular
            v-else-if="loading"
            size="20"
            width="2"
            color="primary"
        />
      </div>

      <v-btn
          v-if="canUpload"
          color="primary"
          variant="tonal"
          icon="mdi-plus"
          :aria-label="uploadButtonLabel"
          :title="uploadButtonLabel"
          :disabled="deleteLoading || loading || permissionDenied || Boolean(loadError)"
          @click="openUploadDialog"
      />
    </div>

    <v-alert
        v-if="permissionDenied && !loading"
        type="warning"
        variant="tonal"
        border="start"
    >
      <v-icon icon="mdi-lock-alert" class="mr-2"/>
      {{ permissionMessageComputed }}
    </v-alert>

    <v-alert
        v-else-if="loadError && !loading"
        type="error"
        variant="tonal"
        border="start"
    >
      {{ loadError }}
    </v-alert>

    <v-progress-linear
        v-if="loading"
        color="primary"
        indeterminate
    />
  </div>
  <!-- </editor-fold> -->

  <v-dialog v-model="uploadDialog" max-width="620">
    <v-card>
      <v-card-title>{{ uploadTitleComputed }}</v-card-title>
      <v-card-text>
        <file-upload-zone
            v-if="uploadDialog && uploadUrl"
            :post-action="uploadUrl"
            :file-id="`attachments-${dialogUid}`"
            :accept="accept"
            :timeout="uploadTimeout"
            @close="handleUploadClose"
        />
      </v-card-text>
    </v-card>
  </v-dialog>

  <v-dialog v-model="deleteDialog" max-width="520">
    <v-card>
      <v-card-title>{{ t("Удаление") }} {{ deletingFilename }}</v-card-title>
      <v-card-text>
        {{ t("Вы уверены, что хотите удалить запись?") }}
        <v-alert
            v-if="deleteError"
            type="error"
            variant="tonal"
            class="mt-4"
        >
          {{ deleteError }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-btn variant="text" :disabled="deleteLoading" @click="closeDeleteDialog">
          {{ t("Нет") }}
        </v-btn>
        <v-spacer/>
        <v-btn
            color="primary"
            :loading="deleteLoading"
            :disabled="deleteLoading"
            @click="confirmDelete"
        >
          {{ t("Да") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import {computed, onMounted, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import http from "@/lib/http";
import {errorUtils} from "@/lib/errors";
import FileUploadZone, {type UploadCompleteEvent,} from "@/components/FileUploadZone.vue";

export type AttachmentItem = {
  filename: string;
  [key: string]: unknown;
};

const props = withDefaults(
    defineProps<{
      listUrl?: string | null;
      uploadUrl?: string | null;
      buildDownloadUrl: (item: AttachmentItem) => string;
      buildDeleteUrl?: (item: AttachmentItem) => string | null;
      canView?: () => boolean;
      canDownload?: (item: AttachmentItem) => boolean;
      canUpload?: () => boolean;
      readOnly?: boolean;
      emptyMessage?: string;
      permissionMessage?: string;
      uploadTitle?: string;
      accept?: string;
      uploadTimeout?: number;
    }>(),
    {
      listUrl: null,
      uploadUrl: null,
      buildDeleteUrl: undefined,
      canView: () => true,
      canDownload: () => true,
      readOnly: false,
      emptyMessage: "",
      permissionMessage: "",
      uploadTitle: "",
      accept: "",
      uploadTimeout: 30_000,
    },
);

const emit = defineEmits<{
  (event: "updated"): void;
}>();

const {t} = useI18n();

const attachments = ref<AttachmentItem[]>([]);
const loading = ref(false);
const loadError = ref("");
const permissionDenied = ref(false);

const uploadDialog = ref(false);
const deleteDialog = ref(false);
const deleteLoading = ref(false);
const deleteError = ref("");
const attachmentToDelete = ref<AttachmentItem | null>(null);
const dialogUid = Math.random().toString(36).slice(2);

const canUpload = computed(() => {
  if (!props.uploadUrl || props.readOnly) {
    return false;
  }
  if (props.canUpload) {
    return Boolean(props.canUpload());
  }
  return props.canView?.() ?? true;
});
const canDelete = computed(() => {
  if (!props.buildDeleteUrl || props.readOnly) {
    return false;
  }
  if (props.canUpload) {
    return Boolean(props.canUpload());
  }
  return props.canView?.() ?? true;
});

const uploadTitleComputed = computed(
    () => props.uploadTitle || t("Загрузить файл"),
);

const uploadButtonLabel = computed(() => uploadTitleComputed.value);

const permissionMessageComputed = computed(
    () => props.permissionMessage || t("Не достаточно прав"),
);

const emptyMessageComputed = computed(
    () => props.emptyMessage || t("Нет доступных файлов"),
);

const displayFiles = computed(() =>
    attachments.value.map((item: AttachmentItem) => ({
      key: `${item.filename}-${(item as { id?: string | number }).id ?? ""}`,
      filename: item.filename,
      downloadUrl: props.buildDownloadUrl(item),
      tooltip: `${t("Скачать документ")} '${item.filename}'`,
      source: item,
    })),
);

const deletingFilename = computed(() => attachmentToDelete.value?.filename ?? "");

function isDownloadAllowed(item: AttachmentItem): boolean {
  return Boolean(props.canDownload?.(item));
}

function openUploadDialog() {
  uploadDialog.value = true;
}

function handleUploadClose(event: UploadCompleteEvent) {
  uploadDialog.value = false;
  if (event.uploaded) {
    reloadAttachments();
    emit("updated");
  }
}

function requestDelete(item: AttachmentItem) {
  if (!canDelete.value) {
    return;
  }
  attachmentToDelete.value = item;
  deleteDialog.value = true;
  deleteError.value = "";
}

function closeDeleteDialog() {
  if (deleteLoading.value) {
    return;
  }
  deleteDialog.value = false;
  attachmentToDelete.value = null;
  deleteError.value = "";
}

async function confirmDelete() {
  if (!attachmentToDelete.value || !props.buildDeleteUrl) {
    return;
  }

  const deleteUrl = props.buildDeleteUrl(attachmentToDelete.value);
  if (!deleteUrl) {
    return;
  }

  deleteLoading.value = true;
  deleteError.value = "";

  try {
    await http.delete(deleteUrl);
    deleteDialog.value = false;
    attachmentToDelete.value = null;
    emit("updated");
    await reloadAttachments();
  } catch (error) {
    deleteError.value = errorUtils.shortMessage(error);
  } finally {
    deleteLoading.value = false;
  }
}

async function reloadAttachments() {
  if (!props.listUrl) {
    attachments.value = [];
    return;
  }
  const canView = props.canView?.() ?? true;
  if (!canView) {
    permissionDenied.value = true;
    attachments.value = [];
    return;
  }

  permissionDenied.value = false;
  loading.value = true;
  loadError.value = "";

  try {
    const response = await http.get<AttachmentItem[]>(props.listUrl);
    attachments.value = response.data;
  } catch (error) {
    loadError.value = errorUtils.shortMessage(error);
    attachments.value = [];
  } finally {
    loading.value = false;
  }
}

watch(
    () => props.listUrl,
    () => {
      reloadAttachments().catch(() => undefined);
    },
);

onMounted(() => {
  reloadAttachments().catch(() => undefined);
});

defineExpose({
  reload: reloadAttachments,
});
</script>
