<template>
  <v-menu>
    <template #activator="{ props }">
      <v-chip variant="outlined" pill v-bind="props">
        <v-icon
          v-if="approval.decision === 'APPROVED'"
          class="approval-chip-icon approved"
          icon="mdi-checkbox-marked-circle"
        />
        <v-icon
          v-else
          class="approval-chip-icon declined"
          icon="mdi-do-not-disturb"
        />
        {{ approval.approverDisplayName }}
        <v-icon
          v-if="approval.outdated"
          class="approval-chip-icon outdated"
          icon="mdi-clock-alert"
        />
      </v-chip>
    </template>

    <v-card width="420">
      <v-list density="compact">
        <v-list-item v-if="approval.outdated">
          <v-list-item-title class="font-weight-medium">
            {{ t("Рассмотрено") }}: {{ formatDateTime(approval.decisionTime) }}
          </v-list-item-title>
          <v-list-item-subtitle>
            {{ t("Внесены изменения") }}: {{ formatDateTime(reportLastUpdateTime) }}
          </v-list-item-subtitle>
        </v-list-item>

        <v-list-item>
          <template #prepend>
            <v-icon
              :icon="
                approval.decision === 'APPROVED'
                  ? 'mdi-checkbox-marked-circle'
                  : 'mdi-alert-circle'
              "
            />
          </template>
          <v-list-item-title>
            {{ t(`APPROVAL_DECISION_ENUM.${approval.decision}`) }}
          </v-list-item-title>
        </v-list-item>

        <v-list-item>
          <template #prepend>
            <v-icon icon="mdi-account" />
          </template>
          <v-list-item-title>{{ approval.approverDisplayName }}</v-list-item-title>
        </v-list-item>

        <v-list-item>
          <template #prepend>
            <v-icon icon="mdi-clock" />
          </template>
          <v-list-item-title>{{ formatDateTime(approval.decisionTime) }}</v-list-item-title>
        </v-list-item>

        <v-list-item>
          <template #prepend>
            <v-icon icon="mdi-pencil" />
          </template>
          <v-list-item-title>{{ approval.comment || "-" }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import type { ApprovalDecision } from "@/services/overtime.service";

defineProps<{
  approval: ApprovalDecision;
  reportLastUpdateTime?: string | null;
}>();

const { t } = useI18n();
</script>

<style scoped>
.approval-chip-icon.approved {
  color: green;
}

.approval-chip-icon.declined {
  color: red;
}

.approval-chip-icon.outdated {
  color: orange;
}
</style>
