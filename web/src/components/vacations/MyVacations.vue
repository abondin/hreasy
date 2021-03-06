<!-- My planned vacations-->
<template>
  <v-card>
    <v-card-title>
      <div>{{ $t('Планируемые отпуска') }}</div>
    </v-card-title>

    <v-card-text>
      <v-data-table
          :loading="loading"
          :loading-text="$t('Загрузка_данных')"
          :headers="headers"
          :items="filteredItems()"
          multi-sort
          hide-default-footer
          :sort-by="['startDate', 'endDate']"
          disable-pagination>
        <template
            v-slot:item.startDate="{ item }">
          {{ formatDate(item.startDate) }}
        </template>
        <template
            v-slot:item.endDate="{ item }">
          {{ formatDate(item.endDate) }}
        </template>
        <template
            v-slot:item.status="{ item }">
          {{ $t(`VACATION_STATUS_ENUM.${item.status}`) }}
        </template>
      </v-data-table>
    </v-card-text>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import vacationService, {MyVacation} from "@/components/vacations/vacation.service";
import {DataTableHeader} from "vuetify";
import {OvertimeUtils} from "@/components/overtimes/overtime.service";
import moment from 'moment';
import VacationEditForm from "@/components/vacations/VacationEditForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace: string = 'dict';

@Component({
  components: {VacationEditForm}
})
export default class MyVacations extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;
  vacations: MyVacation[] = [];

  public allStatuses: Array<any> = [];
  public allYears: Array<number> = [];
  public allMonths: Array<any> = [];

  /**
   * Lifecycle hook
   */
  created() {
    this.allStatuses = ['PLANNED', 'TAKEN', 'COMPENSATION', 'CANCELED'].map(status => {
      return {value: status, text: this.$tc(`VACATION_STATUS_ENUM.${status}`)}
    });
    const currentYear = new Date().getFullYear();
    this.allYears = [(currentYear - 2), (currentYear - 1), currentYear, (currentYear + 1)];
    this.allMonths = [...Array(12).keys()].map(m => {
      return {
        value: m,
        text: moment(m + 1, 'MM').format("MMMM")
      }
    });
    this.reloadHeaders();
    this.fetchData();
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Год'), value: 'year'});
    this.headers.push({text: this.$tc('Начало'), value: 'startDate'});
    this.headers.push({text: this.$tc('Окончание'), value: 'endDate'});
    this.headers.push({text: this.$tc('Статус'), value: 'status'});
    this.headers.push({text: this.$tc('Примечание'), value: 'notes'});
  }


  private filteredItems() {
    return this.vacations;
  }


  private fetchData() {
    this.loading = true;
    return vacationService.myFutureVacations()
        .then(data => {
              this.vacations = data.filter(m => m.startDate && m.endDate);
              return;
            }
        ).finally(() => {
          this.loading = false
        });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }


}
</script>

<style scoped>

</style>
