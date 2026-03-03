<!--
  Drag-and-drop file upload zone.
  Uploader with inline progress, success, and error states.
-->
<template>
  <!--<editor-fold desc="File upload zone">-->
  <div
    class="file-upload-zone"
    :class="{
      'file-upload-zone--drop-active': dropActive,
      'file-upload-zone--disabled': uploading,
    }"
    @dragenter.prevent="handleDragEnter"
    @dragleave.prevent="handleDragLeave"
    @dragover.prevent
    @drop.prevent="handleDrop"
  >
    <p
      v-if="currentFile"
      class="text-center text-medium-emphasis mb-5"
    >
      {{ currentFile.name }}
    </p>
    <p
      v-else-if="!uploadSuccess"
      class="text-center text-medium-emphasis mb-5"
    >
      {{ t("Выберите файл на вашем компьютере или перетащите мышкой") }}
    </p>

    <input
      ref="fileInput"
      type="file"
      :id="`file-${fileId}`"
      :accept="accept"
      class="file-upload-zone__input"
      @change="handleFileInput"
    />

    <v-btn
      v-if="!currentFile && !uploadSuccess"
      color="primary"
      variant="outlined"
      :disabled="uploading"
      @click="triggerFileDialog"
    >
      {{ t("Выбрать") }}
    </v-btn>

    <v-alert
      v-if="uploading"
      type="info"
      variant="tonal"
      class="mt-4"
    >
      {{ uploadProgress }}%
    </v-alert>

    <v-alert
      v-if="uploadSuccess"
      type="success"
      variant="tonal"
      class="mt-4"
    >
      {{ t("Файл успешно загружен с именем", { filename: lastUploadedFileName }) }}
    </v-alert>

    <v-alert
      v-if="uploadErrorMessage"
      type="error"
      variant="tonal"
      class="mt-4"
    >
      {{ uploadErrorMessage }}
    </v-alert>

    <div class="mt-4">
      <v-btn
        variant="text"
        :disabled="uploading"
        @click="closeUploader"
      >
        {{ uploadSuccess ? t("ОK") : t("Отмена") }}
      </v-btn>
    </div>

    <div v-if="dropActive" class="file-upload-zone__overlay">
      <h2>{{ t("Перетащите файл в данную область") }}</h2>
    </div>
  </div>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import axios, {
  isAxiosError,
  type AxiosError,
  type AxiosProgressEvent,
} from "axios";
import http from "@/lib/http";

export interface UploadCompleteEvent {
  uploaded: boolean;
}

type UploadErrorCode =
  | "size"
  | "extension"
  | "timeout"
  | "abort"
  | "network"
  | "server"
  | "denied";

const props = withDefaults(
  defineProps<{
    postAction: string;
    fileId: string;
    maximumSize?: number;
    timeout?: number;
    accept?: string;
  }>(),
  {
    maximumSize: 10 * 1024 * 1024,
    timeout: 30_000,
    accept: "",
  },
);

const emit = defineEmits<{
  (event: "close", payload: UploadCompleteEvent): void;
}>();

const { t } = useI18n();

const fileInput = ref<HTMLInputElement | null>(null);
const currentFile = ref<File | null>(null);
const uploadProgress = ref(0);
const uploading = ref(false);
const uploadErrorCode = ref<UploadErrorCode | null>(null);
const uploadSuccess = ref(false);
const dropCounter = ref(0);
const abortController = ref<AbortController | null>(null);
const lastUploadedFileName = ref("");

const dropActive = computed(() => dropCounter.value > 0);

const uploadErrorMessage = computed(() => {
  if (!uploadErrorCode.value) {
    return "";
  }
  return t(`UPLOAD_ERROR_${uploadErrorCode.value}`, {
    timeout: props.timeout,
    maximumSize: props.maximumSize,
  });
});

function triggerFileDialog() {
  fileInput.value?.click();
}

function handleFileInput(event: Event) {
  const input = event.target as HTMLInputElement | null;
  if (!input?.files || input.files.length === 0) {
    return;
  }
  const file = input.files.item(0);
  if (file) {
    processFile(file);
  }
  input.value = "";
}

