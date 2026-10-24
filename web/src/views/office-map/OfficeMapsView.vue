<template>
  <v-container fluid data-testid="office-maps-view">
    <v-card :title="t('Карты кабинетов')">
      <v-card-text>
        <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
          {{ error }}
        </v-alert>
        <v-row>
          <v-col cols="12" md="6">
            <v-autocomplete
              v-model="officeId"
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
              data-testid="office-maps-office"
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-autocomplete
              v-model="locationId"
              :items="officeLocations"
              item-title="name"
              item-value="id"
              :label="t('Кабинет')"
              :disabled="loading || officeId == null"
              :no-data-text="t('Отсутствуют данные')"
              variant="outlined"
              hide-details
              clearable
              data-testid="office-maps-room"
            />
          </v-col>
        </v-row>
        <v-alert v-if="selectedLocation && !selectedLocation.mapName" type="info" variant="tonal" class="mt-4">
          {{ t('Для этого кабинета карта не загружена') }}
        </v-alert>
        <div v-if="selectedLocation?.mapName" class="map-layout mt-4">
          <OfficeMap
            :map-name="selectedLocation.mapName"
            class="page-map"
            @employee-select="selectedEmployee = $event"
          />
          <v-card v-if="selectedEmployee" class="employee-details" variant="flat">
            <v-card-item :title="selectedEmployee.displayName">
              <template #append>
                <v-btn icon="mdi-close" variant="text" :aria-label="t('Закрыть')" @click="selectedEmployee = null" />
              </template>
            </v-card-item>
            <v-card-text>
              <OfficeMapEmployeeCard :employee-id="selectedEmployee.id" />
            </v-card-text>
          </v-card>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";
import { fetchOffices, fetchOfficeLocations, type DictItem } from "@/services/dict.service";
import type { Employee, OfficeLocationDict } from "@/services/employee.service";
import { errorUtils } from "@/lib/errors";

const { t } = useI18n();
const offices = ref<DictItem[]>([]);
const locations = ref<OfficeLocationDict[]>([]);
const officeId = ref<number | null>(null);
const locationId = ref<number | null>(null);
const selectedEmployee = ref<Employee | null>(null);
const loading = ref(true);
const error = ref("");

const officeLocations = computed(() => locations.value.filter((location) => location.officeId === officeId.value));
const selectedLocation = computed(() => officeLocations.value.find((location) => location.id === locationId.value));

watch(officeId, () => {
  locationId.value = null;
});
watch(locationId, () => { selectedEmployee.value = null; });

async function load() {
  try {
    [offices.value, locations.value] = await Promise.all([fetchOffices(), fetchOfficeLocations()]);
  } catch (cause) {
    error.value = errorUtils.shortMessage(cause);
  } finally {
    loading.value = false;
  }
}

void load();
</script>

<style scoped>
.map-layout {
  display: flex;
}
.page-map {
  flex: 1;
  height: clamp(320px, calc(100dvh - 320px), 900px);
}
.employee-details {
  width: 360px;
  max-height: calc(100dvh - 320px);
  overflow: auto;
}
@media (max-width: 959px) {
  .map-layout {
    flex-direction: column;
  }
  .page-map {
    flex: auto;
  }
  .employee-details {
    width: 100%;
    max-height: none;
  }
}
</style>
