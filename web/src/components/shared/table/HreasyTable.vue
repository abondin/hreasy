<!-- Generic table with search and crud actions -->
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
      <v-col align-self="center" cols="auto">
        <v-tooltip bottom v-if="data.createAllowed()" :disabled="!data.editable()">
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="data.loading" @click="()=>data.openCreateDialog()" icon>
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Добавить новую запись') }}</span>
        </v-tooltip>
      </v-col>

      <!-- Filters -->
      <v-divider vertical class="mr-5"></v-divider>
      <slot name="filters"></slot>
    </v-row>
    <v-row v-if="data.initialized">
      <v-col>
        <v-data-table
            :loading="data.loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="data.headers"
            :items="data.filteredItems()"
            :sort-by="['name']"
            dense
            :items-per-page="data.defaultItemsPerTablePage"
            class="text-truncate table-cursor"
            @click:row="(v)=>data.openUpdateDialog(v)"
        >
          <slot name="columnTemplates"></slot>
        </v-data-table>
        <hreasy-table-update-form v-bind:data="data">
          <template v-slot:fields>
            <slot name="updateFormFields">
            </slot>
          </template>
        </hreasy-table-update-form>
        <hreasy-table-create-form v-bind:data="data">
          <template v-slot:fields>
            <slot name="createFormFields">
            </slot>
          </template>
        </hreasy-table-create-form>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer, {
  CreateBody,
  Filter,
  UpdateBody,
  WithId
} from "@/components/shared/table/TableComponentDataContainer";
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import HreasyTableUpdateForm from "@/components/shared/table/HreasyTableUpdateForm.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";

@Component({
  components: {HreasyTableCreateForm, HreasyTableUpdateForm}
})
export default class HreasyTable<T extends WithId, M extends UpdateBody, C extends CreateBody, F extends Filter<T>> extends Vue {

  @Prop({required: true})
  private data!: TableComponentDataContainer<T, M, C, F>;


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
