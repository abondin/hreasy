<template>
  <v-list-item class="notification-list-item">
    <div class="notification-list-item__header">
      <div class="notification-list-item__heading">
        <div class="notification-list-item__title">
          {{ notification.title }}
        </div>
        <div class="notification-list-item__meta">
          {{ formatDateTime(notification.createdAt) }}
        </div>
      </div>
      <v-tooltip :text="t('notifications.markAsRead')" location="start">
        <template #activator="{ props }">
          <v-btn
            v-bind="props"
            icon="mdi-check"
            size="small"
            variant="text"
            color="primary"
            :aria-label="t('notifications.markAsRead')"
            :loading="acknowledging"
            data-testid="notification-acknowledge-button"
            @click="emit('acknowledge')"
          />
        </template>
      </v-tooltip>
    </div>

    <component
      :is="templateComponent"
      :notification="notification"
      @close-menu="emit('close-menu')"
    />
  </v-list-item>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { formatDateTime } from "@/lib/datetime";
import type { NotificationItem } from "@/services/notification.service";
import { resolveNotificationTemplate } from "@/components/notifications/notification-template-resolver";

const props = defineProps<{
  notification: NotificationItem;
  acknowledging: boolean;
}>();

const emit = defineEmits<{
  (event: "acknowledge"): void;
  (event: "close-menu"): void;
}>();

const { t } = useI18n();

const templateComponent = computed(() => resolveNotificationTemplate(props.notification));
</script>

<style scoped>
.notification-list-item {
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
  padding: 9px 12px 10px;
}

.notification-list-item:last-child {
  border-bottom: 0;
}

.notification-list-item__header {
  align-items: flex-start;
  display: flex;
  gap: 8px;
  justify-content: space-between;
  min-width: 0;
}

.notification-list-item__heading {
  min-width: 0;
}

.notification-list-item__title {
  font-weight: 600;
  line-height: 1.2;
  white-space: normal;
}

.notification-list-item__meta {
  color: rgba(var(--v-theme-on-surface), 0.58);
  font-size: 12px;
  line-height: 1.2;
  margin-top: 2px;
}
</style>
