import { createRouter, createWebHashHistory } from "vue-router";
import TableSandboxView from "@/e2e-harness/views/TableSandboxView.vue";
import TableSandboxEchoView from "@/e2e-harness/views/TableSandboxEchoView.vue";
import TableSandboxPlainView from "@/e2e-harness/views/TableSandboxPlainView.vue";
import TableSandboxVacationsLikeView from "@/e2e-harness/views/TableSandboxVacationsLikeView.vue";
import TableSandboxOvertimesLikeView from "@/e2e-harness/views/TableSandboxOvertimesLikeView.vue";

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: { name: "table-sandbox" } },
    {
      path: "/tables",
      name: "table-sandbox",
      component: TableSandboxView,
    },
    {
      path: "/tables/plain",
      name: "table-sandbox-plain",
      component: TableSandboxPlainView,
    },
    {
      path: "/tables/vacations-like",
      name: "table-sandbox-vacations-like",
      component: TableSandboxVacationsLikeView,
    },
    {
      path: "/tables/overtimes-like",
      name: "table-sandbox-overtimes-like",
      component: TableSandboxOvertimesLikeView,
    },
    {
      path: "/tables/echo",
      name: "table-sandbox-echo",
      component: TableSandboxEchoView,
    },
  ],
});

export default router;
