<template>
  <span>
    <v-container flat v-for="approval in request.approvals" :key="approval.id">
      <v-row align-content="center">
        <v-col cols="auto">
          <v-icon :color="approval.state == 2 ? 'success' : approval.state==3? 'error':''">{{
              getIcon(approval.state)
            }}
          </v-icon>
        </v-col>
        <v-col>
          <div>{{ approval.createdBy.name }} <span class="text-caption">({{
              formatDateTime(approval.createdAt)
            }})</span></div>
          <div v-if="approval.comment">
              {{ approval.comment }}
          </div>
        </v-col>
        <v-col cols="auto">
          <v-tooltip bottom>
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-btn v-bind="tattrs" v-on="ton" x-small text icon
                 @click="dataContainer.openApproveDialog(request, approval)"
                 v-if="approvalActionsAllowed(approval, request)">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('Удалить комментарий') }}</span>
      </v-tooltip>
        </v-col>
      </v-row>
      <v-divider></v-divider>
    </v-container>
  </span>
</template>
<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import {SalaryApprovalState, SalaryIncreaseRequest, SalaryRequestApproval} from "../../salary.service";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";
import {SalaryDetailsDataContainer} from "@/components/salary/details/salary-details.data.container";

@Component({})
export default class SalaryRequestApprovalCard extends Vue {
  @Prop({required: true})
  private request!: SalaryIncreaseRequest;

  @Prop({required: true})
  private dataContainer!: SalaryDetailsDataContainer;


  getIcon(state: SalaryApprovalState) {
    switch (state) {
      case SalaryApprovalState.APPROVE:
        return 'mdi-checkbox-marked-circle';
      case SalaryApprovalState.DECLINE:
        return 'mdi-alert-circle';
      case SalaryApprovalState.COMMENT:
        return 'mdi-comment';
    }
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

  private approvalActionsAllowed(approval: SalaryRequestApproval, request: SalaryIncreaseRequest) {
    return !this.dataContainer.periodClosed || Boolean(approval?.state == SalaryApprovalState.COMMENT)
  }
}
</script>
