<!-- Employees admin table -->
<template>
  <import-preview :workflow="workflow" :headers-loader="headersLoader" filter-function="filterFunction"/>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {ImportEmployeeExcelRows} from "@/components/admin/employee/imp/admin.employee.import.service";
import ImportPreviewTableCell from "@/components/admin/imp/ImportPreviewTableCell.vue";
import {ImportConfig, ImportExcelRow, ImportWorkflow} from "@/components/admin/imp/import.base";
import ImportPreview, {ImportPreviewDataHeader, ImportPreviewFilter} from "@/components/admin/imp/ImportPreview.vue";


@Component({components: {ImportPreview, ImportPreviewTableCell}})
export default class AdminEmployeesImportPreview<C extends ImportConfig, R extends ImportExcelRow>
    extends Vue {
  @Prop({required: true})
  workflow!: ImportWorkflow<C, R>;


  private headersLoader() {
    const headers: ImportPreviewDataHeader[] = [];
    headers.push({text: this.$tc('Строка'), value: 'rowNumber', width: 15});
    headers.push({text: this.$tc('Email'), value: 'email', width: 280});
    headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280, format: 'string'});
    headers.push({text: this.$tc('Телефон'), value: 'phone', width: 150, format: 'string'});
    headers.push({
      text: this.$tc('Дата трудоустройства'),
      value: 'dateOfEmployment',
      width: 50,
      class: "text-wrap",
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    headers.push({
      text: this.$tc('Дата увольнения'),
      value: 'dateOfDismissal',
      class: "text-wrap",
      width: 50,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    headers.push({
      text: this.$tc('День рождения'),
      value: 'birthday',
      class: "text-wrap",
      width: 50,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    headers.push({text: this.$tc('Позиция'), value: 'position', width: 200, format: 'dict'});
    headers.push({text: this.$tc('Подразделение'), value: 'department', width: 300, format: 'dict'});
    headers.push({text: this.$tc('Организация'), value: 'organization', width: 300, format: 'dict'});
    headers.push({
      text: this.$tc('IMPORT_CONFIG.documentSeries'),
      value: 'documentSeries',
      width: 35,
      format: 'string'
    });
    headers.push({
      text: this.$tc('IMPORT_CONFIG.documentNumber'),
      value: 'documentNumber',
      width: 34,
      format: 'string'
    });
    headers.push({
      text: this.$tc('IMPORT_CONFIG.documentIssuedDate'),
      value: 'documentIssuedDate',
      width: 100,
      format: 'date'
    });
    headers.push({
      text: this.$tc('IMPORT_CONFIG.documentIssuedBy'),
      value: 'documentIssuedBy',
      width: 150,
      format: 'string'
    });
    headers.push({
      text: this.$tc('Адрес по регистрации'),
      value: 'registrationAddress',
      width: 500,
      format: 'string'
    });
    headers.push({
      text: this.$tc('Идентификатор во внешней ERP системе'),
      value: 'externalErpId',
      width: 150,
      format: 'string'
    });
    headers.push({text: this.$tc('Пол'), value: 'sex', width: 100, format: 'string'});
    return headers;
  }

  private filterFunction(items: ImportEmployeeExcelRows[], filter: ImportPreviewFilter) {
    return items.filter((item) => {
      let result = true
      if (filter.hideNotUpdatedWithoutErrors) {
        result = result && (item.updatedCellsCount > 0 || item.errorCount > 0);
      }
      if (filter.search && filter.search.trim().length > 0) {
        const search = filter.search.trim().toLowerCase();
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


