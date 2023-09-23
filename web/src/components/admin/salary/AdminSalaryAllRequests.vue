<template>
  <hreasy-table :data="data">
    <template v-slot:filters>
      <v-col>
        <v-text-field v-if="data.filter"
                      v-model="data.filter.search"
                      append-icon="mdi-magnify"
                      :label="$t('Поиск')"
                      single-line
                      hide-details
        ></v-text-field>
      </v-col>
    </template>


    <template v-slot:createFormFields>
      <!-- name -->
      <v-text-field id="dict-form-name"
                    v-model="data.createBody.budgetBusinessAccount"
                    :counter="255"
                    :rules="[v=>(v && v.length <= 255 || $t('Обязательное поле. Не более N символов', {n:255}))]"
                    :label="$t('Наименование')"
                    required>
        >
      </v-text-field>

      <v-select
          v-model="data.createBody.type"
          :label="$t('Тип')"
          :items="[{value:1, text:'Повышение'}, {value:2, text:'Единоразовый бонус'}]">
      </v-select>

      <!-- Additional fields -->
      <slot name="additionalFields"></slot>


    </template>

  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer, {CreateAction, Filter} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import salaryService, {
  SalaryRequest,
  SalaryRequestReportBody,
  SalaryRequestType
} from "@/components/salary/salary.service";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";
import permissionService from "@/store/modules/permission.service";
import {Vue} from "vue-property-decorator";


export class SalaryRequestUpdateBody {

}

export class SalaryRequestFilter extends Filter<SalaryRequest> {
  public search = '';

  applyFilter(items: SalaryRequest[]): SalaryRequest[] {
    return items.filter((item) => {
      let filtered = true;
      const search = this.search.toLowerCase().trim();
      if (search.length > 0) {
        let searchFilter = false;
        const textFilters = TextFilterBuilder.of()
            .splitWords(item.employee?.name)
            .splitWords(item.createdBy?.name)
            .ignoreCase(item.employeeDepartment?.name)
            .ignoreCase(item?.budgetBusinessAccount.name)
            .ignoreCase(item?.reason);
        filtered = filtered && searchUtils.textFilter(this.search, textFilters);
      }
      return filtered;
    });
  }
}

@Component({
  components: {HreasyTable}
})
export default class AdminSalaryAllRequests extends Vue {
  private data = new TableComponentDataContainer<SalaryRequest, SalaryRequestUpdateBody, SalaryRequestReportBody, SalaryRequestFilter>(
      () => salaryService.loadAllSalaryRequests(),
      () =>
          [
            {text: this.$tc('Сотрудник'), value: 'employee.name'},
            {text: this.$tc('Тип'), value: 'type'}
          ],
      null,
      {
        createItemRequest: (body) => salaryService.reportSalaryRequest(body),
        defaultBody: () => this.defaultBody(),
      } as CreateAction<SalaryRequest, SalaryRequestReportBody>,
      null,
      new SalaryRequestFilter(),
      () => permissionService.canAdminDictDepartments(),
      true
  );

  private defaultBody(): SalaryRequestReportBody {
    return {
      type: SalaryRequestType.SALARY_INCREASE,
    } as SalaryRequestReportBody;
  }
}
</script>

