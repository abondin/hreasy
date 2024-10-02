<!--
  Add and move workplaces on office location map
-->
<template>
  <svg
      v-html="data.officeLocationMap" ref="officeLocationMap"
      :width="defaultMapSizes.width" :height="defaultMapSizes.height"
      :class="{'selectOnMapModeEnabled': data.selectedWorkplace}"
      @click.stop="clickOnMap"
  >
  </svg>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop, Watch} from "vue-property-decorator";
import {WorkplacesOnMapContainer} from "@/components/admin/dict/office/workplace/workplaces.data.container";
import WorkplaceOnMapUtils from "@/components/admin/dict/office/workplace/workplace-on-map-utils";
import {DictOfficeWorkplace} from "@/components/admin/dict/dict.admin.service";


@Component({
  components: {}
})
export default class WorkplacesOnMap extends Vue {
  private defaultMapSizes = WorkplaceOnMapUtils.defaultMapSizes;
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
      WorkplaceOnMapUtils.removeAllWorkplaces(svg);
      this.data.workplaces.forEach(w => WorkplaceOnMapUtils.getOrCreateWorkplaceIcon(svg, w, this.selectWorkplaceOnMap));
      WorkplaceOnMapUtils.highlightWorkplace(svg, this.data.selectedWorkplace);
    }
  }

  private clickOnMap(e: MouseEvent) {
    if (this.data?.selectedWorkplace) {
      this.data.createOrUpdateWorkplaceAction.openDialogForWorkplace(this.data.selectedWorkplace.officeLocation.id
          , this.data.selectedWorkplace
          , {x: e.offsetX, y: e.offsetY});
    }
  }

  private selectWorkplaceOnMap(selectedWorkplace: DictOfficeWorkplace) {
    if (this.data.selectedWorkplace === selectedWorkplace) {
      this.data.selectedWorkplace = null;
    } else {
      this.data.selectedWorkplace = selectedWorkplace;
    }
  }


}
</script>

<style lang="css">
.selectOnMapModeEnabled {
  cursor: crosshair;
}
</style>

