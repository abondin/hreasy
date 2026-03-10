<template>
  <v-form ref="employeeEditForm">
    <v-card class="admin-employee-form">
      <v-card-title class="d-flex align-center">
        <span>
          {{
            isNew
              ? t("Создание карточки сотрудника")
              : t("Изменение карточки сотрудника")
          }}
        </span>
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" @click="closeDialog" />
      </v-card-title>

      <v-card-text class="admin-employee-form__content">
        <v-row class="align-start">
          <v-col cols="12">
            <v-card class="pa-4 elevation-0 bg-transparent">
              <v-row class="admin-employee-form__grid">
                <v-col cols="12">
                  <v-text-field
                    v-model="employeeForm.displayName"
                    :counter="255"
                    :rules="displayNameRules"
                    :label="t('ФИО')"
                  />
                </v-col>
                <v-col cols="12" md="6" lg="4">
                  <v-text-field
                    v-model="employeeForm.email"
                    :counter="255"
                    :rules="emailRules"
                    :label="t('Email')"
                    required
                  />
                </v-col>
                <v-col cols="12" md="6" lg="4">
                  <v-text-field
                    v-model="employeeForm.phone"
                    :counter="12"
                    :rules="phoneRules"
                    :label="t('Телефон')"
                    hint="+71112223344"
                  />
                </v-col>
                <v-col cols="12" md="6" lg="4">
                  <v-text-field
                    v-model="employeeForm.telegram"
                    :counter="255"
                    :rules="string255Rules"
                    :label="t('Telegram')"
                  />
                </v-col>
              </v-row>
            </v-card>
          </v-col>

          <v-col cols="12" class="col-lg">
            <v-row class="admin-employee-form__grid">
              <v-col cols="12"><v-divider /></v-col>

              <v-col cols="12" md="6" lg="3">
                <v-autocomplete
                  v-model="employeeForm.organizationId"
                  :items="organizationsWithCurrent"
                  item-title="name"
                  item-value="id"
                  :label="t('Организация')"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-autocomplete
                  v-model="employeeForm.departmentId"
                  :items="departmentsWithCurrent"
                  item-title="name"
                  item-value="id"
                  :label="t('Подразделение')"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-autocomplete
                  v-model="employeeForm.currentProjectId"
                  :items="projectsWithCurrent"
                  item-title="name"
                  item-value="id"
                  :label="t('Текущий проект')"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-text-field
                  v-model="employeeForm.currentProjectRole"
                  :counter="64"
                  :rules="string64Rules"
                  :label="t('Роль на проекте')"
                />
              </v-col>

              <v-col cols="12"><v-divider /></v-col>

              <v-col cols="12" md="6" lg="3">
                <v-autocomplete
                  v-model="employeeForm.levelId"
                  :items="levelsWithCurrent"
                  item-title="name"
                  item-value="id"
                  :label="t('Уровень экспертизы')"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-autocomplete
                  v-model="employeeForm.positionId"
                  :items="positionsWithCurrent"
                  item-title="name"
                  item-value="id"
                  :label="t('Позиция')"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-autocomplete
                  v-model="employeeForm.officeLocationId"
                  :items="officeLocationsWithCurrent"
                  item-title="name"
                  item-value="id"
                  :label="t('Кабинет')"
                  clearable
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-text-field
                  v-model="employeeForm.officeWorkplace"
                  :counter="64"
                  :rules="string64Rules"
                  :label="t('Рабочее место')"
                />
              </v-col>

              <v-col cols="12"><v-divider /></v-col>

              <v-col cols="12" md="6" lg="3">
                <MyDateFormComponent
                  v-model="employeeForm.dateOfEmployment"
                  :label="t('Дата трудоустройства')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <MyDateFormComponent
                  v-model="employeeForm.dateOfDismissal"
                  :label="t('Дата увольнения')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <MyDateFormComponent
                  v-model="employeeForm.birthday"
                  :label="t('День рождения')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-text-field
                  v-model="employeeForm.sex"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Пол')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="6">
                <v-text-field
                  v-model="employeeForm.workType"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Место работы')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="6">
                <v-text-field
                  v-model="employeeForm.workDay"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Рабочий день (Полный/Неполный)')"
                />
              </v-col>

              <v-col cols="12"><v-divider /></v-col>

              <v-col cols="12" md="6" lg="3">
                <v-text-field
                  v-model="employeeForm.documentSeries"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Серия документа')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-text-field
                  v-model="employeeForm.documentNumber"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Номер документа')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <MyDateFormComponent
                  v-model="employeeForm.documentIssuedDate"
                  :label="t('Документ выдан (когда)')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="3">
                <v-text-field
                  v-model="employeeForm.documentIssuedBy"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Документ выдан (кем)')"
                />
              </v-col>

              <v-col cols="12"><v-divider /></v-col>

              <v-col cols="12" lg="6">
                <v-text-field
                  v-model="employeeForm.registrationAddress"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Адрес по регистрации')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="2">
                <v-text-field
                  v-model="employeeForm.cityOfResidence"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Город проживания')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="2">
                <v-text-field
                  v-model="employeeForm.foreignPassport"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Загранпаспорт')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="2">
                <v-text-field
                  v-model="employeeForm.englishLevel"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Уровень английского')"
                />
              </v-col>

              <v-col cols="12"><v-divider /></v-col>

              <v-col cols="12" md="6" lg="2">
                <v-text-field
                  v-model="employeeForm.familyStatus"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('Семейный статус')"
                />
              </v-col>
              <v-col cols="12" md="6" lg="4">
                <v-text-field
                  v-model="employeeForm.spouseName"
                  :counter="255"
                  :rules="string255Rules"
                  :label="t('ФИО супруга/супруги')"
                />
              </v-col>
              <v-col cols="12" lg="6">
                <v-text-field
                  v-model="employeeForm.children"
                  disabled
                  :counter="1024"
                  :rules="string1024Rules"
                  :label="t('Дети')"
                />
              </v-col>
            </v-row>
          </v-col>
        </v-row>

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
import { errorUtils } from "@/lib/errors";
import {
  createAdminEmployee,
  updateAdminEmployee,
  type CreateOrUpdateEmployeeBody,
  type EmployeeWithAllDetails,
} from "@/services/admin/admin-employee.service";
import type { DictItem } from "@/services/dict.service";
import type { ProjectDictDto } from "@/services/projects.service";

