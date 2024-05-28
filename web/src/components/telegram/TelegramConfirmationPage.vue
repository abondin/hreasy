<template>
  <v-container>
    <v-card>
      <v-card-title>{{ $t('Подтверждение Telegram Account') }}</v-card-title>
      <v-card-text v-if="loading">{{ $t('Отправляем ваш запрос на сервер') }}</v-card-text>
      <v-card-text>
        <v-alert v-if="error" class="error">{{ error }}</v-alert>
        <p v-else>
          {{ $t('Поздравляем. Теперь вы можете пользоваться Telegram Bot HR Easy!') }}
        </p>
        <p>
          <router-link to="/">{{ $t('Вернуться на главную') }}</router-link>
        </p>
      </v-card-text>
    </v-card>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {Prop} from "vue-property-decorator";
import telegramConfirmationService from "@/components/telegram/telegram-confirmation.service";
import {errorUtils} from "@/components/errors";


@Component({
  components: {}
})
export default class TelegramConfirmationPage extends Vue {
  loading = false;
  error: string | null = null;
  @Prop({required: true})
  employeeId!: number;
  @Prop({required: true})
  telegramAccount!: string;

  @Prop({required: true})
  confirmationCode!: string;

  /**
   * Lifecycle hook
   */
  created() {
    this.loading = true;
    this.error = null
    return telegramConfirmationService
        .confirmTelegramAccount(this.employeeId, this.telegramAccount, this.confirmationCode)
        .catch(err => this.error = errorUtils.shortMessage(err))
        .finally(() => this.loading = false);
  }
}
</script>

<style scoped lang="scss">
</style>
