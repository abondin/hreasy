<template>
  <div>
    <dict-admin-table v-bind:data="data">
      <template v-slot:additionalFields>
        <v-autocomplete
            v-model="data.updateBody.officeId"
            clearable
            :items="allOffices"
            item-value="id" item-text="name"
            :label="$t('Офис')"
        ></v-autocomplete>
        <v-textarea
            v-model="data.updateBody.description"
            :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
            :label="$t('Описание')">
        </v-textarea>
      </template>
      <template v-slot:item.hasMapFile="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-chip small v-if="item.hasMapFile" v-bind="tattrs" v-on="ton"
                    @click="openDeleteDialog($event, item)">
              <v-icon>mdi-delete</v-icon>
            </v-chip>
            <v-chip small v-else v-bind="tattrs" v-on="ton"
                    @click="openUploadDialog($event, item)">
              <v-icon>mdi-upload</v-icon>
            </v-chip>
          </template>
          <span>{{ item.hasMapFile ? $t('Удалить карту') : $t('Загрузить карту') }}</span>
        </v-tooltip>
      </template>

    </dict-admin-table>
    <in-dialog-form :data="uploadMapAction" form-ref="uploadMapForm" :title="$t('Загрузить SVG файл с картой')">
      <template v-slot:fields>
        <my-file-uploader
            :file-id="'map-'+uploadMapAction.itemId"
            :post-action="getUploadOfficeLocationMapPath(uploadMapAction.itemId)"
            @close="uploadComplete"></my-file-uploader>
      </template>
      <template v-slot:actions>
        <span></span>
      </template>
    </in-dialog-form>

    <in-dialog-form :data="deleteMapAction" form-ref="deleteMapForm" :title="$t('Удалить карту офиса')" @submit="data.reloadData()">
      <template v-slot:fields>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить карту офиса?') }}
        </v-card-text>
      </template>
    </in-dialog-form>
  </div>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import dictAdminService, {
  DictOfficeLocation,
  DictOfficeLocationUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {
  BasicDictFilter
} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import MyFileUploader, {UploadCompleteEvent} from "@/components/shared/MyFileUploader.vue";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";

const namespace_dict = 'dict';
@Component({
  components: {InDialogForm, MyFileUploader, DictAdminTable}
})
export default class DictAdminOfficeLocations extends Vue {

  @Getter("offices", {namespace: namespace_dict})
  private allOffices!: Array<SimpleDict>;

  private deleteMapAction = new InDialogActionDataContainer<number, DictOfficeLocation>(
      (id) => dictAdminService.deleteOfficeLocationMap(id!));

  private uploadMapAction = new InDialogActionDataContainer<number, DictOfficeLocation>(
      () => Promise.resolve()
  );

  private data = new DictTableComponentDataContainer<DictOfficeLocation, DictOfficeLocationUpdateBody, BasicDictFilter<DictOfficeLocation>>(
      () => dictAdminService.loadOfficeLocations(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Офис'), value: 'office.name'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Карта'), value: 'hasMapFile'},
            {text: this.$tc('Архив'), value: 'archived'},
          ],
      {
        updateItemRequest: (id, body) => (dictAdminService.updateOfficeLocation(id, body)),
        itemEditable: (id, body) => true,
        itemToUpdateBody: item =>
            ({
              name: item.name,
              archived: item.archived,
              officeId: item.office?.id,
              description: item.description
            } as DictOfficeLocationUpdateBody),
        createItemRequest: (body) => (dictAdminService.createOfficeLocation(body)),
        defaultBody: () => ({name: '', archived: false} as DictOfficeLocationUpdateBody)
      } as CreateOrUpdateAction<DictOfficeLocation, DictOfficeLocationUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictOfficeLocations()
  );

  created() {
    this.$store.dispatch('dict/reloadOffices')
  }

  reload() {
    this.data.reloadData();
  }

  private getUploadOfficeLocationMapPath(officeLocationId: number) {
    return dictAdminService.getUploadOfficeLocationMapPath(officeLocationId)
  }

  private openDeleteDialog(event: Event, item: DictOfficeLocation) {
    this.deleteMapAction.openDialog(item.id, item);
    event.stopPropagation();
    event.preventDefault();
  }

  private openUploadDialog(event: Event, item: DictOfficeLocation) {
    this.uploadMapAction.openDialog(item.id, item);
    event.stopPropagation();
    event.preventDefault();
  }

  private uploadComplete(event: UploadCompleteEvent) {
    if (event && event.uploaded) {
      this.data.reloadData();
    }
    this.uploadMapAction.cancel();
  }

}
</script>

