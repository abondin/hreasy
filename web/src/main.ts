import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import vuetify from "./plugins/vuetify";
import i18n from "./i18n";
import { useAuthStore } from "@/stores/auth";
import { setAuthenticationErrorHandler } from "@/lib/http";
import "./assets/main.scss";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(vuetify);
app.use(i18n);

const authStore = useAuthStore(pinia);

setAuthenticationErrorHandler(() => {
  if (!authStore.isAuthenticated) {
    return;
  }

  authStore.markSessionExpired();

  const currentRoute = router.currentRoute.value;
  if (currentRoute.name === "login") {
    return;
  }

  const redirectQuery =
    currentRoute.fullPath && currentRoute.fullPath !== "/login"
      ? { returnPath: currentRoute.fullPath }
      : undefined;

  void router.replace({
    name: "login",
    query: redirectQuery,
  });
});

app.mount("#app");
