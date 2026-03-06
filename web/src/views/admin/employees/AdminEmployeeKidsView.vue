<template>
  <v-card class="mt-4">
    <v-card-title class="d-flex ga-2 align-center">
      <v-btn icon="mdi-refresh" variant="text" :loading="loading" @click="load" />
      <v-btn icon="mdi-plus" color="primary" variant="text" :disabled="loading" @click="openCreate" />
      <v-text-field v-model="search" density="compact" :label="t('Поиск')" hide-details class="ml-2" />
    </v-card-title>

    <v-data-table
      :headers="headers"
      :items="filteredItems"
      :loading="loading"
      :loading-text="t('Загрузка_данных')"
      @click:row="onClickRow"
    />

    <v-dialog v-model="dialog" max-width="760">
      <v-card>
        <v-card-title>{{ current?.id ? t("Изменение карточки ребёнка сотрудника") : t("Создание карточки ребёнка сотрудника") }}</v-card-title>
        <v-card-text>
          <v-autocomplete
            v-model="employeeId"
            :items="employees"
            item-title="displayName"
            item-value="id"
            :label="t('Сотрудник')"
          />
          <v-text-field v-model="form.displayName" :label="t('ФИО')" />
          <v-text-field v-model="form.birthday" :label="t('День рождения')" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="dialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="saving" @click="save">{{ t("Сохранить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { useI18n } from "vue-i18n";
import { listEmployees, type Employee } from "@/services/employee.service";
import {
  createEmployeeKid,
  listEmployeeKids,
  updateEmployeeKid,
  type CreateOrUpdateEmployeeKidBody,
  type EmployeeKid,
} from "@/services/admin/admin-employee.service";

const { t } = useI18n();
const loading = ref(false);
const saving = ref(false);
const dialog = ref(false);
const search = ref("");
const kids = ref<EmployeeKid[]>([]);
const employees = ref<Employee[]>([]);
const current = ref<EmployeeKid | null>(null);
const employeeId = ref<number | null>(null);

const form = reactive<CreateOrUpdateEmployeeKidBody>({
  displayName: "",
  birthday: "",
});

const headers = computed(() => [
  { title: t("ФИО"), key: "displayName" },
  { title: t("День рождения"), key: "birthday" },
  { title: t("Сотрудник"), key: "parent.name" },
]);

const filteredItems = computed(() => {
  const q = search.value.trim().toLowerCase();
  return kids.value.filter((it) => {
    if (!q) return true;
    return [it.displayName, it.parent?.name].filter(Boolean).join(" ").toLowerCase().includes(q);
  });
});

function fillForm(item?: EmployeeKid): void {
  form.displayName = item?.displayName ?? "";
  form.birthday = item?.birthday ?? "";
  employeeId.value = item?.parent?.id ?? null;
}

function onClickRow(_: unknown, row: { item: EmployeeKid }): void {
  current.value = row.item;
  fillForm(row.item);
  dialog.value = true;
}

function openCreate(): void {
  current.value = null;
  fillForm();
  dialog.value = true;
}

async function load(): Promise<void> {
  loading.value = true;
  const [allKids, allEmployees] = await Promise.all([listEmployeeKids(), listEmployees()]);
  kids.value = allKids;
  employees.value = allEmployees;
  loading.value = false;
}

async function save(): Promise<void> {
  if (!employeeId.value) return;
  saving.value = true;
  if (current.value) {
    await updateEmployeeKid(employeeId.value, current.value.id, form);
  } else {
    await createEmployeeKid(employeeId.value, form);
  }
  dialog.value = false;
  await load();
  saving.value = false;
}

load().catch(() => undefined);
</script>
