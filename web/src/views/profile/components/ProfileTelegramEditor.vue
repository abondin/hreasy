<!--
  Telegram account display with edit trigger.
  Shows current account as link, confirmation chip, and edit button when editing is allowed.
-->
<template>
  <!--<editor-fold desc="Telegram display with edit button">-->
  <div class="d-inline-flex align-center ga-1">
    <template v-if="account">
      <v-icon size="small" color="info" icon="fa:fab fa-telegram" />
      <a
        :href="accountUrl"
        class="telegram-editor__link text-info d-inline-flex align-center"
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
      class="ml-1"
      color="medium-emphasis"
      data-testid="open-telegram-dialog"
      @click="emitEdit"
    />

    <v-chip
      v-if="isConfirmed"
      color="success"
      size="x-small"
      variant="tonal"
      class="ms-1"
      data-testid="telegram-confirmed-chip"
    >
      {{ t("Подтвержден") }}
    </v-chip>
  </div>
  <!-- </editor-fold> -->
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { extractTelegramAccount } from "@/lib/telegram";

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

<style scoped>
.telegram-editor__link {
  text-decoration: none;
}

.telegram-editor__link:hover {
  text-decoration: underline;
}
</style>
