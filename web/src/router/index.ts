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

Vue.use(VueRouter)

const routes = [
    {path: "/", redirect: '/profile/main'},
    {path: "/login", component: Login},
    {name: "employees", path: "/employees", component: Employees},
    {name: 'employeeProfile', path: "/profile/main", component: EmployeeProfile, props: true},
    {path: "/vacations", component: VacationsList},
    {path: "/overtimes", component: AllOvertimes},
    {path: "/assessments", component: AssessmentShortList},
    {path: "/assessments/:employeeId", component: EmployeeAssessmentProfile, props: true},
    {path: "/assessments/:employeeId/:assessmentId", component: AssessmentDetailedVue, props: true},
    {path: "/admin/projects", component: AdminProjects},
    {path: "/admin/users", component: AdminUsers},
    {path: "/admin/ba", component: AdminBusinessAccounts},
    {path: "/admin/ba/:businessAccountId", component: AdminBusinessAccountDetails, props: true},
    {path: "/admin/articles", component: AdminArticlesList},
    {path: "/admin/employees", component: AdminEmployees},
    {path: "/admin/employees/kids", component: AdminEmployeeKids},
    {
        path: "/admin/dicts",
        component: DictAdminMain,
        children: [
            {path: '', redirect:{name:"admin_dict_departments"}},
            {name: "admin_dict_departments", path: "departments", component: DictAdminDepartments},
            {path: "positions", component: DictAdminPositions},
            {path: "office_locations", component: DictAdminOfficeLocations},
            {path: "levels", component: DictAdminLevels}
        ]
    }
]
const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

const authGuard: NavigationGuard = (to, from, next) => {
    if (to.path == '/login') {
        next();
        return;
    }
    logger.debug("authGuard: Validate route", from, to);
    return store.dispatch("auth/getCurrentUser").then(currentUser => {
        if (currentUser) {
            logger.debug("authGuard: Current user ", currentUser);
            next();
        } else {
            logger.debug("authGuard: Not logged in. Redirect to login page ");
            next('/login')
        }
        return Promise.resolve();
    })
};

router.beforeEach(authGuard);

export default router
