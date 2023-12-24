<template>
  <hreasy-table :data="data"
                :create-new-title="mode=='increases' ? $t('Создание запроса на индексацию ЗП') : $t('Создание запроса на Бонус')"
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
    <template v-slot:item.impl.state="{ item }">
      {{ item.impl?.state ? $t(`SALARY_REQUEST_STAT.${item.impl.state}`) : '' }}
    </template>
    <template v-slot:item.req.increaseAmount="{ item }">
      {{ formatMoney(item.req.increaseAmount) }}
    </template>
    <template v-slot:item.impl.increaseAmount="{ item }">
      {{ formatMoney(item.impl?.increaseAmount) }}
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
      <v-col>
        <v-text-field
            v-model="data.filter.search"
            append-icon="mdi-magnify"
            :label="$t('Поиск')"
            single-line
            hide-details
        ></v-text-field>
      </v-col>
    </template>


    <template v-slot:createFormFields>
      <salary-report-form-fields :create-body="data.createBody"></salary-report-form-fields>
    </template>
  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer, {CreateAction, Filter} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import salaryService, {
  ClosedSalaryRequestPeriod,
  SalaryIncreaseRequest,
  SalaryRequestReportBody,
  SalaryRequestType,
  salaryRequestTypes
} from "@/components/salary/salary.service";
import {Prop, Vue} from "vue-property-decorator";
import logger from "@/logger";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import salaryAdminService from "@/components/admin/salary/admin.salary.service";
import SalaryReportFormFields from "@/components/salary/SalaryReportFormFields.vue";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";


const namespace_dict = 'dict';


@Component({
  components: {
    SalaryReportFormFields, MyDateFormComponent, HreasyTable
  }
})
export default class SalaryRequests extends Vue {

  @Prop({default: 'increases'})
  private mode!: 'increases' | 'bonuses';

  selectedPeriod = ReportPeriod.currentPeriod();
  closedPeriods: ClosedSalaryRequestPeriod[] = [];

  dataLoader: () => Promise<SalaryIncreaseRequest[]> = () => salaryService.getClosedSalaryRequestPeriods()
      .then(data => {
        this.setClosedPeriods(data);
        return data;
      }).then(d => {
        return salaryService.load(this.selectedPeriod.periodId()).then(requests => requests.filter(r => r.type == this.getSalaryRequestTypeByMode()));
      });


  private data = new TableComponentDataContainer<SalaryIncreaseRequest, SalaryIncreaseRequest, SalaryRequestReportBody, SalaryRequestFilter>(
      this.dataLoader,
      this.createHeaders,
      null,
      {
        createItemRequest: (body) => salaryService.reportSalaryRequest(body),
        defaultBody: () => this.defaultReportNewRequestBody(),
      } as CreateAction<SalaryIncreaseRequest, SalaryRequestReportBody>,
      null,
      new SalaryRequestFilter(),
      () => true,
      true
  );


  private defaultReportNewRequestBody(): SalaryRequestReportBody {
    return {
      type: this.getSalaryRequestTypeByMode();
      increaseStartPeriod: this.selectedPeriod.periodId(),
    } as SalaryRequestReportBody;
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Salary increase requests table component created');
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

  private salaryTypes = salaryRequestTypes.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_TYPE.${v}`), value: v};
  });

  private getSalaryRequestTypeByMode() {
    return this.mode == "increases" ? SalaryRequestType.SALARY_INCREASE : SalaryRequestType.BONUS;
  }

  private createHeaders() {
    const headers = [];
    headers.push({text: this.$tc('Сотрудник'), value: 'employee.name'});
    headers.push({text: this.$tc('Текущий проект'), value: 'employeeCurrentProject.name'});
    headers.push({text: this.$tc('Результат'), value: 'impl.state'});
    headers.push({text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name'});
    headers.push({text: this.$tc('Запрошенная сумма в рублях'), value: 'req.increaseAmount'});
    headers.push({text: this.$tc('Реалиованная сумма в рублях'), value: 'impl.increaseAmount'});
    headers.push({text: this.$tc('Реализовано в периоде'), value: 'impl.increaseStartPeriod'});
    if (this.mode == "increases") {
      headers.push({text: this.$tc('Новая позиция'), value: 'impl.newPosition.name'});
    }
    headers.push({text: this.$tc('Создано'), value: 'createdBy.name'});
    headers.push({
      text: this.$tc('Создано (время)'),
      value: 'createdAt',
      sort: DateTimeUtils.dateComparatorNullLast
    });
    headers.push({text: this.$tc('Завершено'), value: 'implementedBy.name'});
    headers.push({
      text: this.$tc('Завершено (время)'),
      value: 'implementedAt',
      sort: DateTimeUtils.dateComparatorNullLast
    });
    return headers;
  }

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
}

export class SalaryRequestFilter extends Filter<SalaryIncreaseRequest> {
  public search = '';

  applyFilter(items: SalaryIncreaseRequest[]): SalaryIncreaseRequest[] {
    return items.filter((item) => {
      let filtered = true;
      const search = this.search.toLowerCase().trim();
      const textFilters = TextFilterBuilder.of()
          .splitWords(item.employee?.name)
          .splitWords(item.createdBy?.name)
          .ignoreCase(item?.budgetBusinessAccount.name)
          .ignoreCase(item?.req?.reason);

      filtered = filtered && searchUtils.textFilter(this.search, textFilters);
      return filtered;
    });
  }
}

</script>

