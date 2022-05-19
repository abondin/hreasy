<template>
  <dict-admin-table v-bind:data="data">
  </dict-admin-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer, {BasicDictFilter} from "@/components/admin/dict/TableComponentDataContainer";
import permissionService from "@/store/modules/permission.service";
import dictAdminService, {DictDepartment, DictDepartmentUpdateBody} from "@/components/admin/dict/dict.admin.service";
import Vue from "vue";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";


@Component({
  components: {DictAdminTable}
})
export default class DictAdminDepartments extends Vue {

  private data = new TableComponentDataContainer<DictDepartment, DictDepartmentUpdateBody, BasicDictFilter<DictDepartment>>(
      () => dictAdminService.loadDepartments(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      (id, body) => (id ? dictAdminService.updateDepartment(id, body)
          : dictAdminService.createDepartment(body)),
      (item: DictDepartment) => ({name: item.name, archived: item.archived} as DictDepartmentUpdateBody),
      () => ({name: '', archived: false} as DictDepartmentUpdateBody),
      new BasicDictFilter(),
      permissionService.canAdminDictDepartments()
  );

  /**
   * Lifecycle hook
   */
  protected created() {
    this.data.init();
  }

  protected reloadData() {
    return this.data.reloadData();
  }


}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
