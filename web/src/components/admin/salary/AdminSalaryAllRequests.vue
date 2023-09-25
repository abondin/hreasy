<template>
  <hreasy-table :data="data" :create-new-title="$t('Создание запроса на индексацию ЗП или бонус')">
    <template v-slot:item.createdAt="{ item }">
      {{ formatDateTime(item.createdAt) }}
    </template>
    <template v-slot:filters>
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

      <v-col>
        <v-text-field v-if="data.filter"
                      v-model="data.filter.search"
                      append-icon="mdi-magnify"
                      :label="$t('Поиск')"
                      single-line
                      hide-details
        ></v-text-field>
      </v-col>
    </template>


    <template v-slot:createFormFields>
      <v-autocomplete
          v-model="data.createBody.employeeId"
          :items="allEmployees"
          item-value="id" item-text="displayName"
          :label="$t('Сотрудник')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>
      <v-select
          v-model="data.createBody.type"
          :label="$t('Тип')"
          :rules="[v => !!v || $t('Обязательное поле')]"
          :items="[{value:1, text:'Повышение'}, {value:2, text:'Единоразовый бонус'}]">
      </v-select>
      <v-autocomplete
          v-model="data.createBody.budgetBusinessAccount"
          item-value="id" item-text="name"
          :items="allBas"
          :label="$t('Бюджет из бизнес аккаунта')"
          :rules="[v => !!v || $t('Обязательное поле')]"
      ></v-autocomplete>
      <my-date-form-component v-model="data.createBody.budgetExpectedFundingUntil"
                              :label="$t('Планируемая дата окончания финансирования')"
                              :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГГГ'))]"
      ></my-date-form-component>

      <v-text-field type="number"
                    v-model="data.createBody.salaryIncrease"
                    :rules="[v => !!v || $t('Обязательное числовое поле')]"
                    :label="$t('Сумма в рублях')"
      >
      </v-text-field>

      <v-textarea
          v-model="data.createBody.reason"
          counter="1024"
          :rules="[v=>(!v || v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
          :label="$t('Обоснование')">
      </v-textarea>
      <v-textarea
          v-model="data.createBody.comment"
          :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
          :label="$t('Примечание')">
      </v-textarea>

      <!-- Additional fields -->
      <slot name="additionalFields"></slot>


    </template>

  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import TableComponentDataContainer, {CreateAction, Filter} from "@/components/shared/table/TableComponentDataContainer";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import salaryService, {
  SalaryRequest,
  SalaryRequestReportBody,
  SalaryRequestType
} from "@/components/salary/salary.service";
import {searchUtils, TextFilterBuilder} from "@/components/searchutils";
import permissionService from "@/store/modules/permission.service";
import {Vue} from "vue-property-decorator";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import logger from "@/logger";
import {DateTimeUtils} from "@/components/datetimeutils";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import overtimeService, {ClosedOvertimePeriod, ReportPeriod} from "@/components/overtimes/overtime.service";


export class SalaryRequestUpdateBody {

}

const namespace_dict = 'dict';

export class SalaryRequestFilter extends Filter<SalaryRequest> {
  public search = '';

  applyFilter(items: SalaryRequest[]): SalaryRequest[] {
    return items.filter((item) => {
      let filtered = true;
      const search = this.search.toLowerCase().trim();
      if (search.length > 0) {
        let searchFilter = false;
        const textFilters = TextFilterBuilder.of()
            .splitWords(item.employee?.name)
            .splitWords(item.createdBy?.name)
            .ignoreCase(item.employeeDepartment?.name)
            .ignoreCase(item?.budgetBusinessAccount.name)
            .ignoreCase(item?.reason);
        filtered = filtered && searchUtils.textFilter(this.search, textFilters);
      }
      return filtered;
    });
  }
}

@Component({
  components: {MyDateFormComponent, HreasyTable}
})
export default class AdminSalaryAllRequests extends Vue {

  selectedPeriod = ReportPeriod.currentPeriod();
  private closedPeriods: ClosedOvertimePeriod[] = [];

  private allEmployees: Employee[] = [];

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  private data = new TableComponentDataContainer<SalaryRequest, SalaryRequestUpdateBody, SalaryRequestReportBody, SalaryRequestFilter>(
      () => salaryService.loadAllSalaryRequests(this.selectedPeriod.periodId()),
      () =>
          [
            {text: this.$tc('Сотрудник'), value: 'employee.name'},
            {text: this.$tc('Отдел'), value: 'department.name'},
            {text: this.$tc('Тип'), value: 'type'},
            {text: this.$tc('Бюджет из бизнес аккаунта'), value: 'budgetBusinessAccount.name'},
            {text: this.$tc('Планируемая дата окончания финансирования'), value: 'budgetExpectedFundingUntil'},
            {text: this.$tc('Сумма в рублях'), value: 'salaryIncrease'},
            {text: this.$tc('Созданно'), value: 'createdBy.name'},
            {text: this.$tc('Созданно (время)'), value: 'createdAt', sort: DateTimeUtils.dateComparatorNullLast},
            {text: this.$tc('Взято в работу'), value: 'inprogressBy.name'},
            {text: this.$tc('Взято в работу (время)'), value: 'inprogressAt', sort: DateTimeUtils.dateComparatorNullLast},
            {text: this.$tc('Реализовано'), value: 'implementedBy.name'},
            {text: this.$tc('Реализовано (время)'), value: 'implementedAt', sort: DateTimeUtils.dateComparatorNullLast},
          ],
      null,
      {
        createItemRequest: (body) => salaryService.reportSalaryRequest(body),
        defaultBody: () => this.defaultBody(),
      } as CreateAction<SalaryRequest, SalaryRequestReportBody>,
      null,
      new SalaryRequestFilter(),
      () => permissionService.canAdminDictDepartments(),
      true
  );

  private defaultBody(): SalaryRequestReportBody {
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
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        )
        .then(() => overtimeService.getClosedOvertimes()
            .then(data => {
              this.closedPeriods = data;
            }));
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
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

  private formatDateTime(date: string | undefined): string | undefined {
    logger.log(`Format date ${date}`);
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

