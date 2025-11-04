<!--
  Dialog for editing employee Telegram account.
  Mirrors legacy behaviour: pre-fills current value, validates format, writes via API, shows status messages.
  -->
<template>
  <!--<editor-fold desc="Telegram update dialog">-->
  <v-dialog
    :model-value="open"
    max-width="520"
    persistent
    @update:model-value="onUpdateModelValue"
  >
    <v-card>
      <v-card-title>
        {{ t("Телеграм аккаунт") }}
      </v-card-title>
      <v-card-subtitle>
        {{ displayName }}
        <v-chip
            v-if="isConfirmed"
            size="small"
            color="success"
            variant="tonal"
            data-testid="telegram-confirmed"
        >
          {{ t("Подтвержден") }}
        </v-chip>
      </v-card-subtitle>

      <v-card-text>
        <v-form ref="form" @submit.prevent="onSubmit">
          <v-text-field
            v-model="telegram"
            :label="t('Аккаунт')"
            :counter="255"
            :rules="rules"
            :disabled="loading"
            variant="outlined"
            density="comfortable"
            clearable
            autocomplete="off"
            autofocus
            data-testid="telegram-input"
          />
          <v-alert
            v-if="errorMessage"
            type="error"
            variant="tonal"
            border="start"
            class="mt-4"
            data-testid="telegram-error"
          >
            {{ errorMessage }}
          </v-alert>
          <v-alert
            v-else-if="successMessage"
            type="success"
            variant="tonal"
            border="start"
            class="mt-4"
            data-testid="telegram-success"
          >
            {{ successMessage }}
          </v-alert>
        </v-form>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          variant="text"
          color="secondary"
          :disabled="loading"
          data-testid="telegram-cancel"
          @click="emitClose"
        >
          {{ t("Отмена") }}
        </v-btn>
        <v-btn
          color="primary"
          :loading="loading"
          :disabled="isSubmitDisabled"
          data-testid="telegram-submit"
          @click="onSubmit"
        >
          {{ t("Применить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed, ref, watch, toRef } from "vue";
import { useI18n } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import { isShortTelegramUsernameOrPhoneValid } from "@/lib/telegram";
import { updateEmployeeTelegram } from "@/services/employee.service";

interface VFormInstance {
  validate: () => Promise<{ valid: boolean }>;
  resetValidation: () => void;
}

const props = defineProps<{
  open: boolean;
  employeeId: number;
  displayName: string;
  initialTelegram?: string | null;
  telegramConfirmedAt?: string | null;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "updated"): void;
}>();

const { t } = useI18n();

const open = toRef(props, "open");
const form = ref<VFormInstance | null>(null);
const telegram = ref(props.initialTelegram ?? "");
const loading = ref(false);
const errorMessage = ref("");
const successMessage = ref("");

watch(
  () => props.initialTelegram,
  (value) => {
    telegram.value = value ?? "";
    successMessage.value = "";
    errorMessage.value = "";
    form.value?.resetValidation();
  },
);

watch(telegram, () => {
  successMessage.value = "";
  errorMessage.value = "";
});

const normalizedOriginal = computed(() =>
  normalizeTelegram(props.initialTelegram),
);
const normalizedCurrent = computed(() => normalizeTelegram(telegram.value));

const rules = [
  (value: string | null | undefined) => {
    if (!value || value.length <= 255) {
      return true;
    }
    return t("Не более N символов", { n: 255 });
  },
  (value: string | null | undefined) => {
    if (isShortTelegramUsernameOrPhoneValid(value)) {
      return true;
    }
    return t(
      "Username без @ и без https://t.me/ или номер телефона с +7 без пробелов",
    );
  },
];

const isSubmitDisabled = computed(() => {
  if (loading.value) {
    return true;
  }
  return normalizedOriginal.value === normalizedCurrent.value;
});

const isConfirmed = computed(() => Boolean(props.telegramConfirmedAt));

function normalizeTelegram(value: string | null | undefined): string | null {
  if (!value) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function emitClose() {
  emit("close");
}

function onUpdateModelValue(value: boolean) {
  if (!value) {
    emitClose();
  }
}

async function onSubmit() {
  if (loading.value || !form.value) {
    return;
  }
  const { valid } = await form.value.validate();
  if (!valid) {
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    await updateEmployeeTelegram(props.employeeId, {
      telegram: normalizedCurrent.value,
    });
    successMessage.value = t("Изменения успешно применились!");
    emit("updated");
    emitClose();
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
</style>
