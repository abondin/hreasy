<template>
  <hreasy-table :data="data"
                :create-new-title="createNewTitle()"
                :update-title="$t('Реализация запроса для сотрудника')"
                :sort-by="['req.increaseStartPeriod']">
    <!--<editor-fold desc="Table columns">-->
    <template v-slot:item.impl.increaseStartPeriod="{ item }">
      {{ fromPeriodId(item.impl?.increaseStartPeriod) }}
    </template>
    <template v-slot:item.employeeInfo.dateOfEmployment="{ item }">
      {{ formatDate(item.employeeInfo.dateOfEmployment) }}
    </template>
    <template v-slot:item.req.increaseStartPeriod="{ item }">
      {{ fromPeriodId(item.req.increaseStartPeriod) }}
    </template>
    <template v-slot:item.createdAt="{ item }">
      {{ formatDateTime(item.createdAt) }}
    </template>
    <template v-slot:item.implementedAt="{ item }">
      {{ formatDateTime(item.implementedAt) }}
    </template>
    <template v-slot:item.impl.state="{ item }">
      {{ item.impl?.state ? $t(`SALARY_REQUEST_STAT.${item.impl.state}`) : '' }}
    </template>
    <template v-slot:item.req.increaseAmount="{ item }">
      {{ formatMoney(item.req.increaseAmount) }}
    </template>
    <template v-slot:item.req.plannedSalaryAmount="{ item }">
      {{ formatMoney(item.req.plannedSalaryAmount) }}
    </template>
    <template v-slot:item.employeeInfo.currentSalaryAmount="{ item }">
      {{ formatMoney(item.employeeInfo.currentSalaryAmount) }}
    </template>
    <template v-slot:item.impl.increaseAmount="{ item }">
      {{ formatMoney(item.impl?.increaseAmount) }}
    </template>
    <template v-slot:item.impl.salaryAmount="{ item }">
      {{ formatMoney(item.impl?.salaryAmount) }}
    </template>
    <template v-slot:item.req.budgetExpectedFundingUntil="{ item }">
      {{ formatDate(item.req.budgetExpectedFundingUntil) }}
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
      <admin-salary-request-implement-form-fields :body="data.updateBody"
                                                  :item="data.selectedItems?.length > 0 ? data.selectedItems[0]:null"></admin-salary-request-implement-form-fields>
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
      <v-col align-self="center" cols="auto">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn link :disabled="exportLoading" @click="exportToExcel()" icon>
                <v-icon>mdi-file-excel</v-icon>
              </v-btn>
            </div>
          </template>
          <p>{{ $t('Экспорт в Excel') }}</p>
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
  SalaryIncreaseRequest,
  SalaryRequestReportBody,
  SalaryRequestType
} from "@/components/salary/salary.service";
import permissionService from "@/store/modules/permission.service";
import {Vue} from "vue-property-decorator";
import logger from "@/logger";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import salaryAdminService from "@/components/admin/salary/admin.salary.service";
import adminSalaryService from "@/components/admin/salary/admin.salary.service";
import SalaryReportFormFields from "@/components/salary/SalaryReportFormFields.vue";
import AdminSalaryAllRequestsFilter, {
  AdminSalaryRequestFilter
} from "@/components/admin/salary/AdminSalaryAllRequestsFilter.vue";
import AdminSalaryRequestImplementFormFields, {
  SalaryRequestFormData,
  SalaryRequestImplementAction
} from "@/components/admin/salary/AdminSalaryRequestImplementFormFields.vue";


const namespace_dict = 'dict';


@Component({
  components: {
    AdminSalaryRequestImplementFormFields,
    AdminSalaryReportFormFields: SalaryReportFormFields,
    AdminSalaryRequestImplementForm: AdminSalaryRequestImplementFormFields,
    AdminSalaryAllRequestsFilter, AdminSalaryReportForm: SalaryReportFormFields, MyDateFormComponent, HreasyTable
  }
})
export default class AdminSalaryAllRequests extends Vue {
  selectedPeriod = ReportPeriod.currentPeriod();
  closedPeriods: ClosedSalaryRequestPeriod[] = [];

  private exportLoading = false;
  private exportCompleted = false;

  dataLoader: () => Promise<SalaryIncreaseRequest[]> = () => salaryService.getClosedSalaryRequestPeriods()
      .then(data => {
        this.setClosedPeriods(data);
        return data;
      }).then(d => {
        return salaryAdminService.loadAllSalaryRequests(this.selectedPeriod.periodId());
      });


  private data = new TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestFormData, SalaryRequestReportBody, AdminSalaryRequestFilter>(
      this.dataLoader,
      () =>
          [
            {text: this.$tc('Сотрудник'), value: 'employee.name'},
            {text: this.$tc('Дата трудоустройства'), value: 'employeeInfo.dateOfEmployment'},
            {text: this.$tc('Текущая заработная плата'), value: 'employeeInfo.currentSalaryAmount'},
            {text: this.$tc('Предполагаемая заработная плата после повышения'), value: 'req.plannedSalaryAmount'},
            {text: this.$tc('Предполагаемое изменение на'), value: 'req.increaseAmount'},
            {text: this.$tc('Месяц старта изменений'), value: 'req.increaseStartPeriod'},
            {text: this.$tc('Должность'), value: 'employeeInfo.position.name'},
            {text: this.$tc('Бизнес аккаунт'), value: 'employeeInfo.ba.name'},
            {text: this.$tc('Менеджер, инициировавший пересмотр'), value: 'createdBy.name'},
            {text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name'},
            {text: this.$tc('Причины пересмотра'), value: 'req.reason'},
            {text: this.$tc('Перспективы биллинга '), value: 'budgetExpectedFundingUntil'},
            {text: this.$tc('Крайний реализованный пересмотр заработной платы'), value: 'employeeInfo.previousSalaryIncreaseText'},
            {text: this.$tc('Планируемая дата окончания финансирования'), value: 'budgetExpectedFundingUntil'},
            {text: this.$tc('Итоговая сумма'), value: 'impl.salaryAmount'},
            {text: this.$tc('Завершено'), value: 'impl.implementedBy.name'},
            {text: this.$tc('Решение'), value: 'impl.state'},
          ],
      new SalaryRequestImplementAction(),
      {
        createItemRequest: (body) => salaryService.reportSalaryRequest(body),
        defaultBody: () => this.defaultReportNewRequestBody(),
      } as CreateAction<SalaryIncreaseRequest, SalaryRequestReportBody>,
      {
        deleteItemRequest: (ids) => salaryService.deleteSalaryRequest(ids)
      },
      new AdminSalaryRequestFilter(),
      () => permissionService.canAdminSalaryRequests(),
      false
  );


  private defaultReportNewRequestBody(): SalaryRequestReportBody {
    return {
      type: this.data.filter.type,
      increaseStartPeriod: this.selectedPeriod.periodId(),
      budgetBusinessAccount: this.data.filter.ba && this.data.filter.ba.length > 0 ? this.data.filter.ba[0] : null
    } as SalaryRequestReportBody;
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin salary component created');
  }

  private exportToExcel() {
    this.exportLoading = true;
    this.exportCompleted = false;
    adminSalaryService.export(this.selectedPeriod.periodId()).then(() => {
      this.exportCompleted = true;
    }).finally(() => {
      this.exportLoading = false;
    })
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

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

  createNewTitle = () => this.data.filter.type == SalaryRequestType.SALARY_INCREASE
      ? this.$t('Создание запроса на индексацию ЗП') : this.$t('Создание запроса на бонус');
}
</script>

