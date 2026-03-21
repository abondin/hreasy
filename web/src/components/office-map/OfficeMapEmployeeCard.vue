<!--
  Employee information card used inside the office map preview.
  Loads full profile data for a selected employee and renders the key fields.
  -->
<template>
  <v-sheet
      class="office-map-employee-card w-100 mx-auto"
      elevation="0"
  >
    <div v-if="loading" class="office-map-employee-card__state">
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
      <v-row no-gutters align="start">
        <v-col cols="auto" class="pr-4">
          <profile-avatar :owner="employee" :read-only="true"/>
        </v-col>
        <v-col>
          <dl class="office-map-employee-card__list">
            <div class="office-map-employee-card__item">
              <dt>{{ t("ФИО") }}:</dt>
              <dd>{{ employee.displayName }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Отдел") }}:</dt>
              <dd>{{ employee.department?.name ?? t("Не задан") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Текущий проект") }}:</dt>
              <dd>{{ employee.currentProject?.name ?? t("Не задан") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Роль на текущем проекте") }}:</dt>
              <dd>{{ employee.currentProject?.role ?? t("Не задана") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Бизнес Аккаунт") }}:</dt>
              <dd>{{ employee.ba?.name ?? t("Не задан") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Почтовый адрес") }}:</dt>
              <dd>{{ employee.email ?? t("Не задан") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Позиция") }}:</dt>
              <dd>{{ employee.position?.name ?? t("Не задана") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Кабинет") }}:</dt>
              <dd>{{ employee.officeLocation?.name ?? t("Не задан") }}</dd>
            </div>
            <div class="office-map-employee-card__item">
              <dt>{{ t("Рабочее место") }}:</dt>
              <dd>{{ employee.officeWorkplace ?? t("Не задан") }}</dd>
            </div>
          </dl>
        </v-col>
      </v-row>
    </template>
  </v-sheet>
</template>

<script setup lang="ts">
import {ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
import type {Employee} from "@/services/employee.service";
import {findEmployee} from "@/services/employee.service";
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

<style scoped>
.office-map-employee-card__state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
}

.office-map-employee-card__list {
  display: grid;
  grid-template-columns: max-content auto;
  gap: 4px 12px;
}

.office-map-employee-card__item dt {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.72);
}

.office-map-employee-card__item dd {
  margin: 0;
}


</style>
