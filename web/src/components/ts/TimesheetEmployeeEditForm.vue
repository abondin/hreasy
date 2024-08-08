<template>
  <v-container>
    <v-card>

      <v-card-title>
        {{ $t('Учёт времени') }}: {{ data?.employee.displayName }}
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
          :no-data-text="$t('По выбранному проекту за выбранные даты записи не найдены')"
          disable-sort
          fixed-header
          :headers="headers"
          :items-per-page="defaultItemsPerTablePage"
          :items="filterTableData()">
      </v-data-table>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import {
  EmployeeShortForTimesheetSummary,
  TimesheetGroupedByDay,
  TimesheetSummary
} from "@/components/ts/timesheet.service";

import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import {ProjectDictDto, SimpleDict} from "@/store/modules/dict";
import {UiConstants} from "@/components/uiconstants";
import {DataTableHeader} from "vuetify";
import {Prop, Watch} from "vue-property-decorator";


const namespace_dict = 'dict';


interface TimesheetEmployeeEditData {
  employee: EmployeeShortForTimesheetSummary,
  vacations: string[],
  records: ProjectTimesheet[]
}

interface ProjectTimesheet {
  project: ProjectDictDto,
  records: Map<string, TimesheetCell>
}

interface TimesheetCell {
  date: string,
  hoursSpent: number,
  comment: string | null
}

@Component({components: {}})
export default class TimesheetEmployeeEditForm extends Vue {

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;
  private data: TimesheetEmployeeEditData | null = null;

  // static dicts
  @Prop({required: true})
  private allProjects!: Array<ProjectDictDto>;

  @Prop({required: true})
  private allBas!: Array<SimpleDict>;

  @Prop({required: true})
  private daysKeys!: Array<{ key: string, date: Moment }>;

  @Prop({required: true})
  private input!: TimesheetSummary | null;

  private headers: DataTableHeader[] = [];

  private error: string | null = null;

  private loading = false;

  /**
   * Lifecycle hook
   */
  created() {
    this.reset();
  }

  @Watch("input")
  private watchInput() {
    this.reset();
  }


  private reset() {
    this.rebuildHeaders();
    this.data = this.input ?
        {
          employee: this.input.employee,
          vacations: this.input.vacations,
          records: this.groupTimesheetByProjets(this.input.timesheet)
        }
        : null;
  }
  private groupTimesheetByProjets(tsByDay: TimesheetGroupedByDay): ProjectTimesheet[]{
      const result:ProjectTimesheet[] = [];
      (tsByDay?Object.values(tsByDay):[]).forEach(obj=>{
        obj.records.forEach(record=>{
          var grouppedByProject = result.find(i=>i.project.id==record.project);
          if (!grouppedByProject){
            grouppedByProject = {
              project: this.allProjects.find(p=>p.id)!,
              records: new Map<string,TimesheetCell> ()
            }
          }
        })
      })
      return result;
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
    return this.data?.records;
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
