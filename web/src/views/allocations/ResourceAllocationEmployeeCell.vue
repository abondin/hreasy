<template>
  <v-autocomplete
    v-if="model.addEmployee"
    :items="model.employeesAvailableToAdd"
    item-title="displayName"
    item-value="id"
    hide-details
    density="compact"
    variant="plain"
    :placeholder="t('Добавить сотрудника')"
    :aria-label="t('Добавить сотрудника')"
    data-testid="resource-allocations-add-employee"
    @keydown.stop
    @keyup.stop
    @update:model-value="onAddEmployee"
  />
  <div v-else class="d-flex align-center ga-2 h-100 overflow-hidden">
    <span class="text-truncate">{{ model.employee }}</span>
    <v-chip
      v-if="model.dismissedLabel"
      color="error"
      size="x-small"
      label
    >
      {{ model.dismissedLabel }}
    </v-chip>
    <span
      v-else-if="model.otherProject"
      class="text-caption text-medium-emphasis text-truncate"
    >
      {{ model.otherProject }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { ResourceAllocationInputEmployee } from "@/services/resource-allocation.service";

defineOptions({ inheritAttrs: false });

interface EmployeeCellModel {
  employee: string;
  dismissedLabel?: string;
  otherProject?: string;
  addEmployee?: boolean;
  employeesAvailableToAdd?: ResourceAllocationInputEmployee[];
}

const props = defineProps<{
  model: EmployeeCellModel;
  addition?: {
    addEmployee: (employeeId: number | null) => void;
  };
}>();
const { t } = useI18n();

function onAddEmployee(employeeId: number | null): void {
  props.addition?.addEmployee(employeeId);
}
</script>
