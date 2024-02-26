<template>
  <v-card>
    <v-container>
      <!--<editor-fold desc="Errors from backend>-->
      <v-row v-if="data.error">
        <v-col>
          <v-alert type="error">{{ data.error }}</v-alert>
        </v-col>
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Types tabs and actions">-->
      <v-row class="ma-0 pa-0" v-if="data" dense>
        <v-col align-self="center" cols="auto">
          <v-btn-toggle v-model="data.filter.type" color="primary" group>
            <v-btn :value=1 class="mr-0">
              {{ $t('Повышения') }}
            </v-btn>
            <v-btn :value=2 class="ml-0">
              {{ $t('Бонусы') }}
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col align-self="center" cols="auto">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
                <v-btn text icon @click="data.reloadData()">
                  <v-icon>refresh</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Обновить данные') }}</span>
          </v-tooltip>
        </v-col>
        <v-spacer></v-spacer>
        <!--<editor-fold desc="Add new item">-->
        <v-col align-self="center" cols="auto">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="ma-0 at-0">
                <v-btn text color="primary" :disabled="data.loading || !data.editable()"
                       @click="()=>data.openCreateDialog()" icon>
                  <v-icon>mdi-plus</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{ $t('Добавить новую запись') }}</span>
          </v-tooltip>
        </v-col>
        <!--</editor-fold>-->
        <!--<editor-fold desc="Export to excel">-->
        <v-col align-self="center" cols="auto" v-if="canExport()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="ma-0 at-0">
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
        <!--</editor-fold>-->
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="The main table">-->
      <v-row v-if="data.initialized">
        <v-col>
          <v-data-table
              :loading="data.loading"
              :loading-text="$t('Загрузка_данных')"
              :headers="data.headers"
              v-model="data.selectedItems"
              :items="data.filteredItems()"
              sort-by="createdAt"
              dense
              :items-per-page="data.defaultItemsPerTablePage"
              class="text-truncate"
              :single-select=true
          >
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
          </v-data-table>
        </v-col>
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Add new request dialog">-->
      <v-dialog v-bind:value="data.createDialog" :disabled="data.loading" persistent>
        <hreasy-table-create-form v-bind:data="data" :title="createNewTitle">
          <template v-slot:fields>
            <salary-report-form-fields :create-body="data.createBody"></salary-report-form-fields>
          </template>
        </hreasy-table-create-form>
      </v-dialog>
      <!-- </editor-fold>-->
    </v-container>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue, Watch} from "vue-property-decorator";
import salaryService, {
  ClosedSalaryRequestPeriod,
  SalaryIncreaseRequest,
  SalaryRequestReportBody,
  SalaryRequestType
} from "@/components/salary/salary.service";
import salaryAdminService from "@/components/admin/salary/admin.salary.service";
import adminSalaryService from "@/components/admin/salary/admin.salary.service";
import TableComponentDataContainer, {CreateAction} from "@/components/shared/table/TableComponentDataContainer";
import AdminSalaryRequestImplementFormFields, {
  SalaryRequestFormData,
  SalaryRequestImplementAction
} from "@/components/admin/salary/AdminSalaryRequestImplementFormFields.vue";
import AdminSalaryAllRequestsFilter, {AdminSalaryRequestFilter} from "@/components/admin/salary/AdminSalaryAllRequestsFilter.vue";
import permissionService from "@/store/modules/permission.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import logger from "@/logger";
import SalaryReportFormFields from "@/components/salary/SalaryReportFormFields.vue";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";


@Component({
  components: {
    AdminSalaryRequestImplementFormFields,
    SalaryReportFormFields,
    AdminSalaryAllRequestsFilter, MyDateFormComponent,
    HreasyTableCreateForm
  }
})
export default class SalaryRequestsMain extends Vue {
  selectedPeriod = ReportPeriod.currentPeriod();
  closedPeriods: ClosedSalaryRequestPeriod[] = [];
  private exportLoading = false;
  private exportCompleted = false;

  dataLoader: () => Promise<SalaryIncreaseRequest[]> = () => salaryService.getClosedSalaryRequestPeriods()
      .then(data => {
        this.setClosedPeriods(data);
        return data;
      }).then(d => {
        return this.canAdmin() ? salaryAdminService.loadAllSalaryRequests(this.selectedPeriod.periodId()) :
            salaryService.load(this.selectedPeriod.periodId());
      });


