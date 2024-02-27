<template>
  <span>
    <salary-request-short-info-component :data="item"></salary-request-short-info-component>
  <v-select
      :disabled="body?.readonly"
      v-model="body.state"
      :label="$t('Решение')"
      :rules="[v => !!v || $t('Обязательное поле')]"
      :items="salaryStats">
  </v-select>
  <v-text-field
      :disabled="body?.readonly"
      v-if="!isRejected()"
      type="number"
      v-model="body.increaseAmount"
      :rules="[v => !!v || $t('Обязательное числовое поле')]"
      :label="$t('Итоговое изменение на')">
  </v-text-field>
  <v-text-field
      :disabled="body?.readonly"
      v-if="!isRejected()"
      type="number"
      v-model="body.salaryAmount"
      :label="$t('Итоговая сумма')">
  </v-text-field>
   <v-select
       :disabled="body?.readonly"
       v-if="!isRejected()"
       v-model="body.increaseStartPeriod"
       :label="$t('Исполнить в периоде')"
       :items="periodsToChoose"
       item-value="id"
       item-text="toString()">
    </v-select>
  <v-autocomplete
      :disabled="body?.readonly"
      v-if="!isBonus() && !isRejected()"
      v-model="body.newPosition"
      :items="allPositions.filter(p=>p.active)"
      item-value="id"
      item-text="name"
      :label="$t('Изменить позицию')"
  ></v-autocomplete>
  <v-text-field
      :disabled="body?.readonly"
      v-model="body.reason"
      counter="256"
      :rules="[v=> isRejected()? (v && v.length <= 256 || $t('Обязательное поле. Не более N символов', {n:256})) : (!v || v.length <= 256 || $t('Не более N символов', {n:256}))]"
      :label="$t('Обоснование')">
  </v-text-field>
  <v-textarea
      :disabled="body?.readonly"
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
  SalaryIncreaseRequest,
  SalaryRequestReportBody,
  SalaryRequestType,
  salaryRequestTypes
} from "@/components/salary/salary.service";
import logger from "@/logger";
import {SimpleDict} from "@/store/modules/dict";
import {Getter} from "vuex-class";
import TableComponentDataContainer, {UpdateAction} from "@/components/shared/table/TableComponentDataContainer";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import salaryAdminService, {
  SalaryRequestImplementationState,
  salaryRequestImplementationStates,
  SalaryRequestImplementBody
} from "@/components/admin/salary/admin.salary.service";
import SalaryRequestCard from "@/components/salary/SalaryRequestCard.vue";
import {UiConstants} from "@/components/uiconstants";
import {SalaryRequestFilter} from "@/components/salary/SalaryRequestFilterComponent.vue";

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
  readonly: boolean;
}

export class SalaryRequestImplementAction implements UpdateAction<SalaryIncreaseRequest, SalaryRequestFormData> {

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
        increaseAmount: formData.increaseAmount,
        salaryAmount: formData.salaryAmount,
        newPosition: formData.newPosition
      } as SalaryRequestImplementBody;
      logger.log(`Mark salary request ${id} as implemented: ${body}`);
      return salaryAdminService.markAsImplemented(id, body);

    }
  }

  public itemToUpdateBody(item: SalaryIncreaseRequest): SalaryRequestFormData {
    return {
      type: item.type,
      state: item.impl?.state,
      increaseAmount: item.impl ? item.impl.increaseAmount : item.req.increaseAmount,
      salaryAmount: item.impl ? item.impl.salaryAmount : item.req.plannedSalaryAmount,
      increaseStartPeriod: item.impl ? item.impl.increaseStartPeriod : item.req.increaseStartPeriod,
      reason: item.impl ? item.impl.reason : '',
      newPosition: item.impl ? item.impl.newPosition : null,
      readonly: Boolean(item.impl)
    } as SalaryRequestFormData;
  }

  public itemEditable(itemId: number, updateBody: SalaryRequestFormData): boolean {
    return !updateBody.readonly;
  }

}

const namespace_dict = 'dict';
@Component({
  components: {SalaryRequestCard}
})
export default class SalaryRequestImplementForm extends Vue {

  @Prop({required: true})
  private data!: TableComponentDataContainer<SalaryIncreaseRequest, SalaryRequestFormData, SalaryRequestReportBody, SalaryRequestFilter>;

  @Prop({required: false})
  private updateTitle?: () => string | string | undefined;


  private submitForm() {
    const form: any = this.$refs.adminUpdateForm;
    if (form.validate()) {
      return this.data.submitUpdateForm();
    }
  }

  private itemReadonly() {
    return !this.data
        || !this.data.updateCommitAllowed()

  }

  private print = UiConstants.print;




  @Prop({required: true})
  private body!: SalaryRequestFormData;

  @Prop({required: true})
  private item!: SalaryIncreaseRequest;

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
