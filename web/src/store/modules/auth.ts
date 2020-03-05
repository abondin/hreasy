/**
 * Store current logged in user
 */
import authService, {CurrentUser, LoginRequest} from "@/store/modules/auth.service";
import {ActionTree, GetterTree, Module, MutationTree} from "vuex";
import {RootState} from "@/store";
import logger from "@/logger";
import {AuthenticationError} from "@/components/errors";

export interface AuthState {
    currentUser: CurrentUser | undefined;
    userLoaded: boolean;
}

export const state: AuthState = {
    currentUser: undefined,
    userLoaded: false
}

export const actions: ActionTree<AuthState, RootState> = {
    login({commit}, loginRequest: LoginRequest): any {
        commit('clearAuth');
        return authService.login(loginRequest.username, loginRequest.password).then(loginResponse => {
            commit('userLoggedIn', loginResponse.currentUser);
        });
    },
    logout({commit}): any {
        return authService.logout().then(response => {
            commit('clearAuth');
        });
    },

    /**
     * Safe fetch current user. Do nothing on 401
     * @param commit
     */
    getCurrentUser({commit}): Promise<CurrentUser | undefined> {
        logger.debug("auth-store: fetching current user")
        if (state.userLoaded) {
            logger.debug("auth-store: get cached current user", state.currentUser)
            return Promise.resolve(state.currentUser);
        } else {
            return authService.currentUser().then(response => {
                logger.debug("auth-store: get current user from server", response)
                commit('userLoggedIn', response);
                return response;
            }, error => {
                if (error instanceof AuthenticationError) {
                    logger.debug("Unable to fetch current user. Login required");
                    return undefined;
                } else {
                    throw error;
                }
            });
        }
    }
}

export const mutations: MutationTree<AuthState> = {
    userLoggedIn(state, payload: CurrentUser) {
        state.currentUser = payload;
        state.userLoaded = true;
    },
    clearAuth(state) {
        state.currentUser = undefined;
        state.userLoaded = false;
    }

}


export const getters: GetterTree<AuthState, RootState> = {
    username(state): string | undefined {
        if (state.currentUser) {
            return state.currentUser.username;
        }
        return undefined;
    },
    displayName(state): string {
        if (state.currentUser) {
            return state.currentUser.username;
        }
        return 'anonymous';
    }
};

const namespaced: boolean = true;


export const auth: Module<AuthState, RootState> = {
    namespaced,
    state,
    getters,
    actions,
    mutations
};

export default auth;
