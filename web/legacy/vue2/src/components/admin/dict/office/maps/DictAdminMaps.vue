<template>
  <v-container v-if="data">
    <v-row v-if="data.error">
      <v-col>
        <v-alert type="error">{{ data.error }}</v-alert>
      </v-col>
    </v-row>
    <v-row dense>
      <v-col>
        {{ $t('Карты офисов и кабинетов') }}
      </v-col>
      <!-- Refresh button -->
      <v-col align-self="center" cols="auto">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
              <v-btn text icon @click="data.reload()">
                <v-icon>refresh</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Обновить данные') }}</span>
        </v-tooltip>
      </v-col>
      <!-- Add new item -->
      <v-col align-self="center" cols="auto">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-btn icon color="primary" v-bind="tattrs" v-on="ton"
                   @click="openUploadDialog($event)">
              <v-icon>mdi-upload</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('Загрузить карту') }}</span>
        </v-tooltip>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-data-table
            :loading="data.loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="data.maps"
            sort-by="mapName"
            dense
            @click:row="item=>previewMapAction.show(item.mapName, item.mapName)"
            :items-per-page="defaultItemsPerTablePage"
            class="text-truncate table-cursor"
        >
          <template v-slot:item.mapName="{ item }">
            <div class="d-flex">
              <span>{{ item.mapName }}</span>
              <div class="ml-auto">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on: ton, attrs: tattrs}">
                    <v-btn color="error" x-small link icon v-bind="tattrs" v-on="ton"
                           @click="openDeleteDialog($event, item.mapName)">
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ $t('Удалить карту') }}</span>
                </v-tooltip>
              </div>
            </div>
          </template>
        </v-data-table>
      </v-col>
    </v-row>
    <in-dialog-form :data="uploadMapAction" form-ref="uploadMapForm" :title="$t('Загрузить SVG файл с картой')">
      <template v-slot:fields>
        <my-file-uploader
            :file-id="'map-uploading'"
            :post-action="getUploadOfficeLocationMapPath()"
            @close="uploadComplete"></my-file-uploader>
      </template>
      <template v-slot:actions>
        <span></span>
      </template>
    </in-dialog-form>

    <in-dialog-form :data="deleteMapAction" form-ref="deleteMapForm" :title="$t('Удалить карту офиса')"
                    @submit="data.reload()">
      <template v-slot:fields>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить карту офиса?') }}
        </v-card-text>
      </template>
    </in-dialog-form>

    <map-preview-component :data="previewMapAction"></map-preview-component>

  </v-container>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import MapPreviewDataContainer from "@/components/admin/dict/office/maps/MapPreviewDataContainer";
import MapsTableDataContainer from "@/components/admin/dict/office/maps/MapsTableDataContainer";
import {UiConstants} from "@/components/uiconstants";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import MyFileUploader, {UploadCompleteEvent} from "@/components/shared/MyFileUploader.vue";
import dictAdminService from "@/components/admin/dict/dict.admin.service";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";
import {OfficeOrOfficeLocationMap} from "@/store/modules/dict.service";
import MapPreviewComponent from "@/components/admin/dict/office/maps/MapPreviewComponent.vue";

@Component({
  components: {MapPreviewComponent, MyFileUploader, InDialogForm, HreasyTable}
})
export default class DictAdminMaps extends Vue {
  private previewMapAction = new MapPreviewDataContainer();
  private uploadMapAction = new InDialogActionDataContainer<number, OfficeOrOfficeLocationMap>(
      () => Promise.resolve()
  );
  private deleteMapAction = new InDialogActionDataContainer<string, OfficeOrOfficeLocationMap>(
      (filename) => dictAdminService.deleteOfficeLocationMap(filename!));

  private data = new MapsTableDataContainer();
  private headers = [
    {text: this.$tc('Файл'), value: 'mapName'}
  ];

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private getUploadOfficeLocationMapPath() {
    return dictAdminService.getUploadOfficeLocationMapPath()
  }

  created() {
    this.data.reload();
  }


  private openUploadDialog(event: Event) {
    this.uploadMapAction.openDialog(null, null);
    event.stopPropagation();
    event.preventDefault();
  }

  private openDeleteDialog(event: Event, filename: string) {
    this.deleteMapAction.openDialog(filename, null);
    event.stopPropagation();
    event.preventDefault();
  }

  private uploadComplete(event: UploadCompleteEvent) {
    if (event && event.uploaded) {
      this.data.reload();
    }
    this.uploadMapAction.cancel();
  }
}
</script>
<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
