<template>
  <v-card :loading="data.loading" v-if="data.filter.officeLocationId">
    <v-card-title>
      {{$t('Расположение рабочих мест в кабинете')}}
    </v-card-title>
    <v-card-text>
      <div v-if="data.officeLocationMap" v-html="data.officeLocationMap" ref="officeLocationMap"
           :style="`width: ${defaultMapSizes.width}; height: ${defaultMapSizes.height}`"></div>
      <div v-else>
        <v-alert type="info">{{$t('Для назначения мест необходимо загрузить карту кабинета в формате SVG')}}</v-alert>
        <v-chip outlined link class="ml-5" @click="data.openSvgMapDialog">
          <v-icon>mdi-plus</v-icon>
          {{ $t('Прикрепить файл') }}
        </v-chip>
      </div>
    </v-card-text>
    <v-dialog v-model="data.uploadSvgMapDialog" persistent>
      <file-upload
          :file-id="'svgMap-'+data.filter.officeLocationId"
          :post-action="data.getSvgMapUploadPath()"
          @close="uploadComplete"></file-upload>
    </v-dialog>
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

