<!--
  Telegram account display with edit trigger.
  Shows current account as link, confirmation chip, and edit button when editing is allowed.
-->
<template>
  <span class="d-inline-flex align-center flex-wrap ga-1">
    <template v-if="account">
      <v-icon size="x-small" color="info" icon="fa:fab fa-telegram" />
      <a
        :href="accountUrl"
        class="d-inline-flex align-center text-info text-decoration-none"
        rel="noopener noreferrer"
        target="_blank"
        data-testid="telegram-link"
      >
        {{ account }}
      </a>
    </template>
    <template v-else>
      <span class="text-medium-emphasis">
        {{ t("Не задан") }}
      </span>
    </template>

    <v-btn
      v-if="canEdit"
      v-bind="actionIconButtonProps"
      icon="mdi-pencil"
      color="medium-emphasis"
      class="ms-1"
      data-testid="open-telegram-dialog"
      @click="emitEdit"
    />

    <v-chip
      v-if="isConfirmed"
      color="success"
      size="x-small"
      variant="tonal"
      density="comfortable"
      class="ms-1"
      data-testid="telegram-confirmed-chip"
    >
      {{ t("Подтвержден") }}
    </v-chip>
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { extractTelegramAccount } from "@/lib/telegram";
import { useAuthStore } from "@/stores/auth";

const props = withDefaults(
  defineProps<{
    employeeId: number;
    account?: string | null;
    confirmedAt?: string | null;
    readOnly?: boolean;
  }>(),
  {
    account: null,
    confirmedAt: null,
    readOnly: true,
  },
);

const emit = defineEmits<{
  (event: "edit"): void;
}>();

const { t } = useI18n();
const authStore = useAuthStore();

const actionIconButtonProps = {
  variant: "text",
  density: "compact",
  size: "x-small",
} as const;

const account = computed(() => props.account?.trim() ?? "");
const isConfirmed = computed(() => Boolean(props.confirmedAt));
const canEdit = computed(
  () => !props.readOnly && props.employeeId === authStore.employeeId,
);
const accountUrl = computed(() => {
  const value = extractTelegramAccount(props.account);
  if (!value) {
    return "https://t.me";
  }
  return `https://t.me/${value}`;
});

function emitEdit() {
  emit("edit");
}
</script>
