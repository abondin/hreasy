import {
  createRouter,
  createWebHistory,
  type RouteLocationNormalized,
} from "vue-router";
import NotFoundView from "@/views/NotFoundView.vue";
import LoginView from "@/views/LoginView.vue";
import ProfileMainView from "@/views/profile/ProfileMainView.vue";
import EmployeesView from "@/views/employees/EmployeesView.vue";
import VacationsView from "@/views/vacations/VacationsView.vue";
import OvertimesView from "@/views/overtimes/OvertimesView.vue";
import MentorshipView from "@/views/mentorship/MentorshipView.vue";
import { useAuthStore } from "@/stores/auth";
import { usePermissions } from "@/lib/permissions";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: { name: "profile-main" },
    },
    {
      path: "/login",
      name: "login",
      component: LoginView,
      meta: { requiresAuth: false },
    },
    {
      path: "/profile",
      name: "profile-main",
      component: ProfileMainView,
      meta: { requiresAuth: true },
    },
    {
      path: "/employees",
      name: "employees",
      component: EmployeesView,
      meta: { requiresAuth: true },
    },
    {
      path: "/vacations",
      name: "vacations",
      component: VacationsView,
      meta: { requiresAuth: true },
    },
    {
      path: "/overtimes",
      name: "overtimes",
      component: OvertimesView,
      meta: { requiresAuth: true },
    },
    {
      path: "/juniors",
      name: "mentorship",
      component: MentorshipView,
      meta: { requiresAuth: true },
    },
    {
      path: "/:pathMatch(.*)*",
      name: "not-found",
      component: NotFoundView,
      meta: { requiresAuth: false },
    },
  ],
});

router.beforeEach(async (to: RouteLocationNormalized) => {
  const authStore = useAuthStore();
  const permissions = usePermissions();

  try {
    await authStore.fetchCurrentUser();
  } catch (error) {
    console.error(error);
    authStore.clearAuth();
  }

  const requiresAuth = to.meta.requiresAuth !== false;

  if (!requiresAuth) {
    if (to.name === "login" && authStore.isAuthenticated) {
      return { name: "profile-main" };
    }
    return true;
  }

  if (!authStore.isAuthenticated) {
    const redirect = { name: "login" } as {
      name: string;
      query?: Record<string, string>;
    };
    if (to.fullPath && to.fullPath !== "/login") {
      redirect.query = { returnPath: to.fullPath };
    }
    return redirect;
  }

  if (
    to.name === "mentorship"
    && !permissions.canAccessJuniorsRegistry()
    && !permissions.canAdminJuniorRegistry()
  ) {
    return { name: "profile-main" };
  }

  return true;
});

export default router;
