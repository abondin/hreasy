<template>
  <hreasy-table :data="data"
                :create-new-title="$t('Создание запроса на индексацию ЗП или бонус')"
                :update-title="getUpdateFormTitle()"
                :sort-by="['req.increaseStartPeriod']">
    <!--<editor-fold desc="Table columns">-->
    <template v-slot:item.impl.increaseStartPeriod="{ item }">
      {{ fromPeriodId(item.impl?.increaseStartPeriod) }}
    </template>
    <template v-slot:item.createdAt="{ item }">
      {{ formatDateTime(item.createdAt) }}
    </template>
    <template v-slot:item.implementedAt="{ item }">
      {{ formatDateTime(item.implementedAt) }}
    </template>
    <template v-slot:item.type="{ item }">
      {{ $t(`SALARY_REQUEST_TYPE.${item.type}`) }}
    </template>
    <template v-slot:item.impl.state="{ item }">
      {{ item.impl?.state ? $t(`SALARY_REQUEST_STAT.${item.impl.state}`) : '' }}
    </template>
    <template v-slot:item.salaryIncrease="{ item }">
      {{ formatMoney(item.salaryIncrease) }}
    </template>
    <template v-slot:item.budgetExpectedFundingUntil="{ item }">
      {{ formatDate(item.budgetExpectedFundingUntil) }}
    </template>
    <!--</editor-fold>-->

    <template v-slot:filters>
      <v-col align-self="center" cols="auto">
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
      </v-col>
      <admin-salary-all-requests-filter :filter="data.filter"></admin-salary-all-requests-filter>
    </template>


    <template v-slot:createFormFields>
      <admin-salary-report-form-fields :create-body="data.createBody"></admin-salary-report-form-fields>
    </template>
    <template v-slot:updateFormFields>
      <admin-salary-request-implement-form-fields :body="data.updateBody"></admin-salary-request-implement-form-fields>
    </template>

    <template v-slot:additionalActions>
      <v-col align-self="center" cols="auto" v-if="selectedPeriod">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
              <v-btn v-if="!periodClosed()" link :disabled="data.loading" @click="closePeriod()"
                     icon>
                <v-icon>mdi-lock</v-icon>
              </v-btn>
              <v-btn v-if="periodClosed()" link :disabled="data.loading" @click="reopenPeriod()"
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
      </v-col>
    </template>

  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer, {CreateAction} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import salaryService, {
  ClosedSalaryRequestPeriod,
  SalaryRequestFullInfo,
  SalaryRequestReportBody,
  SalaryRequestType
} from "@/components/salary/salary.service";
import permissionService from "@/store/modules/permission.service";
import {Vue} from "vue-property-decorator";
import logger from "@/logger";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {NumberUtils} from "@/components/numberutils";
import salaryAdminService from "@/components/admin/salary/admin.salary.service";
import AdminSalaryReportFormFields from "@/components/admin/salary/AdminSalaryReportFormFields.vue";
import AdminSalaryAllRequestsFilter, {SalaryRequestFilter} from "@/components/admin/salary/AdminSalaryAllRequestsFilter.vue";
import AdminSalaryRequestImplementFormFields, {
  SalaryRequestFormData,
  SalaryRequestImplementAction
} from "@/components/admin/salary/AdminSalaryRequestImplementFormFields.vue";


const namespace_dict = 'dict';


@Component({
  components: {
    AdminSalaryRequestImplementFormFields,
    AdminSalaryReportFormFields,
    AdminSalaryRequestImplementForm: AdminSalaryRequestImplementFormFields,
    AdminSalaryAllRequestsFilter, AdminSalaryReportForm: AdminSalaryReportFormFields, MyDateFormComponent, HreasyTable
  }
})
export default class AdminSalaryAllRequests extends Vue {
  selectedPeriod = ReportPeriod.currentPeriod();
  closedPeriods: ClosedSalaryRequestPeriod[] = [];

