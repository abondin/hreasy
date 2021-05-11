<!-- Business accounts positions table -->
<template>
  <v-card>
    <v-card-title>
      <div class="mr-2">{{ $t('Позиции бизнес акаунта') }}</div>
      <!-- Refresh button -->
      <v-text-field
          v-model="filter.search"
          :label="$t('Поиск')" class="mr-5 ml-5"></v-text-field>
      <v-checkbox :label="$t('Показать архивные позиции')" v-model="filter.showArchived">
      </v-checkbox>
      <v-divider vertical class="mr-5 ml-5"></v-divider>
      <!-- Add new businessAccount position -->
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <div v-bind="tattrs" v-on="ton" class="col-auto">
            <v-btn text color="primary" :disabled="loading" @click="openBADialog(undefined)" icon>
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </div>
        </template>
        <span>{{ $t('Создать новую позицию') }}</span>
      </v-tooltip>

    </v-card-title>
    <v-card-text>
      <v-data-table
          :loading="loading"
          :loading-text="$t('Загрузка_данных')"
          :headers="headers"
          :items="filteredPositions()"
          hide-default-footer
          sort-by="name"
          sort
          disable-pagination>
        <template v-slot:item.name="{ item }">
          <v-btn text @click="openBADialog(item)" :class="{archived:item.archived}">{{ item.name }}
          </v-btn>
        </template>
      </v-data-table>

      <v-dialog v-model="baDialog">
        <admin-b-a-position-form
            :business-account-id="businessAccountId"
            :input="selectedPosition" @close="baDialog=false;fetchData()"></admin-b-a-position-form>
      </v-dialog>

    </v-card-text>

  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import Component from "vue-class-component";
import logger from "@/logger";
import adminBaService, {BusinessAccountPosition} from "@/components/admin/business_account/admin.ba.service";
import {Prop} from "vue-property-decorator";
import AdminBAPositionForm from "@/components/admin/business_account/AdminBAPositionForm.vue";

class Filter {
  public showArchived = false;
  public search = '';
}

@Component({
      components: {AdminBAPositionForm}
    }
)
export default class AdminBAPositions extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  @Prop({required: true})
  private businessAccountId!: number;

  @Prop({default: true})
  private loadOnCreate!: boolean;

  positions: BusinessAccountPosition[] = [];

  private filter = new Filter();

  private baDialog = false;
  private selectedPosition: BusinessAccountPosition | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin BA Position component created');
    this.reloadHeaders();
    if (this.loadOnCreate) {
      this.fetchData();
    }
  }

  public refresh() {
    this.fetchData();
  }

  private fetchData() {
    this.loading = true;
    this.positions = [];
    return adminBaService.findPositions(this.businessAccountId)
        .then(positions => {
          this.positions = positions;
        }).finally(() => {
          this.loading = false
        });
  }


  private filteredPositions() {
    return this.positions.filter((p) => {
      let filtered = true;
      if (!this.filter.showArchived) {
        filtered = filtered && (!p.archived);
      }
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        filtered = filtered &&
            (
                (p.name.toLowerCase().indexOf(search) >= 0) ||
                (p.description && p.description.toLowerCase().indexOf(search) >= 0)
            ) as boolean
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Наименование'), value: 'name'});
    this.headers.push({text: this.$tc('Описание'), value: 'description'});
    this.headers.push({text: this.$tc('Ставка (с НДС)'), value: 'rate'});
  }

  public openBADialog(positionToUpdate: BusinessAccountPosition | null) {
    this.selectedPosition = positionToUpdate;
    this.baDialog = true;
  }

}
</script>
<style>
.archived{
  text-decoration: line-through;
}
</style>
