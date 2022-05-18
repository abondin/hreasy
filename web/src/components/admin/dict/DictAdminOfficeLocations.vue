<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:editForm>
      Форма редактирования не готова
    </template>
  </dict-admin-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import TableComponentDataContainer, {BasicDictFilter} from "@/components/admin/dict/TableComponentDataContainer";
import dictAdminService, {
  DictOfficeLocation,
  DictOfficeLocationUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminOfficeLocations extends Vue {

  private data = new TableComponentDataContainer<DictOfficeLocation, DictOfficeLocationUpdateBody, BasicDictFilter<DictOfficeLocation>>(
      () => dictAdminService.loadOfficeLocations(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Офис'), value: 'office'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      (id, body) => (id ? dictAdminService.updateOfficeLocation(id, body)
          : dictAdminService.createOfficeLocation(body)),
      item =>
          ({
            name: item.name,
            archived: item.archived,
            office: item.office,
            description: item.description
          } as DictOfficeLocationUpdateBody),
      () => ({name: '', archived: false} as DictOfficeLocationUpdateBody),
      new BasicDictFilter(),
      permissionService.canAdminDictOfficeLocations()
  );
}
</script>

