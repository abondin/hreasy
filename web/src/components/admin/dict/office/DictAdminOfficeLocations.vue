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
        <v-tooltip bottom v-if="item.hasMapFile">
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <v-chip small v-bind="tattrs" v-on="ton"
                    @click="openPreviewDialog($event, item)">
              <v-icon>mdi-map</v-icon>
            </v-chip>
          </template>
          <span>{{ $t('Посмотреть карту') }}</span>
        </v-tooltip>
      </template>

    </dict-admin-table>

    <map-preview-component :data="previewMapAction"></map-preview-component>

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
import MyFileUploader from "@/components/shared/MyFileUploader.vue";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import MapPreviewComponent from "@/components/admin/dict/office/maps/MapPreviewComponent.vue";
import MapPreviewDataContainer from "@/components/admin/dict/office/maps/MapPreviewDataContainer";

const namespace_dict = 'dict';
@Component({
  components: {MapPreviewComponent, InDialogForm, MyFileUploader, DictAdminTable}
})
export default class DictAdminOfficeLocations extends Vue {

  @Getter("offices", {namespace: namespace_dict})
  private allOffices!: Array<SimpleDict>;

  private previewMapAction = new MapPreviewDataContainer();

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

  private openPreviewDialog(event: Event, item: DictOfficeLocation) {
    event.stopPropagation();
    event.preventDefault();
    this.previewMapAction.show(item.mapName);
  }


}
</script>

