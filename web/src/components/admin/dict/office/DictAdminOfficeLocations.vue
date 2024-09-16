<template>
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
  </dict-admin-table>
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
import DictTableComponentDataContainer, {BasicDictFilter} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";

const namespace_dict = 'dict';
@Component({
  components: {DictAdminTable}
})
export default class DictAdminOfficeLocations extends Vue {

  @Getter("offices", {namespace: namespace_dict})
  private allOffices!: Array<SimpleDict>;

  private data = new DictTableComponentDataContainer<DictOfficeLocation, DictOfficeLocationUpdateBody, BasicDictFilter<DictOfficeLocation>>(
      () => dictAdminService.loadOfficeLocations(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Офис'), value: 'office.name'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => (dictAdminService.updateOfficeLocation(id, body)),
        itemEditable: (id, body)=>true,
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

  created(){
    this.$store.dispatch('dict/reloadOffices')
  }
}
</script>

