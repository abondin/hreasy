<template>
  <div data-testid="admin-dict-office-maps-view">
    <v-card>
      <HREasyTableBase
        :headers="headers"
        :items="filteredItems"
        density="compact"
        :loading="loading"
        :loading-text="t('Загрузка_данных')"
        :no-data-text="t('Отсутствуют данные')"
        :hover="true"
        :sort-by="[{ key: 'mapName', order: 'asc' }]"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex align-center ga-2 flex-wrap">
            <span>{{ t("Карты офисов и кабинетов") }}</span>
            <v-spacer />
            <TableToolbarActions
              :disabled="loading"
              :show-refresh="true"
              :refresh-label="t('Обновить данные')"
              @refresh="load"
            />
            <v-tooltip location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-upload"
                  color="primary"
                  variant="text"
                  :disabled="loading"
                  @click="uploadDialog = true"
                />
              </template>
              <span>{{ t("Загрузить карту") }}</span>
            </v-tooltip>
          </v-card-title>

          <v-card-text class="pt-0">
            <v-row dense>
              <v-col cols="12" md="6" lg="4">
                <v-text-field
                  v-model="search"
                  :label="t('Поиск')"
                  append-inner-icon="mdi-magnify"
                  variant="outlined"
                  density="compact"
                  hide-details
                  clearable
                />
              </v-col>
            </v-row>
          </v-card-text>
        </template>

        <template #before-table>
          <v-alert v-if="error" type="error" variant="tonal" border="start" class="ma-4 mb-0">
            {{ error }}
          </v-alert>
        </template>

        <template #[`item.mapName`]="{ item }">
          <div class="d-flex align-center ga-2">
            <span>{{ item.mapName }}</span>
            <v-spacer />
            <v-tooltip location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-delete"
                  color="error"
                  variant="text"
                  size="small"
                  @click.stop="openDeleteDialog(item.mapName)"
                />
              </template>
              <span>{{ t("Удалить карту") }}</span>
            </v-tooltip>
          </div>
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="uploadDialog" max-width="720" persistent>
      <v-card>
        <v-card-title>{{ t("Загрузить карту") }}</v-card-title>
        <v-card-text>
          <FileUploadZone
            file-id="admin-office-map-upload"
            :post-action="uploadPath"
            accept=".svg,image/svg+xml"
            @close="onUploadClose"
          />
        </v-card-text>
      </v-card>
    </v-dialog>

    <ConfirmDeleteDialog
      :open="deleteDialog"
      :title="t('Удалить карту офиса')"
      :message="t('Вы уверены, что хотите удалить карту офиса?')"
      :item-label="deleteMapName"
      :loading="deleteLoading"
      :error-message="deleteError"
      :confirm-label="t('Удалить')"
      :cancel-label="t('Отменить')"
      @close="closeDeleteDialog"
      @confirm="confirmDelete"
    />

    <OfficeMapPreviewDialog
      v-model="previewOpen"
      :map-name="previewMapName"
      :map-title="previewMapName"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import TableToolbarActions from "@/components/shared/TableToolbarActions.vue";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import FileUploadZone, { type UploadCompleteEvent } from "@/components/FileUploadZone.vue";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";
import { errorUtils } from "@/lib/errors";
import { extractDataTableRow } from "@/lib/data-table";
import {
  deleteAdminOfficeMap,
  fetchAdminOfficeMaps,
  getAdminOfficeMapUploadPath,
  type AdminOfficeMap,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const loading = ref(false);
const error = ref("");
const items = ref<AdminOfficeMap[]>([]);
const search = ref("");
const uploadDialog = ref(false);
const deleteDialog = ref(false);
const deleteLoading = ref(false);
const deleteError = ref("");
const deleteMapName = ref<string | null>(null);
const previewOpen = ref(false);
const previewMapName = ref<string | null>(null);

const uploadPath = getAdminOfficeMapUploadPath();
const headers = computed(() => [
  { title: t("Файл"), key: "mapName", width: "420px" },
]);
const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase();
  if (!query) {
    return items.value;
  }
  return items.value.filter((item) => item.mapName.toLowerCase().includes(query));
});

async function load(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    items.value = await fetchAdminOfficeMaps();
  } catch (loadError) {
    error.value = errorUtils.shortMessage(loadError);
  } finally {
    loading.value = false;
  }
}

function onUploadClose(event: UploadCompleteEvent): void {
  uploadDialog.value = false;
  if (event.uploaded) {
    void load();
  }
}

function openDeleteDialog(mapName: string): void {
  deleteMapName.value = mapName;
  deleteError.value = "";
  deleteDialog.value = true;
}

function closeDeleteDialog(): void {
  deleteDialog.value = false;
  deleteError.value = "";
  deleteMapName.value = null;
}

async function confirmDelete(): Promise<void> {
  if (!deleteMapName.value) {
    return;
  }
  deleteLoading.value = true;
  deleteError.value = "";
  try {
    await deleteAdminOfficeMap(deleteMapName.value);
    closeDeleteDialog();
    await load();
  } catch (requestError) {
    deleteError.value = errorUtils.shortMessage(requestError);
  } finally {
    deleteLoading.value = false;
  }
}

function onClickRow(_event: Event, row: unknown): void {
  const item = extractDataTableRow<AdminOfficeMap>(row);
  if (!item) {
    return;
  }
  previewMapName.value = item.mapName;
  previewOpen.value = true;
}

void load();
</script>
