<!-- Projects admin table -->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <!-- Refresh button -->
        <v-btn text icon @click="fetchData()">
          <v-icon>refresh</v-icon>
        </v-btn>
        <v-divider vertical></v-divider>
        <v-text-field
            v-model="filter.search"
            :label="$t('Поиск')" class="mr-5 ml-5"></v-text-field>
        <v-checkbox :label="$t('Показать закрытые проекты')" v-model="filter.showClosed">
        </v-checkbox>
      </v-card-title>
      <v-card-text>
        <v-data-table
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredProjects()"
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
  public showClosed = false;
  public search = '';
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


  private filteredProjects() {
    return this.projects.filter((p) => {
      var filtered = true;
      if (!this.filter.showClosed) {
        filtered = filtered && (!p.endDate || p.endDate <= new Date());
      }
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        filtered = filtered &&
            (
                (p.name.toLowerCase().indexOf(search) >= 0) ||
                (p.department && p.department.name && p.department.name.toLowerCase().indexOf(search) >= 0) ||
                (p.customer && p.customer.toLowerCase().indexOf(search) >= 0)
            ) as boolean
      }
      return filtered;
    });
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
