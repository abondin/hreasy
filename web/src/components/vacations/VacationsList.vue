<!-- All vacations table-->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <div class="d-flex align-center justify-space-between">
          <!-- Selected Year and Refresh button -->
          <v-btn text icon @click="fetchData()">
            <v-icon>refresh</v-icon>
          </v-btn>
          <v-select
              v-model="selectedYear"
              :items="allYears"
              :label="$t('Год')"
              class="mr-5 ml-5"
          ></v-select>
          <v-divider vertical></v-divider>
          <v-text-field
              v-model="filter.search"
              :label="$t('Поиск')" class="mr-5 ml-5"></v-text-field>


          <!-- Dates selection filter -->
          <my-date-range-component ref="dateSelector" v-model="filter.selectedDates"
                                   :label="$t('Дата начала отпуска')"></my-date-range-component>

          <!-- Current Project Filter -->
          <v-select
              clearable
              class="mr-5"
              v-model="filter.selectedProjects"
              :items="allProjects.filter(p=>p.active)"
              item-value="id"
              item-text="name"
              :label="$t('Текущий проект')"
              multiple
          ></v-select>
          <v-select
              clearable
              class="mr-5"
              v-model="filter.selectedStatuses"
              :items="allStatuses"
              :label="$t('Статус')"
              multiple
          ></v-select>

          <v-divider vertical></v-divider>
          <!-- Add new project -->
          <v-tooltip bottom v-if="canEditVacations">
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="col-auto">
                <v-btn text color="primary" :disabled="loading" @click="openVacationDialog(undefined)" icon>
                  <v-icon>mdi-plus</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Добавить отпуск') }}</span>
          </v-tooltip>


          <!-- Export -->
          <v-tooltip bottom v-if="canExportVacations">
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="col-auto">
                <v-btn text :disabled="loading" @click="exportToExcel()" icon>
                  <v-icon>mdi-file-excel</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Экспорт в excel') }}</span>
          </v-tooltip>
          <v-snackbar
              v-model="exportCompleted"
              timeout="5000"
          >
            {{ $t('Экспорт успешно завершён. Файл скачен.') }}
            <template v-slot:action="{ attrs }">
              <v-btn color="blue" icon v-bind="attrs" @click="exportCompleted = false">
                <v-icon>mdi-close-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-snackbar>

        </div>
      </v-card-title>

      <v-card-text>
        <v-tabs v-model="selectedTab">
          <v-tab>{{ $t('Все отпуска') }}</v-tab>
          <v-tab>{{ $t('Сводная по сотрудникам') }}</v-tab>
        </v-tabs>

        <v-tabs-items v-model="selectedTab">
          <!-- ALl vacations -->
          <v-tab-item>
            <v-data-table
                :loading="loading"
                :loading-text="$t('Загрузка_данных')"
                :headers="headers"
                :items="filteredItems()"
                multi-sort
                :sort-by="['employeeDisplayName']"
                dense
                :items-per-page="defaultItemsPerTablePage"
                class="text-truncate">
              <template v-slot:item.employeeDisplayName="{ item }">
                <v-btn small :disabled="!canEditVacations()" text @click="openVacationDialog(item)">
                  {{ item.employeeDisplayName }}
                </v-btn>
              </template>
              <template
                  v-slot:item.employeeCurrentProject="{ item }">
                {{ item.employeeCurrentProject ? item.employeeCurrentProject.name : '' }}
              </template>
              <template
                  v-slot:item.startDate="{ item }">
                {{ formatDate(item.startDate) }}
              </template>
              <template
                  v-slot:item.endDate="{ item }">
                {{ formatDate(item.endDate) }}
              </template>
              <template
                  v-slot:item.plannedStartDate="{ item }">
                {{ formatDate(item.plannedStartDate) }}
              </template>
              <template
                  v-slot:item.plannedEndDate="{ item }">
                {{ formatDate(item.plannedEndDate) }}
              </template>
              <template
                  v-slot:item.status="{ item }">
                {{ $t(`VACATION_STATUS_ENUM.${item.status}`) }}
              </template>
            </v-data-table>
          </v-tab-item>

          <!-- Vacation summary -->
          <v-tab-item>
            <v-data-table
                :loading="loading"
                :loading-text="$t('Загрузка_данных')"
                :headers="summaryHeaders"
                :items="filteredSummaryItems()"
                multi-sort
                :sort-by="['employeeDisplayName']"
                dense
                :items-per-page="defaultItemsPerTablePage"
                class="text-truncate">

              <template
                  v-slot:item.employeeDisplayName="{ item }">
                <v-btn small text @click="selectEmployee(item)">
                  {{ item.employeeDisplayName }}
                </v-btn>
              </template>
              <template
                  v-slot:item.employeeCurrentProject="{ item }">
                {{ item.employeeCurrentProject ? item.employeeCurrentProject.name : '' }}
              </template>
              <template
                  v-slot:item.upcomingVacation="{ item }">
                {{ formatDate(item.upcomingVacation.startDate) }} - {{ formatDate(item.upcomingVacation.endDate) }}
                ({{ $t(`VACATION_STATUS_ENUM.${item.upcomingVacation.status}`) }})
              </template>
            </v-data-table>
          </v-tab-item>
        </v-tabs-items>

        <v-dialog v-model="vacationDialog">
          <vacation-edit-form
              v-bind:all-employees="allEmployees"
              v-bind:all-statuses="allStatuses"
              v-bind:allYears="allYears"
              v-bind:input="selectedVacation"
              @close="vacationDialog=false;fetchData()"></vacation-edit-form>
        </v-dialog>

      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import vacationService, {Vacation} from "@/components/vacations/vacation.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {DataTableHeader} from "vuetify";
