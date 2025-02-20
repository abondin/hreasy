<!-- Detailed information for selected salary request or bonus-->
<template>
  <v-card class="mt-5" v-if="data">
    <v-card-title class="text-h5">
      {{ data.isSalaryRequest() ? $t('Запрос на повышение') : $t('Запрос на бонус') }}&nbsp;<span
        class="text-subtitle-1">(+{{ formatMoney(data.item.req.increaseAmount) }})</span>
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
    <v-card-subtitle>
      {{formatPeriod(data.item.req.increaseStartPeriod)}}
    </v-card-subtitle>
    <v-card-text>
      <employee-base-info-card :employee-id="data.item.employee.id">
        <!-- editor-fold desc="Информация о сотруднике">-->
        <template v-slot:additionalFields>
          <dt>{{ $t('Дата трудоустройства') }}:</dt>
          <dd>{{ formatDate(data.item.employeeInfo.dateOfEmployment) }}</dd>

          <dt v-if="data.isSalaryRequest()">{{ $t('Предыдущий реализованный пересмотр') }}:
          </dt>

          <dd v-if="data.isSalaryRequest()">
            {{ formatDate(data.item.employeeInfo.previousSalaryIncreaseDate) || '-' }}
            <span v-if="data.item.employeeInfo.previousSalaryIncreaseText"> ({{
                data.item.employeeInfo.previousSalaryIncreaseText
              }})</span>
          </dd>

          <dt v-if="data.isSalaryRequest()">{{ $t('Текущая заработная плата') }}:</dt>
          <dd v-if="data.isSalaryRequest()">
            {{ formatMoney(data.item.employeeInfo.currentSalaryAmount) || '-' }}
          </dd>

          <dt v-if="data.isSalaryRequest()">{{ $t('Ассессмент') }}:</dt>
          <dd v-if="data.isSalaryRequest()">
            <router-link v-if="data.item.assessment" target="_blank"
                         :to="`/assessments/${data.item.employee.id}/${data.item.assessment.id}`">
              {{ data.item.assessment.name }}
            </router-link>
            <span v-else>-</span>
          </dd>
        </template>
        <!--</editor-fold>-->
        <!-- editor-fold desc="Запрос">-->
        <dl class="info-dl text--primary text-wrap ml-lg-5" :set="invalidAmmount=!validateIncreaseAndSalary()">
          <dt>{{ $t('Инициатор') }}:</dt>
          <dd>{{ data.item.createdBy.name }} ({{ formatDateFromDateTime(data.item.createdAt) }})</dd>

          <dt>{{ $t('Бюджет из бизнес аккаунта') }}:</dt>
          <dd>{{ data.item.budgetBusinessAccount.name }}</dd>

          <dt v-if="data.isSalaryRequest()">{{ $t('Перспективы биллинга') }}:</dt>
          <dd v-if="data.isSalaryRequest()">{{ formatDate(data.item.budgetExpectedFundingUntil) || '-' }}</dd>

          <dt>
            {{ data.isSalaryRequest() ? $t('Изменение на') : $t('Сумма бонуса') }}:
          </dt>
          <dd>{{ formatMoney(data.item.req.increaseAmount) }}</dd>

          <dt v-if="data.isSalaryRequest()">
            {{ $t('Заработная плата после повышения') }}:
          </dt>
          <dd v-if="data.isSalaryRequest()" :class="{'error--text':invalidAmmount}">
            {{ formatMoney(data.item.req.plannedSalaryAmount) || '-' }}
            <v-tooltip bottom v-if="invalidAmmount">
              <template v-slot:activator="{ on: ton, attrs: tattrs}">
                <v-icon small color="error" v-bind="tattrs" v-on="ton">mdi-help-circle</v-icon>
              </template>
              {{ $t('Запланированная заработная плата должна совпадать с суммой текущей и изменением') }}
            </v-tooltip>
          </dd>

          <dt v-if="data.isSalaryRequest()">{{ $t('Запрошенная позиция') }}:</dt>
          <dd v-if="data.isSalaryRequest()">{{ data.item.req.newPosition?.name || '-' }}</dd>


          <dt>{{ $t('Обоснование') }}:</dt>
          <dd>{{ data.item.req?.reason || '-' }}</dd>

          <dt>{{ $t('Примечание') }}:</dt>
          <dd>{{ data.item.req?.comment || '-' }}</dd>
        </dl>
        <!--</editor-fold>-->
      </employee-base-info-card>
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
    <in-dialog-form size="lg" form-ref="updateForm" :data="data.updateAction"
                    :title="$t('Внести изменения')"
                    v-on:submit="emitReload">
      <template v-slot:fields>
        <salary-request-update-fields :form-data="data.updateAction.formData"
                                      :employee-id="data.item.employee.id"
                                      :impl-state="data.item.impl?.state||null"
                                      :request-type="data.item.type">

        </salary-request-update-fields>
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
import SalaryRequestUpdateFields from "@/components/salary/details/info/SalaryRequestUpdateFields.vue";
import EmployeeBaseInfoCard from "@/components/shared/EmployeeBaseInfoCard.vue";


@Component({
  components: {EmployeeBaseInfoCard, SalaryRequestUpdateFields, InDialogForm}
})
export default class SalaryRequestDetailsViewInfo extends Vue {


  @Prop({required: true})
  data!: SalaryDetailsDataContainer;


  formatDateFromDateTime = (v: string | undefined) => DateTimeUtils.formatDateFromIsoDateTime(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : null;

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

  openDeleteDialog() {
    return this.data.deleteAction.openDialog(this.data.item.id, null)
  }

  openUpdateDialog() {
    return this.data.openUpdateDialog(this.data.item);
  }

  emitReload() {
    this.$emit('updated');
  }

  validateIncreaseAndSalary(): boolean {
    return SalaryDetailsDataContainer
        .validateIncreaseAndSalary(this.data.item.employeeInfo.currentSalaryAmount, this.data.item.req.increaseAmount, this.data.item.req.plannedSalaryAmount);
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
