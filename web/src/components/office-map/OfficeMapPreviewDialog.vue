<template>
  <v-dialog v-if="mapName" v-model="opened" fullscreen transition="dialog-transition">
    <v-card class="d-flex flex-column">
      <v-toolbar color="grey lighten-1">
        <v-toolbar-title>{{ mapTitle ? `${t("Карта")}: ${mapTitle}` : t("Карта") }}</v-toolbar-title>
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" :aria-label="t('Закрыть')" @click="opened = false" />
      </v-toolbar>
      <OfficeMap
        v-if="opened"
        :map-name="mapName"
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
const employeeOpened = computed({
  get: () => selectedEmployee.value !== null,
  set: (value: boolean) => { if (!value) selectedEmployee.value = null; },
});
watch([opened, () => props.mapName], () => { selectedEmployee.value = null; });
</script>

<style scoped>
.dialog-map {
  flex: 1;
  height: calc(100dvh - 104px);
}
</style>
