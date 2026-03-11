<template>
  <v-autocomplete
    v-if="mode === 'add'"
    v-model="addForm.juniorEmplId"
    :items="employees"
    item-title="displayName"
    item-value="id"
    :label="t('Молодой специалист')"
  />
  <v-autocomplete
    v-model="form.mentorId"
    clearable
    :items="mentorOptions"
    :item-title="mentorTitle"
    item-value="id"
    :label="t('Ментор')"
  />
  <v-autocomplete
    v-model="form.budgetingAccount"
    clearable
    :items="businessAccountOptions"
    :item-title="businessAccountTitle"
    item-value="id"
    :label="t('Бюджет из бизнес аккаунта')"
  />
  <v-combobox
    v-model="form.role"
    :items="roleOptions"
    item-title="value"
    item-value="value"
    clearable
    :label="t('Роль')"
  />
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Employee } from "@/services/employee.service";
import {
  dictOptionTitle,
  withArchivedOptionById,
  withCurrentOptionById,
} from "@/lib/dict-options";
import type {
  AddJuniorRegistryBody,
  CurrentProjectRole,
  SimpleDict,
  UpdateJuniorRegistryBody,
} from "@/services/junior-registry.service";

const { t } = useI18n();
const props = defineProps<{
  form: AddJuniorRegistryBody | UpdateJuniorRegistryBody;
  mode: "add" | "update";
  employees: Employee[];
  businessAccounts: SimpleDict[];
  projectRoles: CurrentProjectRole[];
  currentMentor?: SimpleDict | null;
  currentBudgetingAccount?: SimpleDict | null;
  currentRole?: string | null;
}>();

const addForm = computed(() => props.form as AddJuniorRegistryBody);

type DictLike = { id: number; name?: string | null; value?: string | null };
type MentorLike = { id: number; displayName?: string | null; name?: string | null };

const mentorOptions = computed<MentorLike[]>(() => {
  return withCurrentOptionById<MentorLike>(props.employees, props.currentMentor
    ? ({
      id: props.currentMentor.id,
      displayName: props.currentMentor.name,
      name: props.currentMentor.name,
    } as Employee)
    : null);
});

const businessAccountOptions = computed<DictLike[]>(() => {
  const withCurrent = withCurrentOptionById(props.businessAccounts, props.currentBudgetingAccount);
  return withArchivedOptionById(withCurrent, props.form.budgetingAccount, (id) => ({
    id,
    name: `${t("Архив")} #${id}`,
  }));
});

const roleOptions = computed<CurrentProjectRole[]>(() => {
  const options = [...props.projectRoles];
  const currentRole = props.currentRole?.trim();
  if (!currentRole) {
    return options;
  }
  const hasSelected = options.some((item) => item.value === currentRole);
  if (!hasSelected) {
    options.push({ value: currentRole });
  }
  return options;
});

function mentorTitle(item: unknown): string {
  return dictOptionTitle(item);
}

function businessAccountTitle(item: unknown): string {
  return dictOptionTitle(item);
}
</script>
