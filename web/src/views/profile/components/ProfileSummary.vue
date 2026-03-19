<!--
  Employee base information block without card wrapper.
-->
<template>
  <v-row
    align="center"
    no-gutters
    :class="['profile-summary__layout', { 'profile-summary__layout--stacked': isMobile }]"
  >
    <v-col cols="auto" :class="isMobile ? 'mb-4' : 'mr-6'">
      <profile-avatar
          :owner="employee"
          :read-only="avatarReadOnly"
          @updated="onAvatarUpdated"
      />
    </v-col>

    <v-col>
      <div v-if="showName" class="profile-summary__name">
        {{ employee.displayName }}
      </div>

      <property-list>
      <profile-summary-item :label="t('Отдел')">
          {{ employee.department?.name ?? t("Не задан") }}
      </profile-summary-item>

      <profile-summary-item :label="t('Текущий проект')">
          <v-btn
            v-if="canShowProjectInfo"
            icon="mdi-information"
            color="info"
            variant="text"
            density="compact"
            size="small"
            :title="t('Подробная информация по проекту ')"
            @click.stop="openProjectInfo"
          />
          {{ employee.currentProject?.name ?? t("Не задан") }}
          <v-btn
            v-if="canEditProject"
            icon="mdi-pencil"
            variant="text"
            density="compact"
            size="small"
            class="profile-summary__inline-icon"
            :title="t('Обновление текущего проекта')"
            @click.stop="openProjectUpdate"
          />
      </profile-summary-item>

      <profile-summary-item :label="t('Роль на текущем проекте')">
          {{ employee.currentProject?.role ?? t("Не задана") }}
      </profile-summary-item>

      <profile-summary-item :label="t('Бизнес Аккаунт')">
          {{ employee.ba?.name ?? t("Не задан") }}
      </profile-summary-item>

      <profile-summary-item :label="t('Почтовый адрес')">
          <hover-action-wrapper
            :show-action="Boolean(emailToCopy)"
            :tooltip="copyTooltip('email')"
            @action="copyToClipboard(emailToCopy, 'email')"
          >
            <span>{{ employee.email ?? t("Не задан") }}</span>
          </hover-action-wrapper>
      </profile-summary-item>

      <profile-summary-item :label="t('Позиция')">
          {{ employee.position?.name ?? t("Не задана") }}
      </profile-summary-item>

      <profile-summary-item :label="t('Кабинет')">
          {{ employee.officeLocation?.name ?? t("Не задан") }}
          <v-btn v-if="canShowMap" variant="plain" density="compact" size="small"
              :title="t('Посмотреть карту')"
              icon="mdi-map"
              @click.stop="openMap"
          />
      </profile-summary-item>

      <profile-summary-item :label="t('Телеграм')">
        <span class="d-inline-flex align-center">
          <hover-action-wrapper
            :show-action="Boolean(telegramToCopy)"
            :tooltip="copyTooltip('telegram')"
            @action="copyToClipboard(telegramToCopy, 'telegram')"
          >
            <profile-telegram-editor
                :employee-id="employee.id"
                :account="employee.telegram"
                :confirmed-at="employee.telegramConfirmedAt"
                :read-only="readOnly"
                @edit="emitEditTelegram"
            />
          </hover-action-wrapper>
        </span>
      </profile-summary-item>
      </property-list>
    </v-col>
  </v-row>
  <office-map-preview-dialog
      v-model="mapDialogOpen"
      :map-name="mapName"
      :map-title="mapTitle"
      :workplace="highlightedWorkplace"
  />
  <project-info-dialog
      v-model="projectInfoDialogOpen"
      :project-id="employee.currentProject?.id ?? null"
      :employee-id="employee.id ?? null"
  />
  <project-assignment-dialog
      v-model="projectUpdateDialogOpen"
      :employee-id="employee.id ?? null"
      :employee-name="employee.displayName"
      :current-project="employee.currentProject ?? null"
      @updated="emitProjectUpdated"
  />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, toRef } from "vue";
import {useI18n} from "vue-i18n";
import PropertyList from "@/components/shared/PropertyList.vue";
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";
import ProfileTelegramEditor from "@/views/profile/components/ProfileTelegramEditor.vue";
import type {Employee} from "@/services/employee.service";
import HoverActionWrapper from "@/components/shared/HoverActionWrapper.vue";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";
import ProjectInfoDialog from "@/components/project/ProjectInfoDialog.vue";
import ProjectAssignmentDialog from "@/components/project/ProjectAssignmentDialog.vue";
import { useOfficeMapPreview } from "@/composables/useOfficeMapPreview";
import { useEmployeeProjectActions } from "@/composables/useEmployeeProjectActions";
import { extractTelegramAccount } from "@/lib/telegram";
import { useDisplay } from "vuetify";

const props = withDefaults(
  defineProps<{
    employee: Employee;
    readOnly?: boolean;
    avatarReadOnly?: boolean;
    projectReadOnly?: boolean;
    showName?: boolean;
  }>(),
  {
    readOnly: true,
    avatarReadOnly: undefined,
    projectReadOnly: undefined,
    showName: true,
  },
);

const emit = defineEmits<{
  (event: "avatar-updated"): void;
  (event: "edit-telegram"): void;
  (event: "update-project"): void;
}>();

const { t } = useI18n();
const display = useDisplay();
const employee = toRef(props, "employee");
const readOnly = computed(() => props.readOnly);
const avatarReadOnly = computed(() => props.avatarReadOnly ?? props.readOnly);
const projectReadOnly = computed(() => props.projectReadOnly ?? props.readOnly);
const isMobile = computed(() => display.smAndDown.value);

const {
  mapDialogOpen,
  mapName,
  mapTitle,
  highlightedWorkplace,
  openMap,
} = useOfficeMapPreview(employee);

const {
  canShowProjectInfo,
  canEditProject,
  projectInfoDialogOpen,
  projectUpdateDialogOpen,
  openProjectInfo,
  openProjectUpdate,
} = useEmployeeProjectActions(employee, projectReadOnly);

const canShowMap = computed(() => Boolean(mapName.value));
const copiedField = ref<"email" | "telegram" | null>(null);
const emailToCopy = computed(() => employee.value.email?.trim() || null);
const telegramToCopy = computed(
  () => extractTelegramAccount(employee.value.telegram) ?? employee.value.telegram?.trim() ?? null,
);
let copiedResetTimer: ReturnType<typeof setTimeout> | null = null;

function onAvatarUpdated() {
  emit("avatar-updated");
}

function emitEditTelegram() {
  emit("edit-telegram");
}

function emitProjectUpdated() {
  emit("update-project");
}

function copyTooltip(field: "email" | "telegram") {
  return copiedField.value === field
    ? t("Скопировано в буфер обмена")
    : t("Скопировать в буфер обмена");
}

async function copyToClipboard(value: string | null, field: "email" | "telegram") {
  if (!value || !navigator.clipboard?.writeText) {
    return;
  }
  try {
    await navigator.clipboard.writeText(value);
    copiedField.value = field;
    if (copiedResetTimer) {
      clearTimeout(copiedResetTimer);
    }
    copiedResetTimer = setTimeout(() => {
      copiedField.value = null;
      copiedResetTimer = null;
    }, 1500);
  } catch {
    // Keep silent in this compact UI block.
  }
}

onBeforeUnmount(() => {
  if (copiedResetTimer) {
    clearTimeout(copiedResetTimer);
    copiedResetTimer = null;
  }
});
</script>

<style>
.profile-summary__name {
  font-size: 1.15rem;
  font-weight: 600;
  margin-bottom: 8px;
}

</style>
