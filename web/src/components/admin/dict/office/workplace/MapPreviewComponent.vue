<template>
  <v-overlay :value="data.fullscreen" @click="data.hide()">
    <v-card @click.stop="data.hide()">
      <svg v-if="data.img"
           v-html="data.img" ref="map"
           :width="defaultMapSizes.width" :height="defaultMapSizes.height">
      </svg>
      <v-btn absolute dark fab top right>
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
  defaultMapSizes = WorkplaceOnMapUtils.defaultMapSizes;
  @Prop({required: true})
  private data!: MapPreviewDataContainer;

  created() {
    this.data.mapSelectedListener = () => {
      this.$nextTick(() => {
        const svgElement = this.$refs.map as SVGElement;
        WorkplaceOnMapUtils.adjustSvgViewBox(svgElement);
        WorkplaceOnMapUtils.initializeWorkplace(svgElement, this.data.employees);
      });
    }
  }
}
</script>

<style scoped>

</style>