  dataLoader: () => Promise<SalaryRequestFullInfo[]> = () => salaryService.getClosedSalaryRequestPeriods()
      .then(data => {
        this.setClosedPeriods(data);
        return data;
      }).then(d => {
        return salaryAdminService.loadAllSalaryRequests(this.selectedPeriod.periodId());
      });


  private data = new TableComponentDataContainer<SalaryRequestFullInfo, SalaryRequestFormData, SalaryRequestReportBody, SalaryRequestFilter>(
      this.dataLoader,
      () =>
          [
            {text: this.$tc('Сотрудник'), value: 'employee.name'},
            {text: this.$tc('Тип'), value: 'type'},
            {text: this.$tc('Результат'), value: 'impl.state'},
            {text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name'},
            {text: this.$tc('Запрошенная сумма в рублях'), value: 'req.salaryIncrease'},
            {text: this.$tc('Реалиованная сумма в рублях'), value: 'impl.salaryIncrease'},
            {text: this.$tc('Реализовано в периоде'), value: 'impl.increaseStartPeriod'},
            {text: this.$tc('Новая позиция'), value: 'impl.newPosition.name'},
            {text: this.$tc('Отдел'), value: 'employeeDepartment.name'},
            {text: this.$tc('Планируемая дата окончания финансирования'), value: 'budgetExpectedFundingUntil'},
            {text: this.$tc('Созданно'), value: 'createdBy.name'},
            {text: this.$tc('Созданно (время)'), value: 'createdAt', sort: DateTimeUtils.dateComparatorNullLast},
            {text: this.$tc('Завершено'), value: 'implementedBy.name'},
            {
              text: this.$tc('Завершено (время)'),
              value: 'implementedAt',
              sort: DateTimeUtils.dateComparatorNullLast
            }
          ],
      new SalaryRequestImplementAction(),
      {
        createItemRequest: (body) => salaryService.reportSalaryRequest(body),
        defaultBody: () => this.defaultReportNewRequestBody(),
      } as CreateAction<SalaryRequestFullInfo, SalaryRequestReportBody>,
      {
        deleteItemRequest: (ids) => salaryService.deleteSalaryRequest(ids)
      },
      new SalaryRequestFilter(),
      () => permissionService.canAdminSalaryRequests(),
      false
  );


  private defaultReportNewRequestBody(): SalaryRequestReportBody {
    return {
      type: SalaryRequestType.SALARY_INCREASE,
      increaseStartPeriod: this.selectedPeriod.periodId(),
    } as SalaryRequestReportBody;
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin salary component created');
  }

  private incrementPeriod() {
    this.selectedPeriod.increment();
    this.data.reloadData();
  }

  private decrementPeriod() {
    this.selectedPeriod.decrement();
    this.data.reloadData();
  }

  private periodClosed(): boolean {
    return this.selectedPeriod &&
        this.closedPeriods
        && this.closedPeriods.map(p => p.period).indexOf(this.selectedPeriod.periodId()) >= 0;
  }

  private closePeriod() {
    if (this.selectedPeriod) {
      this.data.doInLoadingSection(() => {
        return salaryAdminService.closeReportPeriod(this.selectedPeriod.periodId()).then(() => {
          this.data.reloadData();
        })
      });
    }
  }

  private reopenPeriod() {
    if (this.selectedPeriod) {
      this.data.doInLoadingSection(() => {
        return salaryAdminService.reopenReportPeriod(this.selectedPeriod.periodId()).then(() => {
          this.data.reloadData();
        })
      });
    }
  }

  private setClosedPeriods(data: ClosedSalaryRequestPeriod[]) {
    this.closedPeriods = data;
  }

  private getUpdateFormTitle(){
    let title = this.$tc('Реализация запроса для сотрудника');
    if (this.data.selectedItems.length > 0){
      title += this.data.selectedItems[0].employee.name;
      if (this.data.selectedItems[0].impl){
        title += ` (уже реализован)`;
      }
    }
    return title;
  }

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => NumberUtils.formatMoney(v);

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

}
</script>

