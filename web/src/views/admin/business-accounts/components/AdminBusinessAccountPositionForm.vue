<template>
  <v-card data-testid="admin-business-account-position-form">
    <v-card-title>
      {{ input?.id ? t("Изменение позиции бизнес аккаунта") : t("Создание позиции бизнес аккаунта") }}
    </v-card-title>
    <v-card-text>
      <v-form ref="formRef" @submit.prevent="submit">
        <v-text-field
          v-model="form.name"
          :label="t('Наименование')"
          :counter="255"
          :rules="nameRules"
          variant="outlined"
        />

        <v-text-field
          v-model.number="form.rate"
          type="number"
          hide-spin-buttons
          :label="t('Ставка (с НДС)')"
          :rules="rateRules"
          variant="outlined"
        />

        <v-textarea
          v-model="form.description"
          :label="t('Описание')"
          :counter="1024"
          :rules="descriptionRules"
          variant="outlined"
        />

        <v-select
          v-model="form.archived"
          :label="t('Архив')"
          :items="archivedOptions"
          item-title="title"
          item-value="value"
          variant="outlined"
        />
      </v-form>

      <v-alert
        v-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mt-4"
      >
        {{ error }}
      </v-alert>
    </v-card-text>
    <v-card-actions>
      <v-spacer />
      <v-btn variant="text" :disabled="saving" @click="$emit('close')">
        {{ t("Закрыть") }}
      </v-btn>
      <v-btn color="primary" :loading="saving" @click="submit">
        {{ input?.id ? t("Изменить") : t("Создать") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import { errorUtils } from "@/lib/errors";
import {
  createBusinessAccountPosition,
  type BusinessAccountPosition,
  updateBusinessAccountPosition,
} from "@/services/admin/admin-business-account.service";

type VFormInstance = InstanceType<typeof VForm>;

interface BusinessAccountPositionFormState {
  name: string;
  description: string;
  rate: number | null;
  archived: boolean;
}

const props = defineProps<{
  businessAccountId: number;
  input?: BusinessAccountPosition | null;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", positionId: number): void;
}>();

const { t } = useI18n();

const formRef = ref<VFormInstance | null>(null);
const saving = ref(false);
const error = ref("");

const form = reactive<BusinessAccountPositionFormState>({
  name: "",
  description: "",
  rate: null,
  archived: false,
});

const archivedOptions = computed(() => [
  { title: t("Нет"), value: false },
  { title: t("Да"), value: true },
]);
const nameRules = computed(() => [
  (value: string) =>
    Boolean(value && value.length <= 255)
    || t("Обязательное поле. Не более N символов", { n: 255 }),
]);
const descriptionRules = computed(() => [
  (value: string) => !value || value.length <= 1024 || t("Обязательное поле. Не более N символов", { n: 1024 }),
]);
const rateRules = computed(() => [
  (value: number | null) =>
    (typeof value === "number" && !Number.isNaN(value) && value !== 0) || t("Обязательное поле"),
]);

watch(
  () => props.input,
  () => {
    form.name = props.input?.name ?? "";
    form.description = props.input?.description ?? "";
    form.rate = props.input?.rate ?? null;
    form.archived = props.input?.archived ?? false;
    error.value = "";
    formRef.value?.resetValidation();
  },
  { immediate: true },
);

async function submit(): Promise<void> {
  const validation = await formRef.value?.validate();
  if (validation && !validation.valid) {
    return;
  }
  if (form.rate == null) {
    return;
  }

  saving.value = true;
  error.value = "";
  try {
    const payload = {
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      rate: Number(form.rate),
      archived: form.archived,
    };
    const id = props.input?.id
      ? await updateBusinessAccountPosition(props.businessAccountId, props.input.id, payload)
      : await createBusinessAccountPosition(props.businessAccountId, payload);
    emit("saved", id);
  } catch (submitError) {
    error.value = errorUtils.shortMessage(submitError);
  } finally {
    saving.value = false;
  }
}
</script>
