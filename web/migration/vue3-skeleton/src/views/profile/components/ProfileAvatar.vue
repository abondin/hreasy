<!--
  Avatar component for the employee profile.
  Provides preview, full-screen view, and upload with crop/resize pipeline.
  -->
<template>
  <!--<editor-fold desc="Avatar block">-->
  <div class="d-flex flex-column align-center text-center" style="width: 164px">
    <v-overlay
      v-model="previewDialog"
      scroll-strategy="close"
      transition="fade-transition"
      scrim="rgba(0, 0, 0, 0.85)"
      class="d-flex align-center justify-center"
      @click:outside="previewDialog = false"
    >
      <v-card class="position-relative" @click="previewDialog = false">
        <v-img
          v-if="computedAvatarUrl"
          :src="computedAvatarUrl"
          :alt="displayName"
          width="500"
          max-height="500"
          cover
        />
        <v-card-text v-else class="text-medium-emphasis text-center">
          {{ t("Фото недоступно") }}
        </v-card-text>
      </v-card>
    </v-overlay>

    <v-avatar
      class="avatar-uploader__avatar"
      color="grey"
      :size="164"
      @click="openPreview"
    >
      <template v-if="computedAvatarUrl">
        <v-img :src="computedAvatarUrl" :alt="displayName" cover />
      </template>
      <template v-else>
        <v-icon icon="mdi-account-circle" size="120" color="grey-lighten-5" />
      </template>
    </v-avatar>

    <div v-if="canEdit" class="d-flex flex-column align-center mt-3">
      <v-btn
        size="x-small"
        variant="text"
        color="primary"
        :loading="uploading"
        :disabled="!canEdit"
        :min-width="0"
        class="text-none font-weight-medium px-0"
        @click="triggerFileSelect"
      >
        {{ currentAvatarExists ? t("Сменить фото") : t("Загрузить фото") }}
      </v-btn>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleFileChange"
      />
    </div>

    <div v-if="canEdit && errorMessage" class="text-error mt-2">
      {{ errorMessage }}
    </div>
    <div v-else-if="canEdit && successMessage" class="text-success mt-2">
      {{ successMessage }}
    </div>
  </div>
  <!-- </editor-fold> -->

  <!-- Cropper dialog -->
  <v-dialog
    v-model="cropDialog"
    max-width="720"
    transition="dialog-bottom-transition"
  >
    <v-card>
      <v-card-title>{{ t("Настройка фото") }}</v-card-title>
      <v-card-text>
        <div
          v-if="cropper.active"
          class="cropper-container"
          @wheel.prevent="handleWheel"
        >
          <div
            ref="cropArea"
            class="crop-area"
            @pointerdown="startPan"
            @pointermove="continuePan"
            @pointerup="stopPan"
            @pointercancel="stopPan"
            @pointerleave="stopPan"
          >
            <img
              :src="cropper.dataUrl"
              alt=""
              draggable="false"
              class="crop-image"
              :style="imageStyle"
            />
            <div class="crop-overlay" />
          </div>
          <div class="mt-4">
            <div class="text-caption mb-2">
              {{ t("Перемещайте и масштабируйте изображение") }}
            </div>
            <v-slider
              v-model="cropper.zoom"
              :min="1"
              :max="cropper.maxZoomFactor"
              :step="0.01"
              color="primary"
            >
              <template #prepend>
                <v-icon icon="mdi-magnify-minus" />
              </template>
              <template #append>
                <v-icon icon="mdi-magnify-plus" />
              </template>
            </v-slider>
          </div>
        </div>
        <div v-else class="text-medium-emphasis">
          {{ t("Выберите изображение") }}
        </div>
      </v-card-text>
      <v-card-actions>
        <v-btn variant="text" @click="cancelCrop">
          {{ t("Отмена") }}
        </v-btn>
        <v-spacer />
        <v-btn color="primary" :loading="uploading" @click="confirmCrop">
          {{ t("Применить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, toRef, watch } from "vue";
import { useI18n } from "vue-i18n";
import {
  getEmployeeAvatarUrl,
  uploadEmployeeAvatar,
} from "@/services/employee.service";
import type { WithAvatar } from "@/services/employee.service";
import { usePermissions, Permissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";

const MAX_FILE_SIZE_BYTES = 15 * 1024 * 1024;
const EXPORT_SIZE = 1024;
const CROP_AREA_SIZE = 280;
const MAX_ZOOM_MULTIPLIER = 3;

const props = withDefaults(
  defineProps<{
    owner: WithAvatar;
    readOnly?: boolean;
  }>(),
  {
    readOnly: true,
  },
);

const emit = defineEmits<{
  (event: "updated"): void;
}>();

const { t } = useI18n();
const permissions = usePermissions();

const fileInput = ref<HTMLInputElement | null>(null);
const previewDialog = ref(false);
const cropDialog = ref(false);

const uploading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

const owner = toRef(props, "owner");
const ownerId = computed(() => owner.value.id);
const displayName = computed(() => owner.value.displayName);

const avatarRevision = ref(0);

watch(ownerId, () => {
  avatarRevision.value = 0;
  previewDialog.value = false;
  cropDialog.value = false;
  cropper.active = false;
  successMessage.value = "";
  errorMessage.value = "";
});

const baseAvatarUrl = computed(() => getEmployeeAvatarUrl(ownerId.value));

const ownerHasAvatar = computed(() => Boolean(owner.value.hasAvatar));

const hasAvatar = computed(
  () => ownerHasAvatar.value || avatarRevision.value !== 0,
);

const computedAvatarUrl = computed(() => {
  if (!hasAvatar.value) {
    return undefined;
  }
  return avatarRevision.value
    ? `${baseAvatarUrl.value}?v=${avatarRevision.value}`
    : baseAvatarUrl.value;
});

const currentAvatarExists = computed(() => hasAvatar.value);

const cropArea = ref<HTMLDivElement | null>(null);
const cropper = reactive({
  active: false,
  dataUrl: "",
  image: null as HTMLImageElement | null,
  naturalWidth: 0,
  naturalHeight: 0,
  minZoomFactor: 1,
  maxZoomFactor: MAX_ZOOM_MULTIPLIER,
  zoom: 1,
  offsetX: 0,
  offsetY: 0,
  pointerId: null as null | number,
  lastPointerX: 0,
  lastPointerY: 0,
});

const canEdit = computed(
  () => !props.readOnly && permissions.hasPermission(Permissions.UpdateAvatar),
);

const imageStyle = computed(() => {
  if (!cropper.active) {
    return {};
  }
  const scale = cropper.minZoomFactor * cropper.zoom;
  return {
    transform: `translate(-50%, -50%) translate(${cropper.offsetX}px, ${cropper.offsetY}px) scale(${scale})`,
  };
});

function triggerFileSelect() {
  errorMessage.value = "";
  successMessage.value = "";
  if (!canEdit.value) {
    return;
  }
  fileInput.value?.click();
}

function openPreview() {
  if (!computedAvatarUrl.value) {
    return;
  }
  previewDialog.value = true;
}

function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";
  if (!file) {
    return;
  }

  if (!file.type.startsWith("image/")) {
    errorMessage.value = t("Поддерживаются только изображения");
    return;
  }

  if (file.size > MAX_FILE_SIZE_BYTES) {
    errorMessage.value = t("Размер файла превышает N МБ", { n: 15 });
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    const result = reader.result;
    if (typeof result !== "string") {
      errorMessage.value = t("Не удалось обработать изображение");
      return;
    }
    prepareCropper(result);
  };
  reader.onerror = () => {
    errorMessage.value = t("Не удалось обработать изображение");
  };
  reader.readAsDataURL(file);
}

function prepareCropper(dataUrl: string) {
  const image = new Image();
  image.onload = () => {
    cropper.dataUrl = dataUrl;
    cropper.image = image;
    cropper.naturalWidth = image.naturalWidth;
    cropper.naturalHeight = image.naturalHeight;
    const minZoom = Math.max(
      CROP_AREA_SIZE / image.naturalWidth,
      CROP_AREA_SIZE / image.naturalHeight,
    );
    cropper.minZoomFactor = minZoom;
    cropper.maxZoomFactor = MAX_ZOOM_MULTIPLIER;
    cropper.zoom = 1;
    cropper.offsetX = 0;
    cropper.offsetY = 0;
    cropper.active = true;
    clampOffsets();
    cropDialog.value = true;
  };
  image.onerror = () => {
    errorMessage.value = t("Не удалось обработать изображение");
  };
  image.src = dataUrl;
}

watch(
  () => cropper.zoom,
  () => {
    clampOffsets();
  },
);

function startPan(event: PointerEvent) {
  if (!cropper.active) {
    return;
  }
  cropper.pointerId = event.pointerId;
  cropper.lastPointerX = event.clientX;
  cropper.lastPointerY = event.clientY;
  cropArea.value?.setPointerCapture(event.pointerId);
}

function continuePan(event: PointerEvent) {
  if (!cropper.active || cropper.pointerId !== event.pointerId) {
    return;
  }
  const dx = event.clientX - cropper.lastPointerX;
  const dy = event.clientY - cropper.lastPointerY;
  cropper.lastPointerX = event.clientX;
  cropper.lastPointerY = event.clientY;
  cropper.offsetX += dx;
  cropper.offsetY += dy;
  clampOffsets();
}

function stopPan(event: PointerEvent) {
  if (cropper.pointerId === event.pointerId) {
    cropper.pointerId = null;
    cropArea.value?.releasePointerCapture(event.pointerId);
  }
}

function handleWheel(event: WheelEvent) {
  if (!cropper.active) {
    return;
  }
  const delta = event.deltaY > 0 ? 0.1 : -0.1;
  const nextZoom = clamp(cropper.zoom - delta, 1, cropper.maxZoomFactor);
  if (nextZoom !== cropper.zoom) {
    cropper.zoom = nextZoom;
  }
}

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max);
}

function clampOffsets() {
  if (!cropper.active) {
    return;
  }
  const scale = cropper.minZoomFactor * cropper.zoom;
  const scaledWidth = cropper.naturalWidth * scale;
  const scaledHeight = cropper.naturalHeight * scale;
  const maxOffsetX = Math.max(0, (scaledWidth - CROP_AREA_SIZE) / 2);
  const maxOffsetY = Math.max(0, (scaledHeight - CROP_AREA_SIZE) / 2);
  cropper.offsetX = clamp(cropper.offsetX, -maxOffsetX, maxOffsetX);
  cropper.offsetY = clamp(cropper.offsetY, -maxOffsetY, maxOffsetY);
}

function cancelCrop() {
  cropDialog.value = false;
  cropper.active = false;
}

async function confirmCrop() {
  if (!cropper.active || uploading.value || !cropper.image) {
    return;
  }
  try {
    uploading.value = true;
    const blob = await generateCroppedBlob();
    await uploadEmployeeAvatar(ownerId.value, blob);
    avatarRevision.value = Date.now();
    successMessage.value = t("Изменения успешно применились!");
    errorMessage.value = "";
    cropDialog.value = false;
    cropper.active = false;
    emit("updated");
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
    successMessage.value = "";
  } finally {
    uploading.value = false;
  }
}

async function generateCroppedBlob(): Promise<Blob> {
  if (!cropper.image) {
    throw new Error("Image not available");
  }
  const canvas = document.createElement("canvas");
  canvas.width = EXPORT_SIZE;
  canvas.height = EXPORT_SIZE;
  const ctx = canvas.getContext("2d");
  if (!ctx) {
    throw new Error("Canvas is not supported");
  }

  const scale = cropper.minZoomFactor * cropper.zoom;
  const sourceCenterX = cropper.naturalWidth / 2 - cropper.offsetX / scale;
  const sourceCenterY = cropper.naturalHeight / 2 - cropper.offsetY / scale;
  const sourceWidth = CROP_AREA_SIZE / scale;
  const sourceHeight = CROP_AREA_SIZE / scale;
  const sourceX = clamp(
    sourceCenterX - sourceWidth / 2,
    0,
    Math.max(0, cropper.naturalWidth - sourceWidth),
  );
  const sourceY = clamp(
    sourceCenterY - sourceHeight / 2,
    0,
    Math.max(0, cropper.naturalHeight - sourceHeight),
  );

  ctx.drawImage(
    cropper.image,
    sourceX,
    sourceY,
    sourceWidth,
    sourceHeight,
    0,
    0,
    EXPORT_SIZE,
    EXPORT_SIZE,
  );

  return new Promise<Blob>((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob) {
          resolve(blob);
        } else {
          reject(new Error("Could not create image blob"));
        }
      },
      "image/jpeg",
      0.9,
    );
  });
}
</script>

<style scoped>
.avatar-uploader__avatar {
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 3px solid rgba(255, 255, 255, 0.7);
}

.cropper-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.crop-area {
  position: relative;
  width: 280px;
  height: 280px;
  overflow: hidden;
  border-radius: 16px;
  background-color: #f5f5f5;
  cursor: grab;
  touch-action: none;
}

.crop-area:active {
  cursor: grabbing;
}

.crop-image {
  position: absolute;
  top: 50%;
  left: 50%;
  user-select: none;
  touch-action: none;
}

.crop-overlay {
  position: absolute;
  inset: 0;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.8);
  pointer-events: none;
}
</style>
