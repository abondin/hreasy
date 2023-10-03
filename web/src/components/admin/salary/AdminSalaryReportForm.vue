<template>
  <span>
  <v-autocomplete
      v-model="createBody.employeeId"
      :items="allEmployees"
      item-value="id" item-text="displayName"
      :label="$t('Сотрудник')"
      :rules="[v => !!v || $t('Обязательное поле')]"
  ></v-autocomplete>
  <v-select
      v-model="createBody.type"
      :label="$t('Тип')"
      :rules="[v => !!v || $t('Обязательное поле')]"
      :items="salaryTypes">
  </v-select>
  <v-autocomplete
      v-model="createBody.budgetBusinessAccount"
      item-value="id" item-text="name"
      :items="allBas"
      :label="$t('Бюджет из бизнес аккаунта')"
      :rules="[v => !!v || $t('Обязательное поле')]"
  ></v-autocomplete>
  <my-date-form-component v-model="createBody.budgetExpectedFundingUntil"
                          :label="$t('Планируемая дата окончания финансирования')"
                          :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГГГ'))]"
  ></my-date-form-component>

  <v-text-field type="number"
                v-model="createBody.salaryIncrease"
                :rules="[v => !!v || $t('Обязательное числовое поле')]"
                :label="$t('Сумма в рублях')"
  >
  </v-text-field>

  <v-textarea
      v-model="createBody.reason"
      counter="1024"
      :rules="[v=>(v && v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
      :label="$t('Обоснование')">
  </v-textarea>
  <v-textarea
      v-model="createBody.comment"
      :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
      :label="$t('Примечание')">
  </v-textarea>
  </span>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import TableComponentontainer from "@/components/shared/table/TableComponentontainer";
import {SalaryRequest, SalaryRequestReportBody, salaryRequestTypes} from "@/components/salary/salary.service";
import {SalaryRequestFilter, SalaryRequestUpdateBody} from "@/components/admin/salary/AdminSalaryAllRequests.vue";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import logger from "@/logger";

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class AdminSalaryReportForm extends Vue {

  @Prop({required: true})
  private createBody!: SalaryRequestReportBody;

  private allEmployees: Employee[] = [];

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  private salaryTypes = salaryRequestTypes.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_TYPE.${v}`), value: v};
  });
  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin salary report form created');
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadDepartments'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        );
  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }


}

</script>

<style scoped>

</style>
