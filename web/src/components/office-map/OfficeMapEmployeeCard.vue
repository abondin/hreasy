<!--
  Employee information card used inside the office map preview.
  Loads full profile data for a selected employee and renders the key fields.
  -->
<template>
  <v-sheet
      class="w-100"
      elevation="0"
  >
    <div v-if="loading" class="d-flex justify-center py-8">
      <v-progress-circular indeterminate color="primary" size="48"/>
    </div>
    <v-alert
        v-else-if="error"
        type="error"
        variant="tonal"
        border="start"
        class="mb-4"
    >
      {{ error }}
    </v-alert>
    <template v-else-if="employee">
      <div class="d-flex align-center ga-4 mb-6">
        <v-avatar size="64" color="surface-light" class="flex-shrink-0">
          <v-img v-if="employee.hasAvatar" :src="getEmployeeAvatarUrl(employee.id)" :alt="employee.displayName" cover />
          <v-icon v-else icon="mdi-account" size="36" color="medium-emphasis" />
        </v-avatar>
        <div class="text-body-1 font-weight-medium text-break">{{ employee.displayName }}</div>
      </div>
      <dl class="d-flex flex-column ga-3">
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Отдел") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.department?.name ?? t("Не задан") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Текущий проект") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.currentProject?.name ?? t("Не задан") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Роль на текущем проекте") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.currentProject?.role ?? t("Не задана") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Бизнес Аккаунт") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.ba?.name ?? t("Не задан") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Почтовый адрес") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.email ?? t("Не задан") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Позиция") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.position?.name ?? t("Не задана") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Кабинет") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.officeLocation?.name ?? t("Не задан") }}</dd>
        </div>
        <div>
          <dt class="text-caption text-medium-emphasis">{{ t("Рабочее место") }}</dt>
          <dd class="ma-0 text-body-2 text-break">{{ employee.officeWorkplace?.trim() || t("Рабочее место не указано") }}</dd>
        </div>
      </dl>
    </template>
  </v-sheet>
</template>

<script setup lang="ts">
import {ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import type {Employee} from "@/services/employee.service";
import {findEmployee, getEmployeeAvatarUrl} from "@/services/employee.service";
import {errorUtils} from "@/lib/errors";

const props = defineProps<{
  employeeId: number | null;
}>();

const {t} = useI18n();

const employee = ref<Employee | null>(null);
const loading = ref(false);
const error = ref("");

watch(
    () => props.employeeId,
    (newId) => {
      if (!newId) {
        employee.value = null;
        error.value = "";
        loading.value = false;
        return;
      }

      loading.value = true;
      error.value = "";
      employee.value = null;
      findEmployee(newId)
          .then((data) => {
            employee.value = data;
          })
          .catch((err) => {
            error.value = errorUtils.shortMessage(err);
          })
          .finally(() => {
            loading.value = false;
          });
    },
    {immediate: true},
);
</script>
