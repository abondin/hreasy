<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:additionalFields>
      <v-text-field
          v-model="data.updateBody.address"
          :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
          :label="$t('Адрес')">
      </v-text-field>
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
import dictAdminService, {DictOffice, DictOfficeUpdateBody} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {
  BasicDictFilter
} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminOffices extends Vue {

  private data = new DictTableComponentDataContainer<DictOffice, DictOfficeUpdateBody, BasicDictFilter<DictOffice>>(
      () => dictAdminService.loadOffices(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Адрес'), value: 'address'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => (dictAdminService.updateOffice(id, body)),
        itemEditable: (id, body) => true,
        itemToUpdateBody: item =>
            ({
              name: item.name,
              archived: item.archived,
              address: item.address,
              description: item.description
            } as DictOfficeUpdateBody),
        createItemRequest: (body) => (dictAdminService.createOffice(body)),
        defaultBody: () => ({name: '', archived: false} as DictOfficeUpdateBody)
      } as CreateOrUpdateAction<DictOffice, DictOfficeUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictOfficeLocations()
  );

}
</script>

