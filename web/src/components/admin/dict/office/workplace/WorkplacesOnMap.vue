<!--
  Add and move workplaces on office location map
-->
<template>
  <div
      v-html="data.officeLocationMap" ref="officeLocationMap"
      :style="`width: ${defaultMapSizes.width}; height: ${defaultMapSizes.height}`">

  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import {WorkplacesOnMapContainer} from "@/components/admin/dict/office/workplace/workplaces.data.container";


@Component({
  components: {}
})
export default class WorkplacesOnMap extends Vue {
  private defaultMapSizes = {width: "800px", height: "600px"};
  @Prop({required: true})
  private data!: WorkplacesOnMapContainer;

}
</script>

