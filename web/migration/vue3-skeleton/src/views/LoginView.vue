<template>
  <div class="d-flex justify-center">
    <v-card
      class="pa-6 skeleton-card"
      elevation="3"
      :loading="loading"
    >
      <v-card-title class="text-h5">
        {{ t('Вход_в_систему') }}
      </v-card-title>
      <v-form @submit.prevent="onSubmit">
        <v-card-text class="text-body-1">
          <v-text-field
            v-model="loginField"
            :label="t('E-mail')"
            name="login"
            type="text"
            prepend-inner-icon="mdi-email-outline"
            autofocus
            autocomplete="username"
            variant="outlined"
          />

          <v-text-field
            v-model="passwordField"
            :label="t('Пароль')"
            name="password"
            type="password"
            prepend-inner-icon="mdi-lock-outline"
            autocomplete="current-password"
            variant="outlined"
          />
        </v-card-text>

        <v-card-text v-if="responseError" class="text-error">
          {{ responseError }}
        </v-card-text>

        <v-card-actions class="justify-end">
          <v-btn
            color="primary"
            type="submit"
            :loading="loading"
          >
            {{ t('Войти') }}
          </v-btn>
        </v-card-actions>
      </v-form>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {useI18n} from 'vue-i18n';
import {useAuthStore} from '@/stores/auth';
import {errorUtils} from '@/lib/errors';

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const {t} = useI18n();

const loginField = ref('');
const passwordField = ref('');
const responseError = ref('');

const loading = computed(() => authStore.loading);

onMounted(() => {
  responseError.value = '';
});

async function onSubmit() {
  responseError.value = '';
  try {
    await authStore.login({
      username: loginField.value,
      password: passwordField.value
    });
    const target = typeof route.query.returnPath === 'string'
      ? {path: route.query.returnPath}
      : {name: 'home'};
    await router.push(target);
  } catch (error) {
    responseError.value = errorUtils.shortMessage(error);
  }
}
</script>
