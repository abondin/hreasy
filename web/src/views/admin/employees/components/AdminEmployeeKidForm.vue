<template>
  <v-form ref="employeeKidEditForm">
    <v-card>
      <v-card-title class="d-flex align-center">
        <span>
          {{
            isNew
              ? t("Создание карточки ребёнка сотрудника")
              : t("Изменение карточки ребёнка сотрудника")
          }}
        </span>
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" @click="closeDialog" />
      </v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12">
            <v-autocomplete
              v-model="kidForm.parentId"
              :items="parentOptions"
              item-title="displayName"
              item-value="id"
              :label="t('Сотрудник')"
              :rules="requiredRules"
              :disabled="!isNew"
              variant="outlined"
            />
          </v-col>
          <v-col cols="12">
            <v-text-field
              v-model="kidForm.displayName"
              :counter="255"
              :rules="displayNameRules"
              :label="t('ФИО')"
              variant="outlined"
              required
            />
          </v-col>
          <v-col cols="12">
            <MyDateFormComponent
              v-model="kidForm.birthday"
              :label="t('Дата рождения')"
            />
          </v-col>
        </v-row>

        <v-alert
          v-if="error"
          type="error"
          variant="tonal"
          border="start"
          class="mt-3"
        >
          {{ error }}
        </v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="closeDialog">{{ t("Закрыть") }}</v-btn>
        <v-btn color="primary" :loading="saving" @click="submit">
          {{ isNew ? t("Создать") : t("Изменить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import { withArchivedOptionById, withCurrentOptionById } from "@/lib/dict-options";
import { errorUtils } from "@/lib/errors";
import type { Employee } from "@/services/employee.service";
import {
  createEmployeeKid,
  updateEmployeeKid,
  type CreateOrUpdateEmployeeKidBody,
  type EmployeeKid,
} from "@/services/admin/admin-employee.service";

type Rule = (value: unknown) => boolean | string;

interface KidFormState {
  id: number | null;
  parentId: number | null;
  displayName: string;
  birthday: string;
}

const props = defineProps<{
  input?: EmployeeKid | null;
  employees: Employee[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved"): void;
}>();

const { t } = useI18n();

const employeeKidEditForm = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null);
const saving = ref(false);
const error = ref<string | null>(null);

const kidForm = reactive<KidFormState>({
  id: null,
  parentId: null,
  displayName: "",
  birthday: "",
});

const isNew = computed(() => !kidForm.id);
const parentOptions = computed(() => {
  const currentParent = props.input?.parent
    ? {
      id: props.input.parent.id,
      displayName: props.input.parent.name,
    }
    : null;
  const withCurrent = withCurrentOptionById(props.employees, currentParent);
  return withArchivedOptionById(withCurrent, kidForm.parentId, (id) => ({
    id,
    displayName: `${t("Архив")} #${id}`,
  }));
});

const requiredRules = computed<Rule[]>(() => [
  (value) => Boolean(value) || t("Обязательное поле"),
]);
const displayNameRules = computed<Rule[]>(() => [
  (value) => {
    if (typeof value !== "string" || !value.trim()) {
      return t("Обязательное поле. Не более N символов", { n: 255 });
    }
    return value.length <= 255 || t("Обязательное поле. Не более N символов", { n: 255 });
  },
]);

watch(
  () => props.input,
  (value) => {
    reset(value ?? undefined);
  },
  { immediate: true },
);

function reset(input?: EmployeeKid) {
  error.value = null;
  kidForm.id = input?.id ?? null;
  kidForm.displayName = input?.displayName ?? "";
  kidForm.birthday = input?.birthday ?? "";
  kidForm.parentId = input?.parent?.id ?? null;
}

async function submit() {
  if (!employeeKidEditForm.value) {
    return;
  }
  const validationResult = await employeeKidEditForm.value.validate();
  if (!validationResult.valid || !kidForm.parentId) {
    return;
  }

  const payload: CreateOrUpdateEmployeeKidBody = {
    displayName: kidForm.displayName.trim(),
    birthday: kidForm.birthday.trim() || undefined,
  };

  saving.value = true;
  error.value = null;
  try {
    if (isNew.value) {
      await createEmployeeKid(kidForm.parentId, payload);
    } else if (kidForm.id) {
      await updateEmployeeKid(kidForm.parentId, kidForm.id, payload);
    }
    emit("saved");
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    saving.value = false;
  }
}

function closeDialog() {
  emit("close");
}
</script>
