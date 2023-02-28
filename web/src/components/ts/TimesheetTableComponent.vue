<template>
  <v-card>
    <v-data-table
        dense
        :loading="loading"
        :loading-text="$t('Загрузка_данных')"
        disable-sort
        fixed-header
        :headers="headers"
        :items-per-page="defaultItemsPerTablePage"
        :items="aggregatedByEmployees">
      <template v-slot:[`item.employee.displayName`]="{ item }">
        {{ item.employee.displayName }}
      </template>
      <template v-for="d of daysKeys" v-slot:[`item.dates.${d.key}`]="item">
        <timesheet-hours-cell v-bind:key="d.key"
                              :value="item.item.dates[d.key]??emptyHours(item.item, d.date)"></timesheet-hours-cell>
      </template>
    </v-data-table>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import {DataTableHeader} from "vuetify";
import timesheetService, {
  TimesheetAggregatedByEmployee,
  TimesheetRecord,
  TimesheetSummaryFilter
} from "@/components/ts/timesheet.service";
import {UiConstants} from "@/components/uiconstants";

import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {errorUtils} from "@/components/errors";
import TimesheetHoursCell from "@/components/ts/TimesheetHoursCell.vue";
import dictService from "@/store/modules/dict.service";


const namespace_dict = 'dict';

@Component({components: {TimesheetHoursCell}})
export default class TimesheetTableComponent extends Vue {

  // static dicts
  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  // dynamic dicts
  private employees: Employee[] = [];
  private notWorkingDays: Array<Moment> = [];


  // Dirty rows from backend
  private records: TimesheetRecord[] = [];
  // Grouped by project and employee rows. Uses in the table
  private aggregatedByEmployees: TimesheetAggregatedByEmployee[] = [];


  private headers: DataTableHeader[] = [];

  private error: string | null = null;

  private daysKeys: Array<{ key: string, date: Moment }> = [];

  private loading = false;
  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;


  private periodFilter: { year: number, from: Moment, to: Moment } = {
    year: DateTimeUtils.now().year(),
    from: DateTimeUtils.now().startOf('month'),
    to: DateTimeUtils.now().endOf('month')
  }

  private periodFilterStr(): TimesheetSummaryFilter {
    return {
      "from": DateTimeUtils.formatToIsoDate(this.periodFilter.from)!,
      "to": DateTimeUtils.formatToIsoDate(this.periodFilter.to)!
    }
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Timesheet table created');
    this.loading = true;
    this.error = null;
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadProjects'))
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() => employeeService.findAll().then(data => {
          this.employees = data as Employee[];
        }))
        .then(() => dictService.notWorkingDays(this.periodFilter.year).then(data => {
          this.notWorkingDays = data.map(str => DateTimeUtils.dateFromIsoString(str));
        }))
        .then(() =>
            timesheetService.timesheetSummary(this.periodFilterStr()).then(records => {
                  this.records = records;
                }
            )
        )
        .then(() => this.rebuildTable())
        .catch((er: any) => {
          this.error = errorUtils.shortMessage(er);
        })
        .finally(() => this.loading = false);
  }

  private rebuildTable() {
    this.rebuildHeaders();
    this.rebuildRows();
  }

  private rebuildHeaders(): DataTableHeader[] {
    this.headers.length = 0;
    this.headers.push({
      text: this.$tc('Сотрудник'),
      value: 'employee.displayName',
      width: "300px"
    });
    this.headers.push({
      text: this.$tc('Всего'),
      value: 'total',
      width: "50px"
    });
    this.daysKeys.length = 0;
    DateTimeUtils.daysBetweenDates(this.periodFilter.from, this.periodFilter.to).forEach((day) => {
      const dayKey = `${DateTimeUtils.formatToDayKey(day)}`;
      this.daysKeys.push({key: dayKey, date: day});
      this.headers.push({
        text: DateTimeUtils.formatToDayMonthDate(day)!,
        value: `dates.${dayKey}`,
        width: "50px"
      });
    });
    return this.headers;
  }

  private rebuildRows() {
    this.aggregatedByEmployees.length = 0;
    this.employees.forEach(employee => {
      const record: TimesheetAggregatedByEmployee = {
        employee: employee,
        dates: {},
        total: {hoursPlanned: 0, hoursSpentBillable: 0, hoursSpentNonBillable: 0}
      }
      this.records.filter(r => r.employee == employee.id).forEach(r => {
        const date = DateTimeUtils.dateFromIsoString(r.date);
        record.dates[DateTimeUtils.formatToDayKey(date)!] = {
          id: r.id,
          hoursPlanned: r.hoursPlanned,
          hoursSpent: r.hoursSpent,
          billable: r.billable,
          description: r.description,
          workingDay: this.isWorkingDay(date)
        }
        record.total.hoursSpentBillable = (r.hoursSpent || 0) * (r.billable === true ? 1 : 0);
        record.total.hoursSpentNonBillable = (r.hoursSpent || 0) * (r.billable === true ? 0 : 1);
        record.total.hoursPlanned = r.hoursPlanned || 0;
      });
      this.aggregatedByEmployees.push(record);
    });
  }

  private emptyHours(record: TimesheetAggregatedByEmployee, date: Moment) {
    return {
      id: null,
      hoursPlanned: null,
      hoursSpent: null,
      billable: true,
      description: null,
      workingDay: this.isWorkingDay(date)
    };
  }

  private isWorkingDay(date: Moment){
    return this.notWorkingDays.filter(d=>d.isSame(date, 'day')).length == 0;
  }
}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
