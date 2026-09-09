<template>
  <v-autocomplete
    v-if="model.addEmployee"
    :items="model.employeesAvailableToAdd"
    :disabled="addition?.disabled"
    item-title="displayName"
    item-value="id"
    :custom-filter="employeeFilter"
    hide-details
    density="compact"
    variant="plain"
    :placeholder="t('Добавить сотрудника')"
    :aria-label="t('Добавить сотрудника')"
    data-testid="resource-allocations-add-employee"
    @keydown.stop
    @keyup.stop
    @update:model-value="onAddEmployee"
  >
    <template #item="{ props: itemProps, item }">
      <v-list-item
        v-bind="itemProps"
        :title="item.displayName"
        :subtitle="employeeSubtitle(item)"
      />
    </template>
  </v-autocomplete>
  <div v-else class="d-flex align-center ga-2 h-100 overflow-hidden">
    <v-btn
      v-if="addition?.openEmployee"
      icon
      size="24"
      variant="text"
      class="flex-shrink-0 resource-allocation-employee-details-button"
      :title="t('Открыть карточку сотрудника')"
      :aria-label="t('Открыть карточку сотрудника')"
      :data-testid="`resource-allocation-open-employee-${model.id}`"
      @pointerdown.stop
      @mousedown.stop
      @touchstart.stop
      @keydown.stop
      @keyup.stop
      @dblclick.stop
      @click.stop="addition.openEmployee(model.id)"
    >
      <v-icon icon="mdi-information-outline" size="14" />
    </v-btn>
    <span class="text-truncate">{{ model.employee }}</span>
    <span
      v-if="model.projectRole"
      class="text-caption text-medium-emphasis text-truncate"
    >
      {{ model.projectRole }}
    </span>
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
import { formatDate } from "@/lib/datetime";
import { matchesSearch } from "@/lib/search";
import type { ResourceAllocationInputEmployee } from "@/services/resource-allocation.service";

defineOptions({ inheritAttrs: false });

interface EmployeeCellModel {
  id: number;
  employee: string;
  dismissedLabel?: string;
  otherProject?: string;
  projectRole?: string;
  addEmployee?: boolean;
  employeesAvailableToAdd?: ResourceAllocationInputEmployee[];
}

const props = defineProps<{
  model: EmployeeCellModel;
  addition?: {
    disabled?: boolean;
    addEmployee: (employeeId: number | null) => void;
    openEmployee?: (employeeId: number) => void;
  };
}>();
const { t } = useI18n();

function onAddEmployee(employeeId: number | null): void {
  props.addition?.addEmployee(employeeId);
}

function employeeFilter(_value: string, query: string, item?: unknown): boolean {
  const employee = (item as { raw?: ResourceAllocationInputEmployee } | undefined)?.raw;
  return matchesSearch(query, [
    employee?.displayName,
    employee?.email,
    employee?.currentProjectName,
    employee?.currentProjectRole,
  ]);
}

function employeeSubtitle(employee: ResourceAllocationInputEmployee): string | undefined {
  const details = [
    employee.currentProjectName
      ? t("Текущий проект: {project}", { project: employee.currentProjectName })
      : null,
    employee.currentProjectRole
      ? t("Роль: {role}", { role: employee.currentProjectRole })
      : null,
    employee.dateOfDismissal
      ? t("Дата увольнения: {date}", { date: formatDate(employee.dateOfDismissal) })
      : null,
  ].filter(Boolean);
  return details.length ? details.join(" · ") : undefined;
}
</script>