  private value = 'budgetBusinessAccount.name';
  private data = new TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestFormData, SalaryRequestReportBody, AdminSalaryRequestFilter>(
      this.dataLoader,
      () => {
        console.log("Reload headers: " + this.data.filter.type);
        const headers = [
          {text: this.$tc('Сотрудник'), value: 'employee.name'},
          {text: this.$tc('Дата трудоустройства'), value: 'employeeInfo.dateOfEmployment'},
          {text: this.$tc('Текущая заработная плата'), value: 'employeeInfo.currentSalaryAmount'},
        ];
        if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
          headers.push({
                text: this.$tc('Предполагаемая заработная плата после повышения'),
                value: 'req.plannedSalaryAmount'
              }, {text: this.$tc('Предполагаемое изменение на'), value: 'req.increaseAmount'},
              {text: this.$tc('Причины пересмотра'), value: 'req.reason'}
          );
        } else {
          headers.push({text: this.$tc('Предполагаемая сумма бонуса'), value: 'req.increaseAmount'}
              , {text: this.$tc('Обоснование'), value: 'req.reason'}
          );
        }
        headers.push(
            {text: this.$tc('Перспективы биллинга '), value: 'budgetExpectedFundingUntil'},
            {text: this.$tc('Месяц старта изменений'), value: 'req.increaseStartPeriod'},
            {text: this.$tc('Должность'), value: 'employeeInfo.position.name'},
            {text: this.$tc('Бизнес аккаунт'), value: 'employeeInfo.ba.name'},
            {text: this.$tc('Менеджер, инициировавший пересмотр'), value: 'createdBy.name'},
            {text: this.$tc('Бюджет из бизнес аккаунта'), value: this.value}
        );
        if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
          headers.push(
              //TODO After salary history storing feature implemented add this fields
              //{text: this.$tc('Крайний реализованный пересмотр заработной платы'), value: 'employeeInfo.previousSalaryIncreaseText'},
              {text: this.$tc('Планируемая дата окончания финансирования'), value: 'budgetExpectedFundingUntil'},
              {text: this.$tc('Итоговое изменение на'), value: 'impl.increaseAmount'},
              {text: this.$tc('Итоговая сумма'), value: 'impl.salaryAmount'},
          );
        } else {
          headers.push(
              {text: this.$tc('Планируемая дата окончания финансирования'), value: 'budgetExpectedFundingUntil'},
              {text: this.$tc('Итоговая сумма бонуса'), value: 'impl.increaseAmount'}
          );
        }
        headers.push(
            {text: this.$tc('Завершено'), value: 'impl.implementedBy.name'},
            {text: this.$tc('Решение'), value: 'impl.state'}
        );
        return headers;
      },
      new SalaryRequestImplementAction(),
      {
        createItemRequest: (body) => salaryService.reportSalaryRequest(body),
        defaultBody: () => this.defaultReportNewRequestBody(),
      } as CreateAction<SalaryIncreaseRequest, SalaryRequestReportBody>,
      null,
      new AdminSalaryRequestFilter(),
      this.canReport,
      true
  );

  private exportToExcel() {
    this.exportLoading = true;
    this.exportCompleted = false;
    adminSalaryService.export(this.selectedPeriod.periodId()).then(() => {
      this.exportCompleted = true;
    }).finally(() => {
      this.exportLoading = false;
    })
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Salary component created');
    this.data.init();
  }

  @Watch("data.filter.type")
  private watchFilterType(newValue: SalaryRequestType) {
    this.data.reloadHeaders();
  }

  private setClosedPeriods(data: ClosedSalaryRequestPeriod[]) {
    this.closedPeriods = data;
  }

  private defaultReportNewRequestBody(): SalaryRequestReportBody {
    return {
      type: this.data.filter.type,
      increaseStartPeriod: this.selectedPeriod.periodId(),
      budgetBusinessAccount: this.data.filter.ba && this.data.filter.ba.length > 0 ? this.data.filter.ba[0] : null
    } as SalaryRequestReportBody;
  }

  private canAdmin(): boolean {
    return permissionService.canAdminSalaryRequests();
  }

  private canReport(): boolean {
    return permissionService.canReportSalaryRequest();
  }

  private canExport(): boolean {
    return permissionService.canAdminSalaryRequests();
  }


  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

  createNewTitle = () => this.data.filter.type == SalaryRequestType.SALARY_INCREASE
      ? this.$t('Создание запроса на индексацию ЗП') : this.$t('Создание запроса на бонус');
}
</script>

