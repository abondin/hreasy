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
    :items="employees"
    item-title="displayName"
    item-value="id"
    :label="t('Ментор')"
  />
  <v-autocomplete
    v-model="form.budgetingAccount"
    clearable
    :items="businessAccounts"
    item-title="name"
    item-value="id"
    :label="t('Бюджет из бизнес аккаунта')"
  />
  <v-combobox
    v-model="form.role"
    :items="projectRoles"
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
}>();

const addForm = computed(() => props.form as AddJuniorRegistryBody);
</script>
