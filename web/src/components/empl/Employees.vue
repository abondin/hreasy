<!-- Employees short info table-->
<template>
  <v-container fluid>
    <v-card>
      <v-card-title>
        <v-row dense>
          <v-col cols="6">
            <v-text-field
                v-model="filter.search"
                append-icon="mdi-magnify"
                :label="$t('Поиск')"
                single-line
                hide-details
            ></v-text-field>
          </v-col>
          <v-col>
            <v-autocomplete
                clearable
                class="mr-5"
                v-model="filter.selectedProjects"
                :items="allProjects"
                :label="$t('Текущий проект')"
                multiple
            ></v-autocomplete>
          </v-col>
          <v-col>
            <v-autocomplete
                clearable
                class="mr-5"
                v-model="filter.selectedBas"
                :items="allBas.filter(p=>p.active)"
                item-value="id"
                item-text="name"
                :label="$t('Бизнес Аккаунт')"
                multiple
            ></v-autocomplete>
          </v-col>
        </v-row>
      </v-card-title>
      <v-data-table
          :loading="loading"
          :loading-text="$t('Загрузка_данных')"
          :headers="headers"
          :items="filtered()"
          sort-by="displayName"
          :show-expand="$vuetify.breakpoint.mdAndUp"
          disable-pagination
          single-expand
          hide-default-footer
      >
        <template v-slot:expanded-item="{ headers, item }">
          <td :colspan="headers.length">
            <employee-card v-bind:employee="item" @employeeUpdated="fetchData()">
            </employee-card>
          </td>
        </template>
        <template v-slot:item.currentProject="{ item }">
          <span v-if="item.currentProject">
            {{ item.currentProject.name }}
            <span v-if="item.currentProject.role"> ({{ item.currentProject.role }})
            </span>
          </span>
        </template>
      </v-data-table>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import employeeService, {Employee} from "@/components/empl/employee.service";
import {DataTableHeader} from "vuetify";
import EmployeeCard from "@/components/empl/EmployeeCard.vue";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";

const namespace_dict: string = 'dict';

class Filter {
  public selectedProjects: Array<number | null> = [];
  public selectedBas: number[] = [];
  public search: string = "";
}

@Component({
  components: {"employee-card": EmployeeCard}
})
export default class EmployeesComponent extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("positions", {namespace: namespace_dict})
  private allPositions!: Array<SimpleDict>;

  @Getter("projects", {namespace: namespace_dict})
  private projects!: Array<SimpleDict>;

  private allProjects: object[] = [];

  employees: Employee[] = [];
  private filter = new Filter();

  /**
   * Lifecycle hook
   */
  created() {
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName'});
    this.headers.push({text: this.$tc('Отдел'), value: 'department.name'});
    this.headers.push({text: this.$tc('E-mail'), value: 'email'});
    this.headers.push({text: this.$tc('Текущий проект'), value: 'currentProject.name'});
    this.headers.push({text: this.$tc('Бизнес Аккаунт'), value: 'ba.name'});
    // TODO Uncomment me after information about positions in STM actualized
    //this.headers.push({text: this.$tc('Позиция'), value: 'position.name'});

    // Reload projects dict to Vuex
    return this.fetchData()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadProjects'))
        .then(() => this.$store.dispatch('dict/reloadSkillGroups'))
        .then(() => this.$store.dispatch('dict/reloadSharedSkills'))
        .then(() => this.$store.dispatch('dict/reloadPositions'))
        .then(() => {
          this.allProjects.length = 0;
          this.allProjects.push({value: null, text: this.$tc('Без проекта')});
          this.allProjects.push({divider: true});
          this.allProjects = this.allProjects.concat(this.projects.filter(p => p.active).map(p => {
            return {
              value: p.id,
              text: p.name
            }
          }));
        });
  }

  private fetchData() {
    this.loading = true;
    return employeeService.findAll()
        .then(data => {
              this.employees = data as Employee[];
            }
        ).finally(() => {
          this.loading = false
        });
  }

  private filtered() {
    return this.employees.filter(e => {
      let filtered = true;
      if (this.filter.selectedProjects && this.filter.selectedProjects.length > 0) {
        filtered = filtered && (
            (!e.currentProject && this.filter.selectedProjects.indexOf(null) >= 0) ||
            (e.currentProject != undefined && this.filter.selectedProjects.indexOf(e.currentProject!.id) >= 0)
        );
      }
      if (this.filter.selectedBas && this.filter.selectedBas.length > 0) {
        filtered = filtered && (e.ba != undefined && this.filter.selectedBas.indexOf(e.ba!.id) >= 0);
      }
      if (this.filter.search) {
        const str = this.filter.search.trim().toLocaleLowerCase();
        let searchFilter: boolean = false;
        // Display Name
        searchFilter = searchFilter || e.displayName.toLocaleLowerCase().indexOf(str) >= 0;
        // Position
        searchFilter = searchFilter || e.position && e.position.name.toLocaleLowerCase().indexOf(str) >= 0;
        // Role on current project
        searchFilter = searchFilter || Boolean(e.currentProject && e.currentProject.role && e.currentProject.role.toLocaleLowerCase().indexOf(str) >= 0);
        // Telegram
        searchFilter = searchFilter || Boolean(e.telegram && e.telegram.toLocaleLowerCase().indexOf(str) >= 0);
        // Email
        searchFilter = searchFilter || (Boolean(e.email) && e.email.toLocaleLowerCase().indexOf(str) >= 0);
        // Skills
        searchFilter = searchFilter || e.skills.filter(s => s.name.toLocaleLowerCase().indexOf(str) >= 0).length > 0;
        filtered = filtered && searchFilter;
      }
      return filtered;
    });
  }

}
</script>

<style scoped>

</style>
