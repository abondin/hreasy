<template>
  <v-card data-testid="admin-user-roles-form">
    <v-card-title>{{ t("Редактирование ролей") }}</v-card-title>
    <v-card-text>
      <v-form ref="formRef" @submit.prevent="submit">
        <v-select
          v-model="form.roles"
          :items="allRoles"
          item-title="name"
          item-value="id"
          :item-props="roleItemProps"
          :label="t('Роли')"
          multiple
          chips
          variant="outlined"
        />

        <v-select
          v-model="form.accessibleDepartments"
          :items="allDepartments"
          item-title="name"
          item-value="id"
          :label="t('Доступные отделы')"
          multiple
          chips
          variant="outlined"
        />

        <v-select
          v-model="form.accessibleProjects"
          :items="allProjects"
          item-title="name"
          item-value="id"
          :label="t('Доступные проекты')"
          multiple
          chips
          variant="outlined"
        />

        <v-select
          v-model="form.accessibleBas"
          :items="allBas"
          item-title="name"
          item-value="id"
          :label="t('Доступные бизнес аккаунты')"
          multiple
          chips
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
        {{ t("Изменить") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { VForm } from "vuetify/components";
import type { DictItem } from "@/services/dict.service";
import { errorUtils } from "@/lib/errors";
import {
  type RoleDict,
  type UserSecurityInfo,
  updateAdminUserRoles,
} from "@/services/admin/admin-user.service";

type VFormInstance = InstanceType<typeof VForm>;

interface UserRolesFormState {
  roles: string[];
  accessibleDepartments: number[];
  accessibleProjects: number[];
  accessibleBas: number[];
}

const props = defineProps<{
  input: UserSecurityInfo | null;
  allDepartments: DictItem[];
  allProjects: Array<{ id: number; name: string; active?: boolean }>;
  allBas: DictItem[];
  allRoles: RoleDict[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved"): void;
}>();

const { t } = useI18n();

const formRef = ref<VFormInstance | null>(null);
const saving = ref(false);
const error = ref("");

const form = reactive<UserRolesFormState>({
  roles: [],
  accessibleDepartments: [],
  accessibleProjects: [],
  accessibleBas: [],
});

watch(
  () => props.input,
  () => {
    form.roles = [...(props.input?.roles ?? [])];
    form.accessibleDepartments = [...(props.input?.accessibleDepartments ?? [])];
    form.accessibleProjects = [...(props.input?.accessibleProjects ?? [])];
    form.accessibleBas = [...(props.input?.accessibleBas ?? [])];
    error.value = "";
    formRef.value?.resetValidation();
  },
  { immediate: true },
);

function roleItemProps(role: RoleDict) {
  return {
    disabled: role.disabled,
  };
}

async function submit(): Promise<void> {
  if (!props.input?.employee?.id) {
    return;
  }

  saving.value = true;
  error.value = "";
  try {
    await updateAdminUserRoles(props.input.employee.id, {
      roles: form.roles,
      accessibleDepartments: form.accessibleDepartments,
      accessibleProjects: form.accessibleProjects,
      accessibleBas: form.accessibleBas,
    });
    emit("saved");
  } catch (submitError) {
    error.value = errorUtils.shortMessage(submitError);
  } finally {
    saving.value = false;
  }
}
</script>
