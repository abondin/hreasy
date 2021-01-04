<!--
Expandable chip to show overtime report approve or decline decision
 -->
<template>

  <v-menu
      v-model="menu"
      bottom
      right
      transition="scale-transition"
      origin="top left"
  >
    <template v-slot:activator="{ on }">
      <v-chip outlined pill v-on="on">
        <v-avatar left>
          <v-icon v-if="approval.decision=='APPROVED'"
                  class="success" style="color: white">mdi-checkbox-marked-circle
          </v-icon>
          <v-icon v-if="approval.decision=='DECLINED'"
                  class="error" style="color: white">mdi-alert-circle
          </v-icon>
        </v-avatar>
        {{ approval.approverDisplayName }}
      </v-chip>
    </template>
    <v-card width="400">
      <v-list>
        <v-list-item @click="() => {}">
          <v-list-item-action>
            <v-icon>{{ approval.decision == 'APPROVED' ? 'mdi-checkbox-marked-circle' : 'mdi-alert-circle' }}</v-icon>
          </v-list-item-action>
          <v-list-item-subtitle>{{ $t('APPROVAL_DECISION_ENUM.' + approval.decision) }}</v-list-item-subtitle>
        </v-list-item>
        <v-list-item @click="() => {}">
          <v-list-item-action>
            <v-icon>mdi-account</v-icon>
          </v-list-item-action>
          <v-list-item-subtitle>{{ approval.approverDisplayName }}</v-list-item-subtitle>
        </v-list-item>
        <v-list-item @click="() => {}">
          <v-list-item-action>
            <v-icon>mdi-clock</v-icon>
          </v-list-item-action>
          <v-list-item-subtitle>{{ formatDateTime(approval.decisionTime) }}</v-list-item-subtitle>
        </v-list-item>
        <v-list-item @click="() => {}">
          <v-list-item-action>
            <v-icon>mdi-pencil</v-icon>
          </v-list-item-action>
          {{ approval.comment }}
        </v-list-item>
      </v-list>
    </v-card>
  </v-menu>

</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop} from "vue-property-decorator";
import {ApprovalDecision, OvertimeUtils} from "@/components/overtimes/overtime.service";


@Component
export default class OvertimeApprovalChip extends Vue {

  private menu = false;

  @Prop({required: true})
  approval!: ApprovalDecision;


  private formatDateTime(date: Date): string | undefined {
    return OvertimeUtils.formatDateTime(date);
  }
}
</script>

<style scoped>

</style>
