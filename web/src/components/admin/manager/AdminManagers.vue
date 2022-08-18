<!-- Managers of departments, business accounts and projects-->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <v-btn text icon @click="fetchData()">
          <v-icon>refresh</v-icon>
        </v-btn>
        <!-- Add manager record -->

        <v-divider vertical class="mr-5"></v-divider>
        <v-row dense>
          <v-col>
            <v-text-field
                v-model="filter.search"
                append-icon="mdi-magnify"
                :label="$t('Поиск')"
                single-line
                hide-details
            ></v-text-field>
          </v-col>
          <v-spacer></v-spacer>
        </v-row>
      </v-card-title>
      <v-card-text>
        <v-alert type="error" v-if="error">{{ error }}</v-alert>
        <v-data-table
            dense
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items-per-page="defaultItemsPerTablePage"
            :items="filteredData()"
            class="text-truncate table-cursor">
          <template v-slot:item.responsibilityType="{ item }">
            {{ $t(`MANAGER_RESPONSIBILITY_TYPE.${item.responsibilityType}`) }}
          </template>
          <template v-slot:item.responsibilityObject.type="{ item }">
            {{ $t(`MANAGER_RESPONSIBILITY_OBJECT.${item.responsibilityObject.type}`) }}
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
import {DateTimeUtils} from "@/components/datetimeutils";
import {errorUtils} from "@/components/errors";
import permissionService from "@/store/modules/permission.service";
import {UiConstants} from "@/components/uiconstants";
import employeeService, {Employee} from "@/components/empl/employee.service";
import adminManagerService, {
  Manager,
  ManagerResponsibilityObject
} from "@/components/admin/manager/admin.manager.service";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";


class Filter {
  public search = '';
}

const namespace_dict: string = 'dict';

@Component({components: {}})
export default class AdminManagers extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  data: Manager[] = [];

  private allEmployees: Employee[] = [];

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new Filter();

  private error: string | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Managers component created');
    this.reloadHeaders();
    this.loading = true;
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadProjects'))
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        )
        .then(() => this.fetchData()).finally(() => this.loading = false);
  }


  private fetchData() {
    this.loading = true;
    this.data = [];
    this.error = null;
    return adminManagerService.findAll()
        .then(d => {
          this.data = d;
        }).catch(error => {
          this.error = errorUtils.shortMessage(error);
        })
        .finally(() => {
          this.loading = false
        });
  }


  private filteredData(): Manager[] {
    return this.data.filter((item) => {
      let filtered = true;
      const search = this.filter.search.toLowerCase().trim();
      if (search.length>0) {
        let searchFilter: boolean = false;
        searchFilter = searchFilter || Boolean(item.employee && item.employee.name && item.employee.name.toLowerCase().indexOf(search) >= 0)
        searchFilter = searchFilter || Boolean(item.responsibilityObject && item.responsibilityObject.name && item.responsibilityObject.name.toLowerCase().indexOf(search) >= 0)
        filtered = filtered && searchFilter;
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Менеджер'), value: 'employee.name'});
    this.headers.push({text: this.$tc('Тип объекта'), value: 'responsibilityObject.type'});
    this.headers.push({text: this.$tc('Объект'), value: 'responsibilityObject.name'});
    this.headers.push({text: this.$tc('Основное направление'), value: 'responsibilityType'});
    this.headers.push({text: this.$tc('Примечание'), value: 'comment'});
  }


  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }


  private canEdit() {
    return permissionService.canAdminManagers();
  }

  private getEmployeeName(id?: number): string {
    if (id) {
      const find = this.allEmployees.find(e => e.id == id);
      return find ? find.displayName : this.$tc("Не найден: ") + id;
    } else {
      return '-';
    }
  }

  private getResponsibilityObject(responsibilityObject?: ManagerResponsibilityObject): string | undefined {
    let result: string | undefined;
    if (responsibilityObject) {
      switch (responsibilityObject.type) {
        case "business_account":
          result = this.getDictById(this.allBas, responsibilityObject.id);
          break;
        case "department":
          result = this.getDictById(this.allDepartments, responsibilityObject.id);
          break;
        case "project":
          result = this.getDictById(this.allProjects, responsibilityObject.id);
          break;
      }
    }
    return result;
  }

  private getDictById(array: SimpleDict[], id?: number): string {
    if (id) {
      const find = array.find(e => e.id == id);
      return find ? find.name : this.$tc("Не найден: ") + id;
    } else {
      return '-';
    }
  }

}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
