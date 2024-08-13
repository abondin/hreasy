<!-- Detailed information for selected junior-->
<template>
  <div class="row">
    <!--<editor-fold desc="Базовая информация">-->
    <div class="col-md-auto">
      <div class="subtitle-1 text-decoration-underline column-title">{{ $t('Информация о сотруднике') }}</div>
      <dl class="info-dl text--primary text-wrap">
        <dt>{{ $t('ФИО') }}:</dt>
        <dd>{{ item.juniorEmpl.name }}</dd>
        <dt>{{ $t('Дата трудоуйства') }}:</dt>
        <dd>{{ formatDate(item.juniorDateOfEmployment) }}</dd>
        <dt>{{ $t('Месяцев в компании') }}:</dt>
        <dd><value-with-status-chip :value="item.juniorInCompanyMonths"></value-with-status-chip>
        </dd>

        <dt>{{ $t('Ментор') }}:</dt>
        <dd>{{ item.mentor?.name || $t('Нет') }}</dd>

        <dt>{{ $t('Роль') }}:</dt>
        <dd>{{ item.role || $t('Нет') }}</dd>

        <dt>{{ $t('Бюджет') }}:</dt>
        <dd>{{ item.budgetingAccount?.name || $t('Нет') }}</dd>

        <dt>{{ $t('Текущий проект') }}:</dt>
        <dd> {{ item.currentProject?.name || $t('Нет') }}</dd>
      </dl>
    </div>
    <div class="col-md-auto" v-if="item.graduation">
      <div class="subtitle-1 text-decoration-underline column-title">{{ $t('Завершение обучения') }}</div>
      <dl class="info-dl text--primary text-wrap">
        <dt>{{ $t('Обучение завершил') }}:</dt>
        <dd>{{ item.graduation.graduatedBy?.name }} ({{ formatDateTime(item.graduation.graduatedAt) }})</dd>
        <dt>{{ $t('Комментарий') }}:</dt>
        <dd>{{ item.graduation.comment }}</dd>
      </dl>
    </div>
    <!--</editor-fold>-->
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {JuniorDto} from "@/components/udr/udr.service";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";


@Component({
  components: {ValueWithStatusChip}
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
@import '~vuetify/src/styles/styles.sass';

.info-dl {
  display: grid;
  grid-template-columns: max-content auto;

  > dt {
    font-weight: bolder;
    min-width: 150px;
    max-width: 300px;
    grid-column-start: 1;
  }

  > dd {
    grid-column-start: 2;
    margin-left: 10px;
    max-width: 400px;
  }
}

.column-title {
  display: flex;
  justify-content: space-between;
}

.column-actions {
  display: flex;
  justify-content: start;
}

</style>
