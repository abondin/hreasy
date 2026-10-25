<template>
  <v-navigation-drawer
    permanent
    :location="display.smAndDown.value ? 'bottom' : 'right'"
    :width="display.smAndDown.value ? 320 : 480"
    :scrim="false"
    data-testid="office-maps-sidebar"
  >
    <template #prepend>
      <div class="pa-4">
        <div class="text-h6">{{ t("Сотрудники") }}</div>
        <div class="text-body-2 text-medium-emphasis mb-3" data-testid="office-maps-scope">{{ scope }}</div>
        <SearchTextField
          v-model="search"
          v-model:settings="searchSettings"
          :label="t('Поиск')"
          :disabled="loading"
          test-id="office-maps-search"
        />
        <v-chip v-if="workplace" closable class="mt-3" @click:close="workplace = null">
          {{ t("Рабочее место") }}: {{ workplace }}
        </v-chip>
      </div>
      <v-divider />
    </template>
    <v-progress-linear v-if="loading" indeterminate />
    <v-list v-else-if="!error" lines="three">
      <v-hover
        v-for="employee in filteredEmployees"
        :key="employee.id"
        v-slot="{ isHovering, props: hoverProps }"
      >
      <v-list-item
        v-bind="hoverProps"
        :active="modelValue?.id === employee.id"
        color="primary"
        :data-testid="'office-maps-employee-' + employee.id"
        @click="emit('update:modelValue', employee)"
      >
        <v-list-item-title class="text-wrap">{{ employee.displayName }}</v-list-item-title>
        <div class="text-body-2 text-medium-emphasis">{{ employee.department?.name }}</div>
        <div class="text-body-2">
          {{ employee.officeLocation?.name ?? t("Кабинет не указан") }}
          · {{ employee.officeWorkplace?.trim() || t("Рабочее место не указано") }}
        </div>
        <template #append>
          <v-btn
            icon="mdi-account-details-outline"
            variant="text"
            size="small"
            :class="{ 'opacity-0': display.lgAndUp.value && !isHovering && focusedEmployeeId !== employee.id }"
            :aria-label="t('Карточка сотрудника')"
            :title="t('Карточка сотрудника')"
            @click.stop="detailsEmployee = employee"
            @focus="focusedEmployeeId = employee.id"
            @blur="focusedEmployeeId = null"
          />
        </template>
      </v-list-item>
      </v-hover>
      <v-list-item v-if="!filteredEmployees.length" :title="emptyMessage" />
    </v-list>
  </v-navigation-drawer>

  <v-dialog v-model="detailsOpen" max-width="720" :fullscreen="display.smAndDown.value" scrollable>
    <v-card v-if="detailsEmployee">
      <v-card-item :title="t('Карточка сотрудника')">
        <template #append>
          <v-btn icon="mdi-close" variant="text" :aria-label="t('Закрыть')" @click="detailsOpen = false" />
        </template>
      </v-card-item>
      <v-card-text>
        <OfficeMapEmployeeCard :key="detailsEmployee.id" :employee-id="detailsEmployee.id" />
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify";
import type { Employee } from "@/services/employee.service";
import { createSearchSettings, matchesSearch } from "@/lib/search";
import SearchTextField from "@/components/shared/SearchTextField.vue";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";

const props = defineProps<{ modelValue: Employee | null; employees: Employee[]; loading: boolean; scope: string; error: string }>();
const emit = defineEmits<{ "update:modelValue": [employee: Employee] }>();
const search = defineModel<string>("search", { default: "" });
const workplace = defineModel<string | null>("workplace", { default: null });
const { t } = useI18n();
const display = useDisplay();
const searchSettings = ref(createSearchSettings());
const focusedEmployeeId = ref<number | null>(null);
const detailsEmployee = ref<Employee | null>(null);
const detailsOpen = computed({
  get: () => detailsEmployee.value !== null,
  set: (open: boolean) => { if (!open) detailsEmployee.value = null; },
});
const filteredEmployees = computed(() => props.employees.filter((employee) =>
  matchesSearch(search.value, [
    employee.displayName, employee.email, employee.department?.name,
    employee.officeLocation?.name, employee.officeWorkplace,
    employee.currentProject?.name, employee.currentProject?.role,
    employee.ba?.name, employee.position?.name,
  ], searchSettings.value),
));
const emptyMessage = computed(() => {
  if (search.value.trim()) return t('По вашему запросу сотрудники не найдены');
  if (workplace.value) return t('За этим местом сотрудники не закреплены');
  return t('В выбранной области сотрудников нет');
});
</script>
