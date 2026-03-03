<template>
  <v-container class="login-container" fluid>
    <v-row class="fill-height" align="center" justify="center">
      <v-col cols="12" sm="10" md="8" lg="6" xl="5">
        <v-card class="login-card overflow-hidden" elevation="10">
          <v-row no-gutters>
            <v-col
              md="5"
              class="d-none d-md-flex flex-column align-center justify-center login-card__hero"
            >
              <v-icon
                icon="mdi-account-group"
                size="56"
                class="mb-4 text-white"
              />
              <div class="text-h5 font-weight-medium text-white">
                {{ t("HR_Easy") }}
              </div>
              <div class="text-body-2 text-white text-white--secondary mt-2">
                {{ t("Вход_в_систему") }}
              </div>
            </v-col>

            <v-col cols="12" md="7">
              <div class="login-card__form pa-6 pa-lg-8">
                <div class="mb-6">
                  <div class="text-h5 font-weight-medium">
                    {{ t("Вход_в_систему") }}
                  </div>
                  <div class="text-body-2 text-medium-emphasis mt-1">
                    {{ t("HR_Easy") }}
                  </div>
                </div>

                <v-progress-linear
                  v-if="loading"
                  indeterminate
                  color="primary"
                  class="mb-4"
                />

                <v-form @submit.prevent="onSubmit">
                <v-text-field
                  v-model="loginField"
                  :label="t('E-mail')"
                  name="login"
                  type="text"
                  prepend-inner-icon="mdi-email-outline"
                  autofocus
                  autocomplete="username"
                  variant="outlined"
                  density="comfortable"
                  :disabled="loading"
                  data-testid="login-input"
                />

                <v-text-field
                  v-model="passwordField"
                  :label="t('Пароль')"
                  name="password"
                    :type="showPassword ? 'text' : 'password'"
                    :append-inner-icon="
                      showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'
                    "
                    prepend-inner-icon="mdi-lock-outline"
                  autocomplete="current-password"
                  variant="outlined"
                  density="comfortable"
                  :disabled="loading"
                  @click:append-inner="togglePasswordVisibility"
                  data-testid="password-input"
                />

                  <v-alert
                    v-if="responseError"
                    type="error"
                    variant="tonal"
                    border="start"
                    class="mb-4"
                  >
                    {{ responseError }}
                  </v-alert>

                  <v-btn
                    color="primary"
                    type="submit"
                    size="large"
                  block
                  :loading="loading"
                  :disabled="isSubmitDisabled"
                  data-testid="login-submit"
                >
                    {{ t("Войти") }}
                  </v-btn>
                </v-form>
              </div>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useAuthStore } from "@/stores/auth";
import { errorUtils } from "@/lib/errors";

const authStore = useAuthStore();
const instance = getCurrentInstance();
const globalProperties = instance?.appContext.config.globalProperties as {
  $router?: {
    push: (location: unknown) => Promise<unknown>;
    currentRoute?: { value?: { query?: Record<string, unknown> } };
  };
  $route?: { query?: Record<string, unknown> };
};
const router = globalProperties?.$router;
const currentRoute = globalProperties?.$route;

const { t } = useI18n();

const loginField = ref("");
const passwordField = ref("");
const responseError = ref("");
const showPassword = ref(false);

const loading = computed(() => authStore.loading);
const isSubmitDisabled = computed(() => {
  return (
    loading.value || !loginField.value.trim() || !passwordField.value.trim()
  );
});

onMounted(() => {
  responseError.value = "";
});

async function onSubmit() {
  responseError.value = "";
  try {
    await authStore.login({
      username: loginField.value,
      password: passwordField.value,
    });
    const routeQuery =
      currentRoute?.query ?? router?.currentRoute?.value?.query;
    const returnPath =
      typeof routeQuery?.returnPath === "string"
        ? routeQuery.returnPath
        : undefined;
    const target = returnPath ? { path: returnPath } : { name: "profile-main" };
    if (router) {
      await router.push(target);
    }
  } catch (error) {
    responseError.value = errorUtils.shortMessage(error);
  }
}

function togglePasswordVisibility() {
  showPassword.value = !showPassword.value;
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #fbe9e7 0%, #ede7f6 40%, #e3f2fd 100%);
}

.login-card {
  border-radius: 18px;
  background-color: rgb(var(--v-theme-surface, 255 255 255));
}

.login-card__hero {
  background: linear-gradient(135deg, #f4511e, #4a148c);
  padding: 48px 32px;
}

.text-white--secondary {
  opacity: 0.72;
}

.login-card__form {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 360px;
}

@media (max-width: 959px) {
  .login-card {
    border-radius: 16px;
  }
}
</style>
