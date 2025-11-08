<!--
  Tech profile documents card.
  Configures the shared file attachments card for tech profiles.
-->
<template>
  <!--<editor-fold desc="Tech profiles card">-->
  <v-card class="profile-tech-profiles-card">
    <v-card-title class="d-flex align-center">
      <span>{{ t("Квалификационные карточки") }}</span>
    </v-card-title>
    <v-card-text>
      <file-attachments
        :list-url="listUrl"
        :upload-url="uploadUrl"
        :build-download-url="buildDownloadUrl"
        :build-delete-url="buildDeleteUrl"
        :can-view="canView"
        :can-download="canDownload"
        :can-upload="canUpload"
        :read-only="readOnly"
        :empty-message="t('Квалификационные_карточки_отсутствуют')"
        :permission-message="t('Не достаточно прав')"
        :accept="ACCEPTED_FILE_TYPES"
        :maximum-size="MAX_FILE_SIZE"
        :upload-timeout="UPLOAD_TIMEOUT"
        :upload-title="t('Загрузить квалификационную карточку')"
        :chips-per-column="3"
        @updated="handleUpdated"
      />
    </v-card-text>
  </v-card>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { usePermissions } from "@/lib/permissions";
import {
  type TechProfile,
  getTechProfileDeleteUrl,
  getTechProfileDownloadUrl,
  getTechProfileUploadUrl,
} from "@/services/techprofile.service";
import FileAttachments, {
  type AttachmentItem,
} from "@/components/FileAttachments.vue";

const props = defineProps<{
  employeeId: number | null;
}>();

const emit = defineEmits<{
  (event: "updated"): void;
}>();

const { t } = useI18n();
const permissions = usePermissions();

const ACCEPTED_FILE_TYPES =
  ".pdf,.doc,.docx,.rtf,.txt,.md,.xls,.xlsx,.zip,.rar,.7z,.png,.jpg,.jpeg,.pptx,.ppsx,.odp,.ppt,.pps";
const MAX_FILE_SIZE = 10 * 1024 * 1024;
const UPLOAD_TIMEOUT = 30_000;

const currentEmployeeId = computed(() => props.employeeId ?? null);

const listUrl = computed(() => {
  const id = currentEmployeeId.value;
  if (!id) {
    return null;
  }
  return `v1/techprofile/${id}`;
});

const uploadUrl = computed(() => {
  const id = currentEmployeeId.value;
  if (!id || !permissions.canUploadTechProfiles(id)) {
    return null;
  }
  return getTechProfileUploadUrl(id);
});

const readOnly = computed(() => {
  const id = currentEmployeeId.value;
  if (!id) {
    return true;
  }
  return !permissions.canUploadTechProfiles(id);
});

function canView() {
  const id = currentEmployeeId.value;
  if (!id) {
    return false;
  }
  return permissions.canDownloadTechProfiles(id);
}

function canUpload() {
  const id = currentEmployeeId.value;
  if (!id) {
    return false;
  }
  return permissions.canUploadTechProfiles(id);
}

function canDownload(item: AttachmentItem): boolean {
  void item;
  return canView();
}

function buildDownloadUrl(raw: AttachmentItem) {
  const id = currentEmployeeId.value;
  if (!id) {
    return "#";
  }
  const profile = raw as unknown as TechProfile;
  return getTechProfileDownloadUrl(id, profile);
}

function buildDeleteUrl(raw: AttachmentItem) {
  const id = currentEmployeeId.value;
  if (!id || !permissions.canUploadTechProfiles(id)) {
    return null;
  }
  const profile = raw as unknown as TechProfile;
  return getTechProfileDeleteUrl(id, profile.filename);
}

function handleUpdated() {
  emit("updated");
}
</script>
