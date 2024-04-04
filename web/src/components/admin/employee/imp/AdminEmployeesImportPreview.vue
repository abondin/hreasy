<!-- Employees admin table -->
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
                {{ $t('Новых сотрудников') }}: {{ workflow.importProcessStats.newItems }}
              </v-chip>
            </template>
            {{ $t('Количество новых сотрудников, которые будут добавлены в систему') }}
          </v-tooltip>

          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-chip v-on="on" label dense class="mr-2">
                {{ $t('Сотрудников с изменениями') }}: {{ workflow.importProcessStats.updatedItems }}
              </v-chip>
            </template>
            {{ $t('Количество сотрудников, которые будут обновлены в системе') }}
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
              <v-chip v-on="on" x-small class="error ml-1 pa-2"><v-icon x-small color="white">mdi-flash-alert</v-icon> {{ item.errorCount }}</v-chip>
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
import {
  ExcelRowDataProperty,
  ImportEmployeeExcelRows,
  ImportEmployeesWorkflow
} from "@/components/admin/employee/imp/admin.employee.import.service";
import ImportPreviewTableCell from "@/components/admin/employee/imp/ImportPreviewTableCell.vue";

class Filter {
  public search = '';
  public hideNotUpdatedWithoutErrors = true;
}

interface ImportPreviewDataHeader<T = any> extends DataTableHeader<T> {
  format?: string
}

@Component({components: {ImportPreviewTableCell}})
export default class AdminEmployeesImportPreview extends Vue {
  headers: ImportPreviewDataHeader[] = [];

  @Prop({required: true})
  workflow!: ImportEmployeesWorkflow;

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new Filter();


  created() {
    this.reloadHeaders();
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Строка'), value: 'rowNumber', width: 15});
    this.headers.push({text: this.$tc('Email'), value: 'email', width: 280});
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280, format: 'string'});
    this.headers.push({text: this.$tc('Телефон'), value: 'phone', width: 150, format: 'string'});
    this.headers.push({
      text: this.$tc('Дата трудоустройства'),
      value: 'dateOfEmployment',
      width: 50,
      class: "text-wrap",
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    this.headers.push({
      text: this.$tc('Дата увольнения'),
      value: 'dateOfDismissal',
      class: "text-wrap",
      width: 50,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    this.headers.push({
      text: this.$tc('День рождения'),
      value: 'birthday',
      class: "text-wrap",
      width: 50,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    this.headers.push({text: this.$tc('Позиция'), value: 'position', width: 200, format: 'dict'});
    this.headers.push({text: this.$tc('Подразделение'), value: 'department', width: 300, format: 'dict'});
    this.headers.push({text: this.$tc('Организация'), value: 'organization', width: 300, format: 'dict'});
    this.headers.push({
      text: this.$tc('IMPORT_CONFIG.documentSeries'),
      value: 'documentSeries',
      width: 35,
      format: 'string'
    });
    this.headers.push({
      text: this.$tc('IMPORT_CONFIG.documentNumber'),
      value: 'documentNumber',
      width: 34,
      format: 'string'
    });
    this.headers.push({
      text: this.$tc('IMPORT_CONFIG.documentIssuedDate'),
      value: 'documentIssuedDate',
      width: 100,
      format: 'date'
    });
    this.headers.push({
      text: this.$tc('IMPORT_CONFIG.documentIssuedBy'),
      value: 'documentIssuedBy',
      width: 150,
      format: 'string'
    });
    this.headers.push({
      text: this.$tc('Адрес по регистрации'),
      value: 'registrationAddress',
      width: 500,
      format: 'string'
    });
    this.headers.push({
      text: this.$tc('Идентификатор во внешней ERP системе'),
      value: 'externalErpId',
      width: 150,
      format: 'string'
    });
    this.headers.push({text: this.$tc('Пол'), value: 'sex', width: 100, format: 'string'});
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