type Rule = (value: unknown) => boolean | string;

interface EmployeeFormState {
  id: number | null;
  displayName: string;
  currentProjectId: number | null;
  currentProjectRole: string;
  departmentId: number | null;
  organizationId: number | null;
  birthday: string;
  sex: string;
  email: string;
  phone: string;
  skype: string;
  telegram: string;
  dateOfEmployment: string;
  levelId: number | null;
  workType: string;
  workDay: string;
  registrationAddress: string;
  documentSeries: string;
  documentNumber: string;
  documentIssuedBy: string;
  documentIssuedDate: string;
  foreignPassport: string;
  cityOfResidence: string;
  englishLevel: string;
  familyStatus: string;
  spouseName: string;
  children: string;
  dateOfDismissal: string;
  positionId: number | null;
  officeLocationId: number | null;
  officeWorkplace: string;
}

const props = defineProps<{
  input?: EmployeeWithAllDetails | null;
  departments: DictItem[];
  organizations: DictItem[];
  projects: ProjectDictDto[];
  positions: DictItem[];
  levels: DictItem[];
  officeLocations: DictItem[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved"): void;
}>();

const { t } = useI18n();

const employeeEditForm = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null);
const saving = ref(false);
const error = ref<string | null>(null);

const employeeForm = reactive<EmployeeFormState>({
  id: null,
  displayName: "",
  currentProjectId: null,
  currentProjectRole: "",
  departmentId: null,
  organizationId: null,
  birthday: "",
  sex: "",
  email: "",
  phone: "",
  skype: "",
  telegram: "",
  dateOfEmployment: "",
  levelId: null,
  workType: t("Основной"),
  workDay: t("Полный"),
  registrationAddress: "",
  documentSeries: "",
  documentNumber: "",
  documentIssuedBy: "",
  documentIssuedDate: "",
  foreignPassport: "",
  cityOfResidence: "",
  englishLevel: "",
  familyStatus: "",
  spouseName: "",
  children: "",
  dateOfDismissal: "",
  positionId: null,
  officeLocationId: null,
  officeWorkplace: "",
});

