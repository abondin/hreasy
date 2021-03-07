<!-- Employees short info table-->
<template>
  <v-container>
    <div v-if="selectedEmployee">
      <v-dialog v-model="employeeDialog" transition="dialog-bottom-transition"
                @input="selectedEmployee=null">
        <v-card>
          <v-card-title>
            {{
              $t('overtimes_for_employee_for_period', {
                employee: selectedEmployee.name,
                period: selectedPeriod.toString()
              })
            }}
            <v-spacer></v-spacer>
            <v-btn text large icon @click="closeEmployeeDialog()" color="secondary">
              <v-icon>close</v-icon>
            </v-btn>

          </v-card-title>
          <v-card-text>
            <employee-overtime-component
                change-period-allowed="false"
                :employee-id="selectedEmployee.id"
                :selected-period="selectedPeriod"></employee-overtime-component>
          </v-card-text>
        </v-card>
      </v-dialog>
    </div>
    <v-card>
      <v-card-title>
        <!-- Refresh button -->
        <v-btn text icon @click="fetchData()">
          <v-icon>refresh</v-icon>
        </v-btn>
        <!-- Report Period -->
        <v-btn @click.stop="decrementPeriod()" text x-small>
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
        <span class="ml-1 mr-2">
          {{ selectedPeriod }}
          <v-icon v-if="periodClosed()" color="primary"
                  :title="$t('Период закрыт для внесения изменений')">mdi-lock</v-icon>
        </span>
        <v-btn @click.stop="incrementPeriod()" text x-small>
          <v-icon>mdi-chevron-right</v-icon>
        </v-btn>
        <v-divider vertical></v-divider>
        <v-text-field
            v-model="search"
            @input="applyFilters()"
            :label="$t('ФИО Сотрудника')" class="mr-5 ml-5"></v-text-field>
        <v-select
            @input="applyFilters()"
            class="mr-5"
            clearable
            v-model="filter.selectedEmployeeCurrentProjects"
            :items="filter.allProjects"
            item-value="id"
            item-text="name"
            :label="$t('Текущий прооект сотрудника')"
            multiple>
        </v-select>
        <v-divider vertical></v-divider>
        <v-select
            @input="applyFilters()"
            class="ml-3 mr-3"
            clearable
            v-model="filter.selectedProjectsWithOvertimes"
            :items="filter.projectsWithOvertimes"
            item-value="id"
            item-text="name"
            :label="$t('Проекты, в которые списаны овертаймы')"
            multiple>
        </v-select>
        <!-- Overtime projects -->
        <v-checkbox :label="$t('Сотрудники без овертаймов')" v-model="filter.showEmpty">
        </v-checkbox>
      </v-card-title>
      <v-card-subtitle>
        <div class="row">
          <div class="col-auto">{{ $t('Итого (с учётом фильтров)') }}: {{ $tc('hours', totalHours()) }}</div>
          <v-spacer></v-spacer>
          <!-- Export -->
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="col-auto">
                <v-btn v-if="canExportOvertimes()" link :disabled="loading" @click="exportToExcel()" icon>
                  <v-icon>mdi-file-excel</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Экспорт то Excel') }}</span>
          </v-tooltip>
          <v-snackbar
              v-model="exportCompleted"
              timeout="5000"
          >
            {{ $t('Экспорт успешно завершён. Файл скачен.') }}
            <template v-slot:action="{ attrs }">
              <v-btn color="blue" icon v-bind="attrs" @click="exportCompleted = false">
                <v-icon>mdi-close-circle-outline</v-icon>
              </v-btn>
            </template>
          </v-snackbar>

          <!-- Configuration -->
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="col-auto">
                <v-btn v-if="canAdminOvertimes() && !periodClosed()" link :disabled="loading" @click="closePeriod()"
                       icon>
                  <v-icon>mdi-lock</v-icon>
                </v-btn>
                <v-btn v-if="canAdminOvertimes() && periodClosed()" link :disabled="loading" @click="reopenPeriod()"
                       icon>
                  <v-icon>mdi-lock-open</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{
                $t(periodClosed() ? 'Переоткрыть период. Вернуть возможность вносить изменения'
                    : 'Закрыть период. Запретить внесение изменений.')
              }}</span>
          </v-tooltip>
        </div>
      </v-card-subtitle>
      <v-card-text>
        <v-data-table
            :loading="loading"
            :loading-text="$t('Загрузка_данных')"
            :headers="headers"
            :items="filteredOvertimes()"
            hide-default-footer
            sort-by="totalHours"
            sort-desc
            disable-pagination>
          <template
              v-slot:item.employee.name="{ item }">
            <v-btn text x-small
                   @click="showEmployeeDialog(item.employee)">{{ item.employee.name }}
            </v-btn>
          </template>
          <template v-slot:item.commonApprovalStatus="{ item }">
            <v-chip v-if="item.commonApprovalStatus==='DECLINED'" outlined>
              <v-icon class="declined">mdi-do-not-disturb</v-icon>
              {{ $t('APPROVAL_DECISION_ENUM.DECLINED') }}
            </v-chip>
            <v-chip v-else-if="item.commonApprovalStatus==='APPROVED_NO_DECLINED'" outlined>
              <v-icon class="approved">mdi-checkbox-marked-circle</v-icon>
              {{ $t('APPROVAL_DECISION_ENUM.APPROVED') }}
            </v-chip>
            <v-chip o v-else-if="item.commonApprovalStatus==='APPROVED_OUTDATED'" outlined>
              <v-icon class="outdated">mdi-clock-alert</v-icon>
              {{ $t('Изменения после согласования') }}
            </v-chip>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import employeeService, {Employee} from "@/components/empl/employee.service";