import moment, {Moment} from 'moment';
import VacationEditForm from "@/components/vacations/VacationEditForm.vue";
import employeeService from "@/components/empl/employee.service";
import permissionService from "@/store/modules/permission.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import {UiConstants} from "@/components/uiconstants";
import MyDateRangeComponent from "@/components/shared/MyDateRangeComponent.vue";
import {
  EmployeeVacationSummary,
  employeeVacationSummaryMapper
} from "@/components/vacations/employeeVacationSummaryService";
import {Watch} from "vue-property-decorator";

const namespace: string = 'dict';

class Filter {
  public selectedStatuses: Array<string> = ['PLANNED', 'TAKEN'];
  public search = '';
  public selectedProjects: Array<number> = [];
  public selectedDates: Array<string> = [];
}

@Component({
  components: {MyDateRangeComponent, VacationEditForm}
})
export default class VacationsListComponent extends Vue {
  headers: DataTableHeader[] = [];
  summaryHeaders: DataTableHeader[] = [];
  loading: boolean = false;
  vacations: Vacation[] = [];
  selectedTab: number = 0;

  @Getter("projects", {namespace})
  private allProjects!: Array<SimpleDict>;

  private readonly currentYear = new Date().getFullYear();

  private selectedYear: number = this.currentYear;


  private filter: Filter = new Filter();

  public allStatuses: Array<any> = [];
  public allEmployees: Array<SimpleDict> = [];
  public allYears: Array<number> = [];

  private vacationDialog = false;
  private selectedVacation: Vacation | null = null;

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private exportCompleted = false;

