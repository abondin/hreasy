<template>
  <v-card>
    <v-card-title>
      <timesheet-table-filter
          :input="filter"
          :all-bas="allBas"
          :all-projects="allProjects"
          @dateIntervalUpdated="refresh()"
      ></timesheet-table-filter>
    </v-card-title>
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
                              :value="item.item.dates[d.key]??emptyHours(item.item, d.date)"
                              @edit="openEditDialog"></timesheet-hours-cell>
      </template>
    </v-data-table>

    <v-dialog v-model="editData.dialog" persistent>
      <timesheet-record-edit-form :input="editData">
      </timesheet-record-edit-form>
    </v-dialog>


  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import {DataTableHeader} from "vuetify";
import {
  EmployeeWithNotWorkingDays,
  TimesheetAggregatedByEmployee,
  TimesheetHours,
  TimesheetRecord
} from "@/components/ts/timesheet.service";
import {UiConstants} from "@/components/uiconstants";

import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import employeeService from "@/components/empl/employee.service";
import {errorUtils} from "@/components/errors";
import TimesheetHoursCell from "@/components/ts/TimesheetHoursCell.vue";
import dictService from "@/store/modules/dict.service";
import vacationService from "@/components/vacations/vacation.service";
import TimesheetRecordEditForm, {EditTimesheetRecordData} from "@/components/ts/TimesheetRecordEditForm.vue";
import TimesheetTableFilter, {TimesheetTableFilterData} from "@/components/ts/TimesheetTableFilter.vue";


const namespace_dict = 'dict';


@Component({components: {TimesheetTableFilter, TimesheetRecordEditForm, TimesheetHoursCell}})
export default class TimesheetTableComponent extends Vue {

  // static dicts
  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  // dynamic dicts
  private employees: Array<EmployeeWithNotWorkingDays> = [];
  private notWorkingDays: Array<Moment> = [];

  // Dirty rows from backend
  private records: TimesheetRecord[] = [];

  // Grouped by project and employee rows. Uses in the table
  private aggregatedByEmployees: TimesheetAggregatedByEmployee[] = [];

  private editData: EditTimesheetRecordData = new EditTimesheetRecordData();

  private headers: DataTableHeader[] = [];

  private error: string | null = null;

  private daysKeys: Array<{ key: string, date: Moment }> = [];

  private loading = false;
  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new TimesheetTableFilterData();


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
        // 1. Get all employees
        .then(() => this.refresh(false))
        .catch((er: any) => {
          this.error = errorUtils.shortMessage(er);
        })
        .finally(() => this.loading = false);
  }

  private refresh(handleLoading = true) {
    if (handleLoading) {
      this.loading = true;
    }
    this.employees.length = 0;
    const promise = employeeService.findAll().then(employees =>
        // 2. Get all not working days for given year
        dictService.notWorkingDays(this.filter.year).then(notWorkingDaysStr =>
            // 3. Get all vacations for given year
            vacationService.findAll([this.filter.year]).then(vacations => {
              // 4. Merge all not working days with employee's vacations to get all not working days for each employee
              const notWorkingDays = notWorkingDaysStr.map(str => DateTimeUtils.dateFromIsoString(str));
              return employees.forEach(employee => {
                const emplNotWorkingDays: Array<Moment> = [];
                vacations.filter(v => v.employee == employee.id && vacationService.isNotWorkingDays(v) && v.startDate && v.endDate)
                    .forEach(v => {
                      const vacationsDays = DateTimeUtils.daysBetweenDates(DateTimeUtils.dateFromIsoString(v.startDate), DateTimeUtils.dateFromIsoString(v.endDate));
                      emplNotWorkingDays.push(...vacationsDays);
                    });
                emplNotWorkingDays.push(...notWorkingDays);
                this.employees.push({
                  id: employee.id,
                  displayName: employee.displayName,
                  notWorkingDays: emplNotWorkingDays
                })
              });
            }))).then(() => {
      this.rebuildHeaders();
      this.rebuildRows();
    });
    if (handleLoading) {
      promise.catch((er: any) => {
        this.error = errorUtils.shortMessage(er);
      }).finally(() => this.loading = false);
    }
    return promise;
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
    DateTimeUtils.daysBetweenDates(this.filter.from, this.filter.to).forEach((day) => {
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
      this.records.filter(r => r.employee.id === employee.id).forEach(r => {
        const date = DateTimeUtils.dateFromIsoString(r.date);
        record.dates[DateTimeUtils.formatToDayKey(date)!] = {
          id: r.id,
          employee: employee,
          date: r.date,
          hoursPlanned: r.hoursPlanned,
          hoursSpent: r.hoursSpent,
          billable: r.billable,
          description: r.description,
          workingDay: this.isWorkingDay(employee.notWorkingDays, date)
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
      employee: record.employee,
      date: date,
      hoursPlanned: null,
      hoursSpent: null,
      billable: true,
      description: null,
      workingDay: this.isWorkingDay(record.employee.notWorkingDays, date)
    };
  }

  private isWorkingDay(notWorkingDays: Array<Moment>, date: Moment) {
    return notWorkingDays.filter(d => DateTimeUtils.isSameDate(d, date)).length == 0;
  }

  private openEditDialog(value: TimesheetHours) {
    console.log('Open edit dialog', value);
    this.editData.show(value);
  }
}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
