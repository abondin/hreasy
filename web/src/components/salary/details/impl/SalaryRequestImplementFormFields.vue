<template>
  <span>
        <!--<editor-fold desc="Fields">-->
        <v-select
            autofocus
            :disabled="itemReadonly()"
            v-model="formData.state"
            :label="$t('Решение')"
            :rules="[v => !!v || $t('Обязательное поле')]"
            :items="salaryStats">
        </v-select>
        <v-text-field
            :disabled="itemReadonly()"
            v-if="!isRejected()"
            type="number"
            v-model="formData.increaseAmount"
            :rules="[v => !!v || $t('Обязательное числовое поле')]"
            :label="isBonus()? $t('Сумма бонуса') : $t('Итоговое изменение на')">
        </v-text-field>
        <v-text-field
            :disabled="itemReadonly()"
            v-if="!isRejected() && !isBonus()"
            type="number"
            v-model="formData.salaryAmount"
            :label="$t('Итоговая сумма')">
        </v-text-field>
        <v-select
            :disabled="itemReadonly()"
            v-if="!isRejected()"
            v-model="formData.increaseStartPeriod"
            :label="$t('Исполнить в периоде')"
            :items="periodsToChoose"
            item-value="id"
            item-text="toString()">
        </v-select>
        <v-autocomplete
            :disabled="itemReadonly()"
            v-if="!isBonus() && !isRejected()"
            v-model="formData.newPosition"
            :items="allPositions.filter(p=>p.active)"
            item-value="id"
            item-text="name"
            :label="$t('Изменить позицию')"
        ></v-autocomplete>
         <v-select
             :disabled="itemReadonly()"
             v-if="isRejected()"
             v-model="formData.rescheduleToNewPeriod"
             :label="$t('Перенести запрос в другой период')"
             :items="periodsToChooseReschedule"
             item-value="id"
             item-text="toString()">
        </v-select>
        <v-text-field
            v-if="isRejected()"
            :disabled="itemReadonly()"
            v-model="formData.rejectReason"
            counter="128"
            :rules="[v=> v && v.length <= 128 || $t('Обязательное поле. Не более N символов', {n:128})]"
            :label="$t('Обоснование отказа')">
        </v-text-field>
        <v-textarea
            :disabled="itemReadonly()"
            v-model="formData.comment"
            :rules="[v=>(!v || v.length <= 256 || $t('Не более N символов', {n:256}))]"
            :label="$t('Примечание')">
        </v-textarea>
    <!--</editor-fold>-->
  </span>
</template>

<script lang="ts">
import {Prop, Vue, Watch} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryRequestType, salaryRequestTypes} from "@/components/salary/salary.service";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {
  SalaryRequestImplementationState,
  salaryRequestImplementationStates
} from "@/components/admin/salary/admin.salary.service";

export interface SalaryRequestImplementationFormData {
  type: SalaryRequestType,
  state: SalaryRequestImplementationState | null;
  increaseAmount: number;

  salaryAmount: number | null;
  /**
   * YYYYMM period. Month starts with 0. 202308 - September of 2023
   */
  increaseStartPeriod: number;
  newPosition: number | null;
  rejectReason: string;
  rescheduleToNewPeriod: number | null;
  comment: string | null;
  completed: boolean;
}


const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class SalaryRequestImplementFormFields extends Vue {

  @Prop({required: true})
  private formData!: SalaryRequestImplementationFormData;


  private itemReadonly() {
    return !this.formData;
  }

  private periodsToChoose = ReportPeriod.currentAndNextPeriods();
  private periodsToChooseReschedule = ReportPeriod.currentAndNextPeriods(1);

  private salaryStats = salaryRequestImplementationStates.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_STAT.${v}`), value: v};
  });

  private salaryTypes = salaryRequestTypes.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_TYPE.${v}`), value: v};
  });

  @Getter("positions", {namespace: namespace_dict})
  private allPositions!: Array<SimpleDict>;

  @Watch('formData.rescheduleToNewPeriod')
  watchRescheduleToNewPeriod() {
    if (this.formData.rescheduleToNewPeriod && !this.formData.rejectReason) {
      this.formData.rejectReason = this.$t('Перенос решения на ПЕРИОД'
          , {
            period: ReportPeriod.fromPeriodId(this.formData.rescheduleToNewPeriod).toString()
          }).toString();
    }
  }

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin salary implement form created');
    this.$store.dispatch('dict/reloadPositions');
  }

  isRejected() {
    return this.formData.state === SalaryRequestImplementationState.REJECTED;
  }

  isBonus() {
    return this.formData.type === SalaryRequestType.BONUS;
  }


}

</script>

<style scoped>

</style>
