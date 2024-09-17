<template>
  <v-card :loading="data.loading" v-if="data.filter.officeLocationId">
    <v-card-title>
      {{ $t('Расположение рабочих мест в кабинете') }}
      <v-spacer></v-spacer>
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
            <v-btn :disabled="!data.filter.officeLocationId ||!data.officeLocationMap" text icon
                   @click="data.deleteMapAction.openDialog(data.filter.officeLocationId, null)">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </div>
        </template>
        <span>{{ $t('Удалить карту') }}</span>
      </v-tooltip>
    </v-card-title>
    <v-card-text class="d-flex align-center justify-center">
      <div v-if="data.officeLocationMap" v-html="data.officeLocationMap" ref="officeLocationMap"
           :style="`width: ${defaultMapSizes.width}; height: ${defaultMapSizes.height}`"></div>
      <div v-else>
        <v-alert type="info">{{ $t('Для назначения мест необходимо загрузить карту кабинета в формате SVG') }}</v-alert>
        <v-chip outlined link class="ml-5" @click="data.openSvgMapDialog()">
          <v-icon>mdi-plus</v-icon>
          {{ $t('Прикрепить файл') }}
        </v-chip>
      </div>
    </v-card-text>
    <v-dialog v-model="data.uploadSvgMapDialog" persistent>
      <my-file-uploader
          :file-id="'svgMap-'+data.filter.officeLocationId"
          :post-action="data.getSvgMapUploadPath()"
          @close="mapUploadComplete"></my-file-uploader>
    </v-dialog>

    <in-dialog-form size="lg" form-ref="deleteMapForm" :data="data.deleteMapAction"
                    :title="$t('Удалить план кабинета')"
                    v-on:submit="reload()">
      <template v-slot:fields>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить план кабинета?') }}
        </v-card-text>
      </template>
    </in-dialog-form>

  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import WorkplacesDataContainer from "@/components/admin/dict/office/workplace/workplaces.data.container";
import MyFileUploader, {UploadCompleteEvent} from "@/components/shared/MyFileUploader.vue";
import logger from "@/logger";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";


@Component({
  components: {InDialogForm, MyFileUploader}
})
export default class WorkspacesOnMap extends Vue {
  private defaultMapSizes = {width: "800px", height: "600px"};
  @Prop({required: true})
  private data!: WorkplacesDataContainer;


  private mapUploadComplete(event: UploadCompleteEvent) {
    logger.log(`Upload complete ${event}`);
    this.data.closeSvgMapDialog();
    if (event && event.uploaded) {
      this.reload();
    }
  }

  private reload() {
    this.data.loadOfficeLocationMap();
  }

}
</script>

