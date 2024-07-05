<!-- All vacations table-->
<template>
  <v-container>
    <v-card>

      <v-tabs v-model="selectedTab">
        <v-tab>{{ $t('Все отпуска') }}</v-tab>
        <v-tab>{{ $t('Сводная по сотрудникам') }}</v-tab>
        <v-tab>{{ $t('Календарь') }}</v-tab>
      </v-tabs>

      <!-- Filter Area -->
      <v-container fluid class="pb-0">
        <v-row>
          <v-col lg="2" sm="6" xs="6" class="pb-0">
            <!-- Selected year -->
            <v-select
                v-model="selectedYear"
                :items="allYears"
                :label="$t('Год')"></v-select>
          </v-col>

          <v-col lg="4" sm="6" xs="6" class="pb-0">
            <!-- Dates selection filter -->
            <my-date-range-component ref="dateSelector" v-model="filter.selectedDates"
                                     :label="$t('Дата начала отпуска')"></my-date-range-component>
          </v-col>

          <!-- Current Project Filter -->
          <v-col lg="3" sm="6" xs="6" class="pb-0">
            <v-select
                clearable
                v-model="filter.selectedProjects"
                :items="allProjects.filter(p=>p.active)"
                item-value="id"
                item-text="name"
                :label="$t('Текущий проект')"
                multiple
            ></v-select>
          </v-col>
          <!-- Vacation Status -->
          <v-col lg="3" sm="6" xs="6" class="pb-0">
            <v-select
                clearable
                v-model="filter.selectedStatuses"
                :items="allStatuses"
                :label="$t('Статус')"
                multiple
            ></v-select>
          </v-col>


        </v-row>

        <v-row>
          <v-col class="pt-0 pb-0">
            <!-- Main search -->
            <v-text-field
                class="mt-0 pt-0"
                v-model="filter.search"
                :label="$t('Поиск')" clearable></v-text-field>
          </v-col>

          <v-spacer></v-spacer>
          <v-col class="row pt-0 pb-0" align-self="center" cols="auto">
            <!-- Refresh button -->
            <v-tooltip bottom v-if="canEditVacations">
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
                  <v-btn text icon @click="fetchData(false)">
                    <v-icon>refresh</v-icon>
                  </v-btn>
                </div>
              </template>
              <span>{{ $t('Обновить данные') }}</span>
            </v-tooltip>
            <!-- Add new project -->
            <v-tooltip bottom v-if="canEditVacations">
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
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
                <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
                  <v-btn text :disabled="loading" @click="exportToExcel()" icon>
                    <v-icon>mdi-file-excel</v-icon>
                  </v-btn>
                </div>
              </template>
              <span>{{ $t('Экспорт в excel') }}</span>
            </v-tooltip>

            <v-snackbar
                v-model="snackbarNotification"
                timeout="5000">
              {{ snackbarMessage }}
              <template v-slot:action="{ attrs }">
                <v-btn color="blue" icon v-bind="attrs" @click="snackbarNotification = false" class="mt-0 pt-0">
                  <v-icon>mdi-close-circle-outline</v-icon>
                </v-btn>
              </template>
            </v-snackbar>
          </v-col>
        </v-row>
      </v-container>

      <!-- Main content -->
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
              <v-menu>
                <template v-slot:activator="{ on, attrs }">
                  <v-btn small text v-bind="attrs" v-on="on">
                    {{ item.employeeDisplayName }}
                  </v-btn>
                </template>
                <v-list>
                  <v-list-item>
                    <v-btn small text @click="copyToClipboard(item)">
                      <v-icon>content_paste</v-icon>
                      {{ $t('Копировать') }}
                    </v-btn>
                  </v-list-item>
                  <v-list-item>
                    <v-btn small text :disabled="!canEditVacations()" @click="openVacationDialog(item)">
                      <v-icon>mdi-pencil</v-icon>
                      {{ $t('Редактировать') }}
                    </v-btn>
                  </v-list-item>
                </v-list>
              </v-menu>
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
              <span v-if="item.upcomingVacation">
              {{ formatDate(item.upcomingVacation.startDate) }} - {{ formatDate(item.upcomingVacation.endDate) }}
              ({{ $t(`VACATION_STATUS_ENUM.${item.upcomingVacation.status}`) }})
                </span>
            </template>
          </v-data-table>
        </v-tab-item>

        <!-- Calendar -->
        <v-tab-item>
          <vacations-calendar
              :vacations="filteredItems()" :year="selectedYear"></vacations-calendar>
        </v-tab-item>
      </v-tabs-items>

      <v-dialog v-model="vacationDialog">
        <vacation-edit-form ref="vacationEditForm"
                            v-bind:all-employees="allEmployees"
                            v-bind:all-statuses="allStatuses"
                            v-bind:allYears="allYears"
                            v-bind:input="selectedVacation"
                            v-bind:default-year="selectedYear"
                            v-bind:days-not-included-in-vacations="daysNotIncludedInVacations"
                            @close="vacationDialog=false;fetchData(false)"></vacation-edit-form>
      </v-dialog>

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
import dictService from "@/store/modules/dict.service";
import VacationsCalendar from "@/components/vacations/VacationsCalendar.vue";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";

