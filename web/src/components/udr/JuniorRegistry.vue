<template>
  <v-card>
    <v-container>
      <!--<editor-fold desc="Errors from backend">-->
      <v-row v-if="data.error">
        <v-col>
          <v-alert type="error">{{ data.error }}</v-alert>
        </v-col>
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Types tabs and actions">-->
      <v-row class="ma-0 pa-0" v-if="data" dense>
        <v-col align-self="center" cols="auto">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
                <v-btn text icon @click="data.reloadData()">
                  <v-icon>refresh</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Обновить данные') }}</span>
          </v-tooltip>
        </v-col>

        <!-- //TODO Filter -->

        <v-spacer></v-spacer>


        <!--<editor-fold desc="Add new item">-->
        <v-col align-self="center" cols="auto">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="ma-0 at-0">
                <v-btn text color="primary" :disabled="data.loading || !data.editable()"
                       @click="()=>data.openCreateDialog()" icon>
                  <v-icon>mdi-plus</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Добавить в реестр') }}</span>
          </v-tooltip>
        </v-col>
        <!--</editor-fold>-->
        <!--<editor-fold desc="Export to excel">-->
        <v-col align-self="center" cols="auto" v-if="data.canExport()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="ma-0 at-0">
                <v-btn link :disabled="data.exportLoading" @click="data.exportToExcel()" icon>
                  <v-icon>mdi-file-excel</v-icon>
                </v-btn>
              </div>
            </template>
            <p>{{ $t('Экспорт в Excel') }}</p>
          </v-tooltip>
          <v-snackbar
              v-model="data.exportCompleted"
              timeout="5000"
          >
            {{ $t('Экспорт успешно завершён. Файл скачен.') }}
            <template v-slot:action="{ attrs }">
              <v-btn color="blue" icon v-bind="attrs" @click="data.confirmExportCompleted()">
                <v-icon>mdi-close-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-snackbar>
        </v-col>
        <!--</editor-fold>-->
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="The main table">-->
      <v-row v-if="data.initialized">
        <v-col>
          <v-data-table
              :loading="data.loading"
              :loading-text="$t('Загрузка_данных')"
              :headers="data.headers"
              v-model="data.selectedItems"
              :items="data.filteredItems()"
              sort-by="junior.name"
              dense
              :items-per-page="data.defaultItemsPerTablePage"
              class="text-truncate"
              :single-select=true
              :show-expand="$vuetify.breakpoint.mdAndUp"
              single-expand
          >
            <!--<editor-fold desc="Table columns">-->
            <template v-slot:item.createdAt="{ item }">
              {{ formatDateTime(item.createdAt) }}
            </template>
            <template v-slot:expanded-item="{ headers, item }">
              <td :colspan="headers.length">
                <div>//TODO {{item}}</div>
              </td>
            </template>
          </v-data-table>
        </v-col>
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Add new request dialog">-->
      <v-dialog v-bind:value="data.createDialog" :disabled="data.loading" persistent max-width="800">
        <hreasy-table-create-form v-bind:data="data" :title="$t('Добавление сотрудника в реестр')">
          <template v-slot:fields>
            <!--
            <salary-report-form-fields :create-body="data.createBody"></salary-report-form-fields>
            -->
          </template>
        </hreasy-table-create-form>
      </v-dialog>
      <!-- </editor-fold>-->


      <!--<editor-fold desc="Delete request">-->
      <v-dialog v-bind:value="data.deleteDialog" :disabled="data.loading" persistent max-width="800">
        <hreasy-table-delete-confimration v-bind:data="data"></hreasy-table-delete-confimration>
      </v-dialog>
      <!-- </editor-fold>-->

    </v-container>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {DataTableHeader} from "vuetify";
import {JuniorRegistryDataContainer} from "@/components/udr/junior_registry.data.container";


@Component({
  components: {
    HreasyTableDeleteConfimration,
    MyDateFormComponent,
    HreasyTableCreateForm
  }
})
export default class JuniorRegistry extends Vue {
  private data = new JuniorRegistryDataContainer(() => {
    const headers: DataTableHeader[] = [
      {text: this.$tc('Молодой специалист'), value: 'junior.name'}
    ];
    return headers;
  });

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Junior registry component created');
    this.data.init();
  }

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
}
</script>

