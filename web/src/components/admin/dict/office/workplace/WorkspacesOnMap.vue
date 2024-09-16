<template>
  <v-card :loading="data.loading">
    <v-card-title>
      {{$t('Расположение рабочих мест в кабинете')}}
    </v-card-title>
    <v-card-text>
      <div v-if="data.officeLocationMap" v-html="data.officeLocationMap" ref="officeLocationMap"
           :style="`width: ${defaultMapSizes.width}; height: ${defaultMapSizes.height}`"></div>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";


@Component({
  components: {}
})
export default class WorkspacesOnMap extends Vue {
  private defaultMapSizes = {width: "800px", height: "600px"};
  @Prop({required: true})
  private data!: WorkplacesDataContainer;


}
</script>