function handleDragEnter() {
  dropCounter.value += 1;
}

function handleDragLeave() {
  dropCounter.value = Math.max(0, dropCounter.value - 1);
}

function handleDrop(event: DragEvent) {
  dropCounter.value = 0;
  const file = event.dataTransfer?.files?.[0];
  if (file) {
    processFile(file);
  }
}

function processFile(file: File) {
  resetState();
  if (!validateSize(file)) {
    uploadErrorCode.value = "size";
    return;
  }
  if (!validateExtension(file)) {
    uploadErrorCode.value = "extension";
    return;
  }
  currentFile.value = file;
  lastUploadedFileName.value = file.name;
  startUpload(file).catch(() => undefined);
}

function validateSize(file: File): boolean {
  return file.size <= props.maximumSize;
}

function validateExtension(file: File): boolean {
  if (!props.accept) {
    return true;
  }
  const acceptList = props.accept
    .split(",")
    .map((item: string) => item.trim())
    .filter(Boolean);
  if (acceptList.length === 0) {
    return true;
  }

  const fileExtension = `.${file.name.split(".").pop() ?? ""}`.toLowerCase();
  const mimeType = file.type.toLowerCase();

  return acceptList.some((acceptItem: string) => {
    if (acceptItem.startsWith(".")) {
      return fileExtension === acceptItem.toLowerCase();
    }
    if (acceptItem.endsWith("/*")) {
      const prefix = acceptItem.slice(0, -1).toLowerCase();
      return mimeType.startsWith(prefix);
    }
    return mimeType === acceptItem.toLowerCase();
  });
}

async function startUpload(file: File) {
  uploading.value = true;
  uploadProgress.value = 0;
  uploadErrorCode.value = null;
  uploadSuccess.value = false;
  abortController.value?.abort();
  abortController.value = new AbortController();

  const formData = new FormData();
  formData.append("file", file);

  try {
    await http.post(props.postAction, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
      timeout: props.timeout,
      signal: abortController.value.signal,
      onUploadProgress(progressEvent: AxiosProgressEvent) {
        const total = progressEvent.total;
        if (!total) {
          return;
        }
        uploadProgress.value = Math.round(
          (progressEvent.loaded / total) * 100,
        );
      },
    });
    uploadSuccess.value = true;
    currentFile.value = null;
  } catch (error) {
    uploadErrorCode.value = resolveErrorCode(error);
  } finally {
    uploading.value = false;
  }
}

function resolveErrorCode(e: unknown): UploadErrorCode {
  if (isAxiosError(e)) {
    const error = e as AxiosError;
    if (axios.isCancel(error)) {
      return "abort";
    }
    if (error.code === "ECONNABORTED") {
      return "timeout";
    }
    const response = error.response;
    if (!response) {
      return "network";
    }
    if (response.status === 403) {
      return "denied";
    }
    if (response.status >= 500) {
      return "server";
    }
    return "server";
  }
  if (e instanceof DOMException && e.name === "AbortError") {
    return "abort";
  }
  return "server";
}

function closeUploader() {
  const uploaded = uploadSuccess.value;
  emit("close", { uploaded });
  if (!uploading.value) {
    resetState();
  }
}

function resetState() {
  uploadProgress.value = 0;
  uploading.value = false;
  uploadErrorCode.value = null;
  uploadSuccess.value = false;
  currentFile.value = null;
  abortController.value?.abort();
  abortController.value = null;
  lastUploadedFileName.value = "";
}
</script>

<style scoped>
.file-upload-zone {
  min-height: 220px;
  position: relative;
  border: 2px dashed rgba(0, 0, 0, 0.32);
  border-radius: 6px;
  text-align: center;
  padding: 40px 20px;
  background-color: #fff;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.file-upload-zone--drop-active {
  border-color: var(--v-theme-primary, #1976d2);
  background-color: rgba(0, 0, 0, 0.04);
}

.file-upload-zone--disabled {
  opacity: 0.75;
  pointer-events: none;
}

.file-upload-zone__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(0, 0, 0, 0.4);
  color: #fff;
  border-radius: 6px;
  pointer-events: none;
}

.file-upload-zone__input {
  display: none;
}
</style>
