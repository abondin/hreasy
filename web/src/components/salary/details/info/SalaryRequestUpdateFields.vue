<template>
  <span>
    <v-text-field type="number"
       v-if="isSalaryRequest()"
       hide-spin-buttons
       v-model="formData.currentSalaryAmount"
       :label="$t('Текущая заработная плата')"
    ></v-text-field>
    <v-text-field type="number"
       v-if="isSalaryRequest()"
       hide-spin-buttons
       v-model="formData.plannedSalaryAmount"
       :label="$t('Планируемая заработная плата')"
    ></v-text-field>
    <v-autocomplete
        v-if="!isBonus() && !isRejected()"
        v-model="formData.newPosition"
        :items="allPositions.filter(p=>p.active)"
        item-value="id"
        item-text="name"
        :label="$t('Запрошенная позиция')"
    ></v-autocomplete>
    <v-autocomplete
      v-if="isSalaryRequest() && employeeAssessments && employeeAssessments.length > 0"
      v-model="formData.assessmentId"
      item-value="id" item-text="plannedDate"
      :items="employeeAssessments"
      :label="$t('Ассессмент')"
    ></v-autocomplete>
    <my-date-form-component v-if="isSalaryRequest()" v-model="formData.budgetExpectedFundingUntil"
      :label="$t('Планируемая дата окончания финансирования')"
      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГГГ'))]"
    ></my-date-form-component>
    <my-date-form-component v-if="isSalaryRequest()" v-model="formData.previousSalaryIncreaseDate"
      :label="$t('Предыдущий реализованный пересмотр (дата)')"
      :rules="[v=>(validateDate(v, true) || $t('Дата в формате ДД.ММ.ГГГГ'))]"
    ></my-date-form-component>

    <v-text-field
      v-if="isSalaryRequest()"
      v-model="formData.previousSalaryIncreaseText"
      counter="256"
      :rules="[v=>(!v || v.length <= 1024 || $t('Не более N символов', {n:1024}))]"
      :label="$t('Предыдущий реализованный пересмотр (примечание)')">
    </v-text-field>

    <v-textarea
      v-model="formData.comment"
      :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
      :label="$t('Примечание')">
    </v-textarea>

  </span>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryRequestType} from "@/components/salary/salary.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import assessmentService, {AssessmentBase} from "@/components/assessment/assessment.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import {SalaryRequestUpdateBody} from "@/components/salary/details/info/salary-request.update.action";

const namespace_dict = 'dict';
@Component({
  components: {MyDateFormComponent}
})
export default class SalaryRequestUpdateFields extends Vue {

  @Prop({required: true})
  private formData!: SalaryRequestUpdateBody;
  @Prop({required: true})
  private employeeId!: number;

  @Prop({required: true})
  private implState!: SalaryRequestImplementationState | null;
  @Prop({required: true})
  private requestType!: SalaryRequestType;

  private employeeAssessments: AssessmentBase[] = [];

  @Getter("positions", {namespace: namespace_dict})
  private allPositions!: Array<SimpleDict>;


  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Salary report form created');
    return this.$nextTick()
        .then(() => this.$store.dispatch('dict/reloadPositions')).then(() => {
          return assessmentService.employeeAssessments(this.employeeId)
              .then(data => {
                this.employeeAssessments = data.filter(a => !a.canceledAt);
              })
              .catch(err => {
                console.error(`Unable to load assessment ${err}`);
              })
        });
  }


  private validateDate(formattedDate: string, allowEmpty = true): boolean {
    return DateTimeUtils.validateFormattedDate(formattedDate, allowEmpty);
  }

  isSalaryRequest(): boolean {
    return Boolean(this.requestType == SalaryRequestType.SALARY_INCREASE);
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
