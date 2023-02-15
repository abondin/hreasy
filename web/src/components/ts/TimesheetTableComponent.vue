<!-- Managers of departments, business accounts and projects-->
<template>
  <v-data-table
      dense
      :loading="loading"
      :loading-text="$t('Загрузка_данных')"
      :headers="headers()"
      :items-per-page="defaultItemsPerTablePage"
      :items="records"
  >
  </v-data-table>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import {DataTableHeader} from "vuetify";
import timesheetService, {TimesheetRecord, TimesheetSummaryFilter} from "@/components/ts/timesheet.service";
import {UiConstants} from "@/components/uiconstants";
import {DateTimeUtils} from "@/components/datetimeutils";
import moment from "moment";


const namespace_dict = 'dict';

@Component({components: {}})
export default class TimesheetTableComponent extends Vue {

  private records: TimesheetRecord[] = [];
  private loading = false;
  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;
  private periodFilter: TimesheetSummaryFilter = {
    "from": DateTimeUtils.formatToIsoDate(moment().startOf('month'))!,
    "to": DateTimeUtils.formatToIsoDate(moment().endOf('month'))!
  };


  private headers(): DataTableHeader[] {
    const headers = [];
    headers.push({text: this.$tc('Сотрудник'), value: 'employee'});
    return headers;
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Timesheet table created');
    this.loading = true;
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadProjects'))
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() =>
            timesheetService.timesheetSummary(this.periodFilter).then(records => {
                  this.records = records;
                }
            )
        ).finally(() => this.loading = false);
  }

}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
