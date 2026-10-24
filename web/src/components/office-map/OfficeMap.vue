<template>
  <div class="office-map">
    <v-alert v-if="error" type="error" variant="tonal">{{ error }}</v-alert>
    <v-progress-circular v-else-if="loading" indeterminate size="64" />
    <svg v-else-if="image" :key="workplace ?? ''" ref="mapRef" class="office-map-svg" v-html="image"></svg>
    <div v-else class="text-medium-emphasis">{{ t("Карта недоступна") }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import svgPanZoom from "svg-pan-zoom";
import type { Employee } from "@/services/employee.service";
import { loadOfficeMapData } from "@/services/office-map.service";
import { initializeWorkplaces } from "@/components/office-map/workplaceOnMap";
import { errorUtils } from "@/lib/errors";

const props = defineProps<{ mapName: string; workplace?: string | null }>();
const emit = defineEmits<{ "employee-select": [employee: Employee] }>();
const { t } = useI18n();
const loading = ref(false);
const error = ref("");
const image = ref("");
const employees = ref<Employee[]>([]);
const mapRef = ref<SVGSVGElement | null>(null);

watch(() => props.mapName, async (filename, _, onCleanup) => {
  let active = true;
  onCleanup(() => { active = false; });
  loading.value = true;
  error.value = "";
  image.value = "";
  employees.value = [];
  try {
    const data = await loadOfficeMapData(filename);
    if (active) {
      employees.value = data.employees;
      image.value = data.svg;
    }
  } catch (cause) {
    if (active) {
      error.value = errorUtils.shortMessage(cause);
    }
  } finally {
    if (active) {
      loading.value = false;
    }
  }
}, { immediate: true });

watch(mapRef, (svg, _, onCleanup) => {
  if (!svg) {
    return;
  }
  initializeWorkplaces(svg, (_name, employee) => {
    if (employee) {
      emit("employee-select", employee);
    }
  }, employees.value, props.workplace ? [props.workplace] : []);
  const panZoom = svgPanZoom(svg, {
    zoomEnabled: true,
    controlIconsEnabled: true,
    fit: true,
    center: true,
    minZoom: 0.7,
    maxZoom: 10,
  });
  // Dialogs can mount before their final dimensions are available.
  const observer = new ResizeObserver(() => {
    if (svg.clientWidth && svg.clientHeight) {
      panZoom.resize();
      panZoom.fit();
      panZoom.center();
    }
  });
  observer.observe(svg);
  onCleanup(() => {
    observer.disconnect();
    panZoom.destroy();
  });
}, { flush: "post" });
</script>

<style scoped>
.office-map {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 320px;
}
.office-map-svg {
  display: block;
  width: 100%;
  height: 100%;
  background: white;
  border-radius: 12px;
}
.office-map-svg :deep(svg) {
  width: 100%;
  height: 100%;
}
</style>
