<!-- My planned vacations-->
<template>
  <v-card>
    <v-card-title>
      <span>{{ $t('Планируемые отпуска') }}</span>
      <v-spacer></v-spacer>

      <v-menu offset-y v-if="openedPeriods && openedPeriods.length>0">
        <template v-slot:activator="{ on: menu, attrs }">
          <v-tooltip top>
            <template v-slot:activator="{ on: tooltip }">
              <v-btn color="primary" v-bind="attrs" v-on="{ ...tooltip, ...menu }">
                {{ $t('Запланировать') }}
              </v-btn>
            </template>
            <span>{{ $t('Запланировать отпуск на будущий год') }}</span>
          </v-tooltip>
        </template>
        <v-list>
          <v-list-item link v-for="(item, index) in openedPeriods" :key="index">
            <v-list-item-title @click="requestAction.openRequestVacationDialog(item.year)">
              {{ item.year }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
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
          class="hover-table"
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
        <template
            v-slot:item.notes="{ item }">
          <div class="d-flex justify-space-between align-center">
            <span>{{ item.notes || '' }}</span>
            <v-tooltip bottom>
              <template v-slot:activator="{ on, attrs }">
                <v-btn v-if="vacationCanBeCanceled(item)"
                    icon
                    @click="cancelRequestAction.openDialog(item.id)"
                    class="delete-btn"
                    v-bind="attrs"
                    v-on="on"
                >
                  <v-icon>mdi-delete</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('Отозвать') }}</span>
            </v-tooltip>
          </div>
        </template>
      </v-data-table>
    </v-card-text>
    <in-dialog-form :title="$t('Запланировать отпуск на X год', {year: requestAction.formData.year})"
                    :data="requestAction" form-ref="requestVacation"
                    v-if="requestAction.formData" v-on:submit="fetchData()">
      <template v-slot:fields>
        <!-- start date -->
        <request-vacations-form-fields :data="requestAction"></request-vacations-form-fields>
      </template>
    </in-dialog-form>
    <in-dialog-form :title="$t('Отозвать отпуск')"
                    :data="cancelRequestAction" form-ref="cancelRequestVacation"
                    v-on:submit="fetchData()">
      <template v-slot:fields>
        <span>{{ $t('Вы уверены, что хотите отозвать отпуск?') }}</span>
      </template>
    </in-dialog-form>
  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import vacationService, {
  MyVacation,
  vacationStatuses,
  VacPlanningPeriod
} from "@/components/vacations/vacation.service";
import {DataTableHeader} from "vuetify";
import moment from 'moment';
import {DateTimeUtils} from "@/components/datetimeutils";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import {RequestOrUpdateVacationActionDataContainer} from "@/components/vacations/request-vacation.data.container";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import dictService from "@/store/modules/dict.service";
import RequestVacationsFormFields from "@/components/vacations/RequestVacationsFormFields.vue";
import {InDialogActionDataContainer} from "@/components/shared/forms/InDialogActionDataContainer";


@Component({
  components: {RequestVacationsFormFields, MyDateFormComponent, InDialogForm}
})
export default class MyVacations extends Vue {
  headers: DataTableHeader[] = [];
  loading = false;
  vacations: MyVacation[] = [];

  public allStatuses: Array<any> = [];
  public allYears: Array<number> = [];
  public allMonths: Array<any> = [];
  openedPeriods: Array<VacPlanningPeriod> = [];
  requestAction = new RequestOrUpdateVacationActionDataContainer();
  cancelRequestAction = new InDialogActionDataContainer<number, void>(
      (id) => {
        if (id) {
          return vacationService.cancelVacationRequest(id);
        }
        return Promise.resolve();
      }
  );

  /**
   * Lifecycle hook
   */
  created() {
    this.allStatuses = vacationStatuses.map(status => {
      return {value: status, text: this.$tc(`VACATION_STATUS_ENUM.${status}`)}
    });
    const currentYear = new Date().getFullYear();
    this.allYears = [(currentYear - 2), (currentYear - 1), currentYear, (currentYear + 1)];
    this.allMonths = Array.from(Array(12).keys()).map(m => {
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
    return this.vacations.filter(v=>['PLANNED', 'REQUESTED'].includes(v.status));
  }


  private fetchData() {
    this.loading = true;
    return vacationService.openPlanningPeriods()
        .then(periods => {
          this.openedPeriods = periods;
          return dictService.daysNotIncludedInVacations(this.allYears)
              .then(days => {
                    this.requestAction.daysNotIncludedInVacations = days;
                    return vacationService.myFutureVacations()
                        .then(data => {
                          this.vacations = data.filter(m => m.startDate && m.endDate);
                          return;
                        });
                  }
              )
        }).finally(() => {
          this.loading = false
        });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

  private vacationCanBeCanceled(vacation: MyVacation): boolean {
    let result = vacation && vacation.status === 'REQUESTED';
    result = result && this.openedPeriods && this.openedPeriods.some(period => period.year === vacation.year);
    return result;
  }

}
</script>

<style scoped>
.hover-table .v-btn.delete-btn {
  visibility: hidden;
}

.hover-table .v-data-table__wrapper tr:hover .v-btn.delete-btn {
  visibility: visible;
}
</style>
