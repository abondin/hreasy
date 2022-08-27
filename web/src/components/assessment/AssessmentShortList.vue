<!-- All employees with latest assessment-->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <v-container>
          <v-row no-gutters align="center" justify="start">
            <!-- Refresh button -->
            <v-col cols="auto">
              <v-btn text icon @click="fetchData()">
                <v-icon>refresh</v-icon>
              </v-btn>
              <v-divider vertical></v-divider>
            </v-col>

            <v-col cols="auto">
              <!-- Export -->
              <v-tooltip bottom>
                <template v-slot:activator="{ on: ton, attrs: tattrs}">
                  <div v-bind="tattrs" v-on="ton" class="col-auto">
                    <v-btn v-if="canExport()" link :disabled="loading" @click="exportToExcel()" icon>
                      <v-icon>mdi-file-excel</v-icon>
                    </v-btn>
                  </div>
                </template>
                <p>{{ $t('Экспорт в Excel') }}</p>
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
            </v-col>

            <v-col>
              <v-text-field
                  v-model="filter.search"
                  :label="$t('Поиск')" class="mr-5 ml-5"></v-text-field>
            </v-col>
            <v-col>
              <v-autocomplete
                  clearable
                  class="mr-5"
                  v-model="filter.selectedBas"
                  :items="allBas.filter(p=>p.active)"
                  item-value="id"
                  item-text="name"
                  :label="$t('Бизнес аккаунт')"
                  multiple
              ></v-autocomplete>
            </v-col>
            <v-col>
              <v-autocomplete
                  clearable
                  class="mr-5"
                  v-model="filter.selectedProjects"
                  :items="allProjects.filter(p=>p.active)"
                  item-value="id"
                  item-text="name"
                  :label="$t('Текущий проект')"
                  multiple
              ></v-autocomplete>
            </v-col>
          </v-row>
        </v-container>
      </v-card-title>

      <v-card-text>
        <!-- Error block -->
        <v-alert v-if="error" type="error">
          {{ error }}
        </v-alert>
        <v-data-table
            dense
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredItems()"
            hide-default-footer
            :sort-by="['lastAssessmentDate']"
            sort-desc
            disable-pagination>
          <template v-slot:item.displayName="{ item }">
            <router-link :to="'/assessments/'+item.employeeId">{{ item.displayName }}</router-link>
          </template>
          <template
              v-slot:item.currentProject="{ item }">
            <span v-if="item.currentProject">
            {{ item.currentProject.name }}
            <span v-if="item.currentProject.role"> ({{ item.currentProject.role }})</span>
            </span>
          </template>
          <template
              v-slot:item.employeeDateOfEmployment="{ item }">
            {{ formatDate(item.employeeDateOfEmployment) }}
          </template>
          <template
              v-slot:item.lastAssessmentCompletedDate="{ item }">
            {{ formatDate(item.lastAssessmentCompletedDate) }}
          </template>
          <template
              v-slot:item.lastAssessmentDate="{ item }">
            {{ formatDate(item.lastAssessmentDate) }}
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DataTableHeader} from "vuetify";
import {DateTimeUtils} from "@/components/datetimeutils";
import assessmentService, {EmployeeAssessmentsSummary} from "@/components/assessment/assessment.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {errorUtils} from "@/components/errors";
import permissionService from "@/store/modules/permission.service";

const namespace_dict: string = 'dict';

class Filter {
  public search = '';
  public selectedProjects: number[] = [];
  public selectedBas: number[] = [];
}

@Component({})
export default class AssessmentShortList extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;
  error: string | null = null;
  assessments: EmployeeAssessmentsSummary[] = [];

  exportCompleted = false;


  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  private filter: Filter = new Filter();

  /**
   * Lifecycle hook
   */
  created() {
    this.reloadHeaders();
    return this.$store.dispatch('dict/reloadProjects')
        .then(()=>this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.fetchData());
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Сотрудник'), value: 'displayName'});
    this.headers.push({text: this.$tc('Бизнес аккаунт'), value: 'ba.name'});
    this.headers.push({text: this.$tc('Проект'), value: 'currentProject'});
    this.headers.push({text: this.$tc('Дата устройства'), value: 'employeeDateOfEmployment'});
    this.headers.push({text: this.$tc('Послений ассессмент запланирован'), value: 'lastAssessmentDate'});
    this.headers.push({text: this.$tc('Последний ассессмент завершен'), value: 'lastAssessmentCompletedDate'});
    this.headers.push({text: this.$tc('Дней без ассессмента'), value: 'daysWithoutAssessment'});
  }


  private filteredItems() {
    return this.assessments.filter(item => {
      let filtered = true;
      if (this.filter.search) {
        filtered = filtered && item.displayName.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
      }
      // Business Account
      if (filtered && this.filter.selectedBas && this.filter.selectedBas.length > 0) {
        filtered = (item.ba && this.filter.selectedBas.indexOf(item.ba.id) >= 0) as boolean;
      }
      // Project
      if (filtered && this.filter.selectedProjects && this.filter.selectedProjects.length > 0) {
        filtered = (item.currentProject && this.filter.selectedProjects.indexOf(item.currentProject.id) >= 0) as boolean;
      }
      return filtered;
    });
  }


  private fetchData() {
    this.loading = true;
    this.error = null;
    return assessmentService.allNotFiredEmployeesWithLatestAssessment()
        .then(data => {
              this.assessments = data;
              return this.assessments;
            }
        ).catch((ex: any) => {
          this.error = errorUtils.shortMessage(ex);
        })
        .finally(() => {
          this.loading = false
        });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private canExport() {
    return permissionService.canExportAssessments();
  }

  private exportToExcel() {
    this.loading = true;
    assessmentService.export().then(() => {
      this.exportCompleted = true;
    }).finally(() => {
      this.loading = false;
    })
  }
}
</script>

<style scoped>

</style>
