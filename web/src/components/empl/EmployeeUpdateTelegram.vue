<!-- Form to update employee telegram account -->
<template>
  <v-card>
    <v-card-title>{{ $t('Телеграм аккаунт') }}</v-card-title>
    <v-card-subtitle>{{ employee.displayName }}</v-card-subtitle>
    <v-form v-model="formValid" @submit.prevent="update">
      <v-card-text>
        <!--
        //TODO Extract telegram account using UiConstants.extractTelegramAccount
        -->
        <v-text-field ref="telegramField"
                      autofocus
                      :counter="255"
                      :rules="[
                          v=>(!v || v.length <= 255 || $t('Не более N символов', {n:255})),
                          v=>validTelegramAccount(v) || $t('Username без @ и без https://t.me/ или номер телефона с +7 без пробелов')
                      ]"
                      v-model="telegram"
                      :label="$tc('Аккаунт')"
        ></v-text-field>
        <div class="error" v-if="error">{{ error }}</div>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
            type="submit"
            color="primary"
            text
            :disabled="isDisabled()"
        >
          {{ $t('Применить') }}
        </v-btn>
      </v-card-actions>
    </v-form>
  </v-card>
</template>

<script lang="ts">
import Vue from "vue";
import {Prop} from "vue-property-decorator";
import employeeService, {Employee} from "./employee.service";
import Component from "vue-class-component";
import {errorUtils} from "@/components/errors";
import {TelegramUtils} from "@/telegram-utils";


@Component({})
export default class EmployeeUpdateTelegram extends Vue {
  @Prop({required: true})
  employee!: Employee;

  private loading = false;
  private formValid = false;

  private telegram: string | null = null;

  error: string | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    if (this.employee && this.employee.telegram) {
      this.telegram = this.employee.telegram;
    } else {
      this.telegram = null;
    }
  }

  update() {
    this.loading = true;
    employeeService.updateTelegram(this.employee.id, {
      telegram: (this.telegram && this.telegram.trim().length > 0) ? this.telegram.trim() : null
    })
        .then(() => {
          this.employee.telegram = this.telegram ? this.telegram : undefined;
          return this.$emit('close');
        })
        .catch((e: any) => this.error = errorUtils.shortMessage(e))
        .finally(() => {
          this.loading = false;
        });
  }


  private isDisabled() {
    return this.loading ||
        !this.formValid ||
        this.telegram == this.employee.telegram;
  }

  private validTelegramAccount(input: string) {
    return TelegramUtils.isShortTelegramUsernameOrPhoneValid(input);
  }

}
</script>

<style scoped>

</style>
