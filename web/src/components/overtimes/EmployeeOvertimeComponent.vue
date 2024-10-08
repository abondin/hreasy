<template>
  <div>
    <v-card>
      <v-card-title>
        <!-- Шапка -->
        <div class="mr-2">{{ $t('Овертаймы') }}</div>
        <div v-if="changePeriodAllowed==true">
          <v-btn @click.stop="decrementPeriod()" text x-small>
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>
          <span class="ml-1 mr-2">{{ selectedPeriod }}</span>
          <v-icon v-if="periodClosed()" color="primary"
                  :title="$t('Период закрыт для внесения изменений')">mdi-lock
          </v-icon>
          <v-btn @click.stop="incrementPeriod()" text x-small>
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>
        </div>
        <v-spacer></v-spacer>
        <span class="mr-5">
                {{ $t('Всего') }}: {{ $tc('hours', totalOvertimeHours()) }}
                    </span>
        <v-card-actions>
          <add-overtime-item-dialog
              v-bind:employee-id="employeeId"
              v-bind:default-project="defaultProject"
              v-bind:period="selectedPeriod"
              v-bind:all-projects="allProjects"
              v-bind:period-closed="periodClosed()"
              @submit="onItemSubmit"
              @close="onItemDialogClose"></add-overtime-item-dialog>
          <approve-overtime-report-dialog
              v-if="canApproveOvertimeReport()"
              v-bind:employee-id="employeeId"
              v-bind:period="selectedPeriod"
              v-bind:previous-decision="myApproval"
              v-bind:period-closed="periodClosed()"
              @submit="onItemSubmit"
              @close="onItemDialogClose"
          ></approve-overtime-report-dialog>
        </v-card-actions>
      </v-card-title>

      <!-- Согласующие -->
      <v-card-subtitle v-if="approvals">
        <v-chip-group column>
          <overtime-approval-chip v-for="approval in approvals"
                                  v-bind:key="approval.id"
                                  :approval="approval"
                                  :reportLastUpdateTime="overtimeReport.lastUpdate"></overtime-approval-chip>
        </v-chip-group>
      </v-card-subtitle>
      <v-data-table
          :loading="loading"
          :loading-text="$t('Загрузка_данных')"
          :headers="headers"
          :items="overtimes"
          sort-by="createdAt"
          sort-desc
          hide-default-footer
          disable-pagination>
        <template v-slot:item.date="{ item }">
          <v-btn :disabled="periodClosed()" @click.stop="openDeleteDialog(item)" icon color="secondary">
            <v-icon>mdi-delete</v-icon>
          </v-btn>
          <span>{{ formatDate(item.date) }}</span>
        </template>
        <template v-slot:item.createdAt="{ item }">
          {{ formatDateTime(item.createdAt) }}
        </template>
        <template v-slot:item.projectId="{ item }">
          {{ projectName(item.projectId) }}
        </template>
      </v-data-table>
    </v-card>

    <v-dialog v-if="itemToDelete"
              v-model="deleteDialog"
              width="500">
      <v-card>
        <v-card-title primary-title>
          {{ $t('Удаление') }}
        </v-card-title>

        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить запись?') }}
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              text
              @click="deleteDialog = false">
            {{ $t('Нет') }}
          </v-btn>
          <v-btn
              color="primary"
              @click="deleteItem()">
            {{ $t('Да') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import overtimeService, {
  ApprovalDecision,
  ClosedOvertimePeriod,
  OvertimeItem,
  OvertimeReport,
  OvertimeUtils,
  ReportPeriod
} from "@/components/overtimes/overtime.service";
import {DataTableHeader} from "vuetify";
import {Prop} from "vue-property-decorator";
import AddOvertimeItemDialog from "@/components/overtimes/AddOvertimeItemDialog.vue";
import employeeService from "@/components/empl/employee.service";
import OvertimeApprovalChip from "@/components/overtimes/OvertimeApprovalChip.vue";
import ApproveOvertimeReportDialog from "@/components/overtimes/ApproveOvertimeReportDialog.vue";
import permissionService from "@/store/modules/permission.service";

const namespace_dict = 'dict';
const namespace_auth = 'auth';
@Component({
  components: {
    ApproveOvertimeReportDialog,
    OvertimeApprovalChip,
    AddOvertimeItemDialog,
    OvertimeAddOrEdit: AddOvertimeItemDialog
  }
})
export default class EmployeeOvertimeComponent extends Vue {
  loading = false;
  overtimeReport!: OvertimeReport;
  headers: DataTableHeader[] = [];

  @Getter("projects", {namespace: namespace_dict})
  private allProjects!: Array<SimpleDict>;

  @Getter("employeeId", {namespace: namespace_auth})
  private currentUserEmployeeId!: number;


  @Prop({required: true})
  employeeId!: number;

  @Prop({required: false, default: true})
  changePeriodAllowed!: boolean;

  @Prop({required: true})
  selectedPeriod!: ReportPeriod;

  @Prop({required: true})
  readonly closedPeriods!: ClosedOvertimePeriod[];

  defaultProject: null | number = null;


  private overtimes: OvertimeItem[] = [];
  private approvals: ApprovalDecision[] = [];

  /**
   * Latest Approval of current user
   */
  private myApproval: ApprovalDecision | null = null;

  private deleteDialog = false;
  private itemToDelete: OvertimeItem | null = null;


  /**
   * Lifecycle hook
   */
  created() {
    this.headers.push({text: this.$tc('Дата'), value: 'date'});
    this.headers.push({text: this.$tc('Проект'), value: 'projectId'});
    this.headers.push({text: this.$tc('Часы'), value: 'hours'});
    this.headers.push({text: this.$tc('Комментарий'), value: 'notes'});
    this.headers.push({text: this.$tc('Запись добавлена'), value: 'createdAt'});
    this.fetchData()
  }

  public periodClosed(): boolean {
    return this.selectedPeriod &&
        this.closedPeriods
        && this.closedPeriods.map(p => p.period).indexOf(this.selectedPeriod.periodId()) >= 0;
  }

  private fetchData() {
    this.loading = true;
    return this.$store.dispatch('dict/reloadProjects')
        .then(() => employeeService.find(this.employeeId))
        .then((employee) => {
          this.defaultProject = employee.currentProject ? employee.currentProject.id : null;
          return this.fetchReport(false);
        }).finally(() => {
          this.loading = false
        });
  }

  private fetchReport(showLoading = true) {
    if (showLoading) {
      this.loading = true;
    }
    const periodId = this.selectedPeriod.periodId();
    return overtimeService.get(this.employeeId, periodId)
        .then(report => {
              return this.refreshReport(report);
            }
        ).finally(() => {
          if (showLoading) {
            this.loading = false
          }
        });
  }

  private refreshReport(report: OvertimeReport | undefined): OvertimeReport {
    if (report) {
      this.overtimeReport = report;
    } else {
      const periodId = this.selectedPeriod.periodId();
      this.overtimeReport = {
        employeeId: this.employeeId,
        period: periodId,
        items: [],
        approvals: [],
        lastUpdate: null
      }
    }
    this.overtimes = this.overtimeReport.items;
    this.approvals = this.overtimeReport.approvals;
    this.myApproval = this.overtimeReport.approvals && this.overtimeReport.approvals
        .find(ap => ap.approver == this.currentUserEmployeeId) || null;
    return this.overtimeReport;
  }

  private formatDate(date: Date): string | undefined {
    return OvertimeUtils.formatDate(date);
  }

  private formatDateTime(date: Date): string | undefined {
    return OvertimeUtils.formatDateTime(date);
  }

  private projectName(projectId: number): string {
    const projects = this.allProjects.filter(p => p.id == projectId);
    if (projects && projects.length > 0) {
      return projects[0].name;
    }
    return this.$tc(`Неизвестный проект ${projectId}`);
  }

  private onItemSubmit() {
    this.fetchReport();
  }

  private deleteItem() {
    if (this.itemToDelete) {
      overtimeService.deleteItem(this.overtimeReport.employeeId, this.overtimeReport.period, this.itemToDelete.id!).then((report) => {
        this.deleteDialog = false;
        return this.refreshReport(report);
      });
    } else {
      alert('Some error occurs. Item to delete not selected... Please contact administrator')
    }
  }

  private onItemDialogClose() {
    // Do nothing?
  }

  private incrementPeriod() {
    this.selectedPeriod.increment();
    this.fetchReport(true);
  }

  private decrementPeriod() {
    this.selectedPeriod.decrement();
    this.fetchReport(true);
  }

  private openDeleteDialog(item: OvertimeItem) {
    this.itemToDelete = item;
    this.$nextTick(() => {
      this.deleteDialog = true;
    });
  }

  private totalOvertimeHours() {
    return OvertimeUtils.totalHours(this.overtimeReport);
  }

  private canApproveOvertimeReport(): boolean {
    if (this.overtimeReport && this.overtimeReport.employeeId &&
        this.overtimeReport.items && this.overtimeReport.items.length > 0) {
      return permissionService.canApproveOvertimeReport(this.overtimeReport.employeeId);
    } else {
      return false;
    }
  }

}
</script>

<style scoped>

</style>
