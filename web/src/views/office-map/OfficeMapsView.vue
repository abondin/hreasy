<template>
  <v-container fluid data-testid="office-maps-view">
    <v-card :title="t('Карты кабинетов')">
      <v-card-text>
        <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
          {{ error }}
          <template #append>
            <v-btn variant="text" @click="load">{{ t('Повторить') }}</v-btn>
          </template>
        </v-alert>
        <v-row align="center">
          <v-col cols="12" md="4">
            <v-autocomplete
              :model-value="officeId"
              @update:model-value="selectOffice"
              :items="offices"
              item-title="name"
              item-value="id"
              :label="t('Офис')"
              :loading="loading"
              :disabled="loading"
              :no-data-text="t('Отсутствуют данные')"
              variant="outlined"
              hide-details
              clearable
              persistent-clear
              data-testid="office-maps-office"
            />
          </v-col>
          <v-col cols="12" md="4">
            <v-autocomplete
              :model-value="locationId"
              @update:model-value="selectLocation"
              :items="officeLocations"
              item-title="name"
              item-value="id"
              :label="t('Кабинет')"
              :disabled="loading || officeId == null"
              :no-data-text="t('Отсутствуют данные')"
              variant="outlined"
              hide-details
              clearable
              persistent-clear
              data-testid="office-maps-room"
            />
          </v-col>
          <v-col v-if="displayedLocation" cols="12" md="4" class="text-subtitle-1" data-testid="office-maps-map-title">
            {{ t('Карта') }}: {{ displayedOfficeName ? displayedOfficeName + ' · ' : '' }}{{ displayedLocation.name }}
          </v-col>
        </v-row>
        <v-alert v-if="!loading && !error && !displayedLocation && !selectedEmployee" type="info" variant="tonal" class="mt-4">
          {{ t('Выберите кабинет или сотрудника') }}
        </v-alert>
        <v-alert v-if="selectedEmployee && !displayedLocation" type="info" variant="tonal" class="mt-4">
          {{ t('Кабинет не указан') }}
        </v-alert>
        <v-alert v-if="displayedLocation && !displayedLocation.mapName" type="info" variant="tonal" class="mt-4">
          {{ t('Для этого кабинета карта не загружена') }}
        </v-alert>
        <OfficeMap
          v-if="displayedLocation?.mapName"
          :map-name="displayedLocation.mapName"
          :employees="employees"
          :location-id="locationId"
          :workplace="selectedEmployee?.officeWorkplace?.trim() || workplaceFilter"
          class="mt-4"
          max-height="calc(100dvh - 240px)"
          @workplace-select="selectWorkplace"
        />
      </v-card-text>
    </v-card>
  </v-container>
  <OfficeMapSidebar
    v-model:search="search"
    :workplace="workplaceFilter"
    @update:workplace="clearWorkplace"
    :model-value="selectedEmployee"
    :employees="scopedEmployees"
    :loading="loading"
    :error="error"
    :scope="scopeLabel"
    @update:model-value="selectEmployee"
  />
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import OfficeMapSidebar from "@/components/office-map/OfficeMapSidebar.vue";
import { listEmployees } from "@/services/employee.service";
import { fetchOffices, fetchOfficeLocations, type OfficeDict } from "@/services/dict.service";
import type { Employee, OfficeLocationDict } from "@/services/employee.service";
import { errorUtils } from "@/lib/errors";

const { t } = useI18n();
const offices = ref<OfficeDict[]>([]);
const employees = ref<Employee[]>([]);
const locations = ref<OfficeLocationDict[]>([]);
const officeId = ref<number | null>(null);
const locationId = ref<number | null>(null);
const selectedEmployee = ref<Employee | null>(null);
const displayedLocation = ref<OfficeLocationDict | null>(null);
const search = ref("");
const workplaceFilter = ref<string | null>(null);
const loading = ref(true);
const error = ref("");

const officeLocations = computed(() => locations.value.filter((location) => location.officeId === officeId.value));
const displayedOfficeName = computed(() => offices.value.find((office) => office.id === displayedLocation.value?.officeId)?.name);
const scopeLabel = computed(() => locations.value.find((location) => location.id === locationId.value)?.name
  ?? offices.value.find((office) => office.id === officeId.value)?.name
  ?? t("Все сотрудники"));

const scopedEmployees = computed(() => employees.value.filter((employee) => {
  if (locationId.value != null && employee.officeLocation?.id !== locationId.value) return false;
  if (officeId.value != null && employee.officeLocation?.officeId !== officeId.value) return false;
  return !workplaceFilter.value || (
    employee.officeLocation?.mapName === displayedLocation.value?.mapName
    && employee.officeWorkplace?.trim().toLowerCase() === workplaceFilter.value.trim().toLowerCase()
  );
}));

function selectOffice(id: number | null) {
  officeId.value = id;
  locationId.value = null;
  displayedLocation.value = null;
  selectedEmployee.value = null;
  workplaceFilter.value = null;
}

function selectLocation(id: number | null) {
  locationId.value = id;
  displayedLocation.value = locations.value.find((location) => location.id === id) ?? null;
  selectedEmployee.value = null;
  workplaceFilter.value = null;
}

function selectEmployee(employee: Employee) {
  selectedEmployee.value = employee;
  displayedLocation.value = employee.officeLocation ?? null;
}

function selectWorkplace(workplace: string) {
  search.value = "";
  selectedEmployee.value = null;
  workplaceFilter.value = workplace;
}

function clearWorkplace() {
  workplaceFilter.value = null;
  selectedEmployee.value = null;
}

async function load() {
  loading.value = true;
  error.value = "";
  try {
    [offices.value, locations.value, employees.value] = await Promise.all([
      fetchOffices(), fetchOfficeLocations(), listEmployees(),
    ]);
    selectOffice(offices.value.find((office) => office.primaryOffice)?.id ?? null);
  } catch (cause) {
    error.value = errorUtils.shortMessage(cause);
  } finally {
    loading.value = false;
  }
}

void load();
</script>
