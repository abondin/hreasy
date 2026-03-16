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
import AssessmentsView from "@/views/assessment/AssessmentsView.vue";
import EmployeeAssessmentsView from "@/views/assessment/EmployeeAssessmentsView.vue";
import AssessmentDetailsView from "@/views/assessment/AssessmentDetailsView.vue";
import SalaryRequestsView from "@/views/salary/SalaryRequestsView.vue";
import SalaryRequestDetailsView from "@/views/salary/SalaryRequestDetailsView.vue";
import SalaryLatestRequestsView from "@/views/salary/SalaryLatestRequestsView.vue";
import MentorshipView from "@/views/mentorship/MentorshipView.vue";
import MentorshipDetailsView from "@/views/mentorship/MentorshipDetailsView.vue";
import AdminEmployeesTabsView from "@/views/admin/employees/AdminEmployeesTabsView.vue";
import AdminEmployeesListView from "@/views/admin/employees/AdminEmployeesListView.vue";
import AdminEmployeeKidsView from "@/views/admin/employees/AdminEmployeeKidsView.vue";
import AdminEmployeesImportView from "@/views/admin/employees/AdminEmployeesImportView.vue";
import AdminEmployeeKidsImportView from "@/views/admin/employees/AdminEmployeeKidsImportView.vue";
import { useAuthStore } from "@/stores/auth";
import { usePermissions } from "@/lib/permissions";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: "/", redirect: { name: "profile-main" } },
    { path: "/login", name: "login", component: LoginView, meta: { requiresAuth: false } },
    { path: "/profile", name: "profile-main", component: ProfileMainView, meta: { requiresAuth: true } },
    { path: "/employees", name: "employees", component: EmployeesView, meta: { requiresAuth: true } },
    { path: "/vacations", name: "vacations", component: VacationsView, meta: { requiresAuth: true } },
    { path: "/overtimes", name: "overtimes", component: OvertimesView, meta: { requiresAuth: true } },
    { path: "/assessments", name: "assessments", component: AssessmentsView, meta: { requiresAuth: true } },
    {
      path: "/assessments/:employeeId",
      name: "employee-assessments",
      component: EmployeeAssessmentsView,
      meta: { requiresAuth: true },
    },
    {
      path: "/assessments/:employeeId/:assessmentId",
      name: "assessment-details",
      component: AssessmentDetailsView,
      meta: { requiresAuth: true },
    },
    { path: "/salaries/requests", name: "salary-requests", component: SalaryRequestsView, meta: { requiresAuth: true } },
    { path: "/salaries/latest", name: "salary-latest", component: SalaryLatestRequestsView, meta: { requiresAuth: true } },
    {
      path: "/salaries/requests/:period/:requestId",
      name: "salary-request-details",
      component: SalaryRequestDetailsView,
      meta: { requiresAuth: true },
    },
    { path: "/juniors", name: "mentorship", component: MentorshipView, meta: { requiresAuth: true } },
    {
      path: "/juniors/:juniorRegistryId",
      name: "mentorship-details",
      component: MentorshipDetailsView,
      meta: { requiresAuth: true },
    },
    {
      path: "/admin/employees",
      component: AdminEmployeesTabsView,
      meta: { requiresAuth: true },
      children: [
        { path: "", redirect: { name: "admin-employees-list" } },
        { path: "list", name: "admin-employees-list", component: AdminEmployeesListView },
        { path: "kids", name: "admin-employees-kids", component: AdminEmployeeKidsView },
        { path: "import", name: "admin-employees-import", component: AdminEmployeesImportView },
        { path: "kids-import", name: "admin-employees-kids-import", component: AdminEmployeeKidsImportView },
      ],
    },
    { path: "/:pathMatch(.*)*", name: "not-found", component: NotFoundView, meta: { requiresAuth: false } },
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
    const redirect = { name: "login" } as { name: string; query?: Record<string, string> };
    if (to.fullPath && to.fullPath !== "/login") {
      redirect.query = { returnPath: to.fullPath };
    }
    return redirect;
  }

  if (
    (to.name === "assessments" || to.name === "employee-assessments" || to.name === "assessment-details")
    && !permissions.canCreateAssessments()
  ) {
    return { name: "profile-main" };
  }

  if (
    (to.name === "salary-requests" || to.name === "salary-request-details")
    && !permissions.canReportSalaryRequest()
    && !permissions.canAdminSalaryRequests()
  ) {
    return { name: "profile-main" };
  }

  if (to.name === "salary-latest" && !permissions.canAdminSalaryRequests()) {
    return { name: "profile-main" };
  }

  if (
    (to.name === "mentorship" || to.name === "mentorship-details")
    && !permissions.canAccessJuniorsRegistry()
    && !permissions.canAdminJuniorRegistry()
  ) {
    return { name: "profile-main" };
  }

  if (
    String(to.name ?? "").startsWith("admin-employees")
    && !permissions.canAdminEmployees()
  ) {
    return { name: "profile-main" };
  }

  return true;
});

export default router;