import {DataTableHeader} from "vuetify";
import EmployeeCard from "@/components/empl/EmployeeCard.vue";
import overtimeService, {
  ClosedOvertimePeriod,
  OvertimeEmployeeSummary,
  OvertimeSummaryContainer,
  OvertimeUtils,
  ReportPeriod
} from "@/components/overtimes/overtime.service";
import logger from "@/logger";
import EmployeeOvertimeComponent from "@/components/overtimes/EmployeeOvertimeComponent.vue";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import {Watch} from "vue-property-decorator";
import permissionService from "@/store/modules/permission.service";
import adminOvertimeService from "@/components/admin/overtime/admin.overtime.service";

const namespace_dict: string = 'dict';

class RawData {
  public employees: Employee[] = [];
  public overtimes: OvertimeEmployeeSummary[] = [];
}

class Filter {
  public showEmpty = true;
  public selectedProjectsWithOvertimes: number[] = [];
  public selectedEmployeeCurrentProjects: number[] = [];
  public projectsWithOvertimes: SimpleDict[] = [];
  public allProjects: SimpleDict[] = [];
}

@Component({
  components: {EmployeeOvertimeComponent, "employee-card": EmployeeCard}
})
export default class AllOvertimes extends Vue {
  headers: DataTableHeader[] = [];
  loading: boolean = false;
  search = '';

  selectedPeriod = ReportPeriod.currentPeriod();

  overtimes: OvertimeSummaryContainer[] = [];
  private rawData = new RawData();
  private closedPeriods: ClosedOvertimePeriod[] = [];

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  private filter = new Filter();
  private selectedEmployee: SimpleDict | null = null;
  private employeeDialog = false;

  private exportCompleted = false;

  /**
   * Lifecycle hook
   */
  created() {
    this.reloadHeaders();
    // Reload projects dict to Vuex
    return this.$store.dispatch('dict/reloadProjects').then(() => this.fetchData());
  }

  @Watch('employeeDialog')
  employeeDialogClosed() {
    if (this.employeeDialog === false) {
      this.fetchData();
    }
  }

  private fetchData() {
    this.loading = true;
    return this.$store.dispatch('dict/reloadProjects').then(() => {
      employeeService.findAll()
          .then(employees => {
            this.rawData.employees = employees;
            return overtimeService.getClosedOvertimes().then((data) => {
              this.closedPeriods = data;
              return overtimeService.getSummary(this.selectedPeriod.periodId()).then((overtimes) => {
                this.rawData.overtimes = overtimes;
                this.applyFilters();
              })
            });
          }).finally(() => {
        this.loading = false
      });
    });
  }

