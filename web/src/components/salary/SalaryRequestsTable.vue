<template>
  <hreasy-table ref="requestsTable" :data="data"
                :load-data-on-init="true">
    <template v-slot:additionalActions>
      <hreasy-table-export-to-excel-action :data=data></hreasy-table-export-to-excel-action>
      <!--<editor-fold desc="Types tabs">-->
      <v-col align-self="center" cols="auto">
        <v-btn-toggle v-model="data.filter.type" color="primary" group>
          <v-btn :value=1 class="mr-0">
            {{ $t('Повышения') }}
            <v-tooltip top>
              <template v-slot:activator="{ on, attrs }">
                <span v-on="on" v-bind="attrs" class="text-caption">({{ increasesCountMsg() }})</span>
              </template>
              {{ $t('Реализовано/Всего') }}
            </v-tooltip>
          </v-btn>
          <v-btn :value=2 class="ml-0">
            {{ $t('Бонусы') }}
            <v-tooltip top>
              <template v-slot:activator="{ on, attrs }">
                <span v-on="on" v-bind="attrs" class="text-caption">({{ bonusCountMsg() }})</span>
              </template>
              {{ $t('Реализовано/Всего') }}
            </v-tooltip>

          </v-btn>
        </v-btn-toggle>
      </v-col>
      <!--</editor-fold>-->
      <v-col align-self="center" cols="auto" class="mr-0 pr-0">
        <hreasy-table-select-period-action :data="data"></hreasy-table-select-period-action>
      </v-col>
      <salary-request-filter-component :filter="data.filter"></salary-request-filter-component>
    </template>
  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import {SalaryRequestType} from "@/components/salary/salary.service";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";
import {DataTableHeader} from "vuetify";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import HreasyTableExportToExcelAction from "@/components/shared/table/HreasyTableExportToExcelAction.vue";
import HreasyTableSelectPeriodAction from "@/components/shared/table/HreasyTableSelectPeriodAction.vue";
import SalaryRequestFilterComponent from "@/components/salary/SalaryRequestFilterComponent.vue";

@Component({
  components: {SalaryRequestFilterComponent, HreasyTableSelectPeriodAction, HreasyTableExportToExcelAction, HreasyTable}
})
export default class SalaryRequestsTable extends Vue {
  private data = new SalaryRequestDataContainer(() => {
    const headers: DataTableHeader[] = [
      {text: this.$tc('Сотрудник'), value: 'employee.name'}
    ];
    if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
      headers.push(
          {
            text: this.$tc('Предполагаемое изменение на'),
            value: 'req.increaseAmount',
            width: "150px",
            class: "text-wrap"
          }
      );
    } else {
      headers.push({
            text: this.$tc('Предполагаемая сумма бонуса'),
            value: 'req.increaseAmount',
            width: "150px",
            class: "text-wrap"
          }
      );
    }
    headers.push(
        {text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name'}
    );
    if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
      headers.push(
          {text: this.$tc('Итоговое изменение на'), value: 'impl.increaseAmount'},
          {text: this.$tc('Итоговая сумма'), value: 'impl.salaryAmount'},
      );
    } else {
      headers.push(
          {text: this.$tc('Итоговая сумма бонуса'), value: 'impl.increaseAmount'}
      );
    }
    headers.push(
        {text: this.$tc('Решение'), value: 'impl.state'}
    );
    return headers;
  });


  private bonusCountMsg(): string {
    return this.data.items.filter(i => i.type == SalaryRequestType.BONUS && i.impl).length
        + "/" +
        this.data.items.filter(i => (i.type == SalaryRequestType.BONUS)).length;
  }

  private increasesCountMsg(): string {
    return this.data.items.filter(i => i.type == SalaryRequestType.SALARY_INCREASE && i.impl).length
        + "/" +
        this.data.items.filter(i => (i.type == SalaryRequestType.SALARY_INCREASE)).length;
  }

}
</script>

<style scoped>

</style>