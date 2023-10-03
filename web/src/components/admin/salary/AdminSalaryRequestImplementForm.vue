<template>
  <span>
    {{ body }}
    <v-select
        v-model="body.state"
        :label="$t('Решение')"
        :items="salaryStats">
    </v-select>
  <v-text-field type="number"
                v-model="body.salaryIncrease"
                :rules="[v => !!v || $t('Обязательное числовое поле')]"
                :label="$t('Сумма в рублях')">
  </v-text-field>
  <v-textarea
      v-model="body.reason"
      counter="1024"
      :rules="[v=>(v && v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
      :label="$t('Обоснование')">
  </v-textarea>
  <v-textarea
      v-model="body.comment"
      :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
      :label="$t('Примечание')">
  </v-textarea>
  </span>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {
  SalaryRequestImplementationState,
  salaryRequestImplementationStates,
  salaryRequestTypes
} from "@/components/salary/salary.service";
import logger from "@/logger";

export interface SalaryRequestImplementBody {
  state: SalaryRequestImplementationState;
  salaryIncrease: number;
  /**
   * YYYYMM period. Month starts with 0. 202308 - September of 2023
   */
  increaseStartPeriod: number;
  assessmentId: number | null;
  reason: string;
  comment: string | null;
}

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class AdminSalaryRequestImplementForm extends Vue {

  @Prop({required: true})
  private body!: SalaryRequestImplementBody;

  private salaryStats = salaryRequestImplementationStates.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_STAT.${v}`), value: v};
  });

  private salaryTypes = salaryRequestTypes.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_TYPE.${v}`), value: v};
  });

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin salary implement form created');
  }


}

</script>

<style scoped>

</style>
