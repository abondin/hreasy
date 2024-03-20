<template>
  <v-card>
    <v-container>
      <!--<editor-fold desc="Errors from backend">-->
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
        <!--<editor-fold desc="Select period">-->
        <v-col align-self="center" cols="auto" class="mr-0 pr-0">
          <!-- Report Period -->
          <v-btn @click.stop="data.decrementPeriod()" text x-small>
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>
          <span class="ml-1 mr-2">
            {{ data.selectedPeriod }}
            <v-icon v-if="data.periodClosed()" color="primary"
                    :title="$t('Период закрыт для внесения изменений')">mdi-lock</v-icon>
          </span>
          <v-btn @click.stop="data.incrementPeriod()" text x-small>
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>
        </v-col>
        <v-col align-self="center" class="ml-0 pl-0  mr-5" cols="auto" v-if="data.canClosePeriod()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="mt-0 pt-0">
                <v-btn v-if="!data.periodClosed()" link :disabled="data.loading" @click="data.closePeriod()"
                       icon>
                  <v-icon>mdi-lock</v-icon>
                </v-btn>
                <v-btn v-if="data.periodClosed()" link :disabled="data.loading" @click="data.reopenPeriod()"
                       icon>
                  <v-icon>mdi-lock-open</v-icon>
                </v-btn>
              </div>
            </template>
            <span>{{
                $t(data.periodClosed() ? 'Переоткрыть период. Вернуть возможность вносить изменения'
                    : 'Закрыть период. Запретить внесение изменений.')
              }}</span>
          </v-tooltip>
        </v-col>
        <!--</editor-fold>-->

        <salary-request-filter-component :filter="data.filter"></salary-request-filter-component>

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
        <v-col align-self="center" cols="auto" v-if="data.canExport()">
          <v-tooltip bottom>
            <template v-slot:activator="{ on: ton, attrs: tattrs}">
              <div v-bind="tattrs" v-on="ton" class="ma-0 at-0">
                <v-btn link :disabled="data.exportLoading" @click="data.exportToExcel()" icon>
                  <v-icon>mdi-file-excel</v-icon>
                </v-btn>
              </div>
            </template>
            <p>{{ $t('Экспорт в Excel') }}</p>
          </v-tooltip>
          <v-snackbar
              v-model="data.exportCompleted"
              timeout="5000"
          >
            {{ $t('Экспорт успешно завершён. Файл скачен.') }}
            <template v-slot:action="{ attrs }">
              <v-btn color="blue" icon v-bind="attrs" @click="data.confirmExportCompleted()">
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
              sort-by="employee.name"
              dense
              :items-per-page="data.defaultItemsPerTablePage"
              class="text-truncate"
              :single-select=true
              :show-expand="$vuetify.breakpoint.mdAndUp"
              single-expand
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
              <span v-if="item.impl" :class="item.impl.state == REJECTED? 'error--text': 'success--text'">{{
                  item.impl?.state ? $t(`SALARY_REQUEST_STAT.${item.impl.state}`) : ''
                }}</span>
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
            <template v-slot:expanded-item="{ headers, item }">
              <td :colspan="headers.length">
                <salary-request-card :item="item" :data-container="data"></salary-request-card>
              </td>
            </template>
          </v-data-table>
        </v-col>
      </v-row>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Add new request dialog">-->
      <v-dialog v-bind:value="data.createDialog" :disabled="data.loading" persistent max-width="800">
        <hreasy-table-create-form v-bind:data="data" :title="createNewTitle">
          <template v-slot:fields>
            <salary-report-form-fields :create-body="data.createBody"></salary-report-form-fields>
          </template>
        </hreasy-table-create-form>
      </v-dialog>
      <!-- </editor-fold>-->

      <!--<editor-fold desc="Implement request dialog">-->
      <v-dialog v-bind:value="data.implementDialog" :disabled="data.loading" persistent max-width="800">
        <salary-request-implement-form :data="data"></salary-request-implement-form>
      </v-dialog>
      <!-- </editor-fold>-->

      <!--<editor-fold desc="Approve request">-->
      <v-dialog v-bind:value="data.approveDialog" :disabled="data.loading" persistent max-width="800">
        <salary-request-approval-form :data="data"></salary-request-approval-form>
      </v-dialog>
      <!-- </editor-fold>-->

      <!--<editor-fold desc="Delete request">-->
      <v-dialog v-bind:value="data.deleteDialog" :disabled="data.loading" persistent max-width="800">
        <hreasy-table-delete-confimration v-bind:data="data"></hreasy-table-delete-confimration>
      </v-dialog>
      <!-- </editor-fold>-->

    </v-container>
  </v-card>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue, Watch} from "vue-property-decorator";
