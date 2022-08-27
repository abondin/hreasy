<template>
  <dict-admin-table v-bind:data="data">
  </dict-admin-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import permissionService from "@/store/modules/permission.service";
import dictAdminService, {DictDepartment, DictDepartmentUpdateBody} from "@/components/admin/dict/dict.admin.service";
import Vue from "vue";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {
  BasicDictFilter,
} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";


@Component({
  components: {DictAdminTable}
})
export default class DictAdminDepartments extends Vue {

  private data = new DictTableComponentDataContainer<DictDepartment, DictDepartmentUpdateBody, BasicDictFilter<DictDepartment>>(
      () => dictAdminService.loadDepartments(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => dictAdminService.updateDepartment(id, body),
        itemToUpdateBody: item =>
            ({name: item.name, archived: item.archived} as DictDepartmentUpdateBody),
        createItemRequest: (body) => dictAdminService.createDepartment(body),
        defaultBody: () => ({name: '', archived: false} as DictDepartmentUpdateBody)
      } as CreateOrUpdateAction<DictDepartment, DictDepartmentUpdateBody>,
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
