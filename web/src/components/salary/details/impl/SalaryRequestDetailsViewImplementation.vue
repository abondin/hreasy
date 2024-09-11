<!-- Detailed information for selected salary request or bonus-->
<template>
  <v-card class="mt-5" v-if="data">
    <v-card-title class="text-h5">
      {{ $t('Реализация') }}
      <v-spacer></v-spacer>
      <span v-if="implementAllowed()">
      <v-tooltip bottom v-if="!Boolean(data.item.impl)">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn color="success" v-bind="tattrs" v-on="ton" icon @click="implement()"
                 :disabled="implementActionDisabled()">
            <v-icon>mdi-pen-plus</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Реализовать') }}</span>
      </v-tooltip>
      <v-tooltip bottom v-if="!Boolean(data.item.impl)">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn color="error" v-bind="tattrs" v-on="ton" icon @click="rejectImpl()"
                 :disabled="implementActionDisabled()">
            <v-icon>mdi-pen-minus</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Отклонить') }}</span>
      </v-tooltip>
      <v-tooltip bottom v-if="Boolean(data.item.impl)">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn color="error" v-bind="tattrs" v-on="ton" icon @click="deleteImpl()"
                 :disabled="implementActionDisabled()">
            <v-icon>mdi-pencil-off</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Сбросить решение') }}</span>
      </v-tooltip>
      <v-tooltip bottom v-if="Boolean(data.item.impl && data.isSalaryRequest())">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" icon @click="updateImplText()"
                 :disabled="implementActionDisabled()">
            <v-icon>mdi-email-edit</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Изменить сообщение') }}</span>
      </v-tooltip>
      </span>
    </v-card-title>
    <v-card-text>
      <div class="row">
        <!--<editor-fold desc="Решение">-->
        <div class=" col-md-auto">
          <div class="subtitle-1 text-decoration-underline column-title">
            {{ $t('Решение') }}
          </div>
          <dl class="info-dl text--primary text-wrap" v-if="data.item.impl">
            <dt v-if="data.item.impl?.state">{{ $t('Результат') }}:</dt>
            <dd v-if="data.item.impl?.state" :class="isRejected()? 'error--text': 'success--text'">
              {{ $t(`SALARY_REQUEST_STAT.${data.item.impl.state}`) }}
            </dd>

            <dt v-if="data.item.impl?.implementedBy">{{ $t('Принял решение') }}:</dt>
            <dd v-if="data.item.impl?.implementedBy">{{ data.item.impl.implementedBy.name }}
              ({{ formatDateFromDateTime(data.item.impl.implementedAt) }})
            </dd>

            <dt v-if="data.item.impl?.increaseAmount">
              {{ data.isSalaryRequest() ? $t('Изменение на') : $t('Сумма бонуса') }}:
            </dt>
            <dd v-if="data.item.impl?.increaseAmount">{{ formatMoney(data.item.impl.increaseAmount) }}</dd>

            <dt v-if="data.item.impl?.salaryAmount">{{ $t('Заработная плата после повышения') }}:</dt>
            <dd v-if="data.item.impl?.salaryAmount">{{ formatMoney(data.item.impl.salaryAmount) }}</dd>

            <dt v-if="data.item.impl?.newPosition">{{ $t('Новая позиция') }}:</dt>
            <dd v-if="data.item.impl?.newPosition">{{ data.item.impl.newPosition.name }}</dd>


            <dt v-if="data.item.impl?.rejectReason">{{ $t('Обоснование отказа') }}:</dt>
            <dd v-if="data.item.impl?.rejectReason">{{ data.item.impl.rejectReason }}</dd>

            <dt v-if="data.item.impl?.increaseStartPeriod">{{ $t('Месяц старта изменений') }}:</dt>
            <dd v-if="data.item.impl?.increaseStartPeriod">{{ formatPeriod(data.item.impl.increaseStartPeriod) }}</dd>

            <dt v-if="data.item.impl?.comment">{{ $t('Примечание') }}:</dt>
            <dd v-if="data.item.impl?.comment">{{ data.item.impl.comment }}</dd>


            <dt v-if="data.item.impl?.increaseText">{{ $t('Сообщение об изменениях') }}:</dt>
            <dd v-if="data.item.impl?.increaseText">{{ data.item.impl.increaseText }}</dd>


          </dl>
          <div v-else>{{ $t('На рассмотрении') }}</div>
        </div>
        <!--</editor-fold>-->
      </div>
    </v-card-text>
    <in-dialog-form size="lg" form-ref="implementForm" :data="data.implementAction"
                    :title="title"
                    v-on:submit="emitReload()">
      <template v-slot:fields>
        <v-card-text v-if="data.item.impl">
          {{ $t('Вы уверены, что хотите сбросить принятое решение по реализации?') }}
        </v-card-text>
        <salary-request-implement-form-fields v-else
                                              :formData="data.implementAction.formData"></salary-request-implement-form-fields>
      </template>
    </in-dialog-form>

    <in-dialog-form size="lg" form-ref="updateImplTextForm" :data="data.updateImplTextAction"
                    :title="$t('Изменить сообщение')"
                    v-on:submit="emitReload()">
      <template v-slot:fields>
        <salary-request-update-impl-text-form-fields
            :formData="data.updateImplTextAction.formData"></salary-request-update-impl-text-form-fields>
      </template>
    </in-dialog-form>

  </v-card>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import {Prop} from "vue-property-decorator";
import {SalaryDetailsDataContainer} from "@/components/salary/details/salary-details.data.container";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import permissionService from "@/store/modules/permission.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import SalaryRequestImplementFormFields from "@/components/salary/details/impl/SalaryRequestImplementFormFields.vue";
import SalaryRequestUpdateImplTextFormFields
  from "@/components/salary/details/impl/SalaryRequestUpdateImplTextFormFields.vue";


@Component({
  components: {SalaryRequestUpdateImplTextFormFields, SalaryRequestImplementFormFields, InDialogForm}
})
export default class SalaryRequestDetailsViewImplementation extends Vue {


  @Prop({required: true})
  data!: SalaryDetailsDataContainer;


  formatDateFromDateTime = (v: string | undefined) => DateTimeUtils.formatDateFromIsoDateTime(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => Number(v).toLocaleString();

  formatPeriod = (v: number) => ReportPeriod.fromPeriodId(v);

  private implementAllowed() {
    return permissionService.canAdminSalaryRequests();
  }

  private implement() {
    this.data.openImplementDialog(this.data.item, SalaryRequestImplementationState.IMPLEMENTED);
  }

  private rejectImpl() {
    this.data.openImplementDialog(this.data.item, SalaryRequestImplementationState.REJECTED);
  }

  isRejected() {
    return Boolean(this.data.item.impl?.state == SalaryRequestImplementationState.REJECTED);
  }

  private deleteImpl() {
    this.data.openImplementDialog(this.data.item);
  }

  private updateImplText() {
    this.data.openUpdateImplTextDialog(this.data.item);
  }

  private implementActionDisabled() {
    return this.data.implementAction.loading || this.data.periodClosed;
  }

  private title() {
    if (!this.data) {
      return '-';
    }
    return this.data.isImplemented() ? this.$tc('Отменить решение на реализацию')
        : (this.data.isSalaryRequest() ? this.$tc('Реализация запроса на повышение') : this.$tc('Реализация запроса на бонус'));
  }

  private emitReload() {
    this.$emit('updated');
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