const isNew = computed(() => !employeeForm.id);

const string255Rules = computed<Rule[]>(() => [maxLenRule(255)]);
const string64Rules = computed<Rule[]>(() => [maxLenRule(64)]);
const string1024Rules = computed<Rule[]>(() => [maxLenRule(1024)]);
const displayNameRules = computed<Rule[]>(() => [maxLenRule(255)]);
const emailRules = computed<Rule[]>(() => [requiredMaxLenRule(255)]);
const phoneRules = computed<Rule[]>(() => [maxLenRule(12)]);

const departmentsWithCurrent = computed(() =>
  withCurrent(props.departments, employeeForm.departmentId),
);
const organizationsWithCurrent = computed(() =>
  withCurrent(props.organizations, employeeForm.organizationId),
);
const projectsWithCurrent = computed(() =>
  withCurrent(props.projects, employeeForm.currentProjectId),
);
const positionsWithCurrent = computed(() =>
  withCurrent(props.positions, employeeForm.positionId),
);
const levelsWithCurrent = computed(() =>
  withCurrent(props.levels, employeeForm.levelId),
);
const officeLocationsWithCurrent = computed(() =>
  withCurrent(props.officeLocations, employeeForm.officeLocationId),
);

watch(
  () => props.input,
  (value) => {
    reset(value ?? undefined);
  },
  { immediate: true },
);

function reset(input?: EmployeeWithAllDetails) {
  error.value = null;
  employeeForm.id = input?.id ?? null;
  employeeForm.displayName = input?.displayName ?? "";
  employeeForm.currentProjectId = input?.currentProjectId ?? null;
  employeeForm.currentProjectRole = input?.currentProjectRole ?? "";
  employeeForm.departmentId = input?.departmentId ?? null;
  employeeForm.organizationId = input?.organizationId ?? null;
  employeeForm.birthday = input?.birthday ?? "";
  employeeForm.sex = input?.sex ?? "";
  employeeForm.email = input?.email ?? "";
  employeeForm.phone = input?.phone ?? "";
  employeeForm.skype = input?.skype ?? "";
  employeeForm.telegram = input?.telegram ?? "";
  employeeForm.dateOfEmployment = input?.dateOfEmployment ?? "";
  employeeForm.levelId = input?.levelId ?? null;
  employeeForm.workType = input?.workType ?? t("Основной");
  employeeForm.workDay = input?.workDay ?? t("Полный");
  employeeForm.registrationAddress = input?.registrationAddress ?? "";
  employeeForm.documentSeries = input?.documentSeries ?? "";
  employeeForm.documentNumber = input?.documentNumber ?? "";
  employeeForm.documentIssuedBy = input?.documentIssuedBy ?? "";
  employeeForm.documentIssuedDate = input?.documentIssuedDate ?? "";
  employeeForm.foreignPassport = input?.foreignPassport ?? "";
  employeeForm.cityOfResidence = input?.cityOfResidence ?? "";
  employeeForm.englishLevel = input?.englishLevel ?? "";
  employeeForm.familyStatus = input?.familyStatus ?? "";
  employeeForm.spouseName = input?.spouseName ?? "";
  employeeForm.children = input?.children ?? "";
  employeeForm.dateOfDismissal = input?.dateOfDismissal ?? "";
  employeeForm.positionId = input?.positionId ?? null;
  employeeForm.officeLocationId = input?.officeLocationId ?? null;
  employeeForm.officeWorkplace = input?.officeWorkplace ?? "";
}

