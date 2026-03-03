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

        <v-autocomplete
            class="mr-5"
            v-model="filter.bas"
            item-value="id" item-text="name"
            :items="allBusinessAccounts"
            :label="$t('Бизнес аккаунт')"
            multiple
            clearable
        ></v-autocomplete>


        <v-autocomplete
            class="mr-5"
            v-model="filter.departments"
            item-value="id" item-text="name"
            :items="allDepartments"
            :label="$t('Отдел')"
            multiple
            clearable
        ></v-autocomplete>

        <v-checkbox :label="$t('Показать закрытые проекты')" v-model="filter.showClosed">
        </v-checkbox>
        <v-divider vertical class="mr-5 ml-5"></v-divider>
        <!-- Add new project -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openCreateDialog(undefined)" icon>
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
            class="table-cursor"
            hide-default-footer
            sort-by="name"
            sort
            disable-pagination
            @click:row="(v)=>navigateProject(v.id)">

          <template
              v-slot:item.planStartDate="{ item }">
            {{ formatDate(item.planStartDate) }}
          </template>
          <template
              v-slot:item.startDate="{ item }">
            {{ formatDate(item.startDate) }}
          </template>
          <template
              v-slot:item.planEndDate="{ item }">
            {{ formatDate(item.planEndDate) }}
          </template>
          <template
              v-slot:item.endDate="{ item }">
            {{ formatDate(item.endDate) }}
          </template>
        </v-data-table>

      </v-card-text>
    </v-card>
    <v-dialog v-model="projectDialog">
      <admin-project-form
          ref="createProjectForm"
          :all-departments="allDepartments"
          :all-business-accounts="allBusinessAccounts"
          @close="event=>projectCreateDialogClosed(event)"></admin-project-form>
    </v-dialog>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import adminProjectService, {
  ProjectCreatedEvent,
  ProjectFullInfo
} from "@/components/admin/project/admin.project.service";
import Component from "vue-class-component";
import AdminProjectForm from "@/components/admin/project/AdminProjectForm.vue";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace_dict = 'dict';

class Filter {
  public showClosed = false;
  public search = '';
  public departments: number[] = [];
  public bas: number[] = [];
}

@Component({
      name: 'AdminProjects',
      components: {AdminProjectForm}
    }
)
export default class AdminProjects extends Vue {
  headers: DataTableHeader[] = [];
  loading = false;

  projects: ProjectFullInfo[] = [];

  private filter = new Filter();

  private projectDialog = false;

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

  private openCreateDialog() {
    this.projectDialog = true;
  }

  private projectCreateDialogClosed(event: any) {
    logger.debug("projectCreateDialogClosed", event);
    if (event instanceof ProjectCreatedEvent) {
      this.projectDialog = false;
      this.navigateProject(event.projectId);
    }
  }

  private filteredProjects() {
    return this.projects.filter((p) => {
      let filtered = true;
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
      if (this.filter.bas && this.filter.bas.length > 0) {
        filtered = filtered && Boolean(
            p.businessAccount && this.filter.bas.indexOf(p.businessAccount.id) >= 0);
      }
      if (this.filter.departments && this.filter.departments.length > 0) {
        filtered = filtered && Boolean(
            p.department && this.filter.departments.indexOf(p.department.id) >= 0);
      }

      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Наименование'), value: 'name'});
    this.headers.push({text: this.$tc('Бизнес аккаунт'), value: 'businessAccount.name'});
    this.headers.push({text: this.$tc('Заказчик'), value: 'customer'});
    this.headers.push({text: this.$tc('Начало (план)'), value: 'planStartDate'});
    this.headers.push({text: this.$tc('Начало (факт)'), value: 'startDate'});
    this.headers.push({text: this.$tc('Окончание (план)'), value: 'planEndDate'});
    this.headers.push({text: this.$tc('Окончание (факт)'), value: 'endDate'});
    this.headers.push({text: this.$tc('Отдел'), value: 'department.name'});
  }

  public navigateProject(projectId: number) {
    if (projectId) {
      this.$router.push(`/admin/projects/${projectId}`);
    }
  }

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

}
</script>
<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
