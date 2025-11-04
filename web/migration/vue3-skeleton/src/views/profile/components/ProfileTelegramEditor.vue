<!--
  Telegram account display with edit trigger.
  Shows current account as link, confirmation chip, and edit button when editing is allowed.
  -->
<template>
  <!--<editor-fold desc="Telegram display with edit button">-->
  <div class="telegram-editor">
    <template v-if="account">
      <a
        :href="accountUrl"
        class="telegram-editor__link"
        rel="noopener noreferrer"
        target="_blank"
        data-testid="telegram-link"
      >
        {{ account }}
      </a>
      <button
        v-if="canEdit"
        type="button"
        class="telegram-editor__icon-button"
        data-testid="open-telegram-dialog"
        @click="emitEdit"
      >
        <v-icon icon="mdi-pencil" size="14" />
      </button>
    </template>
    <template v-else>
      <span class="text-medium-emphasis">
        {{ t("Не задан") }}
      </span>
      <button
        v-if="canEdit"
        type="button"
        class="telegram-editor__icon-button"
        data-testid="open-telegram-dialog"
        @click="emitEdit"
      >
        <v-icon icon="mdi-pencil" size="14" />
      </button>
    </template>

    <v-chip
      v-if="isConfirmed"
      color="success"
      size="x-small"
      variant="tonal"
      class="telegram-editor__chip"
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
.telegram-editor {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.telegram-editor__link {
  color: rgb(33, 150, 243);
  text-decoration: none;
}

.telegram-editor__link:hover {
  text-decoration: underline;
}

.telegram-editor__icon-button {
  border: none;
  background: transparent;
  padding: 0;
  margin: 0;
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(0, 0, 0, 0.56);
}

.telegram-editor__chip {
  margin-left: 4px;
  height: 20px;
  font-size: 11px;
  padding: 0 4px;
}
</style>