  /**
   * Lifecycle hook
   */
  created() {
    this.allStatuses = ['PLANNED', 'TAKEN', 'COMPENSATION', 'CANCELED', 'REJECTED'].map(status => {
      return {value: status, text: this.$tc(`VACATION_STATUS_ENUM.${status}`)}
    });
    this.allYears = [(this.currentYear - 2), (this.currentYear - 1), this.currentYear, (this.currentYear + 1)];

    this.reloadHeaders();
    this.$store.dispatch('dict/reloadProjects')
        .then(() => employeeService.findAll().then(data => {
          this.allEmployees = data.map(e => {
            return {id: e.id, name: e.displayName} as SimpleDict
          });
          return this.allEmployees;
        }))
        .then(() => this.fetchData());
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('ФИО'), value: 'employeeDisplayName'});
    this.headers.push({text: this.$tc('Текущий проект'), value: 'employeeCurrentProject'});
    this.headers.push({text: this.$tc('Год'), value: 'year'});
    this.headers.push({text: this.$tc('Начало'), value: 'startDate'});
    this.headers.push({text: this.$tc('Окончание'), value: 'endDate'});
    this.headers.push({text: this.$tc('Кол-во дней'), value: 'daysNumber'});
    this.headers.push({text: this.$tc('Статус'), value: 'status'});
    this.headers.push({text: this.$tc('Документ'), value: 'document'});
    this.headers.push({text: this.$tc('Примечание'), value: 'notes'});

    this.summaryHeaders.length = 0;
    this.summaryHeaders.push({text: this.$tc('ФИО'), value: 'employeeDisplayName'});
    this.summaryHeaders.push({text: this.$tc('Текущий проект'), value: 'employeeCurrentProject'});
    this.summaryHeaders.push({text: this.$tc('Год'), value: 'year'});
    this.summaryHeaders.push({
      text: this.$tc('Количество отпусков'),
      value: 'vacationsNumber'
    });
    this.summaryHeaders.push({text: this.$tc('Общее количество дней'), value: 'vacationsTotalDays'});
    this.summaryHeaders.push({text: this.$tc('Текущий или ближайший отпуск'), value: 'upcomingVacation'});
  }


  private filteredItems() {
    return this.vacations.filter(item => {
      let filtered = true;
      if (this.filter.search) {
        filtered = filtered && item.employeeDisplayName.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
      }
      if (this.filter.selectedStatuses.length > 0) {
        filtered = filtered && this.filter.selectedStatuses.indexOf(item.status) >= 0;
      }
      if (this.filter.selectedProjects.length > 0) {
        const project = item.employeeCurrentProject;
        filtered = filtered && project && this.filter.selectedProjects.indexOf(project.id) >= 0;
      }

      filtered = filtered && this.inDateRange(item);

      return filtered;
    });
  }

  private filteredSummaryItems(): EmployeeVacationSummary[] {
    return employeeVacationSummaryMapper.map(this.vacations).filter(s => {
      let filtered = true;
      if (this.filter.search) {
        filtered = filtered && s.employeeDisplayName.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
      }
      return filtered;
    });
  }

  @Watch('selectedYear')
  private watchSelectedYear() {
    this.fetchData();
  }

  private fetchData() {
    this.loading = true;
    return vacationService.findAll([this.selectedYear])
        .then(data => {
              this.vacations = (data as Vacation[]).filter(m => m.startDate && m.endDate && m.employeeDisplayName);
              return;
            }
        ).finally(() => {
          this.loading = false
        }).then(() => {
          this.resetSelectedDatesFilterToDefault();
        });
  }

  public exportToExcel() {
    this.loading = true;
    vacationService.export([this.selectedYear]).then(() => {
      this.exportCompleted = true;
    }).finally(() => {
      this.loading = false;
    })
  }

  public openVacationDialog(vacationToUpdate: Vacation | null) {
    this.selectedVacation = vacationToUpdate;
    this.vacationDialog = true;
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private canEditVacations(): boolean {
    return permissionService.canEditAllVacations();
  }

  private canExportVacations(): boolean {
    return permissionService.canExportAllVacations();
  }

  private inDateRange(item: Vacation): boolean {
    if (!this.filter.selectedDates || this.filter.selectedDates.length == 0 || !item.startDate) {
      return true;
    }
    const from = moment(this.filter.selectedDates[0]);
    const to: Moment | null = this.filter.selectedDates.length > 0 ? moment(this.filter.selectedDates[1]) : null;
    let result = true;
    result = result && (!to || !from.isAfter(item.startDate));
    result = result && (!to || !to.isBefore(item.startDate));
    return result;
  }

  private resetSelectedDatesFilterToDefault() {
    const start = moment().set('year', this.selectedYear).startOf('year');
    const end = moment().set('year', this.selectedYear).endOf('year')
    this.filter.selectedDates = [
      start.format(moment.HTML5_FMT.DATE)
      , end.format(moment.HTML5_FMT.DATE)
    ];
  }

  /**
   * Update search filter with selected employee display name and choose the first tab
   * @param item
   * @private
   */
  private selectEmployee(item: EmployeeVacationSummary) {
    this.filter.search = item.employeeDisplayName;
    this.selectedTab = 0;
  }

}
</script>

<style scoped>

</style>
