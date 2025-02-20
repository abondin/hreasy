<!--
Show all employee's salaries increases and bonuses for the all periods
-->
<template>
  <v-card class="mt-5" v-if="data">
    <v-card-title class="text-h5">
      {{ $t('История всех запросов') }}
      <v-spacer></v-spacer>
      <v-btn-toggle dense multiple v-model="selectedModes">
        <v-btn small>{{ $t('Повышения') }}</v-btn>
        <v-btn small>{{ $t('Бонусы') }}</v-btn>
      </v-btn-toggle>
    </v-card-title>
    <v-card-text>
      <v-alert type="error" v-if="error">{{ error }}</v-alert>
      <v-data-table
          :loading="loading"
          :loading-text="$t('Загрузка_данных')"
          :headers="headers"
          :items="filteredItems()"
          multi-sort
          :sort-by="['req.increaseStartPeriod']"
          sort-desc
          dense
          :items-per-page="defaultItemsPerTablePage"
          class="text-truncate">
        <template v-slot:item.req.increaseStartPeriod="{ item }">
          <span :set="title = fromPeriodId(item.req?.increaseStartPeriod)">
            <span v-if="isBonus(item)">
              ({{ $t('Бонус') }})
            </span>
          <span v-if="isCurrentReport(item)">{{ title }}</span>
          <router-link v-else :to="{name:'salariesRequestsDetails', params:{
              period:item.req?.increaseStartPeriod,
              requestId:item.id}}">
            {{ title }}
          </router-link>
          </span>
        </template>
        <template v-slot:item.req.increaseAmount="{ item }">
          {{ formatMoney(item.req.increaseAmount) }}
          <span v-if="item.req.plannedSalaryAmount">
             / {{ formatMoney(item.req.plannedSalaryAmount) }}
          </span>
        </template>
        <template v-slot:item.impl.increaseAmount="{ item }">
          {{ formatMoney(item.impl?.increaseAmount) }}
          <span v-if="item.impl?.salaryAmount">
             / {{ formatMoney(item.impl?.salaryAmount) }}
          </span>
        </template>
        <template v-slot:item.impl.state="{ item }">
              <span v-if="item.impl" :class="item.impl.state === REJECTED? 'error--text': 'success--text'">{{
                  item.impl?.state ? $t(`SALARY_REQUEST_STAT.${item.impl.state}`) : ''
                }}</span>
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {SalaryDetailsDataContainer} from "@/components/salary/details/salary-details.data.container";
import {SalaryIncreaseRequest, SalaryRequestType} from "@/components/salary/salary.service";
import salaryAdminService, {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import {errorUtils} from "@/components/errors";
import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import {ReportPeriod} from "@/components/overtimes/overtime.service";

@Component({})
export default class SalaryRequestEmployeeHistory extends Vue {
  @Prop({required: true})
  data!: SalaryDetailsDataContainer;

  /**
   * 0 - salary request
   * 1 - bonus
   * @private
   */
  private selectedModes = [0];

  private requests: SalaryIncreaseRequest[] = [];
  private error: string | null = null;
  private loading = false;
  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';
  private REJECTED = SalaryRequestImplementationState.REJECTED;

  private headers: DataTableHeader[] = [
    {
      text: this.$tc('Период'),
      value: 'req.increaseStartPeriod',
      width: "150px",
      class: "text-wrap"
    },
    {
      text: this.$tc('Запрошенное повышение или бонус / заработная плата после повышения'),
      value: 'req.increaseAmount',
      width: "150px",
      class: "text-wrap"
    },
    {text: this.$tc('Решение'), value: 'impl.state', width: "150px"},
    {
      text: this.$tc('Реализованное повышение или бонус / заработная плата после повышения'),
      value: 'impl.increaseAmount',
      width: "150px",
      class: "text-wrap"
    }
  ];

  mounted() {
    this.requests = [];
    this.error = null;
    this.loading = true;
    salaryAdminService.loadEmployeeSalaryRequestsForAllPeriods(this.data.item.employee.id).then((result) => {
      this.requests = result;
    }).catch(ex => {
      this.error = errorUtils.shortMessage(ex);
    }).finally(() => {
      this.loading = false;
    })
  }

  filteredItems() {
    return this.requests.filter((r) => {
      let filtered = this.selectedModes.length == 0 ? true : false;
      if (this.selectedModes.indexOf(0) >= 0) {
        filtered = filtered || r.type == SalaryRequestType.SALARY_INCREASE;
      }
      if (this.selectedModes.indexOf(1) >= 0) {
        filtered = filtered || r.type == SalaryRequestType.BONUS;
      }
      return filtered;
    });
  }

  private isCurrentReport(r: SalaryIncreaseRequest) {
    return r.req.increaseStartPeriod == this.data.item.req.increaseStartPeriod
        && r.id == this.data.item.id;
  }

  private isBonus(item: SalaryIncreaseRequest) {
    return item.type == SalaryRequestType.BONUS ? true : false;
  }

}
</script>


<style scoped>

</style>
