import {createRouter, createWebHistory} from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import LegacyStatusView from '@/views/LegacyStatusView.vue';
import NotFoundView from '@/views/NotFoundView.vue';
import LoginView from '@/views/LoginView.vue';
import ProfileMainView from '@/views/profile/ProfileMainView.vue';
import {useAuthStore} from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {requiresAuth: false}
    },
    {
      path: '/legacy-status',
      name: 'legacy-status',
      component: LegacyStatusView
    },
    {
      path: '/profile',
      name: 'profile-main',
      component: ProfileMainView,
      meta: {requiresAuth: true}
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
      meta: {requiresAuth: false}
    }
  ]
});

router.beforeEach(async to => {
  const authStore = useAuthStore();

  try {
    await authStore.fetchCurrentUser();
  } catch (error) {
    console.error(error);
    authStore.clearAuth();
  }

  const requiresAuth = to.meta.requiresAuth !== false;

  if (!requiresAuth) {
    if (to.name === 'login' && authStore.isAuthenticated) {
      return {name: 'home'};
    }
    return true;
  }

  if (!authStore.isAuthenticated) {
    const redirect = {name: 'login'} as {name: string; query?: Record<string, string>};
    if (to.fullPath && to.fullPath !== '/login') {
      redirect.query = {returnPath: to.fullPath};
    }
    return redirect;
  }

  return true;
});

export default router;
