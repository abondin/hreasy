<!-- Business accounts admin table -->
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
        <v-checkbox :label="$t('Показать архивные бизнес аккаунты')" v-model="filter.showArchived">
        </v-checkbox>
        <v-divider vertical class="mr-5 ml-5"></v-divider>
        <!-- Add new businessAccount -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openBADialog(undefined)" icon>
                <v-icon>mdi-plus</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Создать новый бизнес аккаунт') }}</span>
        </v-tooltip>

      </v-card-title>
      <v-card-text>
        <v-data-table
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredBA()"
            hide-default-footer
            sort-by="name"
            sort
            disable-pagination>
          <template v-slot:item.name="{ item }">
            <v-btn text link :to="'/admin/ba/'+item.id" :class="{archived:item.archived}">
              {{ item.name }}
            </v-btn>
          </template>
          <template v-slot:item.managers="{ item }">
            <v-chip v-for="m in item.managers" v-bind:key="m.id">
              {{m.employeeName}}
            </v-chip>
          </template>
        </v-data-table>

        <v-dialog v-model="baDialog">
          <admin-b-a-form
              v-bind:input="selectedBA"
              v-bind:allEmployees="allEmployees"
              @close="baDialog=false;fetchData()"></admin-b-a-form>
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
import AdminBAForm from "@/components/admin/business_account/AdminBAForm.vue";
import adminBaService, {BusinessAccount} from "@/components/admin/business_account/admin.ba.service";
import employeeService, {Employee} from "@/components/empl/employee.service";

class Filter {
  public showArchived = false;
  public search = '';
}

@Component({
      components: {AdminBAForm}
    }
)
export default class AdminBusinessAccounts extends Vue {
  headers: DataTableHeader[] = [];
  loading = false;

  bas: BusinessAccount[] = [];

  private filter = new Filter();

  private baDialog = false;
  private selectedBA: BusinessAccount | null = null;
  private allEmployees: Employee[] = [];

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin BA component created');
    this.reloadHeaders();
    this.loading = true;
    employeeService.findAll().then(employees => {
          this.allEmployees = employees;
        }
    ).finally(() => this.loading = false)
        .then(() => this.fetchData());
  }

  private fetchData() {
    this.loading = true;
    this.bas = [];
    return adminBaService.findAll()
        .then(bas => {
          this.bas = bas;
        }).finally(() => {
          this.loading = false
        });
  }


  private filteredBA() {
    return this.bas.filter((p) => {
      let filtered = true;
      if (!this.filter.showArchived) {
        filtered = filtered && (!p.archived);
      }
      if (this.filter.search) {
        const search = this.filter.search.trim().toLowerCase();
        filtered = filtered &&
            (
                (p.name.toLowerCase().indexOf(search) >= 0) ||
                (p.description && p.description.toLowerCase().indexOf(search) >= 0)
            ) as boolean
      }
      return filtered;
    });
  }


  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Наименование'), value: 'name'});
    this.headers.push({text: this.$tc('Описание'), value: 'description'});
    this.headers.push({text: this.$tc('Менеджеры'), value: 'managers'});
  }

  public openBADialog(baToUpdate: BusinessAccount | null) {
    this.selectedBA = baToUpdate;
    this.baDialog = true;
  }

}
</script>

<style>
.archived {
  text-decoration: line-through;
}
</style>
