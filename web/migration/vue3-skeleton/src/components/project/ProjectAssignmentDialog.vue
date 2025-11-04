<!--
  Dialog that allows updating the employee's current project and role.
  Closely mirrors the legacy EmployeeUpdateCurrentProject behaviour.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="560" scrollable>
    <v-card>
      <v-card-title class="d-flex flex-column align-start">
        <span>{{ t("Обновление текущего проекта") }}</span>
        <span class="text-body-2 text-medium-emphasis">
          {{ employeeName }}
        </span>
      </v-card-title>

      <v-card-text>
        <v-alert
          v-if="errorMessage"
          type="error"
          variant="tonal"
          border="start"
          class="mb-4"
        >
          {{ errorMessage }}
        </v-alert>

        <v-autocomplete
          v-model="selectedProjectId"
          :items="projectItems"
          :label="t('Проекты')"
          :loading="dictionaryLoading"
          :disabled="dictionaryLoading"
          clearable
          item-title="name"
          item-value="id"
          variant="underlined"
          autofocus
        />

        <v-combobox
          v-model="roleOnProject"
          :items="roleItems"
          :label="t('Роль')"
          :loading="dictionaryLoading"
          :disabled="dictionaryLoading"
          clearable
          variant="underlined"
          :rules="[validateRoleLength]"
        >
          <template #no-data>
            <v-list-item>
              <v-list-item-title class="text-body-2">
                {{ t("Элемент не найден") }}
                "<strong>{{ roleOnProject }}</strong>"
                {{ t("Нажмите") }} <kbd>enter</kbd> {{ t("чтобы создать") }}.
              </v-list-item-title>
            </v-list-item>
          </template>
        </v-combobox>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" :disabled="saving" @click="cancel">
          {{ t("Отменить") }}
        </v-btn>
        <v-btn color="primary" :loading="saving" @click="submit">
          {{ t("Применить") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import type { CurrentProjectDict } from "@/services/employee.service";
import {
  fetchCurrentProjectRoles,
  fetchProjects,
  type CurrentProjectRole,
  type ProjectDictDto,
} from "@/services/projects.service";
import {
  updateEmployeeCurrentProject,
} from "@/services/employee.service";

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    employeeId: number | null;
    employeeName: string;
    currentProject?: CurrentProjectDict | null;
  }>(),
  {
    modelValue: false,
    employeeId: null,
    currentProject: null,
  },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
  (event: "updated"): void;
}>();

const { t } = useI18n();

const dialogOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

const dictionaryLoading = ref(false);
const saving = ref(false);
const errorMessage = ref("");

const projects = ref<ProjectDictDto[]>([]);
const projectRoles = ref<CurrentProjectRole[]>([]);

const selectedProjectId = ref<number | null>(null);
const roleOnProject = ref<string | null>(null);

const projectItems = computed(() =>
  projects.value.filter((project) => project.active !== false),
);
const roleItems = computed(() =>
  projectRoles.value.map((role) => role.value),
);

watch(
  () => dialogOpen.value,
  (open) => {
    if (open) {
      initialiseForm();
      if (!projects.value.length || !projectRoles.value.length) {
        void loadDictionaries();
      }
    } else {
      resetState();
    }
  },
);

watch(
  () => props.currentProject,
  () => {
    if (dialogOpen.value) {
      initialiseForm();
    }
  },
);

function initialiseForm() {
  selectedProjectId.value = props.currentProject?.id ?? null;
  roleOnProject.value = props.currentProject?.role ?? null;
  errorMessage.value = "";
}

function resetState() {
  errorMessage.value = "";
  saving.value = false;
}

async function loadDictionaries() {
  dictionaryLoading.value = true;
  try {
    const [projectsResponse, rolesResponse] = await Promise.all([
      fetchProjects(),
      fetchCurrentProjectRoles(),
    ]);
    projects.value = projectsResponse;
    projectRoles.value = rolesResponse;
  } catch (error) {
    errorMessage.value = String(error);
  } finally {
    dictionaryLoading.value = false;
  }
}

function validateRoleLength(value: string | null): true | string {
  if (!value) {
    return true;
  }
  return value.length <= 64
    ? true
    : t("Не более N символов", { n: 64 }) as string;
}

async function submit() {
  if (!props.employeeId) {
    errorMessage.value = t("Профиль_недоступен");
    return;
  }
  saving.value = true;
  errorMessage.value = "";

  const payload =
    selectedProjectId.value !== null
      ? {
          id: selectedProjectId.value,
          role: roleOnProject.value ?? null,
        }
      : undefined;

  try {
    await updateEmployeeCurrentProject(props.employeeId, payload);
    dialogOpen.value = false;
    emit("updated");
    if (
      payload?.role &&
      !projectRoles.value.some((role) => role.value === payload.role)
    ) {
      await loadCurrentProjectRoles();
    }
  } catch (error) {
    errorMessage.value = String(error);
  } finally {
    saving.value = false;
  }
}

async function loadCurrentProjectRoles() {
  try {
    projectRoles.value = await fetchCurrentProjectRoles();
  } catch (error) {
    // silently ignore
    console.error(error);
  }
}

function cancel() {
  dialogOpen.value = false;
}

onMounted(() => {
  if (dialogOpen.value) {
    void loadDictionaries();
  }
});
</script>