import {SalaryRequestType} from "@/components/salary/salary.service";
import AdminSalaryAllRequestsFilter from "@/components/salary/SalaryRequestFilterComponent.vue";
import SalaryRequestFilterComponent from "@/components/salary/SalaryRequestFilterComponent.vue";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import logger from "@/logger";
import SalaryReportFormFields from "@/components/salary/SalaryReportFormFields.vue";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import SalaryRequestCard from "@/components/salary/SalaryRequestCard.vue";
import SalaryRequestImplementForm from "@/components/salary/SalaryRequestImplementForm.vue";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import SalaryRequestApprovalForm from "@/components/salary/SalaryRequestApprovalForm.vue";


@Component({
  components: {
    SalaryRequestApprovalForm,
    HreasyTableDeleteConfimration,
    SalaryRequestFilterComponent,
    SalaryRequestCard,
    SalaryRequestImplementForm,
    SalaryReportFormFields,
    AdminSalaryAllRequestsFilter, MyDateFormComponent,
    HreasyTableCreateForm
  }
})
export default class SalaryRequests extends Vue {
  private data = new SalaryRequestDataContainer(() => {
    const headers = [
      {text: this.$tc('Сотрудник'), value: 'employee.name'},
      {
        text: this.$tc('Текущая заработная плата'),
        value: 'employeeInfo.currentSalaryAmount',
        width: "150px",
        class: "text-wrap"
      },
    ];
    if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
      headers.push({
            text: this.$tc('Предполагаемая заработная плата после повышения'),
            value: 'req.plannedSalaryAmount', width: "150px", class: "text-wrap"
          }, {
            text: this.$tc('Предполагаемое изменение на'),
            value: 'req.increaseAmount',
            width: "150px",
            class: "text-wrap"
          },
      );
    } else {
      headers.push({
            text: this.$tc('Предполагаемая сумма бонуса'),
            value: 'req.increaseAmount',
            width: "150px",
            class: "text-wrap"
          }
      );
    }
    headers.push(
        {text: this.$tc('Месяц старта изменений'), value: 'req.increaseStartPeriod'},
        {text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name'}
    );
    if (this.data.filter.type == SalaryRequestType.SALARY_INCREASE) {
      headers.push(
          {text: this.$tc('Итоговое изменение на'), value: 'impl.increaseAmount'},
          {text: this.$tc('Итоговая сумма'), value: 'impl.salaryAmount'},
      );
    } else {
      headers.push(
          {text: this.$tc('Итоговая сумма бонуса'), value: 'impl.increaseAmount'}
      );
    }
    headers.push(
        {text: this.$tc('Решение'), value: 'impl.state'},
        {text: this.$tc('Завершено'), value: 'impl.implementedBy.name'}
    );
    return headers;
  });

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


  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

  createNewTitle = () => this.data.filter.type == SalaryRequestType.SALARY_INCREASE
      ? this.$t('Создание запроса на индексацию ЗП') : this.$t('Создание запроса на бонус');

  private REJECTED = SalaryRequestImplementationState.REJECTED;
}
</script>

