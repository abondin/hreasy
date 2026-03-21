<template>
  <AdminSimpleDictView
    :title="t('Кабинеты')"
    :headers="headers"
    :load-items="loadItems"
    :create-item="createAdminOfficeLocation"
    :update-item="updateAdminOfficeLocation"
    :create-form="createForm"
    :item-to-form="itemToForm"
    test-id="admin-dict-office-locations-view"
  >
    <template #additional-fields="{ form }">
      <v-autocomplete
        v-model="form.officeId"
        :items="offices"
        item-title="name"
        item-value="id"
        :label="t('Офис')"
        variant="outlined"
        clearable
      />
      <v-autocomplete
        v-model="form.mapName"
        :items="maps"
        item-title="mapName"
        item-value="mapName"
        :label="t('Карта')"
        variant="outlined"
        clearable
      />
      <v-textarea
        v-model="form.description"
        :label="t('Описание')"
        :rules="descriptionRules"
        variant="outlined"
        rows="3"
      />
    </template>

    <template #[`item.office`]="{ item }">
      {{ item.office?.name ?? "" }}
    </template>

    <template #[`item.mapName`]="{ item }">
      <v-tooltip v-if="item.mapName" location="bottom">
        <template #activator="{ props: tooltipProps }">
          <v-chip
            v-bind="tooltipProps"
            size="small"
            @click.stop="openMapPreview(item.mapName, item.name)"
          >
            <v-icon start icon="mdi-map" />
            {{ item.mapName }}
          </v-chip>
        </template>
        <span>{{ t("Посмотреть карту") }}</span>
      </v-tooltip>
    </template>
  </AdminSimpleDictView>

  <OfficeMapPreviewDialog
    v-model="mapPreviewOpen"
    :map-name="previewMapName"
    :map-title="previewMapTitle"
  />
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import OfficeMapPreviewDialog from "@/components/office-map/OfficeMapPreviewDialog.vue";
import AdminSimpleDictView from "@/views/admin/dicts/components/AdminSimpleDictView.vue";
import {
  createAdminOfficeLocation,
  fetchAdminOfficeLocations,
  fetchAdminOfficeMaps,
  fetchAdminOffices,
  type AdminOffice,
  type AdminOfficeLocation,
  type AdminOfficeLocationPayload,
  type AdminOfficeMap,
  updateAdminOfficeLocation,
} from "@/services/admin/admin-dict.service";

const { t } = useI18n();

const offices = ref<AdminOffice[]>([]);
const maps = ref<AdminOfficeMap[]>([]);
const mapPreviewOpen = ref(false);
const previewMapName = ref<string | null>(null);
const previewMapTitle = ref<string | null>(null);

const headers = computed(() => [
  { title: t("Наименования"), key: "name", width: "260px" },
  { title: t("Офис"), key: "office", width: "220px" },
  { title: t("Описание"), key: "description", width: "340px" },
  { title: t("Карта"), key: "mapName", width: "220px" },
  { title: t("Архив"), key: "archived", width: "140px" },
]);

const descriptionRules = computed(() => [
  (value?: string | null) =>
    !value || value.length <= 1024 || t("Не более N символов", { n: 1024 }),
]);

async function loadMeta(): Promise<void> {
  const [loadedOffices, loadedMaps] = await Promise.all([
    fetchAdminOffices(),
    fetchAdminOfficeMaps(),
  ]);
  offices.value = loadedOffices;
  maps.value = loadedMaps;
}

async function loadItems(): Promise<AdminOfficeLocation[]> {
  await loadMeta();
  return fetchAdminOfficeLocations();
}

function createForm(): AdminOfficeLocationPayload {
  return {
    name: "",
    archived: false,
    description: "",
    officeId: null,
    mapName: null,
  };
}

function itemToForm(item: AdminOfficeLocation): AdminOfficeLocationPayload {
  return {
    name: item.name,
    archived: item.archived,
    description: item.description ?? "",
    officeId: item.office?.id ?? null,
    mapName: item.mapName ?? null,
  };
}

function openMapPreview(mapName: string, title: string): void {
  previewMapName.value = mapName;
  previewMapTitle.value = title;
  mapPreviewOpen.value = true;
}
</script>
