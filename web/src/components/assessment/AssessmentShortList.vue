<!-- All employees with latest assessment-->
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
              v-model="filter.showArchived"
              :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]"
              :label="$t('Показать архив')"
              multiple
          ></v-select>
        </div>
      </v-card-title>

      <v-card-text>
        <v-data-table
            dense
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredItems()"
            multi-sort
            hide-default-footer
            :sort-by="['latestActivity']"
            disable-pagination>
          <template
              v-slot:item.latestActivity="{ item }">
            {{ formatDate(item.latestActivity) }}
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
import articleAdminService, {ArticleFull} from "@/components/admin/article/admin.article.service";
import {ALL_ARTICLES_GROUPS} from "@/components/article/article.service";
import assessmentService, {AssessmentShortInfo} from "@/components/assessment/assessment.service";

const namespace: string = 'dict';

class Filter {
  public search = '';
}

@Component({})
export default class AssessmentShortList extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;
  assessments: AssessmentShortInfo[] = [];

  private filter: Filter = new Filter();

  /**
   * Lifecycle hook
   */
  created() {
    this.reloadHeaders();
    this.fetchData();
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Сотрудник'), value: 'displayName'});
    this.headers.push({text: this.$tc('Дата устройства'), value: 'employeeDateOfEmployment'});
    this.headers.push({text: this.$tc('Крайний ассессмент запланирован'), value: 'lastAssessmentDate'});
    this.headers.push({text: this.$tc('Крайний ассессмент завершен'), value: 'lastAssessmentCompletedDate'});
  }


  private filteredItems() {
    return this.assessments.filter(item => {
      let filtered = true;
      if (this.filter.search) {
        filtered = filtered && item.displayName.toLowerCase().indexOf(this.filter.search.toLowerCase()) >= 0;
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
