<template>
  <span>
    <v-autocomplete
        v-if="isSalaryRequest() && employeeAssessments && employeeAssessments.length > 0"
        v-model="body.assessmentId"
        item-value="id" item-text="plannedDate"
        :items="employeeAssessments"
        :label="$t('Ассессмент')"
    ></v-autocomplete>
  <my-date-form-component v-if="isSalaryRequest()" v-model="body.budgetExpectedFundingUntil"
                          :label="$t('Планируемая дата окончания финансирования')"
                          :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГГГ'))]"
  ></my-date-form-component>
    <v-text-field
        v-if="isSalaryRequest()"
        v-model="body.previousSalaryIncreaseText"
        counter="256"
        :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
        :label="$t('Предыдущий реализованный пересмотр')">
  </v-text-field>

  <v-autocomplete
      v-if="!isBonus() && !isRejected()"
      v-model="formData.newPosition"
      :items="allPositions.filter(p=>p.active)"
      item-value="id"
      item-text="name"
      :label="$t('Изменить позицию')"
  ></v-autocomplete>

  <v-textarea
      v-model="body.comment"
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
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";

const namespace_dict = 'dict';
@Component({
  components: {MyDateFormComponent}
})
export default class SalaryRequestUpdateFields extends Vue {

  @Prop({required: true})
  private body!: SalaryRequestReportBody;
  @Prop({required: true})
  private implState!: SalaryRequestImplementationState;
  @Prop({required: true})
  private requestType!: SalaryRequestType;

  private employeeAssessments: AssessmentBase[] = [];

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("positions", {namespace: namespace_dict})
  private allPositions!: Array<SimpleDict>;


  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Salary report form created');
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.$store.dispatch('dict/reloadPositions'));
  }

  @Watch("createBody.employeeId")
  private employeeSelected(employeeId: number) {
    this.employeeAssessments = [];
    const empl = this.allEmployees.find(e => e.id == employeeId);
    if (!empl) {
      console.error(`Employee not found ${employeeId}`)
      return;
    }
    this.body.budgetBusinessAccount = empl.ba?.id;
    this.body.increaseAmount = null;
    this.body.plannedSalaryAmount = null;
    this.body.currentSalaryAmount = null;
    this.body.reason = null;
    this.body.comment = null;
    this.body.budgetExpectedFundingUntil = null;
    this.body.assessmentId = null;
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

  isSalaryRequest(): boolean {
    return Boolean(this.body.type == SalaryRequestType.SALARY_INCREASE);
  }

  isRejected() {
    return this.implState === SalaryRequestImplementationState.REJECTED;
  }

  isBonus() {
    return this.requestType === SalaryRequestType.BONUS;
  }

}

</script>

<style scoped>

</style>
