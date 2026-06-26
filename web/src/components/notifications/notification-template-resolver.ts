import type { Component } from "vue";
import DefaultNotificationTemplate from "@/components/notifications/DefaultNotificationTemplate.vue";
import ProjectTransferNotificationTemplate from "@/components/notifications/ProjectTransferNotificationTemplate.vue";
import type { NotificationItem } from "@/services/notification.service";

const templatesByCategory: Record<string, Component> = {
  project_transfer: ProjectTransferNotificationTemplate,
};

export function resolveNotificationTemplate(notification: NotificationItem): Component {
  return templatesByCategory[notification.category] ?? DefaultNotificationTemplate;
}
