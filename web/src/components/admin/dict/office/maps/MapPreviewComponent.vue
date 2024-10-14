<template>
  <v-dialog v-if="data?.filename" v-model="data.opened"
            fullscreen
            transition="dialog-transition">
    <v-card>
      <v-toolbar color="grey lighten-1">
        <v-toolbar-title>{{ $t('Карта') }}{{ data.title ? (": " + data.title) : '' }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="data.hide()" outlined>
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-toolbar>

      <v-alert type="error" v-if="data.error">
        {{ data.error }}
      </v-alert>
      <v-progress-circular v-if="data.loading"
                           indeterminate
                           size="64"
      ></v-progress-circular>
      <v-card-text class="full-card-text pa-5" v-else>
        <svg v-if="data.img" class="full-screen-svg"
             v-html="data.img" ref="map">
        </svg>
      </v-card-text>
      <v-dialog max-width="900px" v-if="data.selectedEmployee" v-model="data.employeeDetailedOpened">
        <v-card>
          <v-card-text>
            <v-btn icon absolute top right @click="data.employeeDetailedOpened=false" outlined>
              <v-icon>mdi-close</v-icon>
            </v-btn>
            <employee-base-info-card :employee-id="data.selectedEmployee.id"></employee-base-info-card>
          </v-card-text>
        </v-card>
      </v-dialog>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import WorkplaceOnMapUtils from "@/components/admin/dict/office/maps/workplace-on-map-utils";
import MapPreviewDataContainer from "@/components/admin/dict/office/maps/MapPreviewDataContainer";
import logger from "@/logger";
import EmployeeBaseInfoCard from "@/components/shared/EmployeeBaseInfoCard.vue";
import svgPanZoom from 'svg-pan-zoom';


@Component({
  components: {EmployeeBaseInfoCard}
})
export default class MapPreviewComponent extends Vue {
  @Prop({required: true})
  private data!: MapPreviewDataContainer;

  private panZoomInstance?: any;


  mounted() {
    this.data.mapReadyListener = () => {
      const svgElement = this.$refs.map as SVGElement;
      if (svgElement) {
        WorkplaceOnMapUtils.initializeWorkplace(svgElement,
            (workplaceName, employee) => {
              if (employee) {
                this.data.openEmployeeDetails(employee);
              }
            },
            this.data.employees,
            this.data.highlightedWorkplace);
        this.panZoomInstance = svgPanZoom(svgElement, {
          zoomEnabled: true,
          controlIconsEnabled: true,
          fit: true,
          center: true,
          minZoom: 0.7,
          maxZoom: 10,
        });
      } else {
        logger.error("Unable to find svg dom element to render the map");
      }
    }
  }
}
</script>

<style scoped>
.full-card-text {
  width: 90vw;
  height: 90vh;
}

.full-screen-svg {
  width: 100%;
  height: 100%;
}
</style>
