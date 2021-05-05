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
          <v-btn v-if="item.archivedAt" icon>
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-btn v-else @click.stop="openDeleteDialog(item)" icon color="secondary">
            <v-icon>mdi-delete</v-icon>
          </v-btn>


          <v-btn text @click="openBADialog(item)">{{ item.name }}
          </v-btn>
        </template>
      </v-data-table>

      <v-dialog v-model="baDialog">
        <admin-b-a-position-form
            :business-account-id="businessAccountId"
            :input="selectedPosition" @close="baDialog=false;fetchData()"></admin-b-a-position-form>
      </v-dialog>

    </v-card-text>

    <!-- Delete position dialog -->
    <v-dialog v-if="itemToDelete"
              v-model="deleteDialog"
              width="500">
      <v-card>
        <v-card-title primary-title>
          {{ $t('Удаление') }}
        </v-card-title>

        <v-card-text>
          {{ $t('Вы уверены что хотите удалить запись?') }}
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              text
              @click="deleteDialog = false">
            {{ $t('Нет') }}
          </v-btn>
          <v-btn
              color="primary"
              @click="deleteItem()">
            {{ $t('Да') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
import overtimeService, {OvertimeItem} from "@/components/overtimes/overtime.service";

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
  private itemToDelete: BusinessAccountPosition | null = null;
  private deleteDialog = false;

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

  public refresh(){
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
        filtered = filtered && (!p.archivedAt);
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

  private openDeleteDialog(item: BusinessAccountPosition) {
    this.itemToDelete = item;
    this.$nextTick(() => {
      this.deleteDialog = true;
    });
  }

  private deleteItem() {
    if (this.itemToDelete) {
      adminBaService.archivePosition(this.businessAccountId, this.itemToDelete.id).then(() => {
        this.deleteDialog = false;
        return this.refresh();
      });
    } else {
      alert('Some error occurs. Item to delete not selected... Please contact administrator')
    }
  }



}
</script>
