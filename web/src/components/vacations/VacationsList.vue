<!-- All vacations table-->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <div class="d-flex align-center justify-space-between">
          <!-- Refresh button -->
          <v-btn text icon @click="fetchData()">
            <v-icon>refresh</v-icon>
          </v-btn>
          <v-divider vertical></v-divider>
          <v-text-field
              v-model="filter.search"
              :label="$t('Поиск')" class="mr-5 ml-5"></v-text-field>
          <v-select
              clearable
              class="mr-5"
              v-model="filter.selectedYears"
              :items="allYears"
              :label="$t('Год')"
              multiple
          ></v-select>
          <v-select
              clearable
              class="mr-5"
              v-model="filter.selectedMonths"
              :items="allMonths"
              :label="$t('Месяц')"
              multiple
          ></v-select>
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
        </div>
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
          <template v-slot:item.employeeDisplayName="{ item }">
            <v-btn :disabled="!canEditVacations()" text @click="openVacationDialog(item)">{{ item.employeeDisplayName }}
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
import {OvertimeUtils} from "@/components/overtimes/overtime.service";
import moment from 'moment';
import VacationEditForm from "@/components/vacations/VacationEditForm.vue";
import employeeService from "@/components/empl/employee.service";
import permissionService from "@/store/modules/permission.service";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace: string = 'dict';

class Filter {
  public selectedStatuses: Array<string> = ['PLANNED', 'TAKEN'];
  public search = '';
  public selectedProjects: Array<number> = [];
  public selectedYears: Array<number> = [new Date().getFullYear()];
  public selectedMonths: Array<number> = [new Date().getMonth()];
}

@Component({
  components: {VacationEditForm}
})
export default class VacationsListComponent extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;
  vacations: Vacation[] = [];

  @Getter("projects", {namespace})
  private allProjects!: Array<SimpleDict>;

  private filter: Filter = new Filter();

  public allStatuses: Array<any> = [];
  public allYears: Array<number> = [];
  public allMonths: Array<any> = [];
  public allEmployees: Array<SimpleDict> = [];

  private vacationDialog = false;
  private selectedVacation: Vacation | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    this.allStatuses = ['PLANNED', 'TAKEN', 'COMPENSATION', 'CANCELED', 'REJECTED'].map(status => {
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
    this.headers.push({text: this.$tc('Статус'), value: 'status'});
    this.headers.push({text: this.$tc('Документ'), value: 'document'});
    this.headers.push({text: this.$tc('Примечание'), value: 'notes'});
  }


  private filteredItems() {
    return this.vacations.filter(item => {
      var filtered = true;
      if (this.filter.search) {
        filtered = filtered && item.employeeDisplayName.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
      }
      if (this.filter.selectedStatuses.length > 0) {
        filtered = filtered && this.filter.selectedStatuses.indexOf(item.status) >= 0;
      }
      if (this.filter.selectedYears.length > 0) {
        filtered = filtered && this.filter.selectedYears.indexOf(item.year) >= 0;
      }
      if (this.filter.selectedProjects.length > 0) {
        const project = item.employeeCurrentProject;
        filtered = filtered && project && this.filter.selectedProjects.indexOf(project.id) >= 0;
      }
      if (this.filter.selectedMonths.length > 0) {
        var dateInclude = !item.startDate || this.filter.selectedMonths.indexOf(new Date(item.startDate).getMonth()) >= 0;
        dateInclude = dateInclude || (!item.endDate || this.filter.selectedMonths.indexOf(new Date(item.endDate).getMonth()) >= 0)
        filtered = filtered && dateInclude;
      }
      return filtered;
    });
  }


  private fetchData() {
    this.loading = true;
    return vacationService.findAll()
        .then(data => {
              this.vacations = (data as Vacation[]).filter(m => m.startDate && m.endDate && m.employeeDisplayName);
              return;
            }
        ).finally(() => {
          this.loading = false
        });
  }

  public openVacationDialog(vacationToUpdate: Vacation | null) {
    this.selectedVacation = vacationToUpdate;
    this.vacationDialog = true;
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private canEditVacations() : boolean{
    return permissionService.canEditAllVacations();
  }


}
</script>

<style scoped>

</style>
