<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:additionalFields>
      <v-text-field
          v-model="data.updateBody.office"
          :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
          :label="$t('Офис')">
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
import dictAdminService, {
  DictOfficeLocation,
  DictOfficeLocationUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {
  BasicDictFilter,
  CreateOrUpdateAction
} from "@/components/admin/dict/DictTableComponentDataContainer";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminOfficeLocations extends Vue {

  private data = new DictTableComponentDataContainer<DictOfficeLocation, DictOfficeLocationUpdateBody, BasicDictFilter<DictOfficeLocation>>(
      () => dictAdminService.loadOfficeLocations(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Офис'), value: 'office'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => (dictAdminService.updateOfficeLocation(id, body)),
        itemToUpdateBody: item =>
            ({
              name: item.name,
              archived: item.archived,
              office: item.office,
              description: item.description
            } as DictOfficeLocationUpdateBody),
        createItemRequest: (body) => (dictAdminService.createOfficeLocation(body)),
        defaultBody: () => ({name: '', archived: false} as DictOfficeLocationUpdateBody)
      } as CreateOrUpdateAction<DictOfficeLocation, DictOfficeLocationUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictOfficeLocations()
  );
}
</script>

