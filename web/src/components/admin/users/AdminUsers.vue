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
            dense
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredData()"
            hide-default-footer
            sort-by="employee.name"
            sort
            disable-pagination>

          <template v-slot:item.employee.name="{ item }">
            <v-btn small text @click="openEditDialog(item)">{{ item.employee.name }}
            </v-btn>
          </template>

          <!-- Roles -->
          <template v-slot:item.roles="{ item }">
            <v-chip x-small
                    v-for="r in item.roles" v-bind:key="r">
              {{ getById(allRoles, r) }}
            </v-chip>
          </template>


          <!-- Departments -->
          <template v-slot:item.accessibleDepartments="{ item }">
            <v-chip small
                    v-for="depId in item.accessibleDepartments" v-bind:key="depId">
              {{ getById(allDepartments, depId) }}
            </v-chip>
          </template>

          <!-- Business Accounts -->
          <template v-slot:item.accessibleBas="{ item }">
            <v-chip small
                    v-for="baId in item.accessibleBas" v-bind:key="baId">
              {{ getById(allBas, baId) }}
            </v-chip>
          </template>

          <!-- Projects -->
          <template v-slot:item.accessibleProjects="{ item }">
            <v-chip small
                    v-for="projectId in item.accessibleProjects" v-bind:key="projectId">
              {{ getById(allProjects, projectId) }}
            </v-chip>
          </template>

        </v-data-table>

        <v-dialog v-model="editDialog">
          <admin-user-roles-form
              v-bind:input="selectedItem"
              :all-projects="allProjects.filter(p=>p.active)"
              :all-departments="allDepartments.filter(p=>p.active)"
              :all-roles="allRoles"
              :all-bas="allBas"
              @close="editDialog=false;fetchData()"></admin-user-roles-form>
        </v-dialog>

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
import adminUserService, {RoleDict, UserSecurityInfo} from "@/components/admin/users/admin.user.service";
import {DateTimeUtils} from "@/components/datetimeutils";
import AdminUserRolesForm from "@/components/admin/users/AdminUserRolesForm.vue";

const namespace_dict = 'dict';

class Filter {
  public search = '';
}

@Component({components: {AdminUserRolesForm}})
export default class AdminUsers extends Vue {
  headers: DataTableHeader[] = [];
  loading = false;

  data: UserSecurityInfo[] = [];

  private filter = new Filter();

  private editDialog = false;

  private selectedItem: UserSecurityInfo | null = null;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  private allRoles: Array<RoleDict> = [];

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin Employees component created');
    this.reloadHeaders();
    this.allRoles = this.getAllRoles();
    return this.$store.dispatch('dict/reloadProjects')
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
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
      let filtered = true;
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        const projectIds = this.allProjects.filter(p => p.name.toLowerCase().indexOf(search) >= 0).map(p => p.id);
        const departmentIds = this.allDepartments.filter(p => p.name.toLowerCase().indexOf(search) >= 0).map(p => p.id);
        const basIds = this.allBas.filter(p => p.name.toLowerCase().indexOf(search) >= 0).map(p => p.id);
        const roleIds = this.allRoles.filter(p => p.name.toLowerCase().indexOf(search) >= 0).map(p => p.id);
        filtered = filtered &&
            (
                (item.employee.name.toLowerCase().indexOf(search) >= 0) ||
                (projectIds && item.accessibleProjects && item.accessibleProjects.some(i => projectIds.includes(i))) ||
                (departmentIds && item.accessibleDepartments && item.accessibleDepartments.some(i => departmentIds.includes(i))) ||
                (basIds && item.accessibleBas && item.accessibleBas.some(i => basIds.includes(i))) ||
                (roleIds && item.roles && item.roles.some(i => roleIds.includes(i)))
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
    this.headers.push({text: this.$tc('Доступные бизнес аккаунты'), value: 'accessibleBas'});
    this.headers.push({text: this.$tc('Доступные проекты'), value: 'accessibleProjects'});
  }

  private getById(array: SimpleDict[], id?: number): string {
    if (id) {
      const find = array.find(e => e.id == id);
      return find ? find.name : this.$tc("Не найден: ") + id;
    } else {
      return '-';
    }
  }

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  public openEditDialog(selectedUser: UserSecurityInfo) {
    this.selectedItem = selectedUser;
    this.editDialog = true;
  }

  //TODO Get from server/database
  private getAllRoles(): Array<RoleDict> {
    return [
      {id: "global_admin", name: this.$tc('role.global_admin'), disabled: true},
      {id: "hr", name: this.$tc('role.hr'), disabled: false},
      {id: "pm", name: this.$tc('role.pm'), disabled: false},
      {id: "pm_finance", name: this.$tc('role.pm_finance'), disabled: false},
      {id: "salary_manager", name: this.$tc('role.salary_manager'), disabled: false},
      {id: "content_management", name: this.$tc('role.content_management'), disabled: false},
      {id: "mentor", name: this.$tc('role.mentor'), disabled: false}
    ] as Array<RoleDict>
  }
}
</script>
