<template>
  <v-container fluid class="pb-0">
    <v-row>
      <v-col lg="2" sm="6" xs="6" class="pb-0">
        <!-- Selected year -->
        <v-select
            v-model="input.year"
            :items="allYears"
            :label="$t('Год')"></v-select>
      </v-col>

      <v-col lg="4" sm="6" xs="6" class="pb-0">
        <!-- Dates selection filter -->
        <my-date-range-component ref="dateSelector" v-model="input.selectedDates"
                                 :label="$t('Период')"></my-date-range-component>
      </v-col>
    </v-row>
    <!--      &lt;!&ndash; Current Project Filter &ndash;&gt;-->
    <!--      <v-col lg="3" sm="6" xs="6" class="pb-0">-->
    <!--        <v-select-->
    <!--            clearable-->
    <!--            v-model="filter.selectedProjects"-->
    <!--            :items="allProjects.filter(p=>p.active)"-->
    <!--            item-value="id"-->
    <!--            item-text="name"-->
    <!--            :label="$t('Текущий проект')"-->
    <!--            multiple-->
    <!--        ></v-select>-->
    <!--      </v-col>-->
    <!--      &lt;!&ndash; Vacation Status &ndash;&gt;-->
    <!--      <v-col lg="3" sm="6" xs="6" class="pb-0">-->
    <!--        <v-select-->
    <!--            clearable-->
    <!--            v-model="filter.selectedStatuses"-->
    <!--            :items="allStatuses"-->
    <!--            :label="$t('Статус')"-->
    <!--            multiple-->
    <!--        ></v-select>-->
    <!--      </v-col>-->


    <!--    </v-row>-->

    <!--    <v-row>-->
    <!--      <v-col class="pt-0 pb-0">-->
    <!--        &lt;!&ndash; Main search &ndash;&gt;-->
    <!--        <v-text-field-->
    <!--            class="mt-0 pt-0"-->
    <!--            v-model="filter.search"-->
    <!--            :label="$t('Поиск')" clearable></v-text-field>-->
    <!--      </v-col>-->

    <!--      <v-spacer></v-spacer>-->
    <!--      <v-col class="row pt-0 pb-0" align-self="center" cols="auto">-->
    <!--        &lt;!&ndash; Refresh button &ndash;&gt;-->
    <!--        <v-tooltip bottom v-if="canEditVacations">-->
    <!--          <template v-slot:activator="{ on: ton, attrs: tattrs}">-->
    <!--            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">-->
    <!--              <v-btn text icon @click="fetchData(false)">-->
    <!--                <v-icon>refresh</v-icon>-->
    <!--              </v-btn>-->
    <!--            </div>-->
    <!--          </template>-->
    <!--          <span>{{ $t('Обновить данные') }}</span>-->
    <!--        </v-tooltip>-->
    <!--        &lt;!&ndash; Add new project &ndash;&gt;-->
    <!--        <v-tooltip bottom v-if="canEditVacations">-->
    <!--          <template v-slot:activator="{ on: ton, attrs: tattrs}">-->
    <!--            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">-->
    <!--              <v-btn text color="primary" :disabled="loading" @click="openVacationDialog(undefined)" icon>-->
    <!--                <v-icon>mdi-plus</v-icon>-->
    <!--              </v-btn>-->
    <!--            </div>-->
    <!--          </template>-->
    <!--          <span>{{ $t('Добавить отпуск') }}</span>-->
    <!--        </v-tooltip>-->

    <!--        &lt;!&ndash; Export &ndash;&gt;-->
    <!--        <v-tooltip bottom v-if="canExportVacations">-->
    <!--          <template v-slot:activator="{ on: ton, attrs: tattrs}">-->
    <!--            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">-->
    <!--              <v-btn text :disabled="loading" @click="exportToExcel()" icon>-->
    <!--                <v-icon>mdi-file-excel</v-icon>-->
    <!--              </v-btn>-->
    <!--            </div>-->
    <!--          </template>-->
    <!--          <span>{{ $t('Экспорт в excel') }}</span>-->
    <!--        </v-tooltip>-->

    <!--        <v-snackbar-->
    <!--            v-model="snackbarNotification"-->
    <!--            timeout="5000">-->
    <!--          {{ snackbarMessage }}-->
    <!--          <template v-slot:action="{ attrs }">-->
    <!--            <v-btn color="blue" icon v-bind="attrs" @click="snackbarNotification = false" class="mt-0 pt-0">-->
    <!--              <v-icon>mdi-close-circle-outline</v-icon>-->
    <!--            </v-btn>-->
    <!--          </template>-->
    <!--        </v-snackbar>-->
    <!--      </v-col>-->
    <!--    </v-row>-->

  </v-container>

</template>

<script lang="ts">
import Component from "vue-class-component";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import Vue from "vue";
import {Prop, Watch} from "vue-property-decorator";
import {DateTimeUtils} from "@/components/datetimeutils";
import {Moment} from "moment";
import moment from "moment/moment";
import MyDateRangeComponent from "@/components/shared/MyDateRangeComponent.vue";

export class TimesheetTableFilterData {
  private _year: number;
  public selectedDates: Array<string> = [];

  public constructor() {
    const now = DateTimeUtils.now();
    this._year = now.year();
    this.updateDatesOnYearChange();
  }

  private updateDatesOnYearChange() {
    const now = DateTimeUtils.now().set({year: this._year});
    this.selectedDates = [
      DateTimeUtils.formatToIsoDate(now.startOf('month'))!,
      DateTimeUtils.formatToIsoDate(now.endOf('month'))!
    ];
  }

  public get from(): Moment {
    return moment(this.selectedDates[0]);
  }

  public get to(): Moment {
    return moment(this.selectedDates.length > 1 ? this.selectedDates[1] : this.selectedDates[0]);
  }

  public get year() {
    return this._year;
  }

  public set year(year: number) {
    this._year = year;
    this.updateDatesOnYearChange();
  }


}

@Component(
    {components: {MyDateRangeComponent, MyDateFormComponent}}
)
export default class TimesheetTableFilter extends Vue {
  @Prop({required: true})
  private input!: TimesheetTableFilterData;

  private allYears = DateTimeUtils.defaultYears();


  @Watch("input.selectedDates")
  private watchSelectedDates() {
    this.emitDateIntervalUpdated();
  }

  public emitDateIntervalUpdated() {
    this.$nextTick(function () {
      this.$emit('dateIntervalUpdated');
    })
  }

}
</script>

<style scoped>

</style>