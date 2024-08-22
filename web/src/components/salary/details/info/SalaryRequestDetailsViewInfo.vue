<!-- Detailed information for selected salary request or bonus-->
<template>
  <v-card class="mt-5" v-if="data">
    <v-card-title class="text-h5">
      {{ data.isSalaryRequest() ? $t('Запрос на повышение') : $t('Запрос на бонус') }}
      <v-spacer></v-spacer>
      <v-tooltip bottom v-if="updateAllowed()">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" icon @click="openUpdateDialog()" :disabled="updateActionDisabled()">
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Внести изменения') }}</span>
      </v-tooltip>
      <v-tooltip bottom v-if="deleteAllowed()">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" text icon @click="openDeleteDialog()" :disabled="deleteActionDisabled()">
            <v-icon color="error">mdi-delete</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Удалить') }}</span>
      </v-tooltip>
    </v-card-title>
    <v-card-text>
      <div class="row">
        <!-- editor-fold desc="Информация о сотруднике">-->
        <div class="col-md-auto">
          <div class="subtitle-1 text-decoration-underline column-title">{{ $t('Информация о сотруднике') }}</div>

          <dl class="info-dl text--primary text-wrap">
            <dt>{{ $t('ФИО') }}:</dt>
            <dd>{{ data.item.employee.name }}</dd>

            <dt>{{ $t('Дата трудоустройства') }}:</dt>
            <dd>{{ formatDate(data.item.employeeInfo.dateOfEmployment) }}</dd>

            <dt v-if="data.item.employeeInfo.position">{{ $t('Позиция') }}:</dt>
            <dd v-if="data.item.employeeInfo.position">{{ data.item.employeeInfo.position.name }}</dd>

            <dt>{{ $t('Текущий проект') }}:</dt>
            <dd>{{ prettyPrintProject(data.item) }}</dd>

            <dt v-if="data.item.employeeInfo.ba">{{ $t('Бизнес аккаунт') }}:</dt>
            <dd v-if="data.item.employeeInfo.ba">{{ data.item.employeeInfo.ba.name }}</dd>

            <dt v-if="data.item.employeeInfo.currentSalaryAmount">{{ $t('Текущая заработная плата') }}:</dt>
            <dd v-if="data.item.employeeInfo.currentSalaryAmount">
              {{ formatMoney(data.item.employeeInfo.currentSalaryAmount) }}
            </dd>

            <dt v-if="data.isSalaryRequest() && data.item.employeeInfo.previousSalaryIncreaseText">{{
                $t('Предыдущий реализованный пересмотр')
              }}:
            </dt>
            <dd v-if="data.isSalaryRequest() && data.item.employeeInfo.previousSalaryIncreaseText">
              {{ data.item.employeeInfo.previousSalaryIncreaseText }}
            </dd>
          </dl>
        </div>
        <!--</editor-fold>-->

        <!-- editor-fold desc="Запрос">-->
        <div class="col-md-auto">
          <div class="subtitle-1 text-decoration-underline column-title">
            {{ $t('Запрос') }}
          </div>
          <dl class="info-dl text--primary text-wrap">
            <dt>{{ $t('Инициатор') }}:</dt>
            <dd>{{ data.item.createdBy.name }} ({{ formatDateFromDateTime(data.item.createdAt) }})</dd>

            <dt>{{ $t('Бюджет из бизнес аккаунта') }}:</dt>
            <dd>{{ data.item.budgetBusinessAccount.name }}</dd>

            <dt v-if="data.item.budgetExpectedFundingUntil">{{ $t('Перспективы биллинга') }}:</dt>
            <dd v-if="data.item.budgetExpectedFundingUntil">{{ data.item.budgetExpectedFundingUntil }}</dd>

            <dt v-if="data.item.assessment">{{ $t('Ассессмент') }}:</dt>
            <dd v-if="data.item.assessment">
              <router-link target="_blank" :to="`/assessments/${data.item.employee.id}/${data.item.assessment.id}`">
                {{ data.item.assessment.name }}
              </router-link>
            </dd>

            <dt v-if="data.item.req.plannedSalaryAmount">{{ $t('Заработная плата после повышения') }}:</dt>
            <dd v-if="data.item.req.plannedSalaryAmount">{{ formatMoney(data.item.req.plannedSalaryAmount) }}</dd>

            <dt v-if="data.item.req.increaseAmount">
              {{ data.isSalaryRequest() ? $t('Изменение на') : $t('Сумма бонуса') }}:
            </dt>
            <dd v-if="data.item.req.increaseAmount">{{ formatMoney(data.item.req.increaseAmount) }}</dd>

            <dt v-if="data.item.req?.increaseStartPeriod">{{ $t('Месяц старта изменений') }}:</dt>
            <dd v-if="data.item.req?.increaseStartPeriod">{{ formatPeriod(data.item.req.increaseStartPeriod) }}</dd>

            <dt v-if="data.item.req?.reason">{{ $t('Обоснование') }}:</dt>
            <dd v-if="data.item.req?.reason">{{ data.item.req.reason }}</dd>

            <dt v-if="data.item.req?.comment">{{ $t('Примечание') }}:</dt>
            <dd v-if="data.item.req?.comment">{{ data.item.req.comment }}</dd>
          </dl>
        </div>
      </div>
    </v-card-text>
    <in-dialog-form size="md" form-ref="deleteForm" :data="data.deleteAction"
                    :title="$t('Удалить запрос')"
                    v-on:submit="navigateSalariesTable()">
      <template v-slot:fields>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить запрос?') }}
        </v-card-text>
      </template>
    </in-dialog-form>
    <in-dialog-form size="md" form-ref="updateForm" :data="data.deleteAction"
                    :title="$t('Удалить запрос')"
                    v-on:submit="navigateSalariesTable()">
      <template v-slot:fields>
        <v-card-text>
          {{ $t('Вы уверены, что хотите удалить запрос?') }}
        </v-card-text>
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
import {SalaryIncreaseRequest} from "@/components/salary/salary.service";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import permissionService from "@/store/modules/permission.service";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";


@Component({
  components: {InDialogForm}
})
export default class SalaryRequestDetailsViewInfo extends Vue {


  @Prop({required: true})
  data!: SalaryDetailsDataContainer;


  formatDateFromDateTime = (v: string | undefined) => DateTimeUtils.formatDateFromIsoDateTime(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => Number(v).toLocaleString();

  formatPeriod = (v: number) => ReportPeriod.fromPeriodId(v);

  private navigateSalariesTable() {
    this.$router.push('/salaries/requests');
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

  updateAllowed() {
    return permissionService.canUpdateSalaryRequests(this.data.item.createdBy.id);
  }

  deleteAllowed() {
    return permissionService.canDeleteSalaryRequests(this.data.item.createdBy.id);
  }

  updateActionDisabled() {
    return this.data.periodClosed || this.data.isImplemented();
  }

  deleteActionDisabled() {
    return this.data.periodClosed || this.data.isImplemented();
  }

  openDeleteDialog(){
    return this.data.deleteAction.openDialog(this.data.item.id, null)
  }
  openUpdateDialog(){
    return false;
  }

  emitReload() {
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
