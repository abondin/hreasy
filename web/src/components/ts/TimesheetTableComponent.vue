<template>
  <v-card>
    <v-card-title>
      <timesheet-table-filter
          :input="filter"
          :all-bas="allBas"
          :all-projects="allProjects"
          @updated="refresh(true)"
      ></timesheet-table-filter>
    </v-card-title>

    <vue-excel-editor v-if="aggregatedByEmployees" v-model="aggregatedByEmployees"
                      :cell-style="dateCellStyle" filter-row no-header-edit>
      <vue-excel-column field="employeeDisplayName" :label="$t('ФИО')" type="string" width="300px" readonly sticky/>
      <vue-excel-column v-for="(d) of daysKeys" v-bind:key="d.key"
                        :field="`hoursSpent_${d.key}`"
                        :label="dateHeaderLabel(d.date)"
                        :change="onChange"
                        :autocomplete="false"
                        type="number" width="50px"/>
    </vue-excel-editor>
    <v-card-text v-else>
      <v-alert type="info">{{ $t("Необходимо выбрать бизнес аккаунт и проект") }}</v-alert>
    </v-card-text>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import timesheetService, {TimesheetRecord} from "@/components/ts/timesheet.service";

import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import {Getter} from "vuex-class";
import {ProjectDictDto, SimpleDict} from "@/store/modules/dict";
import employeeService from "@/components/empl/employee.service";
import {errorUtils} from "@/components/errors";
import dictService from "@/store/modules/dict.service";
import vacationService from "@/components/vacations/vacation.service";
import TimesheetTableFilter from "@/components/ts/TimesheetTableFilter.vue";
import {TimesheetAggregatedByEmployee, TimesheetTableFilterData} from "@/components/ts/timesheetUiDto";


const namespace_dict = 'dict';


@Component({components: {TimesheetTableFilter}})
export default class TimesheetTableComponent extends Vue {

  // static dicts
  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<ProjectDictDto>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  // Dirty rows from backend
  private records: TimesheetRecord[] = [];

  // Grouped by project and employee rows. Uses in the table
  private aggregatedByEmployees: TimesheetAggregatedByEmployee[] = [];

  private error: string | null = null;

  private daysKeys: Array<{ key: string, date: Moment }> = [];

  private loading = false;

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
    logger.log("Reload all timesheet information from backend", this.filter);
    this.aggregatedByEmployees.length = 0;
    if (!this.filter.ba) {
      return;
    }
    if (handleLoading) {
      this.loading = true;
    }
    const promise =
        // 1. Read all timesheet records from backend
        timesheetService.timesheetSummary(this.filter.timesheetSummaryFilter()).then(records => {
              this.records = records;
            }
            // 2. Read all employees
        ).then(() => employeeService.findAll().then(employees =>
            // 3. Get all not working days for given year
            dictService.notWorkingDays(this.filter.year).then(notWorkingDaysStr =>
                // 4. Get all vacations for given year
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
                    const record: TimesheetAggregatedByEmployee = {
                      employee: employee.id,
                      employeeDisplayName: employee.displayName,
                      notWorkingDayKeys: emplNotWorkingDays.map(d=>'hoursSpent_'+DateTimeUtils.formatToDayKey(d)),
                      ba: this.filter.ba!,
                      project: this.filter.project,
                      editMode: false,
                    }
                    this.rebuildEmployeeHours(record);
                    this.aggregatedByEmployees.push(record);
                  });
                })))
            .then(() => {
              this.rebuildHeaders();
            }));
    if (handleLoading) {
      promise.catch((er: any) => {
        this.error = errorUtils.shortMessage(er);
      }).finally(() => this.loading = false);
    }
    return promise;
  }


  private rebuildHeaders() {
    this.daysKeys.length = 0;
    DateTimeUtils.daysBetweenDates(this.filter.from, this.filter.to).forEach((day) => {
      const dayKey = `${DateTimeUtils.formatToDayKey(day)}`;
      this.daysKeys.push({key: dayKey, date: day});
    });
  }

  private rebuildEmployeeHours(record: TimesheetAggregatedByEmployee): void {
    Object.keys(record).filter(k => k.startsWith('hoursSpent_')).forEach((k: string) => {
      record[k] = 0;
    });
    DateTimeUtils.daysBetweenDates(this.filter.from, this.filter.to).forEach((day) => {
      const r: TimesheetRecord | null = this.records.find(r =>
          DateTimeUtils.isSameDate(DateTimeUtils.dateFromIsoString(r.date!), day)
          && r.employee === record.employee
          && r.businessAccount === this.filter.ba
          && ((this.filter.project == null && r.project == null) || r.project == this.filter.project)
      ) || null;
      record['hoursSpent_' + DateTimeUtils.formatToDayKey(day)!] = (r && r.hoursSpent) || 0;
    });
  }

  private dateHeaderLabel(date: Moment) {
    return DateTimeUtils.formatToDayMonthDate(date);
  }

  private onChange(hoursSpentNew: number, hoursSpentOld: number, item: TimesheetAggregatedByEmployee, cell: any) {
    //TODO
    logger.log(`change-${hoursSpentNew}-${hoursSpentOld}-${item.employeeDisplayName}: ${cell.name}`, hoursSpentNew, hoursSpentOld, item, cell);
  }

  private dateCellStyle(employee: TimesheetAggregatedByEmployee, cell: any) {
    if (!(cell.name as string)?.startsWith('hoursSpent_')) {
      return "";
    }
    if (employee.notWorkingDayKeys.filter(d => d==cell.name).length > 0) {
      return {"background-color": "#a4adbd"}
    }
    return {};
  }
}
</script>

<style scoped lang="css">
.not-working-day {
  background-color: #fffafa;
}
</style>
