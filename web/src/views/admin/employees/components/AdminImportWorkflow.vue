<template>
  <v-card class="mt-4">
    <v-card-title>{{ title }}</v-card-title>
    <v-card-text>
      <v-stepper v-model="step" :items="[t('Загрузка файла'), t('Конфигурация'), t('Предпросмотр')]" />

      <div v-if="step === 1" class="mt-4">
        <v-file-input v-model="file" :label="t('Загрузка файла')" show-size />
        <v-btn color="primary" :loading="loading" :disabled="!file" @click="upload">{{ t("Далее") }}</v-btn>
      </div>

      <div v-else-if="step === 2" class="mt-4">
        <v-row>
          <v-col cols="6"><v-text-field v-model.number="localConfig.sheetNumber" :label="t('Номер листа')" type="number" /></v-col>
          <v-col cols="6"><v-text-field v-model.number="localConfig.tableStartRow" :label="t('Строка начала таблицы')" type="number" /></v-col>
        </v-row>
        <v-btn variant="text" @click="step = 1">{{ t("Назад") }}</v-btn>
        <v-btn color="primary" :loading="loading" @click="preview">{{ t("Далее") }}</v-btn>
      </div>

      <div v-else class="mt-4">
        <v-alert type="info" variant="tonal" class="mb-2">
          {{ t("Новых сотрудников") }}: {{ workflow?.importProcessStats?.newItems ?? 0 }};
          {{ t("Количество обновлений") }}: {{ workflow?.importProcessStats?.updatedItems ?? 0 }};
          {{ t("Ошибки") }}: {{ workflow?.importProcessStats?.errors ?? 0 }}
        </v-alert>
        <v-table density="compact">
          <thead><tr><th>#</th><th>{{ t("Количество ошибок") }}</th></tr></thead>
          <tbody>
            <tr v-for="(row, idx) in previewRows" :key="idx">
              <td>{{ idx + 1 }}</td>
              <td>{{ row.errorCount }}</td>
            </tr>
          </tbody>
        </v-table>
        <v-btn variant="text" @click="step = 2">{{ t("Назад") }}</v-btn>
        <v-btn color="primary" :loading="loading" @click="commit">{{ t("Применить") }}</v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts" generic="C extends ImportConfig, R extends ImportExcelRow">
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import type {
  ImportConfig,
  ImportExcelRow,
  ImportService,
  ImportWorkflow,
} from "@/services/admin/import/import.base";

const props = defineProps<{ title: string; service: ImportService<C, R>; config: C }>();
const { t } = useI18n();
const step = ref(1);
const loading = ref(false);
const file = ref<File | null>(null);
const workflow = ref<ImportWorkflow<C, R> | null>(null);
const localConfig = ref<C>(structuredClone(props.config));
const previewRows = computed(() => workflow.value?.importedRows ?? []);

async function init(): Promise<void> {
  loading.value = true;
  const process = await props.service.getActiveOrStartNewImportProcess();
  workflow.value = process;
  if (process.config) {
    localConfig.value = structuredClone(process.config);
  }
  step.value = Math.max(1, process.state + 1);
  loading.value = false;
}

async function upload(): Promise<void> {
  if (!workflow.value || !file.value) return;
  loading.value = true;
  await props.service.uploadFile(workflow.value.id, file.value);
  step.value = 2;
  loading.value = false;
}

async function preview(): Promise<void> {
  if (!workflow.value) return;
  loading.value = true;
  workflow.value = await props.service.applyConfigAndPreview(workflow.value.id, localConfig.value);
  step.value = 3;
  loading.value = false;
}

async function commit(): Promise<void> {
  if (!workflow.value) return;
  loading.value = true;
  workflow.value = await props.service.commit(workflow.value.id);
  loading.value = false;
}

onMounted(() => {
  init().catch(() => undefined);
});
</script>
