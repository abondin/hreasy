<template>
  <import-workflow-component :on-complete-action="onCompleteAction" :import-service="importService"
                             :default-config-provider="defaultConfigProvider"
                             :preview-filter-function="previewFilterFunction"
                             :preview-headers-loader="previewHeadersLoader"
  />
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import ImportWorkflowComponent from "@/components/admin/imp/ImportWorkflowComponent.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import {ImportPreviewDataHeader, ImportPreviewFilter} from "@/components/admin/imp/ImportPreviewComponent.vue";
import adminEmployeeKidImportService, {
  EmployeeKidImportConfig,
  ImportEmployeeKidExcelRows
} from "@/components/admin/employee/imp/admin.employee.kid.import.service";


@Component({
  components: {ImportWorkflowComponent}
})
export default class AdminEmployeesKidsImportWorkflowComponent extends Vue {

  private importService = adminEmployeeKidImportService;

  onCompleteAction() {
    this.$router.push('/admin/employees/kids');
  }

  defaultConfigProvider(): EmployeeKidImportConfig {
    return {
      sheetNumber: 1,
      tableStartRow: 8,
      columns: {
        displayName: 'G',
        birthday: 'I',
        parentEmail: 'K'
      }
    }
  }


  private previewHeadersLoader() {
    const headers: ImportPreviewDataHeader[] = [];
    headers.push({text: this.$tc('Строка'), value: 'rowNumber', width: 15});
    headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280, format: 'string'});
    headers.push({
      text: this.$tc('День рождения'),
      value: 'birthday',
      class: "text-wrap",
      width: 50,
      sort: DateTimeUtils.dateComparatorNullLast, format: 'date'
    });
    headers.push({text: this.$tc('Сотрудник'), value: 'parent', width: 200, format: 'dict'});
    return headers;
  }

  private previewFilterFunction(items: ImportEmployeeKidExcelRows[], filter: ImportPreviewFilter) {
    return items.filter((item) => {
      let result = true
      if (filter.hideNotUpdatedWithoutErrors) {
        result = result && (item.updatedCellsCount > 0 || item.errorCount > 0);
      }
      if (filter.search && filter.search.trim().length > 0) {
        const search = filter.search.trim().toLowerCase();
        result = result && (
            (item.displayName && item.displayName.toLowerCase().indexOf(search) >= 0)
            || (item.parentEmail && item.parentEmail.toLowerCase().indexOf(search) >= 0)
            || (item.parent && item.parent.name?.toLowerCase().indexOf(search) >= 0)
        ) as boolean;
      }
      return result;
    });
  }

}
</script>

<style scoped>

</style>
