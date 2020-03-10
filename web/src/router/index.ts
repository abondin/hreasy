import Vue from 'vue'
import VueRouter, {NavigationGuard} from 'vue-router'
import Login from "@/components/login/Login.vue";
import Employees from "@/components/empl/Employees.vue";
import store from '@/store';
import logger from "@/logger";
import VacationsList from "@/components/vacations/VacationsList.vue";

Vue.use(VueRouter)

const routes = [
    {path: "/", redirect: '/employees'},
    {path: "/login", component: Login},
    {path: "/employees", component: Employees},
    {path: "/vacations", component: VacationsList}
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
