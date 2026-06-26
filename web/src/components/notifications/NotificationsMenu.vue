<template>
  <v-menu
    v-model="menuOpen"
    location="bottom end"
    :close-on-content-click="false"
    width="440"
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
        <notification-list-item
          v-for="notification in unreadNotifications"
          :key="notification.id"
          :notification="notification"
          :acknowledging="acknowledgingIds.has(notification.id)"
          @acknowledge="acknowledge(notification.id)"
          @close-menu="menuOpen = false"
        />
      </v-list>
    </v-card>
  </v-menu>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import NotificationListItem from "@/components/notifications/NotificationListItem.vue";
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
</style>
