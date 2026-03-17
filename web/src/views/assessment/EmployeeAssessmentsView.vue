<template>
  <v-container fluid class="py-6" data-testid="employee-assessments-view">
    <div class="mx-auto" style="max-width: 1360px;">
      <v-breadcrumbs
        class="px-0"
        density="compact"
        divider="/"
        :items="breadcrumbs"
      >
        <template #item="{ item }">
          <router-link v-if="item.to" :to="item.to" class="text-decoration-none">
            {{ item.title }}
          </router-link>
          <span v-else>{{ item.title }}</span>
        </template>
      </v-breadcrumbs>

      <v-skeleton-loader v-if="loading" type="heading, paragraph, paragraph" class="mt-4" />
      <v-alert v-else-if="error" type="error" variant="tonal" class="mt-4">{{ error }}</v-alert>

      <template v-else-if="employee">
        <v-card class="mt-4">
          <v-card-text class="pa-6">
            <profile-summary :employee="employee" :read-only="true" />
          </v-card-text>
        </v-card>

        <v-card class="mt-5">
          <v-card-title class="d-flex align-center ga-3 flex-wrap">
            <v-select
              v-model="includeCanceled"
              :items="includeCanceledOptions"
              item-title="title"
              item-value="value"
              :label="t('Учитывать отмененные')"
              density="compact"
              hide-details
              style="max-width: 260px;"
            />
            <v-spacer />
            <v-btn
              color="primary"
              icon="mdi-plus"
              variant="text"
              :disabled="loading"
              data-testid="employee-assessments-add"
              @click="openCreateDialog"
            />
          </v-card-title>
          <v-card-text>
            <HREasyTableBase
              table-class="text-truncate"
              :headers="headers"
              :items="filteredItems"
              :loading="loading"
              :loading-text="t('Загрузка_данных')"
              :no-data-text="t('Отсутствуют данные')"
              density="compact"
              :sort-by="[{ key: 'plannedDate', order: 'desc' }]"
              data-testid="employee-assessments-table"
            >
              <template #[`item.plannedDate`]="{ item }">
                <router-link
                  :to="{ name: 'assessment-details', params: { employeeId: String(employee.id), assessmentId: String(item.id) } }"
                >
                  {{ formatDate(item.plannedDate) }}
                </router-link>
              </template>
              <template #[`item.createdAt`]="{ item }">
                {{ item.createdBy ? `${formatDateTime(item.createdAt)} (${item.createdBy.name})` : "" }}
              </template>
              <template #[`item.completedAt`]="{ item }">
                {{ item.completedBy ? `${formatDateTime(item.completedAt)} (${item.completedBy.name})` : "" }}
              </template>
              <template #[`item.canceledAt`]="{ item }">
                {{ item.canceledBy ? `${formatDateTime(item.canceledAt)} (${item.canceledBy.name})` : "" }}
              </template>
            </HREasyTableBase>
          </v-card-text>
        </v-card>
      </template>
    </div>

    <v-dialog v-model="createDialog" max-width="560">
      <v-card>
        <v-card-title>{{ t("Планирование нового ассессмента") }}</v-card-title>
        <v-card-text>
          <v-alert v-if="createError" type="error" variant="tonal" class="mb-3">{{ createError }}</v-alert>
          <v-form ref="createFormRef">
            <my-date-form-component
              v-model="createForm.plannedDate"
              :label="`${t('Дата проведения')}*`"
              :rules="[requiredDateRule]"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="createLoading" @click="createDialog = false">{{ t("Закрыть") }}</v-btn>
          <v-btn color="primary" :loading="createLoading" @click="submitCreate">{{ t("Запланировать") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter, type RouteLocationRaw } from "vue-router";
import type { VForm } from "vuetify/components";
import { formatDate, formatDateTime, formatIsoDate } from "@/lib/datetime";
import { errorUtils } from "@/lib/errors";
import { findEmployee, type Employee } from "@/services/employee.service";
import {
  fetchEmployeeAssessments,
  scheduleAssessment,
  type AssessmentBase,
} from "@/services/assessment.service";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";

type VFormInstance = InstanceType<typeof VForm>;

const { t } = useI18n();
const route = useRoute();
const router = useRouter();

const loading = ref(false);
const createLoading = ref(false);
const error = ref("");
const createError = ref("");
const employee = ref<Employee | null>(null);
const assessments = ref<AssessmentBase[]>([]);
const includeCanceled = ref(false);
const createDialog = ref(false);
const createFormRef = ref<VFormInstance | null>(null);

const createForm = reactive({
  plannedDate: "",
});

const headers = computed(() => [
  { title: t("Дата ассессмента"), key: "plannedDate", width: "180px" },
  { title: t("Создано"), key: "createdAt", width: "260px" },
  { title: t("Завершено"), key: "completedAt", width: "260px" },
  { title: t("Отменено"), key: "canceledAt", width: "260px" },
]);

const includeCanceledOptions = computed(() => [
  { title: t("Нет"), value: false },
  { title: t("Да"), value: true },
]);
const breadcrumbs = computed<Array<{ title: string; to?: RouteLocationRaw }>>(() => [
  {
    title: t("Ассессменты"),
    to: { name: "assessments" },
  },
  {
    title: employee.value?.displayName ?? "-",
  },
]);

const filteredItems = computed(() =>
  assessments.value.filter((item) => includeCanceled.value || !item.canceledAt),
);

const requiredDateRule = (value: unknown) => Boolean(value) || t("Обязательное поле");

watch(() => route.params.employeeId, () => {
  load().catch(() => undefined);
}, { immediate: true });

async function load(): Promise<void> {
  const employeeId = Number(route.params.employeeId);
  if (!Number.isInteger(employeeId)) {
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    const [employeeData, assessmentsData] = await Promise.all([
      findEmployee(employeeId),
      fetchEmployeeAssessments(employeeId),
    ]);
    employee.value = employeeData;
    assessments.value = assessmentsData;
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}

function openCreateDialog(): void {
  createError.value = "";
  createForm.plannedDate = formatIsoDate(new Date());
  createDialog.value = true;
}

async function submitCreate(): Promise<void> {
  const employeeId = employee.value?.id;
  if (!employeeId || !createFormRef.value) {
    return;
  }

  const validation = await createFormRef.value.validate();
  if (!validation.valid) {
    return;
  }

  createLoading.value = true;
  createError.value = "";
  try {
    const assessmentId = await scheduleAssessment(employeeId, {
      plannedDate: createForm.plannedDate,
      managers: [],
    });
    createDialog.value = false;
    await router.push({
      name: "assessment-details",
      params: { employeeId: String(employeeId), assessmentId: String(assessmentId) },
    });
  } catch (err: unknown) {
    createError.value = errorUtils.shortMessage(err);
  } finally {
    createLoading.value = false;
  }
}
</script>
