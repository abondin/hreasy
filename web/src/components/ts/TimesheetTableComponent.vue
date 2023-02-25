<!-- Managers of departments, business accounts and projects-->
<template>
  <v-data-table
      dense
      :loading="loading"
      :loading-text="$t('Загрузка_данных')"
      disable-sort
      fixed-header
      :headers="headers"
      :items-per-page="defaultItemsPerTablePage"
      :items="groupedRows"
      :group-by="groupBy=='employee'?'employee.displayName':'project.name'"
  >
    <template v-slot:group.header="row">
      <td colspan="30" class="text-start">
        <v-btn x-small icon @click="row.toggle()">
          <v-icon>{{ row.isOpen ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
        </v-btn>
        {{ row.group }}
      </td>
    </template>
  </v-data-table>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import {DataTableHeader} from "vuetify";
import timesheetService, {
  TimesheetAggregated,
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


const namespace_dict = 'dict';

/**
 * employee or project depends on group order
 */
export interface RowGroup {
  uuid: string,
  id: number | null,
  displayName: string | null
  empty: boolean
}

@Component({components: {}})
export default class TimesheetTableComponent extends Vue {


  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;


  // Dirty rows from backend
  private records: TimesheetRecord[] = [];
  // Grouped by project and employee rows. Uses in the table
  private groupedRows: TimesheetAggregated[] = [];

  private employees: Employee[] = [];

  private headers: DataTableHeader[] = [];

  private error: string | null = null;

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

  private groupBy: 'employee' | 'project' = 'employee';


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
      text: this.$tc('Проект'),
      value: 'project.displayName',
      width: "350px"
    });
    DateTimeUtils.daysBetweenDates(this.periodFilter.from, this.periodFilter.to).forEach((day) => {
      this.headers.push({
        text: DateTimeUtils.formatToDayMonthDate(day)!,
        value: 'day-' + DateTimeUtils.formatToIsoDate(day),
        width: "50px"
      });
    });
    return this.headers;
  }

  private rebuildRows() {
    this.groupedRows.length = 0;
    if (this.groupBy === "employee") {
      this.employees.forEach(employee => {
        const record: TimesheetAggregated = {
          employee: {uuid: 'empl-' + employee.id, displayName: employee.displayName, id: employee.id, empty: false},
          project: {uuid: 'project-empty', displayName: this.$tc("Без проекта"), id: null, empty: true},
          dates: []
        }
        this.groupedRows.push(record);
      });
    }
  }

}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
