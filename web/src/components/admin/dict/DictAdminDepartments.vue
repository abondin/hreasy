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
            class="text-truncate">

          <template v-slot:item.name="{ item }">
            <v-menu>
              <template v-slot:activator="{ on, attrs }">
                <v-btn small text v-bind="attrs" v-on="on">
                  {{ item.name }}
                </v-btn>
              </template>
              <v-list dense>
                <v-list-item>
                  <v-btn x-small text :disabled="!canEdit()" @click="data.openEditDialog(item)">
                    <v-icon>mdi-pencil</v-icon>
                    {{ $t('Редактировать') }}
                  </v-btn>
                </v-list-item>
                <v-list-item>
                  <v-btn x-small text @click="data.openDeleteDialog(item)">
                    <v-icon>mdi-recycle</v-icon>
                    {{ $t('Архив') }}
                  </v-btn>
                </v-list-item>
              </v-list>
            </v-menu>
          </template>
        </v-data-table>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer from "@/components/admin/dict/TableComponentDataContainer";
import permissionService from "@/store/modules/permission.service";
import dictAdminService, {DictDepartment} from "@/components/admin/dict/dict.admin.service";
import Vue from "vue";

@Component
export default class DictAdminDepartments extends Vue {

  private data = new TableComponentDataContainer<DictDepartment>(
      () => dictAdminService.loadDepartments(),
      () =>
          [
            {text: this.$tc('Наименования'), value: 'name'}
          ]
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

  protected reloadData(): DictDepartment[] {
    return this.data.reloadData();
  }

  protected filteredItems(): DictDepartment[] {
    return this.data.items;
  }

}
</script>

<style scoped>

</style>
