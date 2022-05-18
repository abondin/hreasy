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
import dictAdminService, {DictPosition, DictPositionUpdateBody} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminPositions extends Vue {

  private data = new TableComponentDataContainer<DictPosition, DictPositionUpdateBody, BasicDictFilter<DictPosition>>(
      () => dictAdminService.loadPositions(),
      () =>
          [
            {text: this.$tc('Наименования по штатному расписанию'), value: 'name'},
            {text: this.$tc('Категория'), value: 'category'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      (id, body) => (id ? dictAdminService.updatePosition(id, body)
          : dictAdminService.createPosition(body)),
      item =>
          ({name: item.name, archived: item.archived, category: item.category} as DictPositionUpdateBody),
      () => ({name: '', archived: false} as DictPositionUpdateBody),
      new BasicDictFilter(),
      permissionService.canAdminDictPositions()
  );
}
</script>

