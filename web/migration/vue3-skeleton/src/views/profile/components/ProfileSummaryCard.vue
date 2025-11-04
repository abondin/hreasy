<!--
  Employee base information card.
  In editable mode project/role, avatar and telegram can be changed if permissions allow.
  -->
<template>
  <v-card class="profile-summary-card pa-6">
    <v-row align="center" no-gutters>
      <v-col cols="auto" class="mr-6">
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
            {{ employee.currentProject?.name ?? t("Не задан") }}
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
            {{ employee.email ?? t("Не задан") }}
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

        <div class="profile-summary-card__item">
          <span class="profile-summary-card__label">
            {{ t("Телеграм") }}:
          </span>
          <span class="profile-summary-card__value">
            <profile-telegram-editor
                :employee-id="employee.id"
                :account="employee.telegram"
                :confirmed-at="employee.telegramConfirmedAt"
                :read-only="readOnly"
                @edit="emitEditTelegram"
            />
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
</template>

<script setup lang="ts">
import {computed, ref} from "vue";
import {useI18n} from "vue-i18n";
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
import ProfileTelegramEditor from "@/views/profile/components/ProfileTelegramEditor.vue";
import type {Employee} from "@/services/employee.service";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";

const {employee, readOnly = true} = defineProps<{
  employee: Employee;
  readOnly?: boolean;
}>();

const emit = defineEmits<{
  (event: "avatar-updated"): void;
  (event: "edit-telegram"): void;
}>();

const {t} = useI18n();
const mapDialogOpen = ref(false);

const mapName = computed(() => employee.officeLocation?.mapName ?? null);
const mapTitle = computed(() => employee.officeLocation?.name ?? null);
const highlightedWorkplace = computed(
    () => employee.officeWorkplace ?? null,
);
const canShowMap = computed(
    () => Boolean(mapName.value),
);

function onAvatarUpdated() {
  emit("avatar-updated");
}

function emitEditTelegram() {
  emit("edit-telegram");
}

function openMap() {
  if (canShowMap.value) {
    mapDialogOpen.value = true;
  }
}
</script>

<style scoped>
.profile-summary-card__name {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 12px;
}

.profile-summary-card__item {
  font-size: 0.95rem;
  color: rgba(0, 0, 0, 0.72);
  margin-bottom: 6px;
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


</style>
