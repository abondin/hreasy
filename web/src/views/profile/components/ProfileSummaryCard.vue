<!--
  Employee base information card.
  In editable mode project/role, avatar and telegram can be changed if permissions allow.
  -->
<template>
  <v-card class="profile-summary-card pa-6">
    <v-row
      align="center"
      no-gutters
      :class="['profile-summary-card__layout', { 'profile-summary-card__layout--stacked': isMobile }]"
    >
      <v-col cols="auto" :class="isMobile ? 'mb-4' : 'mr-6'">
        <profile-avatar
            :owner="employee"
            :read-only="readOnly"
            @updated="onAvatarUpdated"
        />
      </v-col>

      <v-col>
        <div class="profile-summary-card__name">
          {{ employee.displayName }}
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">{{ t("Отдел") }}:</span>
          <span class="profile-summary-card__value">
            {{ employee.department?.name ?? t("Не задан") }}
          </span>
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Текущий проект") }}:
          </span>
          <span class="profile-summary-card__value">
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
              class="profile-summary-card__inline-icon"
              :title="t('Обновление текущего проекта')"
              @click.stop="openProjectUpdate"
            />
          </span>
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Роль на текущем проекте") }}:
          </span>
          <span class="profile-summary-card__value">
            {{ employee.currentProject?.role ?? t("Не задана") }}
          </span>
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Бизнес Аккаунт") }}:
          </span>
          <span class="profile-summary-card__value">
            {{ employee.ba?.name ?? t("Не задан") }}
          </span>
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Почтовый адрес") }}:
          </span>
          <span class="profile-summary-card__value">
            <hover-action-wrapper
              :show-action="Boolean(emailToCopy)"
              :tooltip="copyTooltip('email')"
              @action="copyToClipboard(emailToCopy, 'email')"
            >
              <span>{{ employee.email ?? t("Не задан") }}</span>
            </hover-action-wrapper>
          </span>
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Позиция") }}:
          </span>
          <span class="profile-summary-card__value">
            {{ employee.position?.name ?? t("Не задана") }}
          </span>
        </div>

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Кабинет") }}:
          </span>
          <span class="profile-summary-card__value">
            {{ employee.officeLocation?.name ?? t("Не задан") }}
            <v-btn v-if="canShowMap" variant="plain" density="compact" size="small"
                :title="t('Посмотреть карту')"
                icon="mdi-map"
                @click.stop="openMap"
            />
          </span>
        </div>

        <div class="profile-summary-card__item align-center d-flex">
          <span class="profile-summary-card__label">
            {{ t("Телеграм") }}:
          </span>
          <span class="profile-summary-card__value d-inline-flex align-center">
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
        </div>
      </v-col>
    </v-row>
  </v-card>
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
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
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
  }>(),
  {
    readOnly: true,
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
} = useEmployeeProjectActions(employee);

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

<style scoped>
.profile-summary-card__name {
  font-size: 1.15rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.profile-summary-card__item {
  font-size: 0.92rem;
  color: rgba(0, 0, 0, 0.72);
  margin-bottom: 4px;
}

.profile-summary-card__label {
  font-weight: 500;
  margin-right: 4px;
}

.profile-summary-card__value {
  font-weight: 400;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* Keep avatar and text side by side in narrow containers like employees right drawer. */
.profile-summary-card__layout {
  flex-wrap: nowrap;
}

.profile-summary-card__layout--stacked {
  flex-wrap: wrap;
}

</style>
