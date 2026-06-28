<template>
  <default-notification-template :notification="notification" />

  <div v-if="employeeId !== null" class="project-transfer-notification__actions">
    <v-btn
      size="small"
      variant="text"
      color="primary"
      prepend-icon="mdi-open-in-new"
      data-testid="notification-open-project-change-dialog"
      @click.stop="openProjectChangeDialog"
    >
      {{ t("notifications.openProjectChangeDialog") }}
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import type { NotificationItem } from "@/services/notification.service";
import DefaultNotificationTemplate from "@/components/notifications/DefaultNotificationTemplate.vue";

const props = defineProps<{
  notification: NotificationItem;
}>();

const emit = defineEmits<{
  (event: "close-menu"): void;
}>();

const { t } = useI18n();
const router = useRouter();

const employeeId = computed(() => {
  if (!props.notification.context) {
    return null;
  }

  try {
    const context = JSON.parse(props.notification.context) as { employeeId?: unknown };
    const parsed = Number(context.employeeId);
    return Number.isInteger(parsed) ? parsed : null;
  } catch {
    return null;
  }
});

function openProjectChangeDialog() {
  if (employeeId.value === null) {
    return;
  }

  emit("close-menu");
  router.push({
    name: "employee-change-current-project",
    params: { employeeId: String(employeeId.value) },
  }).catch(() => undefined);
}
</script>

<style scoped>
.project-transfer-notification__actions {
  margin-top: 6px;
}
</style>
