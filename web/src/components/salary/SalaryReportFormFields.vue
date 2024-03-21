<template>
  <span>
  <v-autocomplete
      v-model="createBody.employeeId"
      :items="allEmployees"
      item-value="id" item-text="displayName"
      :label="$t('Сотрудник')"
      :rules="[v => !!v || $t('Обязательное поле')]"
  ></v-autocomplete>
  <v-autocomplete
      v-model="createBody.budgetBusinessAccount"
      item-value="id" item-text="name"
      :items="allBas"
      :label="$t('Бюджет из бизнес аккаунта')"
      :rules="[v => !!v || $t('Обязательное поле')]"
  ></v-autocomplete>
    <v-autocomplete
        v-if="employeeAssessments && employeeAssessments.length > 0"
        v-model="createBody.assessmentId"
        item-value="id" item-text="plannedDate"
        :items="employeeAssessments"
        :label="$t('Ассессмент')"
    ></v-autocomplete>
  <my-date-form-component v-model="createBody.budgetExpectedFundingUntil"
                          :label="$t('Планируемая дата окончания финансирования')"
                          :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГГГ'))]"
  ></my-date-form-component>

  <v-text-field type="number"
                hide-spin-buttons
                v-model="createBody.currentSalaryAmount"
                :label="$t('Текущая заработная плата')"
  >
  </v-text-field>
  <v-text-field type="number"
                hide-spin-buttons
                v-model="createBody.increaseAmount"
                :rules="[v => !!v || $t('Обязательное числовое поле')]"
                :label="isSalaryRequest() ? $t('Предполагаемое изменение на') :  $t('Сумма бонуса')"
  >
  </v-text-field>

  <v-text-field type="number"
                v-if="isSalaryRequest()"
                hide-spin-buttons
                v-model="createBody.plannedSalaryAmount"
                :label="$t('Предполагаемая заработная плата после повышения')">
  </v-text-field>

  <v-text-field
      v-model="createBody.reason"
      counter="256"
      :rules="[v=>(v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256}))]"
      :label="$t('Обоснование')">
  </v-text-field>
  <v-textarea
      v-model="createBody.comment"
      :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
      :label="$t('Примечание')">
  </v-textarea>
  </span>
</template>

<script lang="ts">
import {Prop, Vue, Watch} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryRequestReportBody, SalaryRequestType} from "@/components/salary/salary.service";
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import assessmentService, {AssessmentBase} from "@/components/assessment/assessment.service";

const namespace_dict = 'dict';
@Component({
  components: {MyDateFormComponent}
})
export default class SalaryReportFormFields extends Vue {

  @Prop({required: true})
  private createBody!: SalaryRequestReportBody;

  private allEmployees: Employee[] = [];
  private employeeAssessments: AssessmentBase[] = [];

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;


  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Salary report form created');
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() =>
            employeeService.findAll().then(employees => {
                  this.allEmployees = employees;
                }
            )
        );
  }

  @Watch("createBody.employeeId")
  private employeeSelected(employeeId: number) {
    this.employeeAssessments = [];
    const empl = this.allEmployees.find(e => e.id == employeeId);
    if (!empl) {
      console.error(`Employee not found ${employeeId}`)
      return;
    }
    this.createBody.budgetBusinessAccount = empl.ba?.id;
    this.createBody.increaseAmount = null;
    this.createBody.plannedSalaryAmount = null;
    this.createBody.currentSalaryAmount = null;
    this.createBody.reason = null;
    this.createBody.comment = null;
    this.createBody.budgetExpectedFundingUntil = null;
    this.createBody.assessmentId = null;
    return assessmentService.employeeAssessments(employeeId)
        .then(data => {
          this.employeeAssessments = data.filter(a => !a.canceledAt);
        })
        .catch(err => {
          console.error(`Unable to load assessment ${err}`);
        })

  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

  isSalaryRequest(): boolean{
    return Boolean(this.createBody.type == SalaryRequestType.SALARY_INCREASE);
  }

}

</script>

<style scoped>

</style>
