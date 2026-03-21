<template>
  <v-card data-testid="admin-business-account-form">
    <v-card-title>
      {{ input?.id ? t("Изменение бизнес аккаунта") : t("Создание бизнес аккаунта") }}
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
  createAdminBusinessAccount,
  type BusinessAccountInfo,
  updateAdminBusinessAccount,
} from "@/services/admin/admin-business-account.service";

type VFormInstance = InstanceType<typeof VForm>;

interface BusinessAccountFormState {
  name: string;
  description: string;
  archived: boolean;
}

const props = defineProps<{
  input?: BusinessAccountInfo | null;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", businessAccountId: number): void;
}>();

const { t } = useI18n();

const formRef = ref<VFormInstance | null>(null);
const saving = ref(false);
const error = ref("");

const form = reactive<BusinessAccountFormState>({
  name: "",
  description: "",
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
  (value: string) => !value || value.length <= 1024 || t("Не более N символов", { n: 1024 }),
]);

watch(
  () => props.input,
  () => {
    form.name = props.input?.name ?? "";
    form.description = props.input?.description ?? "";
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

  saving.value = true;
  error.value = "";
  try {
    const payload = {
      name: form.name.trim(),
      description: form.description.trim() || undefined,
      archived: form.archived,
    };
    const id = props.input?.id
      ? await updateAdminBusinessAccount(props.input.id, payload)
      : await createAdminBusinessAccount(payload);
    emit("saved", id);
  } catch (submitError) {
    error.value = errorUtils.shortMessage(submitError);
  } finally {
    saving.value = false;
  }
}
</script>
