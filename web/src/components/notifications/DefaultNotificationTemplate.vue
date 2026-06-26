<template>
  <div class="notification-template__body text-body-2">
    <markdown-text-renderer :content="notification.markdownText" />
  </div>
</template>

<script setup lang="ts">
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import type { NotificationItem } from "@/services/notification.service";

defineProps<{
  notification: NotificationItem;
}>();
</script>

<style scoped>
.notification-template__body {
  color: rgba(var(--v-theme-on-surface), 0.82);
  line-height: 1.35;
  margin-top: 5px;
  overflow: hidden;
  white-space: normal;
}

.notification-template__body :deep(.markdown-body) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.notification-template__body :deep(.markdown-body p) {
  margin: 0;
}
</style>
