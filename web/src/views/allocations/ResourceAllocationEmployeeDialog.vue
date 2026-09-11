<template>
  <v-dialog
    :model-value="true"
    max-width="1040"
    scrollable
    @update:model-value="!$event && emit('close')"
  >
    <v-card data-testid="resource-allocation-employee-dialog">
      <v-toolbar density="comfortable">
        <v-toolbar-title>{{ t("Карточка сотрудника") }}</v-toolbar-title>
        <v-btn
          icon="mdi-close"
          :aria-label="t('Закрыть')"
          @click="emit('close')"
        />
      </v-toolbar>
      <v-card-text>
        <v-progress-linear v-if="loading" indeterminate :aria-label="t('Загрузка_данных')" />
        <v-alert v-else-if="error" type="error">{{ errorUtils.shortMessage(error) }}</v-alert>
        <EmployeeDetailsPanel v-else-if="employee" :employee="employee" @employee-updated="reload" />
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useEmployeeProfile } from "@/composables/useEmployeeProfile";
import { errorUtils } from "@/lib/errors";
import EmployeeDetailsPanel from "@/views/employees/components/EmployeeDetailsPanel.vue";

// The parent keys this component by employee ID, isolating pending profile requests.
const props = defineProps<{ employeeId: number }>();
const emit = defineEmits<{ (event: "close"): void }>();
const { t } = useI18n();
const { employee, loading, error, reload } = useEmployeeProfile(() => props.employeeId);
</script>
