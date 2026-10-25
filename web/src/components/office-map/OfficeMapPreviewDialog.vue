<template>
  <v-dialog v-if="mapName" v-model="opened" fullscreen transition="dialog-transition">
    <v-card class="d-flex flex-column">
      <v-toolbar color="grey lighten-1">
        <v-toolbar-title>{{ mapTitle ? `${t("Карта")}: ${mapTitle}` : t("Карта") }}</v-toolbar-title>
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" :aria-label="t('Закрыть')" @click="opened = false" />
      </v-toolbar>
      <v-alert v-if="error" type="error" variant="tonal" class="ma-5">
        {{ error }}
        <template #append>
          <v-btn variant="text" @click="retry++">{{ t('Повторить') }}</v-btn>
        </template>
      </v-alert>
      <v-progress-linear v-else-if="loading" indeterminate />
      <OfficeMap
        v-else-if="opened"
        :map-name="mapName"
        :employees="employees"
        :workplace="workplace"
        class="dialog-map ma-5"
        @employee-select="selectedEmployee = $event"
      />
      <v-dialog v-model="employeeOpened" max-width="1024" :fullscreen="display.smAndDown.value">
        <v-card v-if="selectedEmployee">
          <v-card-item :title="selectedEmployee.displayName">
            <template #append>
              <v-btn icon="mdi-close" variant="text" :aria-label="t('Закрыть')" @click="selectedEmployee = null" />
            </template>
          </v-card-item>
          <v-card-text>
            <OfficeMapEmployeeCard :employee-id="selectedEmployee.id" />
          </v-card-text>
        </v-card>
      </v-dialog>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify";
import type { Employee } from "@/services/employee.service";
import { listEmployees } from "@/services/employee.service";
import { errorUtils } from "@/lib/errors";
import OfficeMap from "@/components/office-map/OfficeMap.vue";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";

const props = defineProps<{
  mapName: string | null;
  mapTitle?: string | null;
  workplace?: string | null;
}>();
const opened = defineModel<boolean>({ default: false });
const { t } = useI18n();
const display = useDisplay();
const selectedEmployee = ref<Employee | null>(null);
const employees = ref<Employee[]>([]);
const loading = ref(false);
const error = ref("");
const retry = ref(0);
const employeeOpened = computed({
  get: () => selectedEmployee.value !== null,
  set: (value: boolean) => { if (!value) selectedEmployee.value = null; },
});
watch([opened, () => props.mapName], () => { selectedEmployee.value = null; });
watch([opened, retry], async ([open], _, onCleanup) => {
  if (!open) return;
  let active = true;
  onCleanup(() => { active = false; });
  loading.value = true;
  error.value = "";
  try {
    const data = await listEmployees();
    if (active) employees.value = data;
  } catch (cause) {
    if (active) error.value = errorUtils.shortMessage(cause);
  } finally {
    if (active) loading.value = false;
  }
}, { immediate: true });
</script>

<style scoped>
.dialog-map {
  flex: 1;
  height: calc(100dvh - 104px);
}
</style>
