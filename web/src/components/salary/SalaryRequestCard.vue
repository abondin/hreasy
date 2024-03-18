<template>
  <v-container fluid>
    <div class="row">
      <!--<editor-fold desc="Информация о сотруднике">-->
      <div class="col-lg-auto col-md-4">
        <div class="subtitle-1">{{ $t('Информация о сотруднике') }}</div>
        <dl class="info-dl text--primary text-wrap">
          <dt>{{ $t('ФИО') }}:</dt>
          <dd>{{ item.employee.name }}</dd>

          <dt>{{ $t('Дата трудоустройства') }}:</dt>
          <dd>{{ formatDate(item.employeeInfo.dateOfEmployment) }}</dd>

          <dt v-if="item.employeeInfo.position">{{ $t('Позиция') }}:</dt>
          <dd v-if="item.employeeInfo.position">{{ item.employeeInfo.position.name }}</dd>

          <dt>{{ $t('Текущий проект') }}:</dt>
          <dd>{{ prettyPrintProject(item) }}</dd>

          <dt v-if="item.employeeInfo.ba">{{ $t('Бизнес аккаунт') }}:</dt>
          <dd v-if="item.employeeInfo.ba">{{ item.employeeInfo.ba.name }}</dd>

          <dt v-if="item.employeeInfo.currentSalaryAmount">{{ $t('Текущая заработная плата') }}:</dt>
          <dd v-if="item.employeeInfo.currentSalaryAmount">{{ formatMoney(item.employeeInfo.currentSalaryAmount) }}</dd>

        </dl>
      </div>
      <!--</editor-fold>-->
      <!--<editor-fold desc="Запрос">-->
      <div class="col-lg-auto col-md-4">
        <div class="subtitle-1">{{ $t('Запрос') }}</div>
        <dl class="info-dl text--primary text-wrap">
          <dt>{{ $t('Инициатор') }}:</dt>
          <dd>{{ item.createdBy.name }} ({{ formatDateFromDateTime(item.createdAt) }})</dd>

          <dt>{{ $t('Бюджет из бизнес аккаунта') }}:</dt>
          <dd>{{ item.budgetBusinessAccount.name }}</dd>

          <dt v-if="item.budgetExpectedFundingUntil">{{ $t('Перспективы биллинга') }}:</dt>
          <dd v-if="item.budgetExpectedFundingUntil">{{ item.budgetExpectedFundingUntil }}</dd>

          <dt v-if="item.assessment">{{ $t('Ассессмент') }}:</dt>
          <dd v-if="item.assessment">
            <router-link target="_blank" :to="`/assessments/${item.employee.id}/${item.assessment.id}`">
              {{ item.assessment.name }}
            </router-link>
          </dd>

          <dt v-if="item.req.plannedSalaryAmount">{{ $t('Заработная плата после повышения') }}:</dt>
          <dd v-if="item.req.plannedSalaryAmount">{{ formatMoney(item.req.plannedSalaryAmount) }}</dd>

          <dt v-if="item.req.increaseAmount">
            {{ item.type == SALARY_INCREASE_TYPE ? $t('Изменение на') : $t('Сумма бонуса') }}:
          </dt>
          <dd v-if="item.req.increaseAmount">{{ formatMoney(item.req.increaseAmount) }}</dd>

          <dt v-if="item.req?.increaseStartPeriod">{{ $t('Месяц старта изменений') }}:</dt>
          <dd v-if="item.req?.increaseStartPeriod">{{ formatPeriod(item.req.increaseStartPeriod) }}</dd>


          <dt v-if="item.req?.reason">{{ $t('Обоснование') }}:</dt>
          <dd v-if="item.req?.reason">{{ item.req.reason }}</dd>

        </dl>
      </div>
      <!--</editor-fold>-->
      <!--<editor-fold desc="Решение">-->
      <div class="col-lg-auto col-md-4">
        <div class="subtitle-1">{{ $t('Решение') }}</div>
        <dl class="info-dl text--primary text-wrap" v-if="item.impl">
          <dt v-if="item.impl?.state">{{ $t('Результат') }}:</dt>
          <dd v-if="item.impl?.state" :class="item.impl.state == REJECTED? 'error--text': 'success--text'">
            {{ $t(`SALARY_REQUEST_STAT.${item.impl.state}`) }}
          </dd>

          <dt v-if="item.impl?.implementedBy">{{ $t('Принял решение') }}:</dt>
          <dd v-if="item.impl?.implementedBy">{{ item.impl.implementedBy.name }}
            ({{ formatDateFromDateTime(item.impl.implementedAt) }})
          </dd>

          <dt v-if="item.impl?.increaseAmount">
            {{ item.type == SALARY_INCREASE_TYPE ? $t('Изменение на') : $t('Сумма бонуса') }}:
          </dt>
          <dd v-if="item.impl?.increaseAmount">{{ formatMoney(item.impl.increaseAmount) }}</dd>

          <dt v-if="item.impl?.salaryAmount">{{ $t('Заработная плата после повышения') }}:</dt>
          <dd v-if="item.impl?.salaryAmount">{{ formatMoney(item.impl.salaryAmount) }}</dd>

          <dt v-if="item.impl?.newPosition">{{ $t('Новая позиция') }}:</dt>
          <dd v-if="item.impl?.newPosition">{{ item.impl.newPosition.name }}</dd>

          <dt v-if="item.impl?.reason">{{ $t('Обоснование') }}:</dt>
          <dd v-if="item.impl?.reason">{{ item.impl.reason }}</dd>

          <dt v-if="item.impl?.increaseStartPeriod">{{ $t('Месяц старта изменений') }}:</dt>
          <dd v-if="item.impl?.increaseStartPeriod">{{ formatPeriod(item.impl.increaseStartPeriod) }}</dd>

        </dl>
        <div v-else>{{ $t('На рассмотрении') }}</div>
      </div>
      <!--</editor-fold>-->
    </div>

    <!--<editor-fold desc="Действия">-->
    <div class="row row--dense">
      <!-- Implement -->
      <v-col align-self="center" cols="auto" v-if="allowImplementFunctionality()">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn v-if="item.impl" text link
                     @click="()=>dataContainer.openImplementDialog(item)" icon>
                <v-icon>mdi-pencil-off</v-icon>
              </v-btn>
              <v-btn v-else text color="primary" link
                     @click="()=>dataContainer.openImplementDialog(item)" icon>
                <v-icon>mdi-pen</v-icon>
              </v-btn>
            </div>
          </template>
          <p>{{ item.impl ? $t('Сбросить решение') : $t('Реализовать запрос') }}</p>
        </v-tooltip>
      </v-col>
      <v-col align-self="center" cols="auto" v-if="allowDeleteFunctionality()">
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="error" link :disabled="Boolean(item.impl)"
                     @click="()=>dataContainer.openDeleteDialogForItem(item)" icon>
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </div>
          </template>
          <p>{{ $t('Удалить') }}</p>
        </v-tooltip>
      </v-col>
    </div>
    <!--</editor-fold>-->

  </v-container>
</template>

<script lang="ts">
import {SalaryIncreaseRequest, SalaryRequestType} from "@/components/salary/salary.service";
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";
import permissionService from "@/store/modules/permission.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";

@Component
export default class SalaryRequestCard extends Vue {

  private SALARY_INCREASE_TYPE = SalaryRequestType.SALARY_INCREASE;
  private REJECTED = SalaryRequestImplementationState.REJECTED;

  @Prop({required: true})
  private item!: SalaryIncreaseRequest;

  @Prop({required: true})
  private dataContainer!: SalaryRequestDataContainer;

  formatDateFromDateTime = (v: string | undefined) => DateTimeUtils.formatDateFromIsoDateTime(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => Number(v).toLocaleString();

  formatPeriod = (v: number) => ReportPeriod.fromPeriodId(v);


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

  private allowImplementFunctionality() {
    return permissionService.canAdminSalaryRequests() && this.dataContainer?.implementAllowed();
  }

  private allowDeleteFunctionality() {
    return permissionService.canReportSalaryRequest() && this.dataContainer?.deleteAllowed();
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
