<!--
  Tech profile documents card.
  Configures the shared file attachments card for tech profiles.
-->
<template>
  <!--<editor-fold desc="Tech profiles card">-->
  <component :is="wrapperTag" class="profile-tech-profiles-card">
    <template v-if="showTitle">
      <component :is="titleTag" class="d-flex align-center">
        <span>{{ t("Квалификационные карточки") }}</span>
      </component>
    </template>
    <component :is="contentTag">
      <file-attachments
        :list-url="listUrl"
        :upload-url="uploadUrl"
        :build-download-url="buildDownloadUrl"
        :build-delete-url="buildDeleteUrl"
        :can-view="canView"
        :can-download="canDownload"
        :can-upload="canUpload"
        :read-only="readOnly"
        :permission-message="t('Не достаточно прав')"
        :accept="ACCEPTED_FILE_TYPES"
        :maximum-size="MAX_FILE_SIZE"
        :upload-timeout="UPLOAD_TIMEOUT"
        :upload-title="t('Загрузить квалификационную карточку')"
        :chips-per-column="3"
        :dense="dense"
        @updated="handleUpdated"
      />
    </component>
  </component>
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

const props = withDefaults(defineProps<{
  employeeId: number | null;
  withCard?: boolean;
  showTitle?: boolean;
  dense?: boolean;
}>(), {
  withCard: true,
  showTitle: true,
  dense: false,
});

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
const wrapperTag = computed(() => (props.withCard ? "v-card" : "div"));
const titleTag = computed(() => (props.withCard ? "v-card-title" : "div"));
const contentTag = computed(() => (props.withCard ? "v-card-text" : "div"));

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
