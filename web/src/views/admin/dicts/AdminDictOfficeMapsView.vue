<template>
  <div class="mt-4" data-testid="admin-dict-office-maps-view">
    <v-card>
      <HREasyTableBase
        table-class="admin-office-maps-table text-truncate"
        :headers="headers"
        :items="filteredItems"
        height="70vh"
        :fixed-header="true"
        density="compact"
        :loading="loading"
        :loading-text="t('\u0417\u0430\u0433\u0440\u0443\u0437\u043A\u0430_\u0434\u0430\u043D\u043D\u044B\u0445')"
        :no-data-text="t('\u041E\u0442\u0441\u0443\u0442\u0441\u0442\u0432\u0443\u044E\u0442 \u0434\u0430\u043D\u043D\u044B\u0435')"
        :hover="true"
        :sort-by="[{ key: 'mapName', order: 'asc' }]"
        :row-props="rowProps"
        @click:row="onClickRow"
      >
        <template #filters>
          <v-card-title class="d-flex align-center ga-2 flex-wrap">
            <span>{{ t("\u041A\u0430\u0440\u0442\u044B \u043E\u0444\u0438\u0441\u043E\u0432 \u0438 \u043A\u0430\u0431\u0438\u043D\u0435\u0442\u043E\u0432") }}</span>
            <v-spacer />
            <v-btn
              data-testid="toolbar-refresh"
              icon="mdi-refresh"
              variant="text"
              :disabled="loading"
              @click="load"
            />
            <v-tooltip location="bottom">
              <template #activator="{ props: tooltipProps }">
                <v-btn
                  v-bind="tooltipProps"
                  icon="mdi-upload"
                  data-testid="toolbar-add"
                  color="primary"
                  variant="text"
                  :disabled="loading"
                  @click="uploadDialog = true"
                />
              </template>
              <span>{{ t("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C \u043A\u0430\u0440\u0442\u0443") }}</span>
            </v-tooltip>
          </v-card-title>

          <v-card-text class="pb-0">
            <v-row dense>
              <v-col cols="12" md="6" lg="4">
                <v-text-field
                  v-model="search"
                  :label="t('\u041F\u043E\u0438\u0441\u043A')"
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
              <span>{{ t("\u0423\u0434\u0430\u043B\u0438\u0442\u044C \u043A\u0430\u0440\u0442\u0443") }}</span>
            </v-tooltip>
          </div>
        </template>
      </HREasyTableBase>
    </v-card>

    <v-dialog v-model="uploadDialog" max-width="720" persistent>
      <v-card>
        <v-card-title>{{ t("\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044C \u043A\u0430\u0440\u0442\u0443") }}</v-card-title>
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
      :title="t('\u0423\u0434\u0430\u043B\u0438\u0442\u044C \u043A\u0430\u0440\u0442\u0443 \u043E\u0444\u0438\u0441\u0430')"
      :message="t('\u0412\u044B \u0443\u0432\u0435\u0440\u0435\u043D\u044B, \u0447\u0442\u043E \u0445\u043E\u0442\u0438\u0442\u0435 \u0443\u0434\u0430\u043B\u0438\u0442\u044C \u043A\u0430\u0440\u0442\u0443 \u043E\u0444\u0438\u0441\u0430?')"
      :item-label="deleteMapName"
      :loading="deleteLoading"
      :error-message="deleteError"
      :confirm-label="t('\u0423\u0434\u0430\u043B\u0438\u0442\u044C')"
      :cancel-label="t('\u041E\u0442\u043C\u0435\u043D\u0438\u0442\u044C')"
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
  { title: t("\u0424\u0430\u0439\u043B"), key: "mapName", width: "420px" },
]);
const filteredItems = computed(() => {
  const query = search.value.trim().toLowerCase();
  if (!query) {
    return items.value;
  }
  return items.value.filter((item) => item.mapName.toLowerCase().includes(query));
});

function rowProps() {
  return { class: "cursor-pointer" };
}

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
