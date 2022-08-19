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
import TableComponentDataContainer, {
  BasicDictFilter,
  CreateAction,
  UpdateAction
} from "@/components/admin/dict/TableComponentDataContainer";
import dictAdminService, {DictPosition, DictPositionUpdateBody} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminPositions extends Vue {

  private data = new TableComponentDataContainer<DictPosition, DictPositionUpdateBody, DictPositionUpdateBody, BasicDictFilter<DictPosition>>(
      () => dictAdminService.loadPositions(),
      () =>
          [
            {text: this.$tc('Наименования по штатному расписанию'), value: 'name'},
            {text: this.$tc('Категория'), value: 'category'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => dictAdminService.updatePosition(id, body),
        itemToUpdateBody: item =>
            ({name: item.name, archived: item.archived, category: item.category} as DictPositionUpdateBody)
      } as UpdateAction<DictPosition, DictPositionUpdateBody>,
      {
        createItemRequest: (body) => dictAdminService.createPosition(body),
        defaultBody: () => ({name: '', archived: false} as DictPositionUpdateBody)
      } as CreateAction<DictPosition, DictPositionUpdateBody>,
      null,
      new BasicDictFilter(),
      permissionService.canAdminDictPositions()
  );
}
</script>


