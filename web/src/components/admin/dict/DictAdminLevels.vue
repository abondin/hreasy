<template>
  <dict-admin-table v-bind:data="data">
    <template v-slot:additionalFields>
      <v-text-field
          v-model="data.updateBody.weight"
          type="number"
          :rules="[v=>(!v || (v <= 100 && v >=0) || $t('Число от до', {min:0,max:100}))]"
          :label="$t('Вес')">
      </v-text-field>
    </template>
  </dict-admin-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import Vue from "vue";
import dictAdminService, {DictLevel, DictLevelUpdateBody} from "@/components/admin/dict/dict.admin.service";
import permissionService from "@/store/modules/permission.service";
import DictAdminTable from "@/components/admin/dict/DictAdminTable.vue";
import DictTableComponentDataContainer, {BasicDictFilter} from "@/components/admin/dict/DictTableComponentDataContainer";
import {CreateOrUpdateAction} from "@/components/shared/table/EditTableComponentDataContainer";

@Component({
  components: {DictAdminTable}
})
export default class DictAdminLevels extends Vue {

  private data = new DictTableComponentDataContainer<DictLevel, DictLevelUpdateBody, BasicDictFilter<DictLevel>>(
      () => dictAdminService.loadLevels(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Вес'), value: 'weight'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      {
        updateItemRequest: (id, body) => dictAdminService.updateLevel(id, body),
        itemToUpdateBody: item =>
            ({name: item.name, archived: item.archived, weight: item.weight} as DictLevelUpdateBody),
        createItemRequest: (body) => dictAdminService.createLevel(body),
        defaultBody: () => ({name: '', archived: false} as DictLevelUpdateBody)
      } as CreateOrUpdateAction<DictLevel, DictLevelUpdateBody>,
      new BasicDictFilter(),
      permissionService.canAdminDictLevels()
  );
}
</script>

