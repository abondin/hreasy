<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:additionalFields>
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
  DictOrganization,
  DictOrganizationUpdateBody
} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {
  BasicDictFilter
} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminOrganizations extends Vue {

  private data = new DictTableComponentDataContainer<DictOrganization, DictOrganizationUpdateBody, BasicDictFilter<DictOrganization>>(
      () => dictAdminService.loadOrganizations(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Описание'), value: 'description'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => dictAdminService.updateOrganization(id, body),
        itemEditable: (id, body) => true,
        itemToUpdateBody: item =>
            ({name: item.name, archived: item.archived, description: item.description} as DictOrganizationUpdateBody),
        createItemRequest: (body) => dictAdminService.createOrganization(body),
        defaultBody: () => ({name: '', archived: false} as DictOrganizationUpdateBody)
      } as CreateOrUpdateAction<DictOrganization, DictOrganizationUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictOrganizations()
  );
}
</script>

