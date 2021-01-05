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
          <v-icon v-if="approval.decision=='APPROVED'"
                  class="approved">mdi-checkbox-marked-circle
          </v-icon>
          <v-icon v-if="approval.decision=='DECLINED'"
                  class="declined">mdi-do-not-disturb
          </v-icon>
        {{ approval.approverDisplayName }}
        <v-icon v-if="approval.outdated" class="outdated">mdi-clock-alert</v-icon>
      </v-chip>
    </template>
    <v-card width="400">
      <v-list v-if="approval.outdated">
        <v-list-item @click="() => {}">
          <dl>
            <dt>{{ $t('Рассмотрено') }}:</dt><dd>{{formatDateTimeShort(approval.decisionTime)}}</dd>
            <dt>{{ $t('Внесены изменения') }}:</dt><dd class="error--text">{{formatDateTimeShort(reportLastUpdateTime)}}</dd>
          </dl>
        </v-list-item>
      </v-list>
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

  @Prop({required: false})
  reportLastUpdateTime!: String;


  private formatDateTime(date: Date): string | undefined {
    return OvertimeUtils.formatDateTime(date);
  }

  private formatDateTimeShort(date: Date): string | undefined {
    return OvertimeUtils.formatDateTimeShort(date);
  }
}
</script>

<style scoped lang="scss">
.v-chip.v-chip--outlined .v-icon.approved {
  color: green;
}

.v-chip.v-chip--outlined .v-icon.declined {
  color: red
}

.v-chip.v-chip--outlined .v-icon.outdated {
  color: orange;
}
</style>
