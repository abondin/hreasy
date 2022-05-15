<template>
  <v-container>
    <v-row v-if="data.error">
      <v-col>
        <v-alert type="error">{{ data.error }}</v-alert>
      </v-col>
    </v-row>
    <v-row dense>
      <v-col align-self="center" cols="auto">
        <!-- Refresh button -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
              <v-btn text icon @click="reloadData()">
                <v-icon>refresh</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Обновить данные') }}</span>
        </v-tooltip>
      </v-col>
      <!-- Add new item -->
      <v-tooltip bottom v-if="canEdit">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <div v-bind="tattrs" v-on="ton" class="col-auto">
            <v-btn text color="primary" :disabled="data.loading" @click="()=>data.openEditDialog(null)" icon>
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </div>
        </template>
        <span>{{ $t('Добавить новую запись') }}</span>
      </v-tooltip>

    </v-row>
    <v-row>
      <v-col>
        <v-data-table
            :loading="data.loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="data.headers"
            :items="filteredItems()"
            :sort-by="['name']"
            dense
            :items-per-page="data.defaultItemsPerTablePage"
            class="text-truncate table-cursor"
            @click:row="(v)=>data.openEditDialog(v)"
        >
          <template
              v-slot:item.archived="{ item }">
            {{ item.archived ? $t('Да') : $t('Нет') }}
          </template>
        </v-data-table>
        <dict-admin-department-form :data="data"></dict-admin-department-form>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer from "@/components/admin/dict/TableComponentDataContainer";
import permissionService from "@/store/modules/permission.service";
import dictAdminService, {DictDepartment, DictDepartmentUpdateBody} from "@/components/admin/dict/dict.admin.service";
import Vue from "vue";
import DictAdminDepartmentForm from "@/components/admin/dict/DictAdminDepartmentForm.vue";

@Component({
  components: {DictAdminDepartmentForm}
})
export default class DictAdminDepartments extends Vue {

  private data = new TableComponentDataContainer<DictDepartment, DictDepartmentUpdateBody>(
      () => dictAdminService.loadDepartments(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'},
            {text: this.$tc('Архив'), value: 'archived'}
          ],
      (id, body) => (id ? dictAdminService.updateDepartment(id, body)
          : dictAdminService.createDepartment(body)),
      item => ({name: item.name, archived: item.archived} as DictDepartmentUpdateBody)
  );

  /**
   * Lifecycle hook
   */
  protected created() {
    this.data.init();
  }

  protected canEdit(): boolean {
    return permissionService.canAdminDictDepartments();
  }

  protected reloadData() {
    return this.data.reloadData();
  }

  protected filteredItems(): DictDepartment[] {
    return this.data.items;
  }

}
</script>

<style scoped>

</style>
