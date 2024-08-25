<template>
  <hreasy-table ref="requestsTable" :data="data"
                sortBy="employee.name"
                :create-new-title="createNewTitle"
                :load-data-on-init="false">
    <!--<editor-fold desc="Actions">-->
    <template v-slot:additionalActions>
      <hreasy-table-export-to-excel-action :data=data></hreasy-table-export-to-excel-action>
      <!--<editor-fold desc="Types tabs">-->
      <v-col align-self="center" cols="auto">
        <v-btn-toggle mandatory v-model="data.filter.type" color="primary" group>
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
    <!--</editor-fold>-->

    <!--<editor-fold desc="Add new request or bonus form">-->
    <template v-slot:createFormFields>
      <salary-report-form-fields :create-body="data.createBody"></salary-report-form-fields>
    </template>
    <!--</editor-fold>-->

    <!--<editor-fold desc="Table columns">-->
    <template v-slot:item.impl.increaseStartPeriod="{ item }">
      {{ fromPeriodId(item.impl?.increaseStartPeriod) }}
    </template>
    <template v-slot:item.employeeInfo.dateOfEmployment="{ item }">
      {{ formatDate(item.employeeInfo.dateOfEmployment) }}
    </template>
    <template v-slot:item.req.increaseStartPeriod="{ item }">
      {{ fromPeriodId(item.req.increaseStartPeriod) }}
    </template>
    <template v-slot:item.createdAt="{ item }">
      {{ formatDateTime(item.createdAt) }}
    </template>
    <template v-slot:item.implementedAt="{ item }">
      {{ formatDateTime(item.implementedAt) }}
    </template>
    <template v-slot:item.impl.state="{ item }">
              <span v-if="item.impl" :class="item.impl.state === REJECTED? 'error--text': 'success--text'">{{
                  item.impl?.state ? $t(`SALARY_REQUEST_STAT.${item.impl.state}`) : ''
                }}</span>
    </template>
    <template v-slot:item.req.increaseAmount="{ item }">
      {{ formatMoney(item.req.increaseAmount) }}
    </template>
    <template v-slot:item.req.plannedSalaryAmount="{ item }">
      {{ formatMoney(item.req.plannedSalaryAmount) }}
    </template>
    <template v-slot:item.employeeInfo.currentSalaryAmount="{ item }">
      {{ formatMoney(item.employeeInfo.currentSalaryAmount) }}
    </template>
    <template v-slot:item.impl.increaseAmount="{ item }">
      {{ formatMoney(item.impl?.increaseAmount) }}
    </template>
    <template v-slot:item.impl.salaryAmount="{ item }">
      {{ formatMoney(item.impl?.salaryAmount) }}
    </template>
    <template v-slot:item.req.budgetExpectedFundingUntil="{ item }">
      {{ formatDate(item.req.budgetExpectedFundingUntil) }}
    </template>

    <template v-slot:item.approvals="{ item }">
      <v-tooltip bottom v-for="approval in approvalsOrderedAsc(item.approvals)" v-bind:key="approval.id"
                 max-width="500px">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-icon v-bind="tattrs" v-on="ton" :set="icon=getApprovalIcon(approval)" :color="icon.color">{{
              icon.icon
            }}
          </v-icon>
        </template>
        <p>{{ approval.createdBy.name }}<br>
          {{ formatDateTime(approval?.createdAt) }}</p>
        <p>{{ approval.comment }}</p>
      </v-tooltip>
    </template>
    <!--</editor-fold>-->

  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue, Watch} from "vue-property-decorator";
import {SalaryApprovalState, SalaryRequestApproval, SalaryRequestType} from "@/components/salary/salary.service";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";
import {DataTableHeader} from "vuetify";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import HreasyTableExportToExcelAction from "@/components/shared/table/HreasyTableExportToExcelAction.vue";
import HreasyTableSelectPeriodAction from "@/components/shared/table/HreasyTableSelectPeriodAction.vue";
import SalaryRequestFilterComponent from "@/components/salary/SalaryRequestFilterComponent.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import SalaryReportFormFields from "@/components/salary/SalaryReportFormFields.vue";

@Component({
  name: 'SalaryRequestsTable',
  components: {
    SalaryReportFormFields,
    SalaryRequestFilterComponent, HreasyTableSelectPeriodAction, HreasyTableExportToExcelAction, HreasyTable
  }
})
export default class SalaryRequestsTable extends Vue {
  private data = new SalaryRequestDataContainer(
      () => {
        const headers: DataTableHeader[] = [
          {text: this.$tc('Сотрудник'), value: 'employee.name', width: "250px"}
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
          headers.push(
              {
                text: this.$tc('Предполагаемая заработная плата после повышения'),
                value: 'req.plannedSalaryAmount',
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
            {text: this.$tc('Инициатор'), value: 'createdBy.name', width: "250px"},
            {text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name', width: "150px"}
        );
        if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
          headers.push(
              {text: this.$tc('Итоговое изменение на'), value: 'impl.increaseAmount', width: "150px"},
              {text: this.$tc('Итоговая сумма'), value: 'impl.salaryAmount', width: "150px"},
          );
        } else {
          headers.push(
              {text: this.$tc('Итоговая сумма бонуса'), value: 'impl.increaseAmount', width: "150px"}
          );
        }
        headers.push(
            {text: this.$tc('Решение'), value: 'impl.state', width: "150px"}
        );
        headers.push(
            {text: this.$tc('Согласования'), value: 'approvals', width: "150px"}
        );
        return headers;
      },
      (period, item) => {
        if (item?.id) {
          this.$router.push(`/salaries/requests/${period.periodId()}/${item.id}`);
        }
      }
  );

  /**
   * Keep-alive hook
   */
  activated() {
    this.data.reloadData();
  }


  @Watch("data.filter.type")
  private watchFilterType(newValue: SalaryRequestType) {
    this.data.reloadHeaders();
  }

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

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

  createNewTitle = () => this.data.filter.type == SalaryRequestType.SALARY_INCREASE
      ? this.$t('Создание запроса на индексацию ЗП') : this.$t('Создание запроса на бонус');
  private REJECTED = SalaryRequestImplementationState.REJECTED;

  private approvalsOrderedAsc(approvals: SalaryRequestApproval[]) {
    if (approvals) {
      return [...approvals].filter(a => a.state != SalaryApprovalState.COMMENT).sort((a, b) => (a.createdAt > b.createdAt) ? 1 : ((b.createdAt > a.createdAt) ? -1 : 0));
    }
    return approvals;
  }

  public getApprovalIcon(approval: SalaryRequestApproval): { icon: string, color: string } {
    switch (approval.state) {
      case SalaryApprovalState.DECLINE:
        return {icon: 'mdi-alert-circle', color: 'error'};
      case SalaryApprovalState.APPROVE:
        return {icon: 'mdi-checkbox-marked-circle', color: 'success'};
      default:
        return {icon: 'mdi-comment', color: ''};
    }
  }

}
</script>

<style scoped>

</style>