<template>
  <v-menu
    v-model="menuOpen"
    location="bottom end"
    :close-on-content-click="false"
    width="360"
    max-width="calc(100vw - 24px)"
  >
    <template #activator="{ props }">
      <v-badge
        :content="unreadBadge"
        :model-value="unreadCount > 0"
        color="error"
        offset-x="4"
        offset-y="4"
      >
        <v-btn
          v-bind="props"
          icon="mdi-bell-outline"
          variant="text"
          color="primary"
          :aria-label="t('notifications.title')"
          data-testid="notifications-menu-button"
        />
      </v-badge>
    </template>

    <v-card class="notifications-menu" rounded="lg" elevation="8">
      <v-toolbar density="compact" color="surface" class="notifications-menu__toolbar">
        <v-toolbar-title class="text-subtitle-2 font-weight-semibold">
          {{ t("notifications.title") }}
        </v-toolbar-title>
        <v-spacer />
        <v-btn
          icon="mdi-refresh"
          variant="text"
          size="small"
          :loading="loading"
          :aria-label="t('notifications.refresh')"
          @click="load"
        />
      </v-toolbar>

      <v-divider />

      <div v-if="loading && unreadNotifications.length === 0" class="notifications-menu__state">
        <v-progress-circular indeterminate size="24" color="primary" />
      </div>

      <div v-else-if="error" class="notifications-menu__state text-error">
        {{ error }}
      </div>

      <div v-else-if="unreadNotifications.length === 0" class="notifications-menu__state text-medium-emphasis">
        {{ t("notifications.empty") }}
      </div>

      <v-list
        v-else
        density="comfortable"
        class="notifications-menu__list"
        data-testid="notifications-menu-list"
      >
        <v-list-item
          v-for="notification in unreadNotifications"
          :key="notification.id"
          class="notifications-menu__item"
        >
          <div class="notifications-menu__item-header">
            <div class="notifications-menu__heading">
              <div class="notifications-menu__title">
                {{ notification.title }}
              </div>
              <div class="notifications-menu__meta">
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
                  :loading="acknowledgingIds.has(notification.id)"
                  data-testid="notification-acknowledge-button"
                  @click="acknowledge(notification.id)"
                />
              </template>
            </v-tooltip>
          </div>

          <div class="notifications-menu__body text-body-2">
            <markdown-text-renderer :content="notification.markdownText" />
          </div>
        </v-list-item>
      </v-list>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import { formatDateTime } from "@/lib/datetime";
import {
  acknowledgeNotification,
  fetchMyNotifications,
  fetchMyUnreadNotificationCount,
  type NotificationItem,
} from "@/services/notification.service";

const { t } = useI18n();

const POLLING_INTERVAL_MS = 30_000;

const menuOpen = ref(false);
const loading = ref(false);
const acknowledgingIds = ref(new Set<number>());
const error = ref<string | null>(null);
const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);

const unreadNotifications = computed(() =>
  notifications.value.filter((notification) => !notification.acknowledgedAt),
);

const unreadBadge = computed(() => (unreadCount.value > 99 ? "99+" : unreadCount.value.toString()));

let pollingTimer: ReturnType<typeof window.setInterval> | null = null;

onMounted(() => {
  void loadCount();
  pollingTimer = window.setInterval(() => {
    void poll();
  }, POLLING_INTERVAL_MS);
  document.addEventListener("visibilitychange", handleVisibilityChange);
});

onUnmounted(() => {
  if (pollingTimer !== null) {
    window.clearInterval(pollingTimer);
  }
  document.removeEventListener("visibilitychange", handleVisibilityChange);
});

watch(menuOpen, (open) => {
  if (open) {
    void load();
  }
});

async function load(options: { silent?: boolean } = {}) {
  if (!options.silent) {
    loading.value = true;
  }
  error.value = null;
  try {
    const items = await fetchMyNotifications();
    notifications.value = items;
    unreadCount.value = countUnread(items);
  } catch {
    error.value = t("notifications.loadError");
  } finally {
    if (!options.silent) {
      loading.value = false;
    }
  }
}

async function loadCount() {
  try {
    unreadCount.value = await fetchMyUnreadNotificationCount();
  } catch {
    unreadCount.value = 0;
  }
}

async function acknowledge(notificationId: number) {
  if (acknowledgingIds.value.has(notificationId)) {
    return;
  }

  acknowledgingIds.value = new Set([...acknowledgingIds.value, notificationId]);
  try {
    await acknowledgeNotification(notificationId);
    const acknowledgedAt = new Date().toISOString();
    notifications.value = notifications.value.map((notification) =>
      notification.id === notificationId
        ? { ...notification, acknowledgedAt }
        : notification,
    );
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  } finally {
    const nextIds = new Set(acknowledgingIds.value);
    nextIds.delete(notificationId);
    acknowledgingIds.value = nextIds;
  }
}

async function poll() {
  if (document.hidden) {
    return;
  }
  if (menuOpen.value) {
    await load({ silent: true });
    return;
  }
  await loadCount();
}

function handleVisibilityChange() {
  if (!document.hidden) {
    void poll();
  }
}

function countUnread(items: NotificationItem[]) {
  return items.filter((notification) => !notification.acknowledgedAt).length;
}
</script>

<style scoped>
.notifications-menu {
  max-height: min(520px, calc(100vh - 88px));
  overflow: hidden;
}

.notifications-menu__toolbar {
  min-height: 44px;
}

.notifications-menu__state {
  align-items: center;
  display: flex;
  justify-content: center;
  min-height: 96px;
  padding: 24px;
}

.notifications-menu__list {
  max-height: min(460px, calc(100vh - 148px));
  overflow-y: auto;
  padding: 0;
}

.notifications-menu__item {
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
  padding: 9px 12px 10px;
}

.notifications-menu__item:last-child {
  border-bottom: 0;
}

.notifications-menu__item-header {
  align-items: flex-start;
  display: flex;
  gap: 8px;
  justify-content: space-between;
  min-width: 0;
}

.notifications-menu__heading {
  min-width: 0;
}

.notifications-menu__title {
  font-weight: 600;
  line-height: 1.2;
  white-space: normal;
}

.notifications-menu__meta {
  color: rgba(var(--v-theme-on-surface), 0.58);
  font-size: 12px;
  line-height: 1.2;
  margin-top: 2px;
}

.notifications-menu__body {
  color: rgba(var(--v-theme-on-surface), 0.82);
  line-height: 1.35;
  margin-top: 5px;
  overflow: hidden;
  white-space: normal;
}

.notifications-menu__body :deep(.markdown-body) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.notifications-menu__body :deep(.markdown-body p) {
  margin: 0;
}
</style>
