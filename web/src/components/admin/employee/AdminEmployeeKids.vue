<!-- Employees kids admin table -->
<template>
  <v-container>
    <v-card>
      <v-card-title>
        <v-btn text icon @click="fetchData()">
          <v-icon>refresh</v-icon>
        </v-btn>
        <!-- Add new child -->
        <v-tooltip bottom v-if="canEditEmployees">
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openEditDialog(undefined)" icon>
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Добавить информацию о ребёнке') }}</span>
        </v-tooltip>

        <v-divider vertical class="mr-5"></v-divider>
        <v-row dense>
          <v-col lg="9" cols="8">
            <v-text-field
                v-model="filter.search"
                append-icon="mdi-magnify"
                :label="$t('Поиск')"
                single-line
                hide-details
            ></v-text-field>
          </v-col>
          <v-spacer></v-spacer>
          <v-col lg="3" cols="4">
            <v-checkbox :label="$t('Скрыть детей уволенных сотрудников')" v-model="filter.hideDismissed">
            </v-checkbox>
          </v-col>
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
            sort-by="displayName"
            class="text-truncate table-cursor"
            @click:row="openEditDialog"
        >
          <template v-slot:item.birthday="{ item }">
            {{ formatDate(item.birthday) }}
          </template>

          <template v-slot:item.parent.active="{ item }">
            {{ item.parent && item.parent.active ? $t('Нет') : $t('Да') }}
          </template>

        </v-data-table>

        <v-dialog v-model="editDialog" persistent>
          <admin-employee-kid-form
              v-bind:input="selectedItem"
              :all-employees="allEmployees"
              @close="editDialog=false;fetchData()"></admin-employee-kid-form>
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
import {DateTimeUtils} from "@/components/datetimeutils";
import AdminEmployeeKidForm from "@/components/admin/employee/AdminEmployeeKidForm.vue";
import adminEmployeeService, {EmployeeKid} from "@/components/admin/employee/admin.employee.service";
import {errorUtils} from "@/components/errors";
import permissionService from "@/store/modules/permission.service";
import {UiConstants} from "@/components/uiconstants";
import employeeService, {Employee} from "@/components/empl/employee.service";


class Filter {
  public search = '';
  public hideDismissed = true;
}

@Component({components: {AdminEmployeeKidForm}})
export default class AdminEmployeeKids extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;

  data: EmployeeKid[] = [];

  private allEmployees: Employee[] = [];

  private defaultItemsPerTablePage = UiConstants.defaultItemsPerTablePage;

  private filter = new Filter();

  private editDialog = false;

  private selectedItem: EmployeeKid | null = null;

  private error: string | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin kids component created');
    this.reloadHeaders();
    this.loading = true;
    return this.$nextTick()
        .then(() => {
          employeeService.findAll().then(employees => {
                this.allEmployees = employees;
              }
          )
        })
        .then(() => this.fetchData()).finally(() => this.loading = false);
  }


  private fetchData() {
    this.loading = true;
    this.data = [];
    this.error = null;
    return adminEmployeeService.findAllKids()
        .then(d => {
          this.data = d;
        }).catch(error => {
          this.error = errorUtils.shortMessage(error);
        })
        .finally(() => {
          this.loading = false
        });
  }


  private filteredData(): EmployeeKid[] {
    return this.data.filter((item) => {
      let filtered = true;
      // Search text field
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        filtered = (
            (item.displayName && item.displayName.toLowerCase().indexOf(search) >= 0)
            || (item.parent && item.parent.name.toLowerCase().indexOf(search) >= 0)
        ) as boolean;
      }
      // Hide dismissed
      if (filtered && this.filter.hideDismissed) {
        filtered = item.parent && item.parent.active;
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280});
    this.headers.push({
      text: this.$tc('День рождения'),
      value: 'birthday',
      width: 150,
      sort: DateTimeUtils.dateComparatorNullLast
    });
    this.headers.push({text: this.$tc('Возраст (лет)'), value: 'age', width: 150});
    this.headers.push({text: this.$tc('Родитель'), value: 'parent.name', width: 280});
    this.headers.push({text: this.$tc('Родитель уволен'), value: 'parent.active', width: 100});
  }

  private getById(array: Employee[], id?: number): string {
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


  public openEditDialog(selectedEmployee: EmployeeKid) {
    this.selectedItem = selectedEmployee;
    this.editDialog = true;
  }

  private canEditEmployees() {
    return permissionService.canAdminEmployees();
  }

}
</script>

<style scoped lang="css">
.table-cursor >>> tbody tr :hover {
  cursor: pointer;
}
</style>
