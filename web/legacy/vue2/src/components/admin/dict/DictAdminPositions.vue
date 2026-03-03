<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:additionalFields>
      <v-text-field id="dict-form-position-category"
                    v-model="data.updateBody.category"
                    :rules="[v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255}))]"
                    :label="$t('Категория')">
      </v-text-field>
    </template>
  </dict-admin-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import dictAdminService, {DictPosition, DictPositionUpdateBody} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {BasicDictFilter} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminPositions extends Vue {

  private data = new DictTableComponentDataContainer<DictPosition, DictPositionUpdateBody, BasicDictFilter<DictPosition>>(
      () => dictAdminService.loadPositions(),
      () =>
          [
            {text: this.$tc('Наименования по штатному расписанию'), value: 'name'},
            {text: this.$tc('Категория'), value: 'category'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => dictAdminService.updatePosition(id, body),
        itemEditable: (id, body)=>true,
        itemToUpdateBody: item =>
            ({name: item.name, archived: item.archived, category: item.category} as DictPositionUpdateBody),
        createItemRequest: (body) => dictAdminService.createPosition(body),
        defaultBody: () => ({name: '', archived: false} as DictPositionUpdateBody)
      } as CreateOrUpdateAction<DictPosition, DictPositionUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictPositions()
  );
}
</script>


