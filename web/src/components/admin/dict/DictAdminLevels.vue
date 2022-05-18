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
import dictAdminService, {DictLevel, DictLevelUpdateBody} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminLevels extends Vue {

  private data = new TableComponentDataContainer<DictLevel, DictLevelUpdateBody, BasicDictFilter<DictLevel>>(
      () => dictAdminService.loadLevels(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Вес'), value: 'weight'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      (id, body) => (id ? dictAdminService.updateLevel(id, body)
          : dictAdminService.createLevel(body)),
      (item: DictLevel) =>
          ({name: item.name, archived: item.archived, weight: item.weight} as DictLevelUpdateBody),
      () => ({name: '', archived: false} as DictLevelUpdateBody),
      new BasicDictFilter(),
      permissionService.canAdminDictLevels()
  );
}
</script>

