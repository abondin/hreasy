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
        <v-divider vertical class="mr-5 ml-5"></v-divider>
      </v-card-title>
      <v-card-text>
        <v-data-table
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredData()"
            hide-default-footer
            sort-by="employee.name"
            sort
            disable-pagination>

          <!-- Roles -->
          <template v-slot:item.roles="{ item }">
            <v-chip
                small
                v-for="r in item.roles" v-bind:key="r"
                close close-icon="mdi-delete"
                @click:close="removeRole(r)">
              <v-avatar left>
                <v-icon>mdi-checkbox-marked-circle</v-icon>
              </v-avatar>
              {{r}}
            </v-chip>
          </template>


          <!-- Departments -->
          <template v-slot:item.accessibleDepartments="{ item }">
            <v-chip
                small
                v-for="depId in item.accessibleDepartments" v-bind:key="depId"
                close close-icon="mdi-delete"
                @click:close="removeDepartment(depId)">
              {{getById(allDepartments, depId)}}
            </v-chip>
          </template>

          <!-- Projects -->
          <template v-slot:item.accessibleProjects="{ item }">
            <v-chip
                small
                v-for="projectId in item.accessibleProjects" v-bind:key="projectId"
                close close-icon="mdi-delete"
                @click:close="removeProject(projectId)">
              {{getById(allProjects, projectId)}}
            </v-chip>
          </template>

        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import {DataTableHeader} from "vuetify";
import Component from "vue-class-component";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import adminUserService, {UserSecurityInfo} from "@/components/admin/admin.user.service";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace_dict: string = 'dict';

class Filter {
  public search = '';
}

@Component({})
export default class AdminUsers extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  data: UserSecurityInfo[] = [];

  private filter = new Filter();

  private selectedItem: UserSecurityInfo | null = null;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin Employees component created');
    this.reloadHeaders();
    // Reload projects dict to Vuex
    return this.$store.dispatch('dict/reloadProjects')
        .then(()=>this.$store.dispatch('dict/reloadDepartments'))
        .then(() => this.fetchData());
  }

  private fetchData() {
    this.loading = true;
    this.data = [];
    return adminUserService.findAll()
        .then(d => {
          this.data = d;
        }).finally(() => {
          this.loading = false
        });
  }


  private filteredData(): UserSecurityInfo[] {
    return this.data.filter((item) => {
      var filtered = true;
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        const projectIds = this.allProjects.filter(p=>p.name.toLowerCase().indexOf(search)>=0).map(p=>p.id);
        const departmentIds = this.allDepartments.filter(p=>p.name.toLowerCase().indexOf(search)>=0).map(p=>p.id);
        filtered = filtered &&
            (
            (item.employee.name.toLowerCase().indexOf(search) >= 0) ||
                (projectIds && item.accessibleProjects && item.accessibleProjects.some(i=>projectIds.includes(i))) ||
                (departmentIds && item.accessibleDepartments && item.accessibleDepartments.some(i=>departmentIds.includes(i))) ||
                (item.roles && item.roles.filter(r=>r.toLocaleString().indexOf(search)>=0).length>0)
            ) as boolean
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('ФИО'), value: 'employee.name'});
    this.headers.push({text: this.$tc('Роли'), value: 'roles'});
    this.headers.push({text: this.$tc('Доступные отделы'), value: 'accessibleDepartments'});
    this.headers.push({text: this.$tc('Доступные проекты'), value: 'accessibleProjects'});
  }

  private removeRole(role: string){
    alert(`Removing role ${role}`);
  }

  private removeDepartment(departmentId: string){
    alert(`Removing department ${departmentId}`);
  }

  private removeProject(projectId: string){
    alert(`Removing project ${projectId}`);
  }

  private getById(array: SimpleDict[], id?: number): string{
    if (id) {
      const find = array.find(e => e.id == id);
      return find ? find.name : this.$tc("Не найден: ") + id;
    } else {
      return '-';
    }
  }

  private formatDate(date: string|undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

}
</script>
