<template>
  <span>
    {{ body }}
  <v-select
      v-model="body.state"
      :label="$t('Решение')"
      :items="salaryStats">
  </v-select>
  <v-text-field
      v-if="!isRejected()"
      type="number"
      v-model="body.salaryIncrease"
      :rules="[v => !!v || $t('Обязательное числовое поле')]"
      :label="$t('Сумма в рублях')">
  </v-text-field>
   <v-select
       v-if="!isRejected()"
       v-model="body.increaseStartPeriod"
       :label="$t('Исполнить в периоде')"
       :items="periodsToChoose"
       item-value="id"
       item-text="toString()">
    </v-select>
  <v-autocomplete
      v-if="!isBonus() && !isRejected()"
      v-model="body.newPosition"
      :items="allPositions.filter(p=>p.active)"
      item-value="id"
      item-text="name"
      :label="$t('Изменить позицию')"
  ></v-autocomplete>
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
  SalaryRequestFullInfo,
  SalaryRequestImplementationState,
  salaryRequestImplementationStates,
  SalaryRequestType,
  salaryRequestTypes
} from "@/components/salary/salary.service";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import {UpdateAction} from "@/components/shared/table/TableComponentDataContainer";
import {ReportPeriod} from "@/components/overtimes/overtime.service";

export interface SalaryRequestImplementBody {
  type: SalaryRequestType,
  state: SalaryRequestImplementationState;
  salaryIncrease: number;
  /**
   * YYYYMM period. Month starts with 0. 202308 - September of 2023
   */
  increaseStartPeriod: number;
  newPosition: number | null;
  reason: string;
  comment: string | null;
}

export class SalaryRequestReportAction implements UpdateAction<SalaryRequestFullInfo, SalaryRequestImplementBody> {

  public updateItemRequest(id: number, body: SalaryRequestImplementBody) {
    return Promise.resolve(console.log(`Update ${body} for {id}`));
  }

  public itemToUpdateBody(item: SalaryRequestFullInfo): SalaryRequestImplementBody {
    return {
      type: item.type,
      state: SalaryRequestImplementationState.IMPLEMENTED,
      salaryIncrease: item.req.salaryIncrease,
      increaseStartPeriod: item.req.increaseStartPeriod,
      reason: '',
      newPosition: item.employeePosition?.id
    } as SalaryRequestImplementBody;
  }

}

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class AdminSalaryRequestImplementForm extends Vue {

  @Prop({required: true})
  private body!: SalaryRequestImplementBody;

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
    return this.body.type == SalaryRequestType.BONUS;
  }

  private isRejected(): boolean {
    return this.body.state == SalaryRequestImplementationState.REJECTED;
  }

}

</script>

<style scoped>

</style>
