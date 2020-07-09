/**
 * Global Error Handling
 */
import {ActionTree, GetterTree, Module, MutationTree} from "vuex";
import {RootState} from "@/store";


export interface ErrorState {
    unhandledrejection : any;
}

export const errorState: ErrorState = {
    unhandledrejection: undefined
}


export const errorActions: ActionTree<ErrorState, RootState> = {
    unhandledrejection({commit}, error: any): any {
        commit('unhandledrejection', error);
    }
}

export const errorMutations: MutationTree<ErrorState> = {
    unhandledrejection(state, error: any) {
        state.unhandledrejection = error;
    }
}


export const errorGetters: GetterTree<ErrorState, RootState> = {
    unhandledrejection(state): any {
        return state.unhandledrejection;
    },
};

const namespaced: boolean = true;


export const error: Module<ErrorState, RootState> = {
    namespaced,
    state: errorState,
    getters: errorGetters,
    actions: errorActions,
    mutations: errorMutations
};

export default error;

