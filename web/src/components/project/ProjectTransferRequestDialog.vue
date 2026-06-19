<!--
  Dialog with active current project transfer request details.
-->
<template>
  <v-dialog v-model="dialogOpen" max-width="560" scrollable data-testid="project-transfer-request-dialog">
    <v-card>
      <v-card-item>
        <template #title>{{ t("Заявка на перевод") }}</template>
        <template #subtitle>{{ employeeName }}</template>
      </v-card-item>

      <v-card-text>
        <v-alert
          type="info"
          variant="tonal"
          border="start"
          class="mb-4"
        >
          {{ t("Сотрудник уже планируется к переводу на другой проект.") }}
        </v-alert>

        <v-list v-if="request" density="compact">
          <v-list-item :title="t('Текущий проект')" :subtitle="request.fromProjectName" />
          <v-list-item :title="t('Новый проект')" :subtitle="request.toProjectName" />
          <v-list-item :title="t('Роль')" :subtitle="request.requestedProjectRole || t('Не задана')" />
          <v-list-item :title="t('Автор заявки')" :subtitle="request.createdByDisplayName" />
          <v-list-item :title="t('Согласующий')" :subtitle="request.approverDisplayName" />
          <v-list-item :title="t('Создана')" :subtitle="formatDateTime(request.createdAt)" />
        </v-list>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" data-testid="project-transfer-request-close" @click="dialogOpen = false">
          {{ t("Закрыть") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import type { CurrentProjectTransferRequest } from "@/services/employee.service";

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    employeeName: string;
    request: CurrentProjectTransferRequest | null;
  }>(),
  {
    modelValue: false,
    request: null,
  },
);

const emit = defineEmits<{
  (event: "update:modelValue", value: boolean): void;
}>();

const { t } = useI18n();

const dialogOpen = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});
</script>
