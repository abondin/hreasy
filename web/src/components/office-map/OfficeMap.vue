<template>
  <v-responsive :aspect-ratio="aspectRatio" :min-height="image ? undefined : 320" content-class="position-relative d-flex align-center justify-center">
    <v-alert v-if="error" type="error" variant="tonal">
      {{ error }}
      <template #append>
        <v-btn variant="text" @click="retry++">{{ t('Повторить') }}</v-btn>
      </template>
    </v-alert>
    <v-progress-circular v-else-if="loading" indeterminate size="64" />
    <svg v-else-if="image" :key="locationId ?? ''" ref="mapRef" class="office-map-svg position-absolute top-0 left-0" v-html="image"></svg>
    <div v-else class="text-medium-emphasis">{{ t("Карта недоступна") }}</div>
  </v-responsive>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import svgPanZoom from "svg-pan-zoom";
import type { Employee } from "@/services/employee.service";
import { fetchOfficeMapSvg } from "@/services/office-map.service";
import { highlightWorkplaces, initializeWorkplaces } from "@/components/office-map/workplaceOnMap";
import { errorUtils } from "@/lib/errors";

const props = defineProps<{ mapName: string; employees: Employee[]; workplace?: string | null; locationId?: number | null }>();
const emit = defineEmits<{
  "employee-select": [employee: Employee];
  "workplace-select": [workplace: string];
}>();
const { t } = useI18n();
const loading = ref(false);
const error = ref("");
const image = ref("");
const retry = ref(0);
const aspectRatio = ref(1);
const mapRef = ref<SVGSVGElement | null>(null);

watch([() => props.mapName, retry], async ([filename], _, onCleanup) => {
  let active = true;
  onCleanup(() => { active = false; });
  loading.value = true;
  error.value = "";
  image.value = "";
  try {
    const svg = await fetchOfficeMapSvg(filename);
    if (active) {
      image.value = svg;
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
  initializeWorkplaces(svg, (name, employee) => {
    emit("workplace-select", name);
    if (employee) {
      emit("employee-select", employee);
    }
  }, props.employees.filter((employee) => employee.officeLocation?.mapName === props.mapName),
  props.workplace ? [props.workplace] : [], props.locationId);
  const panZoom = svgPanZoom(svg, {
    zoomEnabled: true,
    controlIconsEnabled: true,
    fit: true,
    center: true,
    minZoom: 0.7,
    maxZoom: 10,
  });
  const { viewBox } = panZoom.getSizes();
  if (viewBox.width > 0 && viewBox.height > 0) {
    aspectRatio.value = viewBox.width / viewBox.height;
  }
  // Dialogs can mount before their final dimensions are available.
  let resizeFrame = 0;
  const observer = new ResizeObserver(() => {
    cancelAnimationFrame(resizeFrame);
    resizeFrame = requestAnimationFrame(() => {
      if (svg.clientWidth && svg.clientHeight) {
        panZoom.resize();
        panZoom.fit();
        panZoom.center();
      }
    });
  });
  observer.observe(svg);
  onCleanup(() => {
    observer.disconnect();
    cancelAnimationFrame(resizeFrame);
    panZoom.destroy();
  });
}, { flush: "post" });

watch(() => props.workplace, (workplace) => {
  if (mapRef.value) {
    highlightWorkplaces(mapRef.value, workplace ? [workplace] : []);
  }
});
</script>

<style scoped>
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
