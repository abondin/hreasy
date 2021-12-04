<!-- All employees with latest assessment-->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <v-container>
          <v-row no-gutters align="center" justify="start">
            <!-- Refresh button -->
            <v-col cols="1">
              <v-btn text icon @click="fetchData()">
                <v-icon>refresh</v-icon>
              </v-btn>
              <v-divider vertical></v-divider>
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

const namespace_dict: string = 'dict';

class Filter {
  public search = '';
  public selectedProjects: number[] = [];
}

@Component({})
export default class AssessmentShortList extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;
  assessments: EmployeeAssessmentsSummary[] = [];

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  private filter: Filter = new Filter();

  /**
   * Lifecycle hook
   */
  created() {
    this.reloadHeaders();
    return this.$store.dispatch('dict/reloadProjects').then(() => this.fetchData());
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Сотрудник'), value: 'displayName'});
    this.headers.push({text: this.$tc('Проект'), value: 'currentProject.name'});
    this.headers.push({text: this.$tc('Дата устройства'), value: 'employeeDateOfEmployment'});
    this.headers.push({text: this.$tc('Крайний ассессмент запланирован'), value: 'lastAssessmentDate'});
    this.headers.push({text: this.$tc('Крайний ассессмент завершен'), value: 'lastAssessmentCompletedDate'});
    this.headers.push({text: this.$tc('Дней без ассессмента'), value: 'daysWithoutAssessment'});
  }


  private filteredItems() {
    return this.assessments.filter(item => {
      let filtered = true;
      if (this.filter.search) {
        filtered = filtered && item.displayName.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
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
    return assessmentService.allNotFiredEmployeesWithLatestAssessment()
        .then(data => {
              this.assessments = data;
              return this.assessments;
            }
        ).finally(() => {
          this.loading = false
        });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

<style scoped>

</style>
