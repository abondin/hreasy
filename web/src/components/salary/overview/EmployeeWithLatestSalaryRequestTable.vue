<template>
  <hreasy-table ref="overviewTable" :data="data"
                sortBy="employee.name"
                :load-data-on-init="true">
    <template v-slot:additionalActions>
      <employee-with-latest-salary-request-filter-component
          :filter="data.filter"></employee-with-latest-salary-request-filter-component>
    </template>
    <!--<editor-fold desc="Table columns">-->
    <template v-slot:item.requestStartPeriod="{ item }">
      {{ fromPeriodId(item.requestStartPeriod) }}
    </template>
    <template v-slot:item.employeeDisplayName="{ item}">
      <router-link v-if="item.requestId" :to="'/salaries/requests/'+item.requestStartPeriod+'/'+item.requestId">
        {{ item.employeeDisplayName }}
      </router-link>
      <span v-else>{{ item.employeeDisplayName }}</span>
    </template>
    <template v-slot:item.employeeCurrentProject="{ item}">
      {{ item.employeeCurrentProject?.name }}
      <span class="text-caption grey--text"
            v-if="item.employeeCurrentProject?.role">{{ item.employeeCurrentProject?.role }}</span>
    </template>
    <template v-slot:item.requestImplState="{ item }">
              <span v-if="item.requestImplState"
                    :class="item.requestImplState === REJECTED? 'error--text': 'success--text'">{{
                  item.requestImplState ? $t(`SALARY_REQUEST_STAT.${item.requestImplState}`) : ''
                }}</span>
    </template>
    <template v-slot:item.requestReqIncreaseAmount="{ item }">
      <span class="grey--text">{{ formatMoney(item.requestReqIncreaseAmount) }}</span>
    </template>
    <template v-slot:item.requestImplIncreaseAmount="{ item }">
      {{ formatMoney(item.requestImplIncreaseAmount) }}
    </template>
    <template v-slot:item.requestImplSalaryAmount="{ item }">
      {{ formatMoney(item.requestImplSalaryAmount) }}
    </template>

    <!--</editor-fold>-->

  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import {DataTableHeader} from "vuetify";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {
  EmployeeWithLatestSalaryRequestDataContainer
} from "@/components/salary/overview/employee-with-latest-salary-request.data.container";
import EmployeeWithLatestSalaryRequestFilterComponent
  from "@/components/salary/overview/EmployeeWithLatestSalaryRequestFilterComponent.vue";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";

@Component({
  name: 'EmployeeWithLatestSalaryRequestTable',
  components: {
    EmployeeWithLatestSalaryRequestFilterComponent,
    HreasyTable
  }
})
export default class EmployeeWithLatestSalaryRequestTable extends Vue {

  private REJECTED = SalaryRequestImplementationState.REJECTED;

  private data = new EmployeeWithLatestSalaryRequestDataContainer(
      () => {
        const headers: DataTableHeader[] = [
          {
            text: this.$tc('Сотрудник'),
            value: 'employeeDisplayName',
            width: '250px'
          },
          {
            text: this.$tc('Бизнес аккаунт'),
            value: 'employeeBusinessAccount.name',
            width: '200px'
          },
          {
            text: this.$tc('Проект'),
            value: 'employeeCurrentProject',
            width: '200px'
          },
          {
            text: this.$tc('Период'),
            value: 'requestStartPeriod',
            width: '100px'
          },
          {
            text: this.$tc('Запрошенное повышение'),
            value: 'requestReqIncreaseAmount',
            width: '100px',
            class: 'text-wrap'
          },
          {
            text: this.$tc('Итоговое повышение'),
            value: 'requestImplIncreaseAmount',
            width: '100px',
            class: 'text-wrap'
          },
          {
            text: this.$tc('Зарплата после повышения'),
            value: 'requestImplSalaryAmount',
            width: '100px',
            class: 'text-wrap'
          },
          {
            text: this.$tc('Статус'),
            value: 'requestImplState',
            width: '150px'
          }
        ];

        return headers;
      }
  );

  /**
   * Keep-alive hook
   */
  activated() {
    this.data.reloadData();
  }


  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;


}
</script>

<style scoped>

</style>
