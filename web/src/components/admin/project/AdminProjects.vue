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
        <v-divider vertical class="mr-5 ml-5"></v-divider>
        <!-- Add new project -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openProjectDialog(undefined)" icon>
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Создать новый проект') }}</span>
        </v-tooltip>

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
          <template v-slot:item.name="{ item }">
            <v-btn text @click="openProjectDialog(item)">{{ item.name }}
            </v-btn>
          </template>
          <template
              v-slot:item.startDate="{ item }">
            {{ formatDate(item.startDate) }}
          </template>
          <template
              v-slot:item.endDate="{ item }">
            {{ formatDate(item.endDate) }}
          </template>
        </v-data-table>

        <v-dialog v-model="projectDialog">
          <admin-project-form
              ref="adminProjectForm"
              v-bind:input="selectedProject"
              :all-departments="allDepartments"
              :all-business-accounts="allBusinessAccounts"
              @close="projectDialog=false;fetchData()"></admin-project-form>
        </v-dialog>

      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import adminProjectService, {ProjectFullInfo} from "@/components/admin/project/admin.project.service";
import Component from "vue-class-component";
import AdminProjectForm from "@/components/admin/project/AdminProjectForm.vue";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace_dict: string = 'dict';

class Filter {
  public showClosed = false;
  public search = '';
}

@Component({
      components: {AdminProjectForm}
    }
)
export default class AdminProjects extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  projects: ProjectFullInfo[] = [];

  private filter = new Filter();

  private projectDialog = false;
  private selectedProject: ProjectFullInfo | null = null;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBusinessAccounts!: Array<SimpleDict>;


  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin Project component created');
    this.reloadHeaders();
    // Reload projects dict to Vuex
    return this.$store.dispatch('dict/reloadDepartments')
        .then(() => this.$store.dispatch("dict/reloadBusinessAccounts"))
        .then(() => this.fetchData());
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
        filtered = filtered && (p.active);
      }
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        filtered = filtered &&
            (
                (p.name.toLowerCase().indexOf(search) >= 0) ||
                (p.department && p.department.name && p.department.name.toLowerCase().indexOf(search) >= 0) ||
                (p.businessAccount && p.businessAccount.name && p.businessAccount.name.toLowerCase().indexOf(search) >= 0) ||
                (p.customer && p.customer.toLowerCase().indexOf(search) >= 0)
            ) as boolean
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Наименование'), value: 'name'});
    this.headers.push({text: this.$tc('Бизнес аккаунт'), value: 'businessAccount.name'});
    this.headers.push({text: this.$tc('Заказчик'), value: 'customer'});
    this.headers.push({text: this.$tc('Начало'), value: 'startDate'});
    this.headers.push({text: this.$tc('Окончание'), value: 'endDate'});
    this.headers.push({text: this.$tc('Отдел'), value: 'department.name'});
  }

  public openProjectDialog(projectToUpdate: ProjectFullInfo | null) {
    this.selectedProject = projectToUpdate;
    this.projectDialog = true;
    // Wait dialog appears and reset all old valued
    this.$nextTick(() => {
      (this.$refs.adminProjectForm as AdminProjectForm).reset();
    });
  }

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

}
</script>
