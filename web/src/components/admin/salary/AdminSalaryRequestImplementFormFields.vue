<template>
  <span>
  <v-select
      disabled="itemEditable()"
      v-model="body.state"
      :label="$t('Решение')"
      :items="salaryStats">
  </v-select>
  <v-text-field
      disabled="itemEditable()"
      v-if="!isRejected()"
      type="number"
      v-model="body.salaryIncrease"
      :rules="[v => !!v || $t('Обязательное числовое поле')]"
      :label="$t('Сумма в рублях')">
  </v-text-field>
   <v-select
       disabled="itemEditable()"
       v-if="!isRejected()"
       v-model="body.increaseStartPeriod"
       :label="$t('Исполнить в периоде')"
       :items="periodsToChoose"
       item-value="id"
       item-text="toString()">
    </v-select>
  <v-autocomplete
      disabled="itemEditable()"
      v-if="!isBonus() && !isRejected()"
      v-model="body.newPosition"
      :items="allPositions.filter(p=>p.active)"
      item-value="id"
      item-text="name"
      :label="$t('Изменить позицию')"
  ></v-autocomplete>
  <v-textarea
      disabled="itemEditable()"
      v-model="body.reason"
      counter="1024"
      :rules="[v=>(v && v.length <= 1024 || $t('Обязательное поле. Не более N символов', {n:1024}))]"
      :label="$t('Обоснование')">
  </v-textarea>
  <v-textarea
      disabled="itemEditable()"
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
import salaryAdminService, {SalaryRequestImplementBody} from "@/components/admin/salary/admin.salary.service";

export interface SalaryRequestFormData {
  type: SalaryRequestType,
  state: SalaryRequestImplementationState | null;
  salaryIncrease: number;
  /**
   * YYYYMM period. Month starts with 0. 202308 - September of 2023
   */
  increaseStartPeriod: number;
  newPosition: number | null;
  reason: string;
  comment: string | null;
  readonly: boolean;
}

export class SalaryRequestImplementAction implements UpdateAction<SalaryRequestFullInfo, SalaryRequestFormData> {

  public updateItemRequest(id: number, formData: SalaryRequestFormData) {
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
        salaryIncrease: formData.salaryIncrease,
        newPosition: formData.newPosition
      } as SalaryRequestImplementBody;
      logger.log(`Mark salary request ${id} as implemented: ${body}`);
      return salaryAdminService.markAsImplemented(id, body);

    }
  }

  public itemToUpdateBody(item: SalaryRequestFullInfo): SalaryRequestFormData {
    return {
      type: item.type,
      state: item.impl?.state,
      salaryIncrease: item.req.salaryIncrease,
      increaseStartPeriod: item.req.increaseStartPeriod,
      reason: '',
      newPosition: item.employeePosition?.id,
      readonly: Boolean(item.impl)
    } as SalaryRequestFormData;
  }

  public itemEditable(itemId: number, updateBody: SalaryRequestFormData): boolean {
    return !updateBody.readonly;
  }

}

const namespace_dict = 'dict';
@Component({
  components: {}
})
export default class AdminSalaryRequestImplementFormFields extends Vue {

  @Prop({required: true})
  private body!: SalaryRequestFormData;

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

  private isStateSelected(): boolean {
    return Boolean(this.body.state);
  }

  private isRejected(): boolean {
    return this.body.state == SalaryRequestImplementationState.REJECTED;
  }

}

</script>

<style scoped>

</style>
