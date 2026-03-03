import { defineStore } from "pinia";
import type {
  CurrentUser,
  EmployeeShortInfo,
  LoginRequest,
} from "@/services/auth.service";
import {
  currentUser as fetchCurrentUserApi,
  login as loginApi,
  logout as logoutApi,
} from "@/services/auth.service";
import { AuthenticationError } from "@/lib/errors";

export interface SecurityInfo {
  username: string;
  authorities: string[];
  employeeId: number;
  accessibleBas: number[];
}

interface AuthState {
  currentUser: CurrentUser | null;
  userLoaded: boolean;
  loading: boolean;
}

function mapEmployee(info: EmployeeShortInfo) {
  return {
    employeeId: info.employeeId,
    accessibleBas: info.accessibleBas,
  };
}

export const useAuthStore = defineStore("auth", {
  state: (): AuthState => ({
    currentUser: null,
    userLoaded: false,
    loading: false,
  }),
  getters: {
    username(state): string | undefined {
      return state.currentUser?.username;
    },
    displayName(state): string {
      return state.currentUser?.username ?? "anonymous";
    },
    employeeId(state): number | null {
      return state.currentUser?.employee.employeeId ?? null;
    },
    securityInfo(state): SecurityInfo | null {
      const user = state.currentUser;
      if (!user) {
        return null;
      }
      return {
        username: user.username,
        authorities: user.authorities,
        employeeId: user.employee.employeeId,
        accessibleBas: user.employee.accessibleBas,
      };
    },
    isAuthenticated(state): boolean {
      return !!state.currentUser;
    },
  },
  actions: {
    clearAuth() {
      this.currentUser = null;
      this.userLoaded = false;
    },
    setCurrentUser(user: CurrentUser) {
      this.currentUser = {
        ...user,
        employee: mapEmployee(user.employee),
      };
      this.userLoaded = true;
    },
    async login(request: LoginRequest) {
      this.loading = true;
      try {
        this.clearAuth();
        const response = await loginApi(request);
        this.setCurrentUser(response.currentUser);
        return response.currentUser;
      } finally {
        this.loading = false;
      }
    },
    async logout() {
      try {
        await logoutApi();
      } finally {
        this.clearAuth();
      }
    },
    async fetchCurrentUser() {
      if (this.userLoaded) {
        return this.currentUser ?? undefined;
      }
      try {
        const user = await fetchCurrentUserApi();
        this.setCurrentUser(user);
        return user;
      } catch (error) {
        if (error instanceof AuthenticationError) {
          this.clearAuth();
          return undefined;
        }
        throw error;
      }
    },
  },
});
