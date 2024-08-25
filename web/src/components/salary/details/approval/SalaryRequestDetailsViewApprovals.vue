<!-- Detailed information for selected salary request or bonus-->
<template>
  <v-card class="mt-5" v-if="data">
    <v-card-title class="text-h5">
      {{ $t('Согласования и комментарии') }}
      <v-spacer></v-spacer>
      <span v-if="approveAllowed()">
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" icon @click="approve()" :disabled="commentActionDisabled()">
            <v-icon color="success">mdi-checkbox-marked-circle</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Согласовать') }}</span>
      </v-tooltip>
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" text icon @click="decline()" :disabled="commentActionDisabled()">
            <v-icon color="error">mdi-alert-circle</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Отклонить') }}</span>
      </v-tooltip>
      <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" icon @click="comment()" :disabled="commentActionDisabled()">
            <v-icon>mdi-comment</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Добавить комментарий') }}</span>
      </v-tooltip>

        </span>
    </v-card-title>
    <v-card-text>
      <div class="row">
        <!--<editor-fold desc="Согласования и коммантерии">-->
        <div class="col-12" v-if="data.item.approvals?.length>0">
          <salary-request-approval-card :request="data.item"
                                        :data-container="data"></salary-request-approval-card>
        </div>
        <div class="col-12" v-else>{{ $t('Добавьте первый комментарий или согласование') }}</div>
        <!--</editor-fold>-->
      </div>
    </v-card-text>
    <in-dialog-form size="lg" form-ref="approveForm" :data="data.approveAction"
                    :title="approveDialogFormTitle"
                    v-on:submit="emitReload()">
      <template v-slot:fields>
        <v-card-text v-if="data.approveAction.formData.approvalId">
          {{ $t('Вы уверены, что хотите удалить согласование/комментарий?') }}
        </v-card-text>
        <salary-request-approval-form-fields v-else
                                             :formData="data.approveAction.formData"></salary-request-approval-form-fields>
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
import {SalaryApprovalState} from "@/components/salary/salary.service";
import permissionService from "@/store/modules/permission.service";
import SalaryRequestApprovalFormFields from "@/components/salary/details/approval/SalaryRequestApprovalFormFields.vue";
import InDialogForm from "@/components/shared/forms/InDialogForm.vue";
import SalaryRequestApprovalCard from "@/components/salary/details/approval/SalaryRequestApprovalCard.vue";


@Component({
  components: {SalaryRequestApprovalCard, InDialogForm, SalaryRequestApprovalFormFields}
})
export default class SalaryRequestDetailsViewImplementation extends Vue {


  @Prop({required: true})
  data!: SalaryDetailsDataContainer;

  private approveAllowed() {
    return permissionService.canApproveSalaryRequest(this.data.item?.budgetBusinessAccount?.id);
  }

  private commentActionDisabled() {
    return this.data.approveAction.loading || this.data.periodClosed;
  }


  private approve() {
    this.data.openApproveDialog(this.data.item, null, SalaryApprovalState.APPROVE);
  }

  private decline() {
    this.data.openApproveDialog(this.data.item, null, SalaryApprovalState.DECLINE);
  }

  private comment() {
    this.data.openApproveDialog(this.data.item, null, SalaryApprovalState.COMMENT);
  }

  private emitReload() {
    this.$emit('updated');
  }

  private approveDialogFormTitle() {
    if (!this.data) {
      return '-';
    }
    return this.data.approveAction.formData?.approvalId ? this.$tc('Удалить согласование/комментарий')
        : this.$tc('Добавить согласование/комментарий');
  }

  formatDateFromDateTime = (v: string | undefined) => DateTimeUtils.formatDateFromIsoDateTime(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => Number(v).toLocaleString();

  formatPeriod = (v: number) => ReportPeriod.fromPeriodId(v);

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
