<!-- Projects admin table -->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <!-- Refresh button -->
        <v-btn text icon @click="fetchData()">
          <v-icon>refresh</v-icon>
        </v-btn>
      </v-card-title>
      <v-card-text>
        <v-data-table
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="projects"
            hide-default-footer
            sort-by="name"
            sort
            disable-pagination>
          <template
              v-slot:item.startDate="{ item }">
            {{ formatDate(item.startDate) }}
          </template>
          <template
              v-slot:item.endDate="{ item }">
            {{ formatDate(item.endDate) }}
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import adminProjectService, {ProjectFullInfo} from "@/components/admin/admin.project.service";
import Component from "vue-class-component";
import {OvertimeUtils} from "@/components/overtimes/overtime.service";


class Filter {
  public showClosed = true;
}

@Component
export default class AdminProjects extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  projects: ProjectFullInfo[] = [];

  private filter = new Filter();

  /**
   * Lifecycle hook
   */
  created() {
    this.reloadHeaders();
    // Reload projects dict to Vuex
    return this.fetchData();
  }

  private fetchData() {
    this.loading = true;
    this.projects = [];
    return adminProjectService.findAll()
        .then(projects => {
          this.projects = projects;
        }).finally(() => {
          this.loading = false
        });
  }

  private applyFilters() {
    // TODO
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Наименование'), value: 'name'});
    this.headers.push({text: this.$tc('Заказчик'), value: 'customer'});
    this.headers.push({text: this.$tc('Начало'), value: 'startDate'});
    this.headers.push({text: this.$tc('Окончание'), value: 'endDate'});
    this.headers.push({text: this.$tc('Отдел'), value: 'department.name'});
  }

  private formatDate(date: Date): string | undefined {
    return OvertimeUtils.formatDate(date);
  }

}
</script>
