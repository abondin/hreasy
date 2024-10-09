<template>
  <v-overlay :value="data.fullscreen" class="map-overlay" absolute>
    <v-alert type="error" v-if="data.error">
      {{ data.error }}
    </v-alert>
    <v-progress-circular v-if="data.loading"
                         indeterminate
                         size="64"
    ></v-progress-circular>
    <v-card light v-else @click.stop="data.hide()">
      <v-card-text class="full-screen-card">
        <svg v-if="data.img"
             v-html="data.img" ref="map" class="full-screen-svg">
        </svg>
      </v-card-text>
      <v-btn absolute fab top right @click="data.hide()">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card>
  </v-overlay>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import WorkplaceOnMapUtils from "@/components/admin/dict/office/workplace/workplace-on-map-utils";
import MapPreviewDataContainer from "@/components/admin/dict/office/workplace/MapPreviewDataContainer";


@Component({})
export default class MapPreviewComponent extends Vue {
  @Prop({required: true})
  private data!: MapPreviewDataContainer;

  mounted() {
    this.data.mapReadyListener = () => {
      this.$nextTick(() => {
        const svgElement = this.$refs.map as SVGElement;
        const {width, height} = svgElement.getBoundingClientRect();
        WorkplaceOnMapUtils.adjustSvgViewBox(svgElement, width, height);
        WorkplaceOnMapUtils.initializeWorkplace(svgElement, this.data.employees);
      });
    }
  }
}
</script>

<style scoped>
.map-overlay {
  position: fixed !important;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999; /* Ensure it's on top */
}

.full-screen-card {
  width: 90vw;
  height: 90vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  justify-content: center;
  align-items: center;
  display: flex;
}

.full-screen-svg {
  width: 100%;
  height: 100%;
}
</style>
