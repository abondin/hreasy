<template>
  <v-dialog fullscreen v-if="data?.officeLocation" :value="data.opened" >
    <v-card>
      <v-card-title>
        {{ $t('Карта') + ": " + data.officeLocation.name }}
        <v-spacer></v-spacer>
        <v-btn icon @click="data.hide()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-alert type="error" v-if="data.error">
        {{ data.error }}
      </v-alert>
      <v-progress-circular v-if="data.loading"
                           indeterminate
                           size="64"
      ></v-progress-circular>
      <v-card-text class="full-card-text" v-else>
        <svg v-if="data.img" class="full-screen-svg"
             v-html="data.img" ref="map">
        </svg>
      </v-card-text>
    </v-card>
  </v-dialog>
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
.full-card-text{
  width: 90vw;
  height: 90vh;
}

.full-screen-svg {
  width: 100%;
  height: 100%;
}
</style>
