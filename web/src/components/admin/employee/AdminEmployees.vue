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
            dense
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items-per-page="15"
            :items="filteredData()"
            sort-by="displayName"
            class="text-truncate"
        >

          <template v-slot:item.displayName="{ item }">
            <v-btn small text @click="openEditDialog(item)">{{ item.displayName }}
            </v-btn>
          </template>

          <template v-slot:item.departmentId="{ item }">
            {{ getById(allDepartments, item.departmentId) }}
          </template>

          <template v-slot:item.positionId="{ item }">
            {{ getById(allPositions, item.positionId) }}
          </template>

          <template v-slot:item.currentProjectId="{ item }">
            {{ getById(allProjects, item.currentProjectId) }}
          </template>

          <template v-slot:item.levelId="{ item }">
            {{ getById(allLevels, item.levelId) }}
          </template>

          <template v-slot:item.birthday="{ item }">
            {{ formatDate(item.birthday) }}
          </template>

          <template v-slot:item.dateOfEmployment="{ item }">
            {{ formatDate(item.dateOfEmployment) }}
          </template>

          <template v-slot:item.dateOfDismissal="{ item }">
            {{ formatDate(item.dateOfDismissal) }}
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

  @Getter("positions", {namespace: namespace_dict})
  private allPositions!: Array<SimpleDict>;

  @Getter("levels", {namespace: namespace_dict})
  private allLevels!: Array<SimpleDict>;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin Employees component created');
    this.reloadHeaders();
    return this.$store.dispatch('dict/reloadProjects')
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() => this.$store.dispatch('dict/reloadPositions'))
        .then(() => this.$store.dispatch('dict/reloadLevels'))
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
      let filtered = true;
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        filtered = (
            (item.displayName && item.displayName.toLowerCase().indexOf(search) >= 0)
            || (item.email && item.email.toLowerCase().indexOf(search) >= 0)
            || (item.skype && item.skype.toLowerCase().indexOf(search) >= 0)
            || (item.phone && item.phone.toLowerCase().indexOf(search) >= 0)
        ) as boolean;
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('ФИО'), value: 'displayName', width: 280});
    this.headers.push({text: this.$tc('Email'), value: 'email', width: 280});
    this.headers.push({text: this.$tc('Текущий проект'), value: 'currentProjectId', width: 280});
    this.headers.push({text: this.$tc('Телефон'), value: 'phone', width: 150});
    this.headers.push({text: this.$tc('Skype'), value: 'skype', width: 150});
    this.headers.push({text: this.$tc('День рождения'), value: 'birthday', width: 150});
    this.headers.push({text: this.$tc('Дата трудоустройства'), value: 'dateOfEmployment', width: 150});
    this.headers.push({text: this.$tc('Дата увольнения'), value: 'dateOfDismissal', width: 150});
    this.headers.push({text: this.$tc('Позиция'), value: 'positionId', width: 280});
    this.headers.push({text: this.$tc('Уровень экспертизы'), value: 'levelId', width: 150});
    this.headers.push({text: this.$tc('Документ УЛ'), value: 'documentFull', width: 280});
    this.headers.push({text: this.$tc('Адрес по регистрации'), value: 'registrationAddress', width: 500});
    this.headers.push({text: this.$tc('Пол'), value: 'sex', width: 100});
    this.headers.push({text: this.$tc('ФИО супруга/супруги'), value: 'spouseName', width: 280});
    this.headers.push({text: this.$tc('Рабочий день'), value: 'workDay', width: 100});
    this.headers.push({text: this.$tc('Место работы'), value: 'workType', width: 150});
    this.headers.push({text: this.$tc('Подразделение'), value: 'departmentId', width: 300});
    this.headers.push({text: this.$tc('Город проживания'), value: 'cityOfResidence', width: 250});
    this.headers.push({text: this.$tc('Дети'), value: 'children', width: 280});
    this.headers.push({text: this.$tc('Семейный статус'), value: 'familyStatus', width: 150});
    this.headers.push({text: this.$tc('Загранпаспорт'), value: 'foreignPassport', width: 150});
    this.headers.push({text: this.$tc('Уровень английского'), value: 'englishLevel', width: 280});
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