const namespace = 'dict';

class Filter {
  public selectedStatuses: Array<string> = ['PLANNED', 'TAKEN'];
  public search = '';
  public selectedProjects: Array<number> = [];
  public selectedDates: Array<string> = [];
}

@Component({
  name: 'VacationsList',
  components: {VacationsCalendar, MyDateRangeComponent, VacationEditForm}
})
export default class VacationsListComponent extends Vue {
  headers: DataTableHeader[] = [];
  summaryHeaders: DataTableHeader[] = [];
  loading = false;
  vacations: Vacation[] = [];
  selectedTab = 0;

  @Getter("projects", {namespace})
  private allProjects!: Array<SimpleDict>;

  private readonly currentYear = new Date().getFullYear();

  private selectedYear: number = this.currentYear;


  private filter: Filter = new Filter();

  public allStatuses: Array<any> = [];
  public allEmployees: Array<SimpleDict> = [];
  public allYears: Array<number> = [];
  public daysNotIncludedInVacations: Array<string> = [];

  private vacationDialog = false;
  private selectedVacation: Vacation | null = null;

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private snackbarNotification = false;
  private snackbarMessage = '';

  /**
   * Lifecycle hook
   */
  created() {
    this.allStatuses = ['PLANNED', 'TAKEN', 'COMPENSATION', 'CANCELED', 'REJECTED'].map(status => {
      return {value: status, text: this.$tc(`VACATION_STATUS_ENUM.${status}`)}
    });
    this.allYears = DateTimeUtils.defaultYears();

    this.reloadHeaders();
    this.$store.dispatch('dict/reloadProjects')
        .then(() => dictService.daysNotIncludedInVacations(this.allYears))
        .then(data => {
          this.daysNotIncludedInVacations = data;
        })
        .then(() => employeeService.findAll().then(data => {
          this.allEmployees = data.map(e => {
            return {id: e.id, name: e.displayName} as SimpleDict
          });
          return this.allEmployees;
        }))
        .then(() => this.fetchData(true));
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
      // Current project
      filtered = filtered && searchUtils.array(this.filter.selectedProjects, item.employeeCurrentProject?.id);
      // Status
      filtered = filtered && searchUtils.array(this.filter.selectedStatuses, item.status);

      const textFilters = TextFilterBuilder.of()
          // Display name
          .splitWords(item.employeeDisplayName)
          // Project role
          .ignoreCase(item.employeeCurrentProject?.role);

      filtered = filtered && searchUtils.textFilter(this.filter.search, textFilters);
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
    this.fetchData(true);
  }

  private fetchData(resetFilter: boolean) {
    this.loading = true;
    return vacationService.findAll([this.selectedYear])
        .then(data => {
              this.vacations = (data as Vacation[]).filter(m => m.startDate && m.endDate && m.employeeDisplayName);
              return;
            }
        ).finally(() => {
          this.loading = false
        }).then(() => {
          if (resetFilter) {
            this.resetSelectedDatesFilterToDefault();
          }
        });
  }

  public exportToExcel() {
    this.loading = true;
    vacationService.export([this.selectedYear]).then(() => {
      this.snackbarNotification = true;
      this.snackbarMessage = this.$tc('Экспорт успешно завершён. Файл скачен.');
    }).finally(() => {
      this.loading = false;
    })
  }

  public openVacationDialog(vacationToUpdate: Vacation | null) {
    this.selectedVacation = vacationToUpdate;
    this.vacationDialog = true;
    // Wait before input bind to the form
    this.$nextTick(() => {
      if (this.$refs.vacationEditForm) {
        (this.$refs.vacationEditForm as VacationEditForm).reset();
      }
    })
  }

  /**
   * Copy selected vacation to clipboard as a table
   */
  public copyToClipboard(vacation: Vacation | null) {
    if (!vacation) {
      return;
    }
    this.$copyText(
        `${vacation.employeeDisplayName}
        ${vacation.employeeCurrentProject ? vacation.employeeCurrentProject.name : ''}
     ${this.formatDate(vacation.startDate)}-${this.formatDate(vacation.endDate)} ${vacation.daysNumber}
     ${this.$tc('VACATION_STATUS_ENUM.' + vacation.status)}`
            .replace(/\s\s+/g, ' ')
    ).then(() => {
      this.snackbarNotification = true;
      this.snackbarMessage = this.$tc('Скопировано в буфер обмена');
    });
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
