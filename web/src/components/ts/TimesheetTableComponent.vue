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
      <template v-for="s of daysKeys" v-slot:[`item.dates.${s}`]="item">
        <timesheet-hours-cell v-bind:key="s" :value="item.item.dates[s]"></timesheet-hours-cell>
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
import moment, {Moment} from "moment";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {errorUtils} from "@/components/errors";
import TimesheetHoursCell from "@/components/ts/TimesheetHoursCell.vue";


const namespace_dict = 'dict';

@Component({components: {TimesheetHoursCell}})
export default class TimesheetTableComponent extends Vue {


  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;


  // Dirty rows from backend
  private records: TimesheetRecord[] = [];
  // Grouped by project and employee rows. Uses in the table
  private aggregatedByEmployees: TimesheetAggregatedByEmployee[] = [];

  private employees: Employee[] = [];

  private headers: DataTableHeader[] = [];

  private error: string | null = null;

  private daysKeys: string[] = [];

  private loading = false;
  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private periodFilter: { from: Moment, to: Moment } = {
    from: moment().startOf('month'),
    to: moment().endOf('month')
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
      this.daysKeys.push(dayKey);
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
        record.dates[DateTimeUtils.formatToDayKey(DateTimeUtils.dateFromIsoString(r.date))!] = {
          id: r.id,
          hoursPlanned: r.hoursPlanned,
          hoursSpent: r.hoursSpent,
          billable: r.billable,
          description: r.description
        }
        record.total.hoursSpentBillable = (r.hoursSpent || 0) * (r.billable === true ? 1 : 0);
        record.total.hoursSpentNonBillable = (r.hoursSpent || 0) * (r.billable === true ? 0 : 1);
        record.total.hoursPlanned = r.hoursPlanned || 0;
      });
      this.aggregatedByEmployees.push(record);
    });
  }

}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
