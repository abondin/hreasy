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
import {Prop, Vue, Watch} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryRequestReportBody, salaryRequestTypes} from "@/components/salary/salary.service";
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

  private salaryTypes = salaryRequestTypes.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_TYPE.${v}`), value: v};
  });

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
    const empl = this.allEmployees.find(e => e.id==employeeId);
    if (!empl){
      console.error(`Employee not found ${employeeId}`)
      return ;
    }
    this.createBody.budgetBusinessAccount = empl.ba?.id;
    this.createBody.salaryIncrease=null;
    this.createBody.reason=null;
    this.createBody.comment=null;
    this.createBody.budgetExpectedFundingUntil=null;
    this.createBody.assessmentId=null;
    return assessmentService.employeeAssessments(employeeId)
        .then(data => {
          this.employeeAssessments = data.filter(a=>!a.canceledAt);
        })
        .catch(err => {
          console.error(`Unable to load assessment ${err}`);
        })

  }

  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }


}

</script>

<style scoped>

</style>
