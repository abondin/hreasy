/**
 * Store current logged in user
 */
import {ActionTree, GetterTree, Module, MutationTree} from "vuex";
import {RootState} from "@/store";
import dictService, {SharedSkillName} from "@/store/modules/dict.service";

export interface SimpleDict {
    id: number;
    name: string;
    active: boolean;
}

export interface DictLoadedState {
    projects : Array<SimpleDict>;
    businessAccounts: Array<SimpleDict>;
    departments: Array<SimpleDict>;
    skillGroups: Array<SimpleDict>;
    sharedSkillsNames: Array<SharedSkillName>;
}

export const dictState: DictLoadedState = {
    projects: [],
    businessAccounts: [],
    departments: [],
    skillGroups: [],
    sharedSkillsNames: []
}


export const dictActions: ActionTree<DictLoadedState, RootState> = {
    reloadProjects({commit}): any {
        return dictService.loadAllProjects().then(projects => {
            commit('projectsLoaded', projects);
        });
    },
    reloadBusinessAccounts({commit}): any {
        return dictService.loadAllBusinessAccounts().then(projects => {
            commit('businessAccountsLoaded', projects);
        });
    },
    reloadDepartments({commit}): any {
        return dictService.loadAllDepartments().then(deps => {
            commit('departmentsLoaded', deps);
        });
    },
    reloadSkillGroups({commit}): any {
        return dictService.loadAllSkillGroups().then(groups => {
            commit('skillGroupsLoaded', groups);
        });
    },
    reloadSharedSkills({commit}): any {
        return dictService.loadSharedSkills().then(skills => {
            commit('sharedSkillsLoaded', skills);
        });
    }
}

export const dictMutations: MutationTree<DictLoadedState> = {
    projectsLoaded(state, projects: Array<SimpleDict>) {
        state.projects = projects;
    },
    businessAccountsLoaded(state, bas: Array<SimpleDict>) {
        state.businessAccounts = bas;
    },
    departmentsLoaded(state, deps: Array<SimpleDict>) {
        state.departments = deps;
    },
    skillGroupsLoaded(state, groups: Array<SimpleDict>) {
        state.skillGroups = groups;
    },
    sharedSkillsLoaded(state, names: Array<SharedSkillName>) {
        state.sharedSkillsNames = names;
    }
}


export const dictGetters: GetterTree<DictLoadedState, RootState> = {
    projects(state): Array<SimpleDict> {
        return state.projects;
    },
    businessAccounts(state): Array<SimpleDict> {
        return state.businessAccounts;
    },
    departments(state): Array<SimpleDict> {
        return state.departments;
    },
    skillGroups(state): Array<SimpleDict> {
        return state.skillGroups;
    },
    sharedSkills(state): Array<SharedSkillName> {
        return state.sharedSkillsNames;
    }
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

