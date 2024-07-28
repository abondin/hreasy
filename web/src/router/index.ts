import Vue from 'vue'
import VueRouter, {NavigationGuard} from 'vue-router'
import Login from "@/components/login/Login.vue";
import Employees from "@/components/empl/Employees.vue";
import store from '@/store';
import logger from "@/logger";
import VacationsList from "@/components/vacations/VacationsList.vue";
import EmployeeProfile from "@/components/empl/EmployeeProfile.vue";
import AllOvertimes from "@/components/overtimes/AllOvertimes.vue";
import AdminProjects from "@/components/admin/project/AdminProjects.vue";
import AdminUsers from "@/components/admin/users/AdminUsers.vue";
import AdminBusinessAccounts from "@/components/admin/business_account/AdminBusinessAccounts.vue";
import AdminBusinessAccountDetails from "@/components/admin/business_account/AdminBusinessAccountDetails.vue";
import AdminArticlesList from "@/components/admin/article/AdminArticlesList.vue";
import AdminEmployees from "@/components/admin/employee/AdminEmployees.vue";
import AssessmentShortList from "@/components/assessment/AssessmentShortList.vue";
import EmployeeAssessmentProfile from "@/components/assessment/EmployeeAssessmentProfile.vue";
import AssessmentDetailedVue from "@/components/assessment/AssessmentDetailedVue.vue";
import AdminEmployeeKids from "@/components/admin/employee/AdminEmployeeKids.vue";
import DictAdminMain from "@/components/admin/dict/DictAdminMain.vue";
import DictAdminLevels from "@/components/admin/dict/DictAdminLevels.vue";
import DictAdminDepartments from "@/components/admin/dict/DictAdminDepartments.vue";
import DictAdminPositions from "@/components/admin/dict/DictAdminPositions.vue";
import DictAdminOfficeLocations from "@/components/admin/dict/DictAdminOfficeLocations.vue";
import PageNotFoundComponent from "@/components/PageNotFoundComponent.vue";
import AdminEmployeeAndKidsTabs from "@/components/admin/employee/AdminEmployeeAndKidsTabs.vue";
import AdminManagers from "@/components/admin/manager/AdminManagers.vue";
import AdminProjectDetails from "@/components/admin/project/AdminProjectDetails.vue";
import AdminEmployeesImportWorkflowComponent from "@/components/admin/employee/imp/AdminEmployeesImportWorkflow.vue";
import TimesheetTableComponent from "@/components/ts/TimesheetTableComponent.vue";
import SalaryRequests from "@/components/salary/SalaryRequests.vue";
import DictAdminOrganizations from "@/components/admin/dict/DictAdminOrganizations.vue";
import AdminEmployeesKidsImportWorkflowComponent
    from "@/components/admin/employee/imp/AdminEmployeesKidsImportWorkflow.vue";
import TelegramConfirmationPage from "@/components/telegram/TelegramConfirmationPage.vue";
import JuniorRegistry from "@/components/udr/JuniorRegistry.vue";

Vue.use(VueRouter)

const routes = [
    {path: "/", redirect: '/profile/main'},
    {path: "/404", component: PageNotFoundComponent},
    {path: "*", redirect: "/404"},
    {path: "/login", component: Login},
    {
        name: "TelegramConfirmationPage",
        path: "/telegram/confirm/:employeeId/:telegramAccount/:confirmationCode",
        component: TelegramConfirmationPage,
        props: true
    },
    {name: "employees", path: "/employees", component: Employees},
    {name: 'employeeProfile', path: "/profile/main", component: EmployeeProfile, props: true},
    {path: "/vacations", component: VacationsList},
    {path: "/overtimes", component: AllOvertimes},
    {path: "/assessments", component: AssessmentShortList},
    {path: "/assessments/:employeeId", component: EmployeeAssessmentProfile, props: true},
    {
        path: "/assessments/:employeeId/:assessmentId",
        component: AssessmentDetailedVue,
        name: 'AssessmentDetailedVue',
        props: true
    },
    {path: "/salaries/requests", component: SalaryRequests, name: "salariesRequests"},
    {path: "/timesheet", component: TimesheetTableComponent},
    {path: "/juniors", component: JuniorRegistry},
    {path: "/admin/projects", component: AdminProjects},
    {path: "/admin/projects/:projectId", component: AdminProjectDetails, props: true},
    {path: "/admin/users", component: AdminUsers},
    {path: "/admin/ba", component: AdminBusinessAccounts},
    {path: "/admin/ba/:businessAccountId", component: AdminBusinessAccountDetails, props: true},
    {path: "/admin/managers", component: AdminManagers},
    {path: "/admin/articles", component: AdminArticlesList},
    {
        path: "/admin/employees",
        component: AdminEmployeeAndKidsTabs,
        children: [
            {path: '', redirect: 'list'},
            {path: 'list', component: AdminEmployees},
            {path: 'kids', component: AdminEmployeeKids},
            {path: 'import', component: AdminEmployeesImportWorkflowComponent},
            {path: 'kids-import', component: AdminEmployeesKidsImportWorkflowComponent}
        ]
    },
    {
        path: "/admin/dicts",
        component: DictAdminMain,
        children: [
            {path: '', redirect: {name: "admin_dict_organizations"}},
            {name: "admin_dict_organizations", path: "organizations", component: DictAdminOrganizations},
            {name: "admin_dict_departments", path: "departments", component: DictAdminDepartments},
            {path: "positions", component: DictAdminPositions},
            {path: "office_locations", component: DictAdminOfficeLocations},
            {path: "levels", component: DictAdminLevels}
        ]
    },
]
const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

const authGuard: NavigationGuard = (to, from, next) => {
    if (to.path == '/login' || to.name == 'TelegramConfirmationPage') {
        next();
        return;
    }
    logger.debug("authGuard: Validate route", from, to);
    return store.dispatch("auth/getCurrentUser").then(currentUser => {
        if (currentUser) {
            logger.debug("authGuard: Current user ", currentUser);
            next();
        } else {
            logger.debug("authGuard: Not logged in. Redirect to login page");
            if (to && from.path) {
                next({path: '/login', query: {'returnPath': to.path}});
            } else {
                next({path: 'login'});
            }
        }
        return Promise.resolve();
    })
};

router.beforeEach(authGuard);

export default router
