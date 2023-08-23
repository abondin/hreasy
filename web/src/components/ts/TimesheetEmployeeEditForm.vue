<template>
  <v-container>
    <v-card>

      <v-card-title>
        {{ $t('Учёт времени') }}: {{ record?.employee.displayName }}
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog()">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-title></v-card-title>
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
        {{record}}
      </v-data-table>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {TimesheetSummary} from "@/components/ts/timesheet.service";

import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import {ProjectDictDto, SimpleDict} from "@/store/modules/dict";
import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import {Prop} from "vue-property-decorator";


const namespace_dict = 'dict';


@Component({components: {}})
export default class TimesheetEmployeeEditForm extends Vue {

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  // static dicts
  @Prop({required: true})
  private allProjects!: Array<ProjectDictDto>;

  @Prop({required: true})
  private allBas!: Array<SimpleDict>;

  @Prop({required: true})
  private daysKeys!: Array<{ key: string, date: Moment }>;

  @Prop({required: true})
  private record!: TimesheetSummary | null;

  private headers: DataTableHeader[] = [];

  private error: string | null = null;

  private loading = false;

  /**
   * Lifecycle hook
   */
  created() {
    this.rebuildHeaders();
  }

  private rebuildHeaders() {
    this.headers.length = 0;
    this.headers.push({
      text: this.$tc('Проект'),
      value: 'employee.project',
      width: "300px"
    });
    this.daysKeys.forEach(d => {
      this.headers.push({
        text: DateTimeUtils.formatToDayMonthDate(d.date)!,
        value: `timesheet.${d.key}`,
        width: "50px"
      });
    });
    return this.headers;
  }

  private filterTableData() {
    return Object.values(this.record?.timesheet||[]);
  }

  private dateHeaderLabel(date: Moment) {
    return DateTimeUtils.formatToDayMonthDate(date);
  }

  private closeDialog() {
    this.$nextTick(function () {
      this.$emit('close');
    })
  }


}
</script>

<style scoped lang="css">
.not-working-day {
  background-color: #fffafa;
}
</style>
