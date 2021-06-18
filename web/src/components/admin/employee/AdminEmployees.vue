<!-- Employees admin table -->
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
            sort-by="employee.displayName"
            sort
            disable-pagination>

          <template v-slot:item.employee.name="{ item }">
            <v-btn text @click="openEditDialog(item)">{{ item.employee.displayName }}
            </v-btn>
          </template>

        </v-data-table>

        <v-dialog v-model="editDialog">
          <admin-employee-form
              v-bind:input="selectedItem"
              @close="editDialog=false;fetchData()"></admin-employee-form>
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
import {DateTimeUtils} from "@/components/datetimeutils";
import AdminEmployeeForm from "@/components/admin/employee/AdminEmployeeForm.vue";
import adminEmployeeService, {EmployeeWithAllDetails} from "@/components/admin/employee/admin.employee.service";

const namespace_dict: string = 'dict';

class Filter {
  public search = '';
}

@Component({components: {AdminEmployeeForm}})
export default class AdminEmployees extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  data: EmployeeWithAllDetails[] = [];

  private filter = new Filter();

  private editDialog = false;

  private selectedItem: EmployeeWithAllDetails | null = null;

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
    return this.$store.dispatch('dict/reloadProjects')
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() => this.fetchData());
  }

  private fetchData() {
    this.loading = true;
    this.data = [];
    return adminEmployeeService.findAll()
        .then(d => {
          this.data = d;
        }).finally(() => {
          this.loading = false
        });
  }


  private filteredData(): EmployeeWithAllDetails[] {
    return this.data.filter((item) => {
      var filtered = true;
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        //TODO
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Email'), value: 'email'});
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName'});
    //TODO Add fields
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

  public openEditDialog(selectedEmployee: EmployeeWithAllDetails) {
    this.selectedItem = selectedEmployee;
    this.editDialog = true;
  }

}
</script>