function withCurrent<T extends { id: number; active?: boolean | null }>(
  items: T[],
  currentValueId: number | null,
): T[] {
  return items.filter((item) =>
    item.active === false ? item.id === currentValueId : true,
  );
}

function maxLenRule(max: number): Rule {
  return (value) => {
    if (value == null || value === "") {
      return true;
    }
    if (typeof value !== "string") {
      return true;
    }
    return value.length <= max || t("Не более N символов", { n: max });
  };
}

function requiredMaxLenRule(max: number): Rule {
  return (value) => {
    if (typeof value !== "string" || !value.trim()) {
      return t("Обязательное поле. Не более N символов", { n: max });
    }
    return value.length <= max || t("Обязательное поле. Не более N символов", { n: max });
  };
}

function normalizeString(value: string): string | null {
  const normalized = value.trim();
  return normalized ? normalized : null;
}

function toPayload(): CreateOrUpdateEmployeeBody {
  return {
    displayName: employeeForm.displayName.trim(),
    currentProjectId: employeeForm.currentProjectId,
    currentProjectRole: normalizeString(employeeForm.currentProjectRole),
    departmentId: employeeForm.departmentId,
    organizationId: employeeForm.organizationId,
    birthday: normalizeString(employeeForm.birthday),
    sex: normalizeString(employeeForm.sex),
    email: normalizeString(employeeForm.email) ?? "",
    phone: normalizeString(employeeForm.phone),
    skype: normalizeString(employeeForm.skype),
    telegram: normalizeString(employeeForm.telegram),
    dateOfEmployment: normalizeString(employeeForm.dateOfEmployment),
    levelId: employeeForm.levelId,
    workType: normalizeString(employeeForm.workType),
    workDay: normalizeString(employeeForm.workDay),
    registrationAddress: normalizeString(employeeForm.registrationAddress),
    documentSeries: normalizeString(employeeForm.documentSeries),
    documentNumber: normalizeString(employeeForm.documentNumber),
    documentIssuedBy: normalizeString(employeeForm.documentIssuedBy),
    documentIssuedDate: normalizeString(employeeForm.documentIssuedDate),
    foreignPassport: normalizeString(employeeForm.foreignPassport),
    cityOfResidence: normalizeString(employeeForm.cityOfResidence),
    englishLevel: normalizeString(employeeForm.englishLevel),
    familyStatus: normalizeString(employeeForm.familyStatus),
    spouseName: normalizeString(employeeForm.spouseName),
    children: normalizeString(employeeForm.children),
    dateOfDismissal: normalizeString(employeeForm.dateOfDismissal),
    positionId: employeeForm.positionId,
    officeLocationId: employeeForm.officeLocationId,
    officeWorkplace: normalizeString(employeeForm.officeWorkplace),
  };
}

async function submit() {
  if (!employeeEditForm.value) {
    return;
  }
  const validationResult = await employeeEditForm.value.validate();
  if (!validationResult.valid) {
    return;
  }

  error.value = null;
  saving.value = true;
  try {
    const payload = toPayload();
    if (isNew.value) {
      await createAdminEmployee(payload);
    } else if (employeeForm.id) {
      await updateAdminEmployee(employeeForm.id, payload);
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

<style scoped>
.admin-employee-form__content {
  max-height: 72vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.admin-employee-form__grid {
  row-gap: 6px;
}

.admin-employee-form :deep(.v-field) {
  --v-input-control-height: 42px;
}

.admin-employee-form :deep(.v-card-actions) {
  border-top: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}
</style>
