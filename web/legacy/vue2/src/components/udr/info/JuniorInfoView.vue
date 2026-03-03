<!-- Detailed information for selected junior-->
<template>
  <employee-base-info-card :employee-id="item.juniorEmpl.id">
    <dl class="info-dl text--primary text-wrap ml-lg-5">
      <dt>{{ $t('Ментор') }}:</dt>
      <dd>{{ item.mentor?.name || $t('Нет') }}</dd>

      <dt>{{ $t('Бюджет') }}:</dt>
      <dd>{{ item.budgetingAccount?.name || $t('Нет') }}</dd>

      <dt>{{ $t('Дата трудоустройства') }}:</dt>
      <dd>{{ formatDate(item.juniorDateOfEmployment) }}</dd>

      <dt>{{ $t('Месяцев в компании') }}:</dt>
      <dd>
        <value-with-status-chip :value="item.juniorInCompanyMonths"></value-with-status-chip>
      </dd>
      <dt v-if="item.graduation">{{ $t('Обучение завершил') }}:</dt>
      <dd v-if="item.graduation">{{ item.graduation.graduatedBy?.name }} ({{
          formatDateTime(item.graduation.graduatedAt)
        }})
      </dd>
      <dt v-if="item.graduation">{{ $t('Комментарий') }}:</dt>
      <dd v-if="item.graduation">{{ item.graduation.comment }}</dd>
    </dl>
  </employee-base-info-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {JuniorDto} from "@/components/udr/udr.service";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";
import EmployeeBaseInfoCard from "@/components/shared/EmployeeBaseInfoCard.vue";


@Component({
  components: {EmployeeBaseInfoCard, ValueWithStatusChip}
})
export default class JuniorInfoReports extends Vue {

  @Prop({required: true})
  item!: JuniorDto;

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

<style scoped lang="scss">

</style>
