<template>
  <v-container>
    <v-row fill-height>
      <v-col cols="auto">
        <v-card>
          <v-card-title>{{ $t('Проект для указания рабочего времени') }}</v-card-title>
          <timesheet-table-navigator
              :input="navigatorData"
              :all-bas="allBas"
              :all-projects="allProjects"
              @updated="refresh(true)"
          ></timesheet-table-navigator>
        </v-card>
      </v-col>
      <v-col>
        <v-card v-if="navigatorData.isReady()">
          <v-card-title>
            <timesheet-table-toolbar :model="toolbarData"></timesheet-table-toolbar>
          </v-card-title>
          <v-data-table
              dense
              :loading="loading"
              :loading-text="$t('Загрузка_данных')"
              :no-data-text="$t('По выбранныму проекту за выбранные даты записи не найдены')"
              disable-sort
              fixed-header
              :headers="headers"
              :items-per-page="defaultItemsPerTablePage"
              :items="filterTableData()">
          </v-data-table>
        </v-card>
        <v-card v-else>
          <v-card-text>
            <v-alert type="info">{{ $t("Необходимо выбрать бизнес аккаунт и проект") }}</v-alert>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
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
import TimesheetTableNavigator from "@/components/ts/TimesheetTableNavigator.vue";
import {TimesheetAggregatedByEmployee, TimesheetTableNavigatorData} from "@/components/ts/timesheetUiDto";
import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import TimesheetTableToolbar, {TimesheetTableToolbarData} from "@/components/ts/TimesheetTableToolbar.vue";


const namespace_dict = 'dict';


@Component({components: {TimesheetTableToolbar, TimesheetTableNavigator: TimesheetTableNavigator}})
export default class TimesheetTableComponent extends Vue {

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  // static dicts
  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<ProjectDictDto>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  private headers: DataTableHeader[] = [];

  // Dirty rows from backend
  private records: TimesheetRecord[] = [];

  // Grouped by project and employee rows. Uses in the table
  private aggregatedByEmployees: TimesheetAggregatedByEmployee[] = [];

  private error: string | null = null;

  private daysKeys: Array<{ key: string, date: Moment }> = [];

  private loading = false;

  private navigatorData = new TimesheetTableNavigatorData();
  private toolbarData = new TimesheetTableToolbarData();


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
    logger.log("Reload all timesheet information from backend", this.navigatorData);
    this.aggregatedByEmployees.length = 0;
    if (!this.navigatorData.ba) {
      return;
    }
    if (handleLoading) {
      this.loading = true;
    }
    const promise =
        // 1. Read all timesheet records from backend
        timesheetService.timesheetSummary(this.navigatorData.timesheetSummaryApiQueryFilter()).then(records => {
              this.records = records;
            }
            // 2. Read all employees
        ).then(() => employeeService.findAll().then(employees =>
            // 3. Get all not working days for given year
            dictService.notWorkingDays(this.navigatorData.year).then(notWorkingDaysStr =>
                // 4. Get all vacations for given year
                vacationService.findAll([this.navigatorData.year]).then(vacations => {
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
                      notWorkingDayKeys: emplNotWorkingDays.map(d => 'hoursSpent_' + DateTimeUtils.formatToDayKey(d)),
                      ba: this.navigatorData.ba!,
                      project: this.navigatorData.project,
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
    this.headers.length = 0;
    this.headers.push({
      text: this.$tc('Сотрудник'),
      value: 'employeeDisplayName',
      width: "300px"
    });
    this.daysKeys.length = 0;
    DateTimeUtils.daysBetweenDates(this.navigatorData.from, this.navigatorData.to).forEach((day) => {
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

  private rebuildEmployeeHours(record: TimesheetAggregatedByEmployee): void {
    Object.keys(record).filter(k => k.startsWith('hoursSpent_')).forEach((k: string) => {
      record[k] = 0;
    });
    DateTimeUtils.daysBetweenDates(this.navigatorData.from, this.navigatorData.to).forEach((day) => {
      const r: TimesheetRecord | null = this.records.find(r =>
          DateTimeUtils.isSameDate(DateTimeUtils.dateFromIsoString(r.date!), day)
          && r.employee === record.employee
          && r.businessAccount === this.navigatorData.ba
          && ((this.navigatorData.project == null && r.project == null) || r.project == this.navigatorData.project)
      ) || null;
      record['hoursSpent_' + DateTimeUtils.formatToDayKey(day)!] = (r && r.hoursSpent) || 0;
    });
  }

  private filterTableData() {
    const filtered =  this.aggregatedByEmployees.filter(r => {
      let result = true;
      // 1. Check only selected project
      if (this.toolbarData.filter.onlySelectedProject) {
        result = result && (
            r.ba == this.navigatorData.ba &&
           (!this.navigatorData.project || this.navigatorData.project == r.project)
        )
      }
      if (!result){
        console.log(`Not filtered ${r.employeeDisplayName}`);
      }
      return result;
    });
    console.log(`filterTableData ${filtered.length} of ${this.aggregatedByEmployees.length}. Filter: ${this.toolbarData.filter.onlySelectedProject}:`);
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
    if (employee.notWorkingDayKeys.filter(d => d == cell.name).length > 0) {
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
