<template>
  <v-card>
    <v-card-title>
      <v-container>
        <!--<editor-fold desc="Stats">-->
        <v-row dense v-if="workflow.importProcessStats">
          <v-chip label dense class="mr-2">{{ $t('Количество обработанных строк') }}:
            {{ workflow.importProcessStats.processedRows }}
          </v-chip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" label dense class="mr-2" :class="{error:workflow.importProcessStats.errors>0}">
                {{ $t('Количество ошибок') }}: {{ workflow.importProcessStats.errors }}
              </v-chip>
            </template>
            {{ $t('Поля с ошибками обновляться не будут!') }}
          </v-tooltip>

          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" label dense class="mr-2">
                {{ $t('Новых записей') }}: {{ workflow.importProcessStats.newItems }}
              </v-chip>
            </template>
            {{ $t('Количество новых записей, которые будут добавлены в систему') }}
          </v-tooltip>

          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" label dense class="mr-2">
                {{ $t('Записей с изменениями') }}: {{ workflow.importProcessStats.updatedItems }}
              </v-chip>
            </template>
            {{ $t('Количество записей, которые будут обновлены в системе') }}
          </v-tooltip>
        </v-row>
        <!--</editor-fold>-->

        <!--<editor-fold desc="Filters">-->
        <v-row dense>
          <v-col>
            <v-text-field
                v-model="filter.search"
                append-icon="mdi-magnify"
                :label="$t('Поиск')"
                single-line
                hide-details
            ></v-text-field>
          </v-col>
          <v-col cols="auto">
            <v-select
                v-model="filter.hideNotUpdatedWithoutErrors"
                :label="$t('Скрыть строки без изменений и без ошибок')"
                :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]">
            </v-select>
          </v-col>
        </v-row>
        <!--</editor-fold>-->
      </v-container>
    </v-card-title>
    <v-card-text>
      <v-data-table
          dense
          :headers="headers"
          :items-per-page="defaultItemsPerTablePage"
          :items="filterRows(workflow.importedRows)"
          class="text-truncate">
        <template v-slot:item.rowNumber="{item}">
          <span>{{ item.rowNumber }}</span>
          <v-tooltip right v-if="item.errorCount>0">
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" x-small class="error ml-1 pa-2">
                <v-icon x-small color="white">mdi-flash-alert</v-icon>
                {{ item.errorCount }}
              </v-chip>
            </template>
            {{ $t('Количество ошибок при разборе строки документа') }}
          </v-tooltip>
        </template>

        <template v-slot:item.email="{item}">
          <span :class="{'new': item.new}">{{ item.email }}</span>
        </template>
        <template v-for="header in headers.filter(h=>h.format)"
                  v-slot:[`item.${header.value}`]="{value}">
            <span :key="header.value">
              <import-preview-table-cell :cell="value" :format="header.format"></import-preview-table-cell>
            </span>
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {UiConstants} from "@/components/uiconstants";
import {Prop} from "vue-property-decorator";
import {ImportEmployeeExcelRows} from "@/components/admin/employee/imp/admin.employee.import.service";
import ImportPreviewTableCell from "@/components/admin/imp/ImportPreviewTableCell.vue";
import {ExcelRowDataProperty, ImportConfig, ImportExcelRow, ImportWorkflow} from "@/components/admin/imp/import.base";

export class ImportPreviewFilter {
  public search = '';
  public hideNotUpdatedWithoutErrors = true;
}

export interface ImportPreviewDataHeader<T = any> extends DataTableHeader<T> {
  format?: string
}

@Component({components: {ImportPreviewTableCell}})
export default class ImportPreviewComponent<C extends ImportConfig, R extends ImportExcelRow>
    extends Vue {
  headers: ImportPreviewDataHeader[] = [];

  @Prop({required: true})
  workflow!: ImportWorkflow<C, R>;

  @Prop({required: true})
  headersLoader!: () => ImportPreviewDataHeader[];

  @Prop({required: true})
  filterFunction!: (rows: R[], filter: ImportPreviewFilter) => R[];

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new ImportPreviewFilter();


  created() {
    this.reloadHeaders();
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push(...this.headersLoader());
  }


  private formatDate(date: ExcelRowDataProperty<any>): string | undefined {
    return DateTimeUtils.formatFromIso(date?.importedValue);
  }

  private filterRows(items: ImportEmployeeExcelRows[]) {
    return items.filter((item) => {
      let result = true
      if (this.filter.hideNotUpdatedWithoutErrors) {
        result = result && (item.updatedCellsCount > 0 || item.errorCount > 0);
      }
      if (this.filter.search && this.filter.search.trim().length > 0) {
        const search = this.filter.search.trim().toLowerCase();
        result = result && (
            (item.displayName?.importedValue && item.displayName.importedValue.toLowerCase().indexOf(search) >= 0)
            || (item.email && item.email.toLowerCase().indexOf(search) >= 0)
        ) as boolean;
      }
      return result;
    });
  }

}
</script>

<style scoped lang="scss">
@import 'import.scss';

.table-cursor > > > tbody tr :hover {
  cursor: pointer;
}

</style>

