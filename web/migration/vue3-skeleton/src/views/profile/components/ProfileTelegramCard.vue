<!--
  Telegram account editor for employee profile.
  Shows current value and allows updating via API with basic validation.
  -->
<template>
  <!--<editor-fold desc="Telegram update form">-->
  <v-form
    ref="form"
    @submit.prevent="onSubmit"
  >
    <v-card class="profile-telegram-card">
      <v-card-title>{{ t('Телеграм аккаунт') }}</v-card-title>
      <v-card-subtitle>{{ displayName }}</v-card-subtitle>
      <v-card-text>
        <v-text-field
          v-model="telegram"
          :label="t('Аккаунт')"
          :counter="255"
          :rules="rules"
          :disabled="loading"
          variant="outlined"
          clearable
        />
        <v-alert
          v-if="errorMessage"
          type="error"
          variant="tonal"
          border="start"
          class="mt-4"
        >
          {{ errorMessage }}
        </v-alert>
        <v-alert
          v-else-if="successMessage"
          type="success"
          variant="tonal"
          border="start"
          class="mt-4"
        >
          {{ successMessage }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          color="primary"
          type="submit"
          :loading="loading"
          :disabled="isSubmitDisabled"
        >
          {{ t('Применить') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import {computed, ref, watch} from 'vue';
import {useI18n} from 'vue-i18n';
import {errorUtils} from '@/lib/errors';
import {isShortTelegramUsernameOrPhoneValid} from '@/lib/telegram';
import {updateEmployeeTelegram} from '@/services/employee.service';

interface VFormInstance {
  validate: () => Promise<{valid: boolean}>;
  resetValidation: () => void;
}

const props = defineProps<{
  employeeId: number;
  displayName: string;
  initialTelegram?: string | null;
}>();

const emit = defineEmits<{
  (event: 'updated'): void;
}>();

const {t} = useI18n();

const form = ref<VFormInstance | null>(null);
const telegram = ref(props.initialTelegram ?? '');
const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

watch(
  () => props.initialTelegram,
  value => {
    telegram.value = value ?? '';
    successMessage.value = '';
    errorMessage.value = '';
    form.value?.resetValidation();
  }
);

watch(
  telegram,
  () => {
    successMessage.value = '';
    errorMessage.value = '';
  }
);

const normalizedOriginal = computed(() => normalizeTelegram(props.initialTelegram));
const normalizedCurrent = computed(() => normalizeTelegram(telegram.value));

const rules = [
  (value: string | null | undefined) => {
    if (!value || value.length <= 255) {
      return true;
    }
    return t('Не более N символов', {n: 255});
  },
  (value: string | null | undefined) => {
    if (isShortTelegramUsernameOrPhoneValid(value)) {
      return true;
    }
    return t('Username без @ и без https://t.me/ или номер телефона с +7 без пробелов');
  }
];

const isSubmitDisabled = computed(() => {
  if (loading.value) {
    return true;
  }
  return normalizedOriginal.value === normalizedCurrent.value;
});

function normalizeTelegram(value: string | null | undefined): string | null {
  if (!value) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

async function onSubmit() {
  if (!form.value) {
    return;
  }

  const {valid} = await form.value.validate();
  if (!valid || loading.value) {
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  try {
    await updateEmployeeTelegram(props.employeeId, {
      telegram: normalizedCurrent.value
    });
    successMessage.value = t('Изменения успешно применились!');
    emit('updated');
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.profile-telegram-card {
  border-radius: 16px;
}
</style>
