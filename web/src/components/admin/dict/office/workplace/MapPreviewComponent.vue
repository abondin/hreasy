<template>
  <v-overlay :value="data.fullscreen" absolute>
    <v-card @click.stop="data.hide()">
      <v-card-text class="full-screen-card">
        <svg v-if="data.img"
             v-html="data.img" ref="map" class="full-screen-svg">
        </svg>
      </v-card-text>
      <v-btn absolute dark fab top right @click="data.hide()">
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
    this.data.mapSelectedListener = () => {
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
.full-screen-card {
  width: 90vw;
  height: 90vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.full-screen-svg {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
