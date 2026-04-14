import { createApp } from "vue";
import vuetify from "@/plugins/vuetify";
import i18n from "@/i18n";
import router from "@/e2e-harness/router";
import HarnessApp from "@/e2e-harness/HarnessApp.vue";
import "@/assets/main.scss";

createApp(HarnessApp)
  .use(router)
  .use(vuetify)
  .use(i18n)
  .mount("#app");
