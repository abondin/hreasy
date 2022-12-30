<!-- Employees admin table -->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <v-text-field
            v-model="filter.search"
            append-icon="mdi-magnify"
            :label="$t('Поиск')"
            single-line
            hide-details
        ></v-text-field>
      </v-card-title>
      <v-card-text>
        <v-data-table
            dense
            :headers="headers"
            :items-per-page="defaultItemsPerTablePage"
            :items="data"
            :search="filter.search"
            sort-by="displayName"
            class="text-truncate">
          <template v-slot:item.email="{item}">
            <v-tooltip right v-if="item.errorCount>0">
              <template v-slot:activator="{ on }">
                <v-chip v-on="on" x-small class="error mr-1 pa-2" >{{ item.errorCount }}</v-chip>
              </template>
              {{ $t('Количество ошибок при разборе документа') }}
            </v-tooltip>
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
      <v-card-actions>
        <v-btn @click="$emit('back')">{{ $t('Изменить настройки') }}</v-btn>
        <v-btn @click="$emit('apply')">{{ $t('Обновить данные') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
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
  ImportEmployeeExcelRows
} from "@/components/admin/employee/imp/admin.employee.import.service";
import ImportPreviewTableCell from "@/components/admin/employee/imp/ImportPreviewTableCell.vue";

class Filter {
  public search = '';
}

interface ImportPreviewDataHeader<T = any> extends DataTableHeader<T> {
  format?: string
}

@Component({components: {ImportPreviewTableCell}})
export default class AdminEmployeesImportPreview extends Vue {
  headers: ImportPreviewDataHeader[] = [];

  @Prop({required: true})
  data!: ImportEmployeeExcelRows[];

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new Filter();


  created() {
    this.reloadHeaders();
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Email'), value: 'email', width: 280});
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280, format: 'string'});
    this.headers.push({text: this.$tc('Телефон'), value: 'phone', width: 150, format: 'string'});
    this.headers.push({
      text: this.$tc('Дата трудоустройства'),
      value: 'dateOfEmployment',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    this.headers.push({
      text: this.$tc('Дата увольнения'),
      value: 'dateOfDismissal',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    this.headers.push({
      text: this.$tc('День рождения'),
      value: 'birthday',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    this.headers.push({text: this.$tc('Позиция'), value: 'position', width: 200, format: 'dict'});
    this.headers.push({text: this.$tc('Подразделение'), value: 'department', width: 300, format: 'dict'});
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
    this.headers.push({text: this.$tc('Пол'), value: 'sex', width: 100, format: 'string'});
  }


  private formatDate(date: ExcelRowDataProperty<any>): string | undefined {
    return DateTimeUtils.formatFromIso(date?.importedValue);
  }


}
</script>

<style scoped lang="scss">
@import 'import.scss';

.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}

</style>

