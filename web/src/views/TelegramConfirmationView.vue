<template>
  <v-container class="py-8" fluid data-testid="telegram-confirmation-view">
    <v-row justify="center">
      <v-col cols="12" sm="10" md="8" lg="6">
        <v-card>
          <v-card-title>{{ t("Подтверждение Telegram Account") }}</v-card-title>
          <v-card-text v-if="loading">{{ t("Отправляем ваш запрос на сервер") }}</v-card-text>
          <v-card-text v-else>
            <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
              {{ error }}
            </v-alert>
            <p v-else>
              {{ t("Поздравляем. Теперь вы можете пользоваться Telegram Bot HR Easy!") }}
            </p>
            <p class="mb-0">
              <router-link to="/">{{ t("Вернуться на главную") }}</router-link>
            </p>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { errorUtils } from "@/lib/errors";
import { confirmTelegramAccount } from "@/services/telegram-confirmation.service";

const { t } = useI18n();
const route = useRoute();

const loading = ref(false);
const error = ref("");

const employeeId = computed(() => Number(route.params.employeeId));
const telegramAccount = computed(() => String(route.params.telegramAccount ?? ""));
const confirmationCode = computed(() => String(route.params.confirmationCode ?? ""));

watch([employeeId, telegramAccount, confirmationCode], () => {
  confirm().catch(() => undefined);
}, { immediate: true });

async function confirm(): Promise<void> {
  if (!Number.isInteger(employeeId.value) || !telegramAccount.value || !confirmationCode.value) {
    error.value = t("Недостаточно прав");
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    await confirmTelegramAccount(
      employeeId.value,
      telegramAccount.value,
      confirmationCode.value,
    );
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}
</script>
