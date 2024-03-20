<template>
  <v-list two-line dense>
    <template v-for="approval in request.approvals">
      <v-list-item v-bind:key="approval.id">
        <v-list-item-avatar>
          <v-icon :color="approval.state == 2 ? 'success' : approval.state==3? 'error':''">{{
              getIcon(approval.state)
            }}
          </v-icon>
        </v-list-item-avatar>

        <v-list-item-content>
          <v-list-item-title>{{ approval.createdBy.name }} ({{ formatDateTime(approval.createdAt) }})
          </v-list-item-title>
          <v-list-item-subtitle v-if="approval.comment">{{ approval.comment }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </template>
  </v-list>
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
