<!--
  Fullscreen dialog for displaying office location maps.
  Mirrors the legacy Vue 2 implementation: loads SVG maps, wires svg-pan-zoom,
  highlights workplaces, and opens colleague details.
  -->
<template>
  <!--<editor-fold desc="Map dialog">-->
  <v-dialog
      v-if="hasMapTarget"
      v-model="mapData.opened"
      fullscreen
      transition="dialog-transition"
  >
    <v-card class="map-preview-card">
      <v-toolbar color="grey lighten-1">
        <v-toolbar-title>{{ toolbarTitle }}</v-toolbar-title>
        <v-spacer/>
        <v-btn icon="mdi-close" variant="text" @click="closeDialog"/>
      </v-toolbar>

      <v-alert
          v-if="mapData.error"
          type="error"
          variant="tonal"
          border="start"
          class="ma-6"
      >
        {{ mapData.error }}
      </v-alert>

      <v-card-text v-else class="full-card-text pa-5">
        <v-progress-circular
            v-if="mapData.loading"
            indeterminate
            size="64"
        />
        <svg
            v-else-if="mapData.img"
            ref="mapRef"
            class="full-screen-svg"
            v-html="mapData.img"
        ></svg>
        <div v-else class="map-preview-empty text-medium-emphasis">
          {{ t("Карта недоступна") }}
        </div>
      </v-card-text>

      <v-dialog
          max-width="1024"
          v-model="mapData.employeeDetailedOpened"
          :fullscreen="isMobile"
          :scrim="true"
          transition="dialog-bottom-transition"
      >
        <v-card v-if="mapData.selectedEmployee">
          <v-card-title class="d-flex align-center">
            <span>{{ mapData.selectedEmployee.displayName }}</span>
            <v-spacer/>
            <v-btn icon="mdi-close" variant="text" @click="mapData.employeeDetailedOpened = false"/>
          </v-card-title>
          <v-card-text>
            <office-map-employee-card
                :employee-id="mapData.selectedEmployee.id"
            />
          </v-card-text>
        </v-card>
      </v-dialog>
    </v-card>
  </v-dialog>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, reactive, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import svgPanZoom from "svg-pan-zoom";
import type {Employee} from "@/services/employee.service";
import MapPreviewData from "@/components/office-map/MapPreviewData";
import {initializeWorkplaces} from "@/components/office-map/workplaceOnMap";
import OfficeMapEmployeeCard from "@/components/office-map/OfficeMapEmployeeCard.vue";
import {useDisplay} from "vuetify";

const props = defineProps<{
  mapName: string | null;
  mapTitle?: string | null;
  workplace?: string | null;
}>();

const dialogOpen = defineModel<boolean>({default: false});

const {t} = useI18n();
const display = useDisplay();

const mapData = reactive(new MapPreviewData());
const mapRef = ref<SVGElement | null>(null);

const hasMapTarget = computed(() => Boolean(props.mapName));
const toolbarTitle = computed(() => (
    props.mapTitle ? `${t("Карта")}: ${props.mapTitle}` : t("Карта")
));
const highlightedWorkplaces = computed(() => {
  const value = props.workplace;
  return value ? [value] : [];
});

const isMobile = computed(() => display.smAndDown.value);
let panZoomInstance: ReturnType<typeof svgPanZoom> | null = null;

mapData.mapReadyListener = async () => {
  await nextTick();
  if (mapData.opened) {
    initialiseSvg();
  }
};

watch(
    () => dialogOpen.value,
    (open) => {
      if (open) {
        if (props.mapName) {
          void mapData.show(
              props.mapName,
              props.mapTitle ?? null,
              highlightedWorkplaces.value,
          );
        } else {
          mapData.error = t("Карта недоступна");
          mapData.opened = true;
        }
      } else {
        mapData.hide();
        destroyPanZoom();
      }
    },
);

watch(
    () => mapData.opened,
    (open) => {
      if (!open && dialogOpen.value) {
        dialogOpen.value = false;
      }
    },
);

watch(
    () => props.mapName,
    (newName, oldName) => {
      if (
          dialogOpen.value &&
          newName &&
          newName !== oldName
      ) {
        void mapData.show(
            newName,
            props.mapTitle ?? null,
            highlightedWorkplaces.value,
        );
      }
    },
);

watch(
    () => highlightedWorkplaces.value,
    (value) => {
      mapData.highlightedWorkplace = value;
      if (mapData.opened) {
        requestAnimationFrame(() => initialiseSvg());
      }
    },
    {deep: true},
);

function closeDialog() {
  mapData.hide();
  dialogOpen.value = false;
}

function handleWorkplaceClick(
    workplaceName: string,
    employee?: Employee,
) {
  if (!employee) {
    return;
  }
  mapData.selectedEmployee = employee;
  mapData.employeeDetailedOpened = true;
}

function initialiseSvg() {
  destroyPanZoom();
  const svgElement = resolveTargetSvg();
  if (!svgElement) {
    return;
  }

  initializeWorkplaces(
      svgElement,
      handleWorkplaceClick,
      mapData.employees,
      mapData.highlightedWorkplace,
  );

  panZoomInstance = svgPanZoom(svgElement, {
    zoomEnabled: true,
    controlIconsEnabled: true,
    fit: true,
    center: true,
    minZoom: 0.7,
    maxZoom: 10,
  });

  panZoomInstance.resize();
  panZoomInstance.fit();
  panZoomInstance.center();

  requestAnimationFrame(() => {
    initializeWorkplaces(
        svgElement,
        handleWorkplaceClick,
        mapData.employees,
        mapData.highlightedWorkplace,
    );
  });
}

function resolveTargetSvg(): SVGSVGElement | null {
  const outer = mapRef.value;
  if (!outer) {
    return null;
  }
  if (outer instanceof SVGSVGElement) {
    return outer;
  }
  const nested = outer.querySelector<SVGSVGElement>("svg");
  return nested ?? null;
}

function destroyPanZoom() {
  if (panZoomInstance) {
    panZoomInstance.destroy();
    panZoomInstance = null;
  }
}

onBeforeUnmount(() => {
  destroyPanZoom();
});
</script>

<style scoped>
.map-preview-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.full-card-text {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.full-screen-svg {
  display: block;
  width: 90vw;
  height: 90vh;
  background-color: white;
  border-radius: 12px;
}

.full-screen-svg :deep(svg) {
  width: 100%;
  height: 100%;
}

.map-preview-empty {
  font-size: 1rem;
}
</style>
