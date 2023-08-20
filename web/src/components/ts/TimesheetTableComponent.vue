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
        <v-alert type="error" v-if="error">{{ error }}</v-alert>
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
            <template v-for="s of daysKeys" v-slot:[`item.timesheet.${s.key}`]="item">
              <span v-bind:key="s.key">{{item.item.timesheet[s.key]?.totalHours}}</span>
            </template>
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
import timesheetService, {TimesheetSummary} from "@/components/ts/timesheet.service";

import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import {Getter} from "vuex-class";
import {ProjectDictDto, SimpleDict} from "@/store/modules/dict";
import {errorUtils} from "@/components/errors";
import TimesheetTableNavigator from "@/components/ts/TimesheetTableNavigator.vue";
import {TimesheetTableNavigatorData} from "@/components/ts/timesheetUiDto";
import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import TimesheetTableToolbar, {TimesheetTableToolbarData} from "@/components/ts/TimesheetTableToolbar.vue";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";


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
  private records: TimesheetSummary[] = [];


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
    if (handleLoading) {
      this.loading = true;
    }
    const promise =
        // 1. Read all timesheet records from backend
        timesheetService.timesheetSummary(this.navigatorData.timesheetSummaryApiQueryFilter()).then(records => {
              this.records = records;
            }
            // 2. Read all employees
        ).then(() => {
          this.rebuildHeaders();
        });
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
      value: 'employee.displayName',
      width: "300px"
    });
    this.daysKeys.length = 0;
    DateTimeUtils.daysBetweenDates(this.navigatorData.from, this.navigatorData.to).forEach((day) => {
      const dayKey = `${DateTimeUtils.formatToDayKey(day)}`;
      this.daysKeys.push({key: dayKey, date: day});
      this.headers.push({
        text: DateTimeUtils.formatToDayMonthDate(day)!,
        value: `timesheet.${dayKey}`,
        width: "50px"
      });
    });
    return this.headers;
  }

  private filterTableData() {
    const filtered = this.records.filter(r => {
      let result = true;
      // 1. Check only selected project
      if (this.toolbarData.filter.onlySelectedProject) {
        result = result && (
            (!this.navigatorData.ba || r.employee.currentProjectBa == this.navigatorData.ba) &&
            (!this.navigatorData.project || this.navigatorData.project == r.employee.currentProject)
        )
      }
      result = result && searchUtils.textFilter(this.toolbarData.filter.search,
          TextFilterBuilder.of()
              // Display name
              .splitWords(r.employee.displayName));
      return result;
    });
    return filtered;
  }

  private dateHeaderLabel(date: Moment) {
    return DateTimeUtils.formatToDayMonthDate(date);
  }


}
</script>

<style scoped lang="css">
.not-working-day {
  background-color: #fffafa;
}
</style>
