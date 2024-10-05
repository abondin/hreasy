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

export class MapPreviewDataContainer {
  private _fullscreen = false;
  private _img: string | null = null;
  private _mapSelectedListener: (() => any) | null = null;

  public show(svg: string) {
    this._img = svg;
    if (svg) {
      this._fullscreen = true;
      if (this._mapSelectedListener) {
        this._mapSelectedListener();
      }
    }
  }

  set mapSelectedListener(listener: () => any | null) {
    this._mapSelectedListener = listener;
  }

  public hide() {
    this._fullscreen = false;
    this._img = null;
  }

  get img(): string | null {
    return this._img;
  }

  get fullscreen(): boolean {
    return this._fullscreen;
  }

}

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
        WorkplaceOnMapUtils.initializeWorkplace(svgElement);
      });
    }
  }
}
</script>

<style scoped>

</style>