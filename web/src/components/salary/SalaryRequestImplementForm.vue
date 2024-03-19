<template>
  <v-form ref="salaryImplementForm" v-if="data.implementBody">
    <v-card>
      <v-card-title>{{ title() }}</v-card-title>

      <!--<editor-fold desc="Reset implementation">-->
      <v-card-text v-if="data.implementBody.completed">
        {{ $t('Вы уверены что хотите сбросить принятое решение по реализации?') }}
      </v-card-text>
      <!--</editor-fold> -->

      <!--<editor-fold desc="Implementation">-->
      <v-card-text v-else>
        <!--<editor-fold desc="Fields">-->
        <v-select
            :disabled="itemReadonly()"
            v-model="data.implementBody.state"
            :label="$t('Решение')"
            :rules="[v => !!v || $t('Обязательное поле')]"
            :items="salaryStats">
        </v-select>
        <v-text-field
            :disabled="itemReadonly()"
            v-if="!isRejected()"
            type="number"
            v-model="data.implementBody.increaseAmount"
            :rules="[v => !!v || $t('Обязательное числовое поле')]"
            :label="$t('Итоговое изменение на')">
        </v-text-field>
        <v-text-field
            :disabled="itemReadonly()"
            v-if="!isRejected()"
            type="number"
            v-model="data.implementBody.salaryAmount"
            :label="$t('Итоговая сумма')">
        </v-text-field>
        <v-select
            :disabled="itemReadonly()"
            v-if="!isRejected()"
            v-model="data.implementBody.increaseStartPeriod"
            :label="$t('Исполнить в периоде')"
            :items="periodsToChoose"
            item-value="id"
            item-text="toString()">
        </v-select>
        <v-autocomplete
            :disabled="itemReadonly()"
            v-if="!isBonus() && !isRejected()"
            v-model="data.implementBody.newPosition"
            :items="allPositions.filter(p=>p.active)"
            item-value="id"
            item-text="name"
            :label="$t('Изменить позицию')"
        ></v-autocomplete>
        <v-text-field
            :disabled="itemReadonly()"
            v-model="data.implementBody.reason"
            counter="256"
            :rules="[v=> isRejected()? (v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256})) : (!v || v.length <= 256 || $t('Не более N символов', {n:256}))]"
            :label="$t('Обоснование')">
        </v-text-field>
        <v-textarea
            :disabled="itemReadonly()"
            v-model="data.implementBody.comment"
            :rules="[v=>(!v || v.length <= 4096 || $t('Не более N символов', {n:4096}))]"
            :label="$t('Примечание')">
        </v-textarea>
        <!--</editor-fold>-->
      </v-card-text>
      <!--</editor-fold> -->
      <!-- Error block -->
      <v-alert v-if="data.actionError" type="error">
        {{ data.actionError }}
      </v-alert>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-progress-circular class="mr-2" v-if="data.loading" indeterminate></v-progress-circular>
        <v-btn @click="data.closeImplementDialog()">{{ $t('Закрыть') }}</v-btn>
        <v-btn @click="submitForm" color="primary" :disabled="data.loading || itemReadonly()">{{
            data.implementBody.completed? $t('Сбросить') : $t('Реализовать')
          }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-form>
</template>

<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {SalaryIncreaseRequest, SalaryRequestType, salaryRequestTypes} from "@/components/salary/salary.service";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import salaryAdminService, {
  SalaryRequestImplementationState,
  salaryRequestImplementationStates,
  SalaryRequestImplementBody
} from "@/components/admin/salary/admin.salary.service";
import SalaryRequestCard from "@/components/salary/SalaryRequestCard.vue";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";

export interface SalaryRequestFormData {
  type: SalaryRequestType,
  state: SalaryRequestImplementationState | null;
  increaseAmount: number;

  salaryAmount: number | null;
  /**
   * YYYYMM period. Month starts with 0. 202308 - September of 2023
   */
  increaseStartPeriod: number;
  newPosition: number | null;
  reason: string;
  comment: string | null;
  completed: boolean;
}

export class SalaryRequestImplementAction {

  public implementItemRequest(id: number, formData: SalaryRequestFormData) {
    if (formData.completed){
      logger.log(`Reset implementation for request ${id}`);
      return salaryAdminService.resetImplementation(id);
    }
    if (formData.state == SalaryRequestImplementationState.REJECTED) {
      const body = {
        comment: formData.comment,
        reason: formData.reason
      };
      logger.log(`Reject salary request ${id}: ${body}`);
      return salaryAdminService.reject(id, body);
    } else {
      const body = {
        comment: formData.comment,
        reason: formData.reason,
        increaseStartPeriod: formData.increaseStartPeriod,
        increaseAmount: formData.increaseAmount,
        salaryAmount: formData.salaryAmount,
        newPosition: formData.newPosition
      } as SalaryRequestImplementBody;
      logger.log(`Mark salary request ${id} as implemented: ${body}`);
      return salaryAdminService.markAsImplemented(id, body);
    }
  }

  public itemToBody(item: SalaryIncreaseRequest): SalaryRequestFormData {
    return {
      type: item.type,
      state: item.impl?.state,
      increaseAmount: item.impl ? item.impl.increaseAmount : item.req.increaseAmount,
      salaryAmount: item.impl ? item.impl.salaryAmount : item.req.plannedSalaryAmount,
      increaseStartPeriod: item.impl ? item.impl.increaseStartPeriod : item.req.increaseStartPeriod,
      reason: item.impl ? item.impl.reason : '',
      newPosition: item.impl ? item.impl.newPosition : null,
      completed: Boolean(item.impl)
    } as SalaryRequestFormData;
  }
}

const namespace_dict = 'dict';
@Component({
  components: {SalaryRequestCard}
})
export default class SalaryRequestImplementForm extends Vue {

  @Prop({required: true})
  private data!: SalaryRequestDataContainer;

  private submitForm() {
    const form: any = this.$refs.salaryImplementForm;
    if (form.validate()) {
      return this.data.submitImplementForm();
    }
  }

  private itemReadonly() {
    return !this.data;
  }

  private title() {
    if (!this.data){
      return '-';
    }
    return this.data.isImplemented() ? this.$tc('Отменить решение на реализацию')
        : (this.isBonus() ? this.$tc('Реализация запроса на бонус') : this.$tc('Реализация запроса на повышение'));
  }


  private periodsToChoose = ReportPeriod.currentAndNextPeriods();

  private salaryStats = salaryRequestImplementationStates.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_STAT.${v}`), value: v};
  });

  private salaryTypes = salaryRequestTypes.map(v => {
    return {text: this.$tc(`SALARY_REQUEST_TYPE.${v}`), value: v};
  });

  @Getter("positions", {namespace: namespace_dict})
  private allPositions!: Array<SimpleDict>;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Admin salary implement form created');
    this.$store.dispatch('dict/reloadPositions');
  }


  private isBonus(): boolean {
    return this.data.implementBody?.type == SalaryRequestType.BONUS;
  }

  private isRejected(): boolean {
    return this.data.implementBody?.state == SalaryRequestImplementationState.REJECTED;
  }


}

</script>

<style scoped>

</style>
