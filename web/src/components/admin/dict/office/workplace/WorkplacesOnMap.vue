<!--
  Add and move workplaces on office location map
-->
<template>
  <svg
      v-html="data.officeLocationMap" ref="officeLocationMap"
      :class="{'selectOnMapModeEnabled': data.selectOnMapModeEnabled}"
      :style="`width: ${defaultMapSizes.width}; height: ${defaultMapSizes.height}`">
  </svg>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop, Watch} from "vue-property-decorator";
import {WorkplacesOnMapContainer} from "@/components/admin/dict/office/workplace/workplaces.data.container";
import WorkplaceOnMapUtils from "@/components/admin/dict/office/workplace/workplace-on-map-utils";


@Component({
  components: {}
})
export default class WorkplacesOnMap extends Vue {
  private defaultMapSizes = {width: "800px", height: "600px"};
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
      WorkplaceOnMapUtils.removeAllWorkplaces(svg);
      this.data.workplaces.forEach(w => WorkplaceOnMapUtils.getOrCreateWorkplaceIcon(svg, w, (w) => this.data.selectedWorkplace = w));
      WorkplaceOnMapUtils.highlightWorkplace(svg, this.data.selectedWorkplace);
    }
  }


}
</script>

<style lang="css">
.selectOnMapModeEnabled {
  cursor: crosshair;
}
</style>

