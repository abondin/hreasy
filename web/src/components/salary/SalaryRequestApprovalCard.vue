<template>
  <span>
    <v-card flat class="d-flex flex-row approval-card" v-for="approval in request.approvals" :key="approval.id">
        <div class="mr-2">
          <v-icon x-small :color="approval.state == 2 ? 'success' : approval.state==3? 'error':''">{{
              getIcon(approval.state)
            }}
          </v-icon>
        </div>
        <div>
          {{ approval.createdBy.name }} <span class="text-caption">({{ formatDateTime(approval.createdAt) }})</span><span v-if="approval.comment">:</span>
          <p class="comment text-caption">{{ approval.comment }}</p>
        </div>
        <div class="ml-2 action">
          <v-btn x-small text icon @click="dataContainer.openApproveDialog(request, approval)">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
    </v-card>
  </span>
</template>
<script lang="ts">
import {Prop, Vue} from "vue-property-decorator";
import {SalaryApprovalState, SalaryIncreaseRequest} from "./salary.service";
import {SalaryRequestDataContainer} from "./salary.data.container";
import Component from "vue-class-component";
import {DateTimeUtils} from "@/components/datetimeutils";

@Component({})
export default class SalaryRequestCard extends Vue {
  @Prop({required: true})
  private request!: SalaryIncreaseRequest;

  @Prop({required: true})
  private dataContainer!: SalaryRequestDataContainer;


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
}
</script>
<style lang="css">
.comment {
  max-width: 300px;
  max-height: 100px;
  text-wrap: balance;
  overflow-y: hidden;
}
.approval-card:hover .action {
  visibility: visible;
}
.approval-card .action {
  visibility: hidden;
}
</style>
