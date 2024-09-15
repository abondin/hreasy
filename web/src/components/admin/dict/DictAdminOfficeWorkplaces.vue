<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:additionalFields>
      <v-autocomplete
          v-model="data.updateBody.officeLocationId"
          clearable
          :items="allOfficeLocations"
          item-value="id" item-text="name"
          :label="$t('Кабинет')"
      ></v-autocomplete>
      <v-textarea
          v-model="data.updateBody.description"
          :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
          :label="$t('Описание')">
      </v-textarea>
    </template>
  </dict-admin-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import dictAdminService, {
  DictOfficeWorkplace,
  DictOfficeWorkplaceUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {
  BasicDictFilter
} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";

const namespace_dict = 'dict';
@Component({
  components: {DictAdminTable}
})
export default class DictAdminOfficeWorkplaces extends Vue {

  @Getter("officeLocations", {namespace: namespace_dict})
  private allOfficeLocations!: Array<SimpleDict>;

  private data = new DictTableComponentDataContainer<DictOfficeWorkplace, DictOfficeWorkplaceUpdateBody, BasicDictFilter<DictOfficeWorkplace>>(
      () => dictAdminService.loadOfficeWorkplaces(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Кабинет'), value: 'office.name'},
            {text: this.$tc('Кабинет'), value: 'officeLocation.name'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => (dictAdminService.updateOfficeWorkplace(id, body)),
        itemEditable: (id, body) => true,
        itemToUpdateBody: item =>
            ({
              name: item.name,
              archived: item.archived,
              officeLocationId: item.officeLocation?.id,
              description: item.description,
              mapPosition: item.mapPosition
            } as DictOfficeWorkplaceUpdateBody),
        createItemRequest: (body) => (dictAdminService.createOfficeWorkplace(body)),
        defaultBody: () => ({name: '', archived: false} as DictOfficeWorkplaceUpdateBody)
      } as CreateOrUpdateAction<DictOfficeWorkplace, DictOfficeWorkplaceUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictOfficeLocations()
  );

  created() {
    this.$store.dispatch('dict/reloadOfficeLocations')
  }
}
</script>

