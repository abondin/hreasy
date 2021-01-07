/**
 * Store current logged in user
 */
import {ActionTree, GetterTree, Module, MutationTree} from "vuex";
import {RootState} from "@/store";
import dictService from "@/store/modules/dict.service";

export interface SimpleDict {
    id: number;
    name: string;
    active: boolean;
}

export interface DictLoadedState {
    projects : Array<SimpleDict>;
}

export const dictState: DictLoadedState = {
    projects: []
}


export const dictActions: ActionTree<DictLoadedState, RootState> = {
    reloadProjects({commit}): any {
        return dictService.loadAllProjects().then(projects => {
            commit('projectsLoaded', projects);
        });
    }
}

export const dictMutations: MutationTree<DictLoadedState> = {
    projectsLoaded(state, projects: Array<SimpleDict>) {
        state.projects = projects;
    }
}


export const dictGetters: GetterTree<DictLoadedState, RootState> = {
    projects(state): Array<SimpleDict> {
        return state.projects;
    },
};

const namespaced: boolean = true;


export const dict: Module<DictLoadedState, RootState> = {
    namespaced,
    state: dictState,
    getters: dictGetters,
    actions: dictActions,
    mutations: dictMutations
};

export default dict;

