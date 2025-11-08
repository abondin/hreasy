<!--
  Dialog to add a new skill with group selector, name combobox, rating and notes.
  Calls provided submit handler to persist data and shows inline success/error states.
  -->
<template>
  <!--<editor-fold desc="Add skill dialog">-->
  <v-dialog
    :model-value="open"
    max-width="640"
    persistent
    @update:model-value="onDialogModelChange"
  >
    <v-card>
      <v-card-title>
        {{ t("Добавление навыка") }}
      </v-card-title>

      <v-card-text>
        <v-form ref="form" @submit.prevent="onSubmit">
          <v-autocomplete
            v-model="formState.groupId"
            :items="skillGroups"
            item-title="name"
            item-value="id"
            clearable
            :label="t('Группа')"
            :rules="[requiredRule]"
            variant="outlined"
            density="comfortable"
            :disabled="loading"
            autofocus
          />

          <v-combobox
            v-model="formState.name"
            :label="t('Название навыка')"
            :items="filteredSkillNames"
            :disabled="loading"
            :rules="[nameRule]"
            variant="outlined"
            density="comfortable"
            class="mt-4"
          />

          <div class="mt-4">
            <div class="mb-2 font-weight-medium">
              {{ t("Уровень") }}
            </div>
            <v-rating
              v-model="formState.ratingValue"
              half-increments
              hover
              size="32"
              color="amber"
              :disabled="loading"
            />
          </div>

          <v-textarea
            v-model="formState.ratingNotes"
            :label="t('Примечание к уровню')"
            variant="outlined"
            :counter="1024"
            :rules="[notesRule]"
            :disabled="loading"
            class="mt-4"
            auto-grow
          />
        </v-form>

        <v-alert
          v-if="successMessage"
          type="success"
          variant="tonal"
          border="start"
          class="mt-4"
        >
          {{ successMessage }}
        </v-alert>
        <v-alert
          v-else-if="errorMessage"
          type="error"
          variant="tonal"
          border="start"
          class="mt-4"
        >
          {{ errorMessage }}
        </v-alert>
      </v-card-text>

      <v-card-actions>
        <v-checkbox
          v-model="addMore"
          :label="t('Добавить ещё')"
          :disabled="loading"
          hide-details
        />
        <v-spacer />
        <v-btn
          variant="text"
          color="secondary"
          :disabled="loading"
          @click="close"
        >
          {{ t("Закрыть") }}
        </v-btn>
        <v-btn
          color="primary"
          :loading="loading"
          :disabled="loading"
          @click="onSubmit"
        >
          {{ t("Добавить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { errorUtils } from "@/lib/errors";
import type {
  AddSkillBody,
  SharedSkillName,
  Skill,
  SkillGroup,
} from "@/services/skills.service";

interface VFormInstance {
  validate: () => Promise<{ valid: boolean }>;
  resetValidation: () => void;
}

const props = defineProps<{
  open: boolean;
  skillGroups: readonly SkillGroup[];
  sharedSkillNames: readonly SharedSkillName[];
  submitSkill: (payload: AddSkillBody) => Promise<Skill>;
}>();

const emit = defineEmits<{ (event: "close"): void }>();

const { t } = useI18n();
const form = ref<VFormInstance | null>(null);
const loading = ref(false);
const successMessage = ref("");
const errorMessage = ref("");
const addMore = ref(true);

const formState = reactive({
  groupId: undefined as number | undefined,
  name: "",
  ratingValue: undefined as number | undefined,
  ratingNotes: "",
});

const filteredSkillNames = computed(() => {
  if (!formState.groupId) {
    return [] as string[];
  }
  return props.sharedSkillNames
    .filter((item) => item.groupId === formState.groupId)
    .map((item) => item.name);
});

watch(
  () => props.open,
  (open) => {
    if (open) {
      resetForm();
    }
  },
);

function requiredRule(value: number | undefined) {
  return value ? true : t("Обязательное поле");
}

function nameRule(value: string) {
  const normalized = value?.trim();
  if (normalized && normalized.length <= 255) {
    return true;
  }
  if (!normalized) {
    return t("Обязательное поле. Не более N символов", { n: 255 });
  }
  return t("Не более N символов", { n: 255 });
}

function notesRule(value: string | null | undefined) {
  if (!value || value.length <= 1024) {
    return true;
  }
  return t("Не более N символов", { n: 1024 });
}

function resetForm() {
  formState.groupId = undefined;
  formState.name = "";
  formState.ratingValue = undefined;
  formState.ratingNotes = "";
  successMessage.value = "";
  errorMessage.value = "";
  addMore.value = true;
  form.value?.resetValidation();
}

function close() {
  emit("close");
}

function onDialogModelChange(value: boolean) {
  if (!value) {
    close();
  }
}

async function onSubmit() {
  if (loading.value || !form.value) {
    return;
  }
  const { valid } = await form.value.validate();
  if (!valid || !formState.groupId) {
    return;
  }
  const payload: AddSkillBody = {
    groupId: formState.groupId,
    name: formState.name.trim(),
    rating: formState.ratingValue
      ? {
          rating: formState.ratingValue,
          notes: formState.ratingNotes,
        }
      : null,
  };

  loading.value = true;
  successMessage.value = "";
  errorMessage.value = "";

  try {
    const created = await props.submitSkill(payload);
    successMessage.value = t("Навык успешно создан", { skill: created.name });
    if (addMore.value) {
      form.value.resetValidation();
      formState.name = "";
      formState.ratingValue = undefined;
      formState.ratingNotes = "";
    } else {
      close();
    }
  } catch (error) {
    errorMessage.value = errorUtils.shortMessage(error);
  } finally {
    loading.value = false;
  }
}
</script>