  /**
   * Recalculate aggregates based on given filters
   */
  private applyFilters() {
    this.overtimes.length = 0;
    const projectsWithOvertimes: number [] = [];
    this.rawData.employees
        // Filter by Employee Display Name
        .filter(e => !this.search || e.displayName.toLowerCase().indexOf(this.search.toLowerCase()) >= 0)
        // Filter by current project
        .filter(e => this.filter.selectedEmployeeCurrentProjects.length == 0
            || (e.currentProject && this.filter.selectedEmployeeCurrentProjects.indexOf(e.currentProject.id) >= 0))
        .forEach(e => this.overtimes.push(new OvertimeSummaryContainer({
          id: e.id,
          name: e.displayName,
          active: true
        }, {
          selectedProjects: this.filter.selectedProjectsWithOvertimes
        })));

    this.rawData.overtimes.forEach(serverOvertime => {
      const existing = this.overtimes.find(o => o.employee.id == serverOvertime.employeeId);
      serverOvertime.items.forEach(i => {
        if (projectsWithOvertimes.indexOf(i.projectId) == -1) {
          projectsWithOvertimes.push(i.projectId);
        }
      });
      if (existing) {
        existing.commonApprovalStatus = serverOvertime.commonApprovalStatus;
        existing.addDays(serverOvertime.items);
      } else {
        logger.error(`Unable to find overtime for employee ${serverOvertime.employeeId}`);
      }
    });
    this.filter.projectsWithOvertimes = this.allProjects.filter(p => projectsWithOvertimes.indexOf(p.id) >= 0);
    this.filter.allProjects = this.allProjects.filter(p => p.active);
  }

  private incrementPeriod() {
    this.selectedPeriod.increment();
    this.reloadHeaders();
    this.fetchData();
  }

  private decrementPeriod() {
    this.selectedPeriod.decrement();
    this.reloadHeaders();
    this.fetchData();
  }

  private reloadHeaders() {
    this.headers.length = 0;
    this.headers.push({text: this.$tc('Сотрудник'), value: 'employee.name'});
    this.headers.push({text: this.$tc('Всего'), value: 'totalHours'});
    this.headers.push({
      text: this.$tc('Статус согласования'),
      value: 'commonApprovalStatus', width: '10%'
    });
  }

  private showEmployeeDialog(employee: SimpleDict) {
    this.selectedEmployee = employee;
    this.employeeDialog = true;
  }

  private closeEmployeeDialog() {
    this.selectedEmployee = null;
    this.employeeDialog = false;
  }

  private filteredOvertimes(): OvertimeSummaryContainer[] {
    return this.overtimes.filter(i => {
      let passed = true;
      // Check showEmpty filter
      passed = passed && (this.filter.showEmpty || i.totalHours > 0)
      return passed;
    });
  }

  private exportToExcel() {
    this.loading = true;
    overtimeService.export(this.selectedPeriod.periodId()).then(() => {
      this.exportCompleted = true;
    }).finally(() => {
      this.loading = false;
    })
  }

  private closePeriod() {
    this.loading = true;
    adminOvertimeService.closeOvertimePeriod(this.selectedPeriod.periodId()).then(() => {
      return this.fetchData();
    }).finally(() => {
      this.loading = false;
    })
  }

  private reopenPeriod() {
    this.loading = true;
    adminOvertimeService.reopenOvertimePeriod(this.selectedPeriod.periodId()).then(() => {
      return this.fetchData();
    }).finally(() => {
      this.loading = false;
    })
  }

  private canExportOvertimes(): boolean {
    return permissionService.canExportOvertimes();
  }

  private canAdminOvertimes(): boolean {
    return permissionService.canAdminOvertimes();
  }

  private totalHours(): number {
    return OvertimeUtils.totalHoursForSummary(this.filteredOvertimes());
  }

  private periodClosed(): boolean {
    return this.selectedPeriod &&
        this.closedPeriods
        && this.closedPeriods.map(p => p.period).indexOf(this.selectedPeriod.periodId()) >= 0;
  }

}
</script>

<!-- TODO Move to separate style (uses in OvertimeApprovalChip as well) -->
<style scoped lang="scss">
.v-chip.v-chip--outlined .v-icon.approved {
  color: green;
}

.v-chip.v-chip--outlined .v-icon.declined {
  color: red
}

.v-chip.v-chip--outlined .v-icon.outdated {
  color: orange;
}
</style>
