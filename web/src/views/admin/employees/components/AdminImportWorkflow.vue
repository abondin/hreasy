<template>
  <v-card class="mt-4">
    <v-card-title>{{ title }}</v-card-title>
    <v-card-text>
      <v-skeleton-loader v-if="loading && !workflow" type="card" />

      <template v-else-if="workflow">
        <v-alert v-if="error" type="error" variant="tonal" border="start" class="mb-4">
          {{ error }}
        </v-alert>

        <v-alert
          v-if="workflow.filename"
          type="info"
          variant="tonal"
          border="start"
          class="mb-4"
        >
          <div class="d-flex flex-wrap align-center ga-3">
            <span>{{ t("Выбранный файл") }}: <strong>{{ workflow.filename }}</strong></span>
            <v-spacer />
            <v-btn
              color="warning"
              variant="outlined"
              size="small"
              :loading="loading"
              @click="abortDialog = true"
            >
              {{ t("Выбрать другой файл") }}
            </v-btn>
          </div>
        </v-alert>

        <v-stepper
          :model-value="displayStep"
          :items="[t('Загрузка файла'), t('Конфигурация'), t('Предпросмотр')]"
          hide-actions
          @update:model-value="onStepperChange"
        />

        <div v-if="step === 1" class="mt-4">
          <FileUploadZone
            :post-action="uploadUrl"
            :file-id="`import-${workflow.id}`"
            accept=".xlsx,.xls,.csv"
            @close="onFileUploadClose"
          />
        </div>

        <div v-else-if="step === 2" class="mt-4">
          <v-card variant="outlined" class="pa-4 mb-4">
            <div class="text-subtitle-2 mb-3">{{ t("Общие настройки") }}</div>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="localConfig.sheetNumber"
                  :label="t('Порядковый номер листа в документе')"
                  :rules="positiveNumberRules"
                  type="number"
                  min="1"
                  variant="outlined"
                >
                  <template #append-inner>
                    <v-tooltip location="top">
                      <template #activator="{ props: tooltipProps }">
                        <v-icon v-bind="tooltipProps" icon="mdi-help-circle-outline" size="18" />
                      </template>
                      <span>{{ t("Если в документе один лист - укажите цифру 1") }}</span>
                    </v-tooltip>
                  </template>
                </v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="localConfig.tableStartRow"
                  :label="t('Порядковый номер первой строки с данными')"
                  :rules="positiveNumberRules"
                  type="number"
                  min="1"
                  variant="outlined"
                >
                  <template #append-inner>
                    <v-tooltip location="top" max-width="650">
                      <template #activator="{ props: tooltipProps }">
                        <v-icon v-bind="tooltipProps" icon="mdi-help-circle-outline" size="18" />
                      </template>
                      <v-img :src="tableStartRowImage" max-width="620" />
                    </v-tooltip>
                  </template>
                </v-text-field>
              </v-col>
            </v-row>
          </v-card>

          <v-card variant="outlined" class="pa-4">
            <div class="d-flex align-center mb-3">
              <div class="text-subtitle-2">
                {{ t("Названия (английские буквы) или порядковые номера столбцов") }}
              </div>
              <v-tooltip location="top">
                <template #activator="{ props: tooltipProps }">
                  <v-icon v-bind="tooltipProps" icon="mdi-help-circle-outline" size="18" class="ml-2" />
                </template>
                <div class="d-flex flex-column ga-2">
                  <v-img :src="emailColumnImage" max-width="620" />
                  <v-img :src="emailColumnAltImage" max-width="620" />
                </div>
              </v-tooltip>
            </div>
            <v-row>
              <v-col
                v-for="column in configColumns"
                :key="column"
                cols="12"
                md="6"
                lg="4"
              >
                <v-text-field
                  v-model="localConfig.columns[column]"
                  :label="t(`IMPORT_CONFIG.${column}`)"
                  :rules="columnRules"
                  variant="outlined"
                  clearable
                  maxlength="3"
                  @blur="normalizeColumn(column)"
                />
              </v-col>
            </v-row>
          </v-card>
        </div>

        <div v-else-if="step === 3" class="mt-4">
          <v-row dense class="mb-2">
            <v-col cols="12" md="3">
              <v-chip label>
                {{ t("Количество обработанных строк") }}:
                {{ workflow.importProcessStats?.processedRows ?? 0 }}
              </v-chip>
            </v-col>
            <v-col cols="12" md="3">
              <v-chip label :color="(workflow.importProcessStats?.errors ?? 0) > 0 ? 'error' : undefined">
                {{ t("Количество ошибок") }}: {{ workflow.importProcessStats?.errors ?? 0 }}
              </v-chip>
            </v-col>
            <v-col cols="12" md="3">
              <v-chip label>
                {{ statsLabels.newItems }}: {{ workflow.importProcessStats?.newItems ?? 0 }}
              </v-chip>
            </v-col>
            <v-col cols="12" md="3">
              <v-chip label>
                {{ statsLabels.updatedItems }}: {{ workflow.importProcessStats?.updatedItems ?? 0 }}
              </v-chip>
            </v-col>
          </v-row>

          <v-row dense class="mb-3">
            <v-col cols="12" md="8">
              <v-text-field
                v-model="previewFilter.search"
                append-inner-icon="mdi-magnify"
                :label="t('Поиск')"
                variant="outlined"
                density="compact"
                hide-details
                clearable
              />
            </v-col>
            <v-col cols="12" md="4">
              <v-checkbox
                v-model="previewFilter.hideNotUpdatedWithoutErrors"
                :label="t('Скрыть строки без изменений и без ошибок')"
                density="compact"
                hide-details
              />
            </v-col>
          </v-row>

          <HREasyTableBase
            :headers="previewHeaders"
            :items="filteredPreviewRows"
            :item-key="'rowNumber'"
            height="52vh"
            :fixed-header="true"
            density="compact"
            :no-data-text="t('Отсутствуют данные')"
            table-class="text-truncate"
          >
            <template #[`item.rowNumber`]="{ item }">
              <div class="d-flex align-center ga-2">
                <span>{{ item.rowNumber }}</span>
                <v-chip
                  v-if="item.errorCount > 0"
                  size="x-small"
                  color="error"
                  label
                >
                  <v-icon icon="mdi-flash-alert" size="12" class="mr-1" />
                  {{ item.errorCount }}
                </v-chip>
              </div>
            </template>

            <template #[`item.email`]="{ item }">
              <span :class="{ 'import-new': item.new }">{{ item.email ?? "-" }}</span>
            </template>

            <template
              v-for="header in formattedPreviewHeaders"
              :key="header.key"
              #[`item.${header.key}`]="{ item }"
            >
              <div class="text-truncate">
                <template v-if="isExcelCell(getField(item, header.key))">
                  <span
                    v-if="getExcelCell(getField(item, header.key)).currentValue != null && getExcelCell(getField(item, header.key)).updated"
                    class="import-old mr-1"
                  >
                    {{ formatCellValue(getExcelCell(getField(item, header.key)).currentValue, header.format) }}
                  </span>
                  <span :class="getExcelCell(getField(item, header.key)).updated ? 'import-new' : 'import-same'">
                    {{ formatCellValue(getExcelCell(getField(item, header.key)).importedValue, header.format) }}
                  </span>
                  <v-tooltip v-if="getExcelCell(getField(item, header.key)).error" location="top">
                    <template #activator="{ props: tooltipProps }">
                      <v-icon v-bind="tooltipProps" icon="mdi-alert" size="16" color="error" class="ml-1" />
                    </template>
                    <span>{{ getExcelCell(getField(item, header.key)).error }}</span>
                  </v-tooltip>
                </template>
                <template v-else>
                  {{ getField(item, header.key) ?? "-" }}
                </template>
              </div>
            </template>
          </HREasyTableBase>
        </div>

        <div v-else class="mt-4">
          <v-alert type="success" variant="tonal" border="start">
            {{ t("Изменения успешно применились!") }}
          </v-alert>
        </div>

        <v-card-actions class="px-0 pt-4">
          <v-btn
            v-if="step > 1 && step < 4"
            variant="text"
            :disabled="loading"
            @click="goBack"
          >
            {{ t("Назад") }}
          </v-btn>
          <v-spacer />
          <v-btn
            v-if="step > 1 || workflow.state > 0"
            color="primary"
            :loading="loading"
            :disabled="actionButtonDisabled"
            @click="applyStep"
          >
            {{ actionButtonText }}
          </v-btn>
        </v-card-actions>
      </template>
    </v-card-text>
  </v-card>

  <v-dialog v-model="applyDialog" width="500">
    <v-card>
      <v-card-title>{{ t("Применение изменений в системе") }}</v-card-title>
      <v-card-text>
        {{ t("Вы проверили все вносимые изменения и готовы применить их?") }}
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="applyDialog = false">{{ t("Нет") }}</v-btn>
        <v-btn color="primary" :loading="loading" @click="commit">{{ t("Да") }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="abortDialog" width="520">
    <v-card>
      <v-card-title>{{ t("Выбрать другой файл") }}</v-card-title>
      <v-card-text>
        {{ t("Текущий процесс импорта будет отменён. Продолжить?") }}
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" @click="abortDialog = false">{{ t("Нет") }}</v-btn>
        <v-btn color="warning" :loading="loading" @click="abortProcess">{{ t("Да") }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts" generic="C extends ImportConfig & { columns: Record<string, string | null> }, R extends ImportExcelRow">
import { computed, onMounted, reactive, ref, toRaw } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import FileUploadZone, { type UploadCompleteEvent } from "@/components/FileUploadZone.vue";
import { errorUtils } from "@/lib/errors";
import { formatDate } from "@/lib/datetime";
import tableStartRowImage from "@/assets/employee-import/tableStartRow.jpg";
import emailColumnImage from "@/assets/employee-import/email_column.jpg";
import emailColumnAltImage from "@/assets/employee-import/email_column_alt.jpg";
import type {
  ExcelRowDataProperty,
  ImportConfig,
  ImportExcelRow,
  ImportService,
  ImportWorkflow,
} from "@/services/admin/import/import.base";

type CellFormat = "string" | "date" | "dict";
type Rule = (value: unknown) => boolean | string;

interface PreviewHeader {
  title: string;
  key: string;
  width?: string | number;
  format?: CellFormat;
}

interface ImportStatsLabels {
  newItems: string;
  updatedItems: string;
}

const props = defineProps<{
  title: string;
  service: ImportService<C, R>;
  config: C;
  previewHeadersLoader?: () => PreviewHeader[];
  previewFilterFunction?: (
    rows: R[],
    filter: { search: string; hideNotUpdatedWithoutErrors: boolean },
  ) => R[];
  onCompleteAction?: () => void;
  statsLabels?: ImportStatsLabels;
}>();

const { t } = useI18n();

const loading = ref(false);
const error = ref<string | null>(null);
const step = ref<1 | 2 | 3 | 4>(1);
const workflow = ref<ImportWorkflow<C, R> | null>(null);
const localConfig = ref<C>(cloneConfig(props.config));
const applyDialog = ref(false);
const abortDialog = ref(false);

const previewFilter = reactive({
  search: "",
  hideNotUpdatedWithoutErrors: true,
});

const positiveNumberRules: Rule[] = [
  (value) => Number(value) > 0 || t("Обязательное числовое поле"),
];

const columnRules: Rule[] = [
  (value) => {
    if (value == null || value === "") {
      return true;
    }
    if (typeof value !== "string") {
      return t("Ошибка");
    }
    return /^[A-Za-z]{1,3}$|^\d{1,3}$/.test(value.trim())
      || "A, B, AA или 1, 2, 27";
  },
];

const configColumns = computed(() => Object.keys(localConfig.value.columns ?? {}));

const previewHeaders = computed(() => {
  if (props.previewHeadersLoader) {
    return props.previewHeadersLoader();
  }
  return [
    { title: t("Строка"), key: "rowNumber", width: 110 },
    { title: t("Email"), key: "email", width: 250 },
    { title: t("Количество ошибок"), key: "errorCount", width: 160 },
  ];
});

const formattedPreviewHeaders = computed(() =>
  previewHeaders.value.filter((header) => header.format),
);

const statsLabels = computed<ImportStatsLabels>(() => props.statsLabels ?? {
  newItems: t("Новых сотрудников"),
  updatedItems: t("Сотрудников с изменениями"),
});

const filteredPreviewRows = computed(() => {
  const items = (workflow.value?.importedRows ?? []) as R[];
  if (props.previewFilterFunction) {
    return props.previewFilterFunction(items, previewFilter) as R[];
  }
  const search = previewFilter.search.trim().toLowerCase();
  return items.filter((item) => {
    if (previewFilter.hideNotUpdatedWithoutErrors && item.updatedCellsCount <= 0 && item.errorCount <= 0) {
      return false;
    }
    if (!search) {
      return true;
    }
    return JSON.stringify(item).toLowerCase().includes(search);
  });
});

const actionButtonText = computed(() => {
  if (step.value === 3) {
    return t("Применить");
  }
  if (step.value === 4) {
    return t("Закрыть");
  }
  return t("Далее");
});

const displayStep = computed(() => (step.value > 3 ? 3 : step.value));

const actionButtonDisabled = computed(() => {
  if (loading.value) {
    return true;
  }
  if (step.value === 1) {
    return !workflow.value?.filename;
  }
  return false;
});

const uploadUrl = computed(() =>
  workflow.value ? props.service.getUploadImportFileUrl(workflow.value.id) : "",
);

onMounted(() => {
  init().catch(() => undefined);
});

function onStepperChange(nextStep: number) {
  if (nextStep >= 1 && nextStep <= 3) {
    step.value = nextStep as 1 | 2 | 3;
  }
}

async function init(): Promise<void> {
  await wrapRequest(async () => {
    const process = await props.service.getActiveOrStartNewImportProcess();
    workflow.value = process;
    if (process.config) {
      localConfig.value = cloneConfig(process.config);
    }
    refreshStep(process);
  });
}

function refreshStep(process: ImportWorkflow<C, R>) {
  step.value = Math.max(1, process.state + 1) as 1 | 2 | 3 | 4;
}

async function applyStep() {
  if (!workflow.value) {
    return;
  }
  if (step.value === 1) {
    await init();
    return;
  }
  if (step.value === 2) {
    await applyConfig();
    return;
  }
  if (step.value === 3) {
    applyDialog.value = true;
    return;
  }
  if (step.value === 4) {
    props.onCompleteAction?.();
  }
}

function goBack() {
  if (step.value > 1) {
    step.value = (step.value - 1) as 1 | 2 | 3 | 4;
  }
}

async function uploadFile() {
  await init();
}

async function applyConfig() {
  if (!workflow.value) {
    return;
  }
  await wrapRequest(async () => {
    const refreshed = await props.service.applyConfigAndPreview(
      workflow.value!.id,
      cloneConfig(localConfig.value),
    );
    workflow.value = refreshed;
    refreshStep(refreshed);
  });
}

async function commit() {
  if (!workflow.value) {
    return;
  }
  applyDialog.value = false;
  await wrapRequest(async () => {
    const refreshed = await props.service.commit(workflow.value!.id);
    workflow.value = refreshed;
    refreshStep(refreshed);
  });
}

async function wrapRequest(fn: () => Promise<void>) {
  loading.value = true;
  error.value = null;
  try {
    await fn();
  } catch (e: unknown) {
    error.value = errorUtils.shortMessage(e);
  } finally {
    loading.value = false;
  }
}

function isExcelCell(value: unknown): value is ExcelRowDataProperty<unknown> {
  if (!value || typeof value !== "object") {
    return false;
  }
  return "importedValue" in value && "updated" in value;
}

function getField(item: R, key: string): unknown {
  return (item as Record<string, unknown>)[key];
}

function getExcelCell(value: unknown): ExcelRowDataProperty<unknown> {
  return value as ExcelRowDataProperty<unknown>;
}

function normalizeColumn(columnKey: string) {
  const value = localConfig.value.columns[columnKey];
  if (!value) {
    return;
  }
  localConfig.value.columns[columnKey] = value.trim().toUpperCase();
}

function onFileUploadClose(event: UploadCompleteEvent) {
  if (!event.uploaded) {
    return;
  }
  uploadFile().catch(() => undefined);
}

async function abortProcess() {
  if (!workflow.value) {
    abortDialog.value = false;
    return;
  }
  abortDialog.value = false;
  await wrapRequest(async () => {
    const aborted = await props.service.abort(workflow.value!.id);
    workflow.value = aborted;
    const restarted = await props.service.getActiveOrStartNewImportProcess();
    workflow.value = restarted;
    localConfig.value = cloneConfig(restarted.config ?? props.config);
    step.value = 1;
  });
}

function cloneConfig(value: C): C {
  const raw = toRaw(value);
  try {
    return structuredClone(raw) as C;
  } catch {
    return JSON.parse(JSON.stringify(raw)) as C;
  }
}

function formatCellValue(value: unknown, format: CellFormat = "string"): string {
  if (value == null || value === "") {
    return "-";
  }
  if (format === "date" && typeof value === "string") {
    return formatDate(value) ?? value;
  }
  if (format === "dict" && typeof value === "object" && value !== null && "name" in value) {
    return String((value as { name: string }).name);
  }
  return String(value);
}
</script>

<style scoped>
.import-new {
  color: #1d9d01;
}

.import-old {
  text-decoration: line-through;
  text-decoration-color: red;
}

.import-same {
  color: #666666;
}
</style>
