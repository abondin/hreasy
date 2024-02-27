<template>
  <v-container>
    <!--<editor-fold desc="Информация">-->
    <div class="row">
      <!--<editor-fold desc="Информация о сотруднике">-->
      <div class="col-lg-4 col-6">
        <div class="subtitle-1">{{ $t('Информация о сотруднике') }}</div>
        <dl class="info-dl text--primary text-wrap">
          <dt>{{ $t('ФИО') }}:</dt>
          <dd>{{ data.employee.name }}</dd>

          <dt>{{ $t('Дата трудоустройства') }}:</dt>
          <dd>{{ formatDate(data.employeeInfo.dateOfEmployment) }}</dd>

          <dt v-if="data.employeeInfo.position">{{ $t('Позиция') }}:</dt>
          <dd v-if="data.employeeInfo.position">{{ data.employeeInfo.position.name }}</dd>

          <dt>{{ $t('Текущий проект') }}:</dt>
          <dd>{{ prettyPrintProject(data) }}</dd>

          <dt v-if="data.employeeInfo.ba">{{ $t('Бизнес аккаунт') }}:</dt>
          <dd v-if="data.employeeInfo.ba">{{ data.employeeInfo.ba.name }}</dd>

          <dt v-if="data.employeeInfo.currentSalaryAmount">{{ $t('Текущая заработная плата') }}:</dt>
          <dd v-if="data.employeeInfo.currentSalaryAmount">{{ formatMoney(data.employeeInfo.currentSalaryAmount) }}</dd>

        </dl>
      </div>
      <!--</editor-fold>-->
      <!--<editor-fold desc="Запрос">-->
      <div class="col-lg-4 col-6">
        <div class="subtitle-1">{{ $t('Запрос') }}</div>
        <dl class="info-dl text--primary text-wrap">
          <dt>{{ $t('Инициатор') }}:</dt>
          <dd>{{ data.createdBy.name }} ({{ formatDateTime(data.createdAt) }})</dd>

          <dt>{{ $t('Бюджет из бизнес аккаунта') }}:</dt>
          <dd>{{ data.budgetBusinessAccount.name }}</dd>

          <dt v-if="data.budgetExpectedFundingUntil">{{ $t('Перспективы биллинга') }}:</dt>
          <dd v-if="data.budgetExpectedFundingUntil">{{ data.budgetExpectedFundingUntil }}</dd>

          <dt v-if="data.assessment">{{ $t('Ассессмент') }}:</dt>
          <dd v-if="data.assessment">
            <router-link target="_blank" :to="`/assessments/${data.employee.id}/${data.assessment.id}`">
              {{ data.assessment.name }}
            </router-link>
          </dd>

          <dt v-if="data.req.plannedSalaryAmount">{{ $t('Заработная плата после повышения') }}:</dt>
          <dd v-if="data.req.plannedSalaryAmount">{{ formatMoney(data.req.plannedSalaryAmount) }}</dd>

          <dt v-if="data.req.increaseAmount">
            {{ data.type == SALARY_INCREASE_TYPE ? $t('Изменение на') : $t('Сумма бонуса') }}:
          </dt>
          <dd v-if="data.req.increaseAmount">{{ formatMoney(data.req.increaseAmount) }}</dd>
        </dl>
      </div>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Решение">-->
      <div class="col-lg-4 col-6">
        <div class="subtitle-1">{{ $t('Решение') }}</div>
        <dl class="info-dl text--primary text-wrap" v-if="data.impl">
          <dt>{{ $t('Инициатор') }}:</dt>
          <dd>{{ data.createdBy.name }} ({{ formatDateTime(data.createdAt) }})</dd>

          <dt>{{ $t('Бюджет из бизнес аккаунта') }}:</dt>
          <dd>{{ data.budgetBusinessAccount.name }}</dd>

          <dt v-if="data.budgetExpectedFundingUntil">{{ $t('Перспективы биллинга') }}:</dt>
          <dd v-if="data.budgetExpectedFundingUntil">{{ data.budgetExpectedFundingUntil }}</dd>

          <dt v-if="data.assessment">{{ $t('Ассессмент') }}:</dt>
          <dd v-if="data.assessment">
            <router-link target="_blank" :to="`/assessments/${data.employee.id}/${data.assessment.id}`">
              {{ data.assessment.name }}
            </router-link>
          </dd>

          <dt v-if="data.req.plannedSalaryAmount">{{ $t('Заработная плата после повышения') }}:</dt>
          <dd v-if="data.req.plannedSalaryAmount">{{ formatMoney(data.req.plannedSalaryAmount) }}</dd>

          <dt v-if="data.req.increaseAmount">
            {{ data.type == SALARY_INCREASE_TYPE ? $t('Изменение на') : $t('Сумма бонуса') }}:
          </dt>
          <dd v-if="data.req.increaseAmount">{{ formatMoney(data.req.increaseAmount) }}</dd>
        </dl>
        <div v-else>{{ $t('На рассмотрении') }}</div>
      </div>
      <!--</editor-fold>-->
    </div>
    <!--</editor-fold>-->
    <!--<editor-fold desc="Действия">-->
    <div class="row row--dense">
      <!-- Implement -->
      <v-col align-self="center" cols="auto" v-if="canAdmin()">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" link :disabled="data.impl" @click="()=>{}" icon>
                <v-icon>mdi-pen</v-icon>
              </v-btn>
            </div>
          </template>
          <p>{{ $t('Реализация запроса для сотрудника') }}</p>
        </v-tooltip>
      </v-col>
    </div>
    <!--</editor-fold>-->

    <!--<editor-fold desc="Реализация запроса для сотрудника">-->

    <!--</editor-fold>-->
  </v-container>
</template>

<script lang="ts">
import {SalaryIncreaseRequest, SalaryRequestType} from "@/components/salary/salary.service";
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import permissionService from "@/store/modules/permission.service";

@Component
export default class SalaryRequestCard extends Vue {

  private SALARY_INCREASE_TYPE = SalaryRequestType.SALARY_INCREASE;
  private BONUS_TYPE = SalaryRequestType.BONUS;

  @Prop({required: true})
  private data!: SalaryIncreaseRequest;

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => Number(v).toLocaleString();

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;

  private isRejected(): boolean {
    return this.data.impl?.state == SalaryRequestImplementationState.REJECTED;
  }


  private prettyPrintProject(item: SalaryIncreaseRequest) {
    let prettyString = "";
    if (item.employeeInfo?.currentProject) {
      prettyString = item.employeeInfo.currentProject.name;
      if (item.employeeInfo.currentProject.role) {
        prettyString = prettyString + ' / ' + item.employeeInfo.currentProject.role;
      }
    } else {
      prettyString = " - ";
    }
    return prettyString;
  }

  private canAdmin(): boolean {
    return permissionService.canAdminSalaryRequests();
  }

  private canReport(): boolean {
    return permissionService.canReportSalaryRequest();
  }

  private canExport(): boolean {
    return permissionService.canAdminSalaryRequests();
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
    grid-column-start: 1;
  }

  > dd {
    grid-column-start: 2;
    margin-left: 10px;
  }
}

</style>
