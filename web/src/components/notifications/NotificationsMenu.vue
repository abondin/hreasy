<template>
  <v-menu
    v-model="menuOpen"
    location="bottom end"
    :close-on-content-click="false"
    width="640"
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
        <v-tooltip :text="markAllLabel" location="bottom">
          <template #activator="{ props: tooltipProps }">
            <span v-bind="tooltipProps">
              <v-btn icon="mdi-check-all" variant="text" size="small"
                :aria-label="markAllLabel" :loading="acknowledgingAll"
                :disabled="loading || acknowledgingIds.size > 0 || visibleNotifications.length === 0"
                data-testid="notifications-acknowledge-all" @click="acknowledgeAll" />
            </span>
          </template>
        </v-tooltip>
        <v-btn
          icon="mdi-refresh"
          variant="text"
          size="small"
          :loading="loading"
          :disabled="acknowledgingIds.size > 0"
          :aria-label="t('notifications.refresh')"
          @click="load"
        />
      </v-toolbar>

      <v-divider />

      <v-chip-group v-model="selectedCategory" mandatory show-arrows class="px-3 flex-shrink-0"
        data-testid="notifications-categories">
        <v-chip v-for="category in categories" :key="category.value" :value="category.value" filter size="small">
          {{ category.title }} · {{ category.count }}
        </v-chip>
      </v-chip-group>
      <v-alert v-if="acknowledgeError" type="error" density="compact" closable
        class="mx-3 mb-2 flex-shrink-0" @click:close="acknowledgeError = null">
        {{ acknowledgeError }}
      </v-alert>

      <div v-if="loading && unreadNotifications.length === 0" class="notifications-menu__state">
        <v-progress-circular indeterminate size="24" color="primary" />
      </div>

      <div v-else-if="error" class="notifications-menu__state text-error">
        {{ error }}
      </div>

      <div v-else-if="visibleNotifications.length === 0" class="notifications-menu__state text-medium-emphasis">
        {{ t("notifications.empty") }}
      </div>

      <v-list
        v-else
        density="comfortable"
        class="notifications-menu__list"
        data-testid="notifications-menu-list"
      >
        <notification-list-item
          v-for="notification in visibleNotifications"
          :key="notification.id"
          :notification="notification"
          :acknowledging="acknowledgingAll || acknowledgingIds.has(notification.id)"
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
  acknowledgeNotifications,
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
const selectedCategory = ref("");
const acknowledgingAll = ref(false);
const acknowledgeError = ref<string | null>(null);
let loadVersion = 0;

const unreadNotifications = computed(() =>
  notifications.value.filter((notification) => !notification.acknowledgedAt),
);
const visibleNotifications = computed(() => unreadNotifications.value.filter(item =>
  !selectedCategory.value || item.category === selectedCategory.value,
));
const categories = computed(() => [
  { value: "", title: t("notifications.allTypes"), count: unreadNotifications.value.length },
  ...[...new Set(notifications.value.map(item => item.category))].sort().map(category => ({
    value: category,
    title: t(`notifications.category.${category}`, category),
    count: unreadNotifications.value.filter(item => item.category === category).length,
  })),
]);
const markAllLabel = computed(() => t(selectedCategory.value
  ? "notifications.markCategoryAsRead" : "notifications.markAllAsRead"));

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

watch(selectedCategory, () => {
  acknowledgeError.value = null;
});

async function load(options: { silent?: boolean } = {}) {
  if (acknowledgingIds.value.size) return;
  const version = ++loadVersion;
  if (!options.silent) {
    loading.value = true;
    acknowledgeError.value = null;
  }
  error.value = null;
  try {
    const items = await fetchMyNotifications();
    if (version !== loadVersion) return;
    notifications.value = items;
    unreadCount.value = countUnread(items);
  } catch {
    if (version === loadVersion) error.value = t("notifications.loadError");
  } finally {
    if (!options.silent) {
      loading.value = false;
    }
  }
}

async function loadCount() {
  if (acknowledgingIds.value.size) return;
  const version = loadVersion;
  try {
    const count = await fetchMyUnreadNotificationCount();
    if (version === loadVersion) unreadCount.value = count;
  } catch {
    // Keep the last known count until the next successful poll.
  }
}

async function acknowledge(notificationId: number) {
  if (acknowledgingAll.value || acknowledgingIds.value.has(notificationId)) {
    return;
  }

  acknowledgingIds.value = new Set([...acknowledgingIds.value, notificationId]);
  loadVersion++;
  acknowledgeError.value = null;
  try {
    await acknowledgeNotification(notificationId);
    const acknowledgedAt = new Date().toISOString();
    notifications.value = notifications.value.map((notification) =>
      notification.id === notificationId
        ? { ...notification, acknowledgedAt }
        : notification,
    );
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  } catch {
    acknowledgeError.value = t("notifications.acknowledgeError");
  } finally {
    const nextIds = new Set(acknowledgingIds.value);
    nextIds.delete(notificationId);
    acknowledgingIds.value = nextIds;
  }
}

async function acknowledgeAll() {
  if (loading.value || acknowledgingIds.value.size || !visibleNotifications.value.length) return;
  const ids = visibleNotifications.value.map(item => item.id);
  acknowledgingAll.value = true;
  acknowledgingIds.value = new Set(ids);
  acknowledgeError.value = null;
  loadVersion++;
  try {
    const acknowledged = new Set(await acknowledgeNotifications(ids));
    const acknowledgedAt = new Date().toISOString();
    notifications.value = notifications.value.map(item => acknowledged.has(item.id) ? { ...item, acknowledgedAt } : item);
    unreadCount.value = countUnread(notifications.value);
  } catch {
    acknowledgeError.value = t("notifications.acknowledgeError");
  } finally {
    acknowledgingIds.value = new Set();
    acknowledgingAll.value = false;
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
  display: flex;
  flex-direction: column;
  max-height: min(720px, calc(100vh - 88px));
  overflow: hidden;
}

.notifications-menu__toolbar {
  flex-shrink: 0;
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
  min-height: 0;
  overflow-y: auto;
  padding: 0;
}
</style>
