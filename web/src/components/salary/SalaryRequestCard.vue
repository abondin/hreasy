<template>
  <v-container fluid>
    <div class="row">
      <!--<editor-fold desc="Информация о сотруднике">-->
      <div class="col-xl-3 col-lg-auto">
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
      <div class="col-xl-3 col-lg-auto">
        <div class="subtitle-1 column-title">
          {{ $t('Запрос') }}
          <div class="btn-group" v-if="allowDeleteFunctionality()">
            <v-tooltip bottom>
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton" disabled="data.loading">
                  <v-btn x-small text color="error" link
                         @click="()=>dataContainer.openDeleteDialogForItem(item)" icon>
                    <v-icon>mdi-close</v-icon>
                  </v-btn>
                </div>
              </template>
              <p>{{ $t('Удалить') }}</p>
            </v-tooltip>
          </div>
        </div>
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

          <dt v-if="item.req?.comment">{{ $t('Примечание') }}:</dt>
          <dd v-if="item.req?.comment">{{ item.req.comment }}</dd>

        </dl>
      </div>
      <!--</editor-fold>-->
      <!--<editor-fold desc="Решение">-->
      <div class="col-xl-3 col-lg-6">
        <div class="subtitle-1 column-title">
          {{ $t('Решение') }}
          <div class="btn-group" v-if="allowImplementFunctionality()">
            <v-tooltip bottom>
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton">
                  <v-btn x-small v-if="item.impl" text link
                         @click="()=>dataContainer.openImplementDialog(item)" icon>
                    <v-icon>mdi-pencil-off</v-icon>
                  </v-btn>
                  <v-btn v-else x-small text color="primary" link
                         @click="()=>dataContainer.openImplementDialog(item)" icon>
                    <v-icon>mdi-pen-plus</v-icon>
                  </v-btn>
                </div>
              </template>
              <p>{{ item.impl ? $t('Сбросить решение') : $t('Реализовать запрос') }}</p>
            </v-tooltip>
          </div>
        </div>
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


          <dt v-if="item.impl?.rejectReason">{{ $t('Обоснование отказа') }}:</dt>
          <dd v-if="item.impl?.rejectReason">{{ item.impl.rejectReason }}</dd>

          <dt v-if="item.impl?.increaseStartPeriod">{{ $t('Месяц старта изменений') }}:</dt>
          <dd v-if="item.impl?.increaseStartPeriod">{{ formatPeriod(item.impl.increaseStartPeriod) }}</dd>

          <dt v-if="item.impl?.comment">{{ $t('Примечание') }}:</dt>
          <dd v-if="item.impl?.comment">{{ item.impl.comment }}</dd>


        </dl>
        <div v-else>{{ $t('На рассмотрении') }}</div>
      </div>
      <!--</editor-fold>-->

      <!--<editor-fold desc="Согласования и комментарии">-->
      <div class="col-xl-3 col-lg-auto" v-if="approveAllowed(item)">
        <div class="subtitle-1 column-title">
          {{ $t('Согласования и комментарии') }}
          <div class="btn-group">
            <!-- Добавить комментарии -->
            <v-tooltip bottom>
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton">
                  <v-btn x-small text icon @click="comment(item)">
                    <v-icon>mdi-comment</v-icon>
                  </v-btn>
                </div>
              </template>
              {{ $t('Добавить комментарий') }}
            </v-tooltip>
            <!-- Согласовать -->
            <v-tooltip bottom>
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton">
                  <v-btn color="success" x-small text icon @click="approve(item)">
                    <v-icon>mdi-checkbox-marked-circle</v-icon>
                  </v-btn>
                </div>
              </template>
              {{ $t('Согласовать') }}
            </v-tooltip>
            <!-- Отклонить -->
            <v-tooltip bottom>
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <div v-bind="tattrs" v-on="ton">
                  <v-btn color="error" x-small text icon @click="decline(item)">
                    <v-icon>mdi-alert-circle</v-icon>
                  </v-btn>
                </div>
              </template>
              {{ $t('Отклонить') }}
            </v-tooltip>
          </div>
        </div>
        <salary-request-approval-card :request="item" :data-container="dataContainer"></salary-request-approval-card>
      </div>
      <!--</editor-fold>-->

    </div>

  </v-container>
</template>

<script lang="ts">
import {SalaryApprovalState, SalaryIncreaseRequest, SalaryRequestType} from "@/components/salary/salary.service";
import {Prop, Vue} from "vue-property-decorator";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {SalaryRequestDataContainer} from "@/components/salary/salary.data.container";
import permissionService from "@/store/modules/permission.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import {SalaryRequestImplementationState} from "@/components/admin/salary/admin.salary.service";
import SalaryRequestApprovalCard from "@/components/salary/SalaryRequestApprovalCard.vue";

@Component({
  components: {SalaryRequestApprovalCard}
})
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

  private approveAllowed(item: SalaryIncreaseRequest) {
    return permissionService.canApproveSalaryRequest(item?.budgetBusinessAccount?.id) && this.dataContainer?.approveAllowed();
  }

  private approve(item: SalaryIncreaseRequest) {
    this.dataContainer.openApproveDialog(item, null, SalaryApprovalState.APPROVE);
  }

  private decline(item: SalaryIncreaseRequest) {
    this.dataContainer.openApproveDialog(item, null, SalaryApprovalState.DECLINE);
  }

  private comment(item: SalaryIncreaseRequest) {
    this.dataContainer.openApproveDialog(item, null, SalaryApprovalState.COMMENT);
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
    max-width: 200px;
    grid-column-start: 1;
  }

  > dd {
    grid-column-start: 2;
    margin-left: 10px;
  }
}

.column-title {
  display: flex;
  justify-content: space-between;
}

.column-title .btn-group {
  display: flex;
  opacity: 0.7;
}

.column-title:hover .btn-group {
  opacity: 1;
}

</style>
