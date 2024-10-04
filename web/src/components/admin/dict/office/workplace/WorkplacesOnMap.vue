<!--
  Add and move workplaces on office location map
-->
<template>
  <svg
      v-html="data.officeLocationMap" ref="officeLocationMap"
      :width="defaultMapSizes.width" :height="defaultMapSizes.height"
      @click.stop="clickOnMap"
  >
  </svg>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop, Watch} from "vue-property-decorator";
import WorkplaceOnMapUtils from "@/components/admin/dict/office/workplace/workplace-on-map-utils";


export class WorkplaceOnMapData {
  officeLocationMap: string = '';
}

@Component({
  components: {}
})
export default class WorkplacesOnMap extends Vue {
  defaultMapSizes = WorkplaceOnMapUtils.defaultMapSizes;
  @Prop({required: true})
  private data!: WorkplacesOnMapContainer;

  @Watch("data.workplaces")
  public workplacesUpdated() {
    this.redrawWorkplaces();
  }

  @Watch("data.selectedWorkplace")
  public selectionChanged() {
    const svg = this.$refs.officeLocationMap as SVGElement | null;
    if (svg) {
      WorkplaceOnMapUtils.highlightWorkplace(svg, this.data.selectedWorkplace);
    }
  }

  public mounted() {
    this.redrawWorkplaces();
  }

  private redrawWorkplaces() {
    const svg = this.$refs.officeLocationMap as SVGElement | null;
    if (svg) {
      WorkplaceOnMapUtils.adjustSvgViewBox(svg);
      WorkplaceOnMapUtils.initializeWorkplace(svg);
      WorkplaceOnMapUtils.highlightWorkplace(svg, this.data.selectedWorkplace);
    }
  }


}
</script>

