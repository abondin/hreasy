<!-- Employees short info table-->
<template>
  <v-container fluid>
    <v-card>
      <v-card-title>
        <v-row dense>
          <v-col lg="6" cols="12">
            <v-text-field
                v-model="filter.search"
                append-icon="mdi-magnify"
                :label="$t('Поиск')"
                single-line
                hide-details
            ></v-text-field>
          </v-col>
          <v-col lg="6" cols="12">
            <v-select
                clearable
                class="mr-5"
                v-model="filter.selectedProjects"
                :items="allProjects.filter(p=>p.active)"
                item-value="id"
                item-text="name"
                :label="$t('Текущий проект')"
                multiple
            ></v-select>
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
          hide-default-footer
      >
        <template v-slot:expanded-item="{ headers, item }">
          <td :colspan="headers.length">
            <employee-card v-bind:employee="item">
            </employee-card>
          </td>
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
  public selectedProjects: number[] = [];
  public search: string = "";
}

@Component({
  components: {"employee-card": EmployeeCard}
})
export default class EmployeesComponent extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

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
    // Reload projects dict to Vuex
    return this.fetchData().then(() => this.$store.dispatch('dict/reloadProjects')
        .then(() => this.$store.dispatch('dict/reloadSkillGroups').then(() => {
          this.$store.dispatch('dict/reloadSharedSkills')
        })));
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
        filtered = filtered && (e.currentProject != undefined && this.filter.selectedProjects.indexOf(e.currentProject!.id) >= 0);
      }
      if (this.filter.search) {
        const str = this.filter.search.trim().toLocaleLowerCase();
        let searchFilter = false;
        searchFilter = searchFilter || e.displayName.toLocaleLowerCase().indexOf(str) >= 0;
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
