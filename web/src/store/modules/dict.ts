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

export interface PositionOnMap {
    x?: number;
    y?: number;
}

export const enum ValueWithStatusEnum {
    OK = 1,
    WARNING = 2,
    ERROR = 3
}

export interface ValueWithStatus<T> {
    value: T;
    status: ValueWithStatusEnum;
}

export interface ProjectDictDto extends SimpleDict {
    baId: number;
}

export interface ManagerOfObject {
    id: number;
    employeeId: number,
    employeeName: string,
    responsibilityType: string,
    comment?: string;
}

export interface CurrentProjectRole {
    value: string;
}

export interface DictLoadedState {
    projects: Array<ProjectDictDto>;
    businessAccounts: Array<SimpleDict>;
    departments: Array<SimpleDict>;
    organizations: Array<SimpleDict>;
    positions: Array<SimpleDict>;
    levels: Array<SimpleDict>;
    offices: Array<SimpleDict>;
    officeLocations: Array<SimpleDict>;
    skillGroups: Array<SimpleDict>;
    sharedSkillsNames: Array<SharedSkillName>;
    currentProjectRoles: Array<CurrentProjectRole>;
}

export const dictState: DictLoadedState = {
    projects: [],
    businessAccounts: [],
    departments: [],
    organizations: [],
    positions: [],
    levels: [],
    offices: [],
    officeLocations: [],
    skillGroups: [],
    sharedSkillsNames: [],
    currentProjectRoles: []
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
    reloadOrganizations({commit}): any {
        return dictService.loadAllOrganizations().then(deps => {
            commit('organizationsLoaded', deps);
        });
    },
    reloadPositions({commit}): any {
        return dictService.loadAllPositions().then(deps => {
            commit('positionsLoaded', deps);
        });
    },
    reloadLevels({commit}): any {
        return dictService.loadAllLevels().then(deps => {
            commit('levelsLoaded', deps);
        });
    },
    reloadOffices({commit}): any {
        return dictService.loadAllOffices().then(offices => {
            commit('officesLoaded', offices);
        });
    },
    reloadOfficeLocations({commit}): any {
        return dictService.loadAllOfficeLocations().then(locations => {
            commit('officeLocationsLoaded', locations);
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
    },
    reloadCurrentProjectRoles({commit}): any {
        return dictService.loadAllCurrentProjectRoles().then(roles => {
            commit('currentProjectRolesLoaded', roles);
        });
    },
}

export const dictMutations: MutationTree<DictLoadedState> = {
    projectsLoaded(state: DictLoadedState, projects: Array<ProjectDictDto>) {
        state.projects = projects;
    },
    businessAccountsLoaded(state: DictLoadedState, bas: Array<SimpleDict>) {
        state.businessAccounts = bas;
    },
    departmentsLoaded(state: DictLoadedState, deps: Array<SimpleDict>) {
        state.departments = deps;
    },
    organizationsLoaded(state: DictLoadedState, deps: Array<SimpleDict>) {
        state.organizations = deps;
    },
    positionsLoaded(state: DictLoadedState, value: Array<SimpleDict>) {
        state.positions = value;
    },
    levelsLoaded(state: DictLoadedState, value: Array<SimpleDict>) {
        state.levels = value;
    },
    officesLoaded(state: DictLoadedState, value: Array<SimpleDict>) {
        state.offices = value;
    },
    officeLocationsLoaded(state: DictLoadedState, value: Array<SimpleDict>) {
        state.officeLocations = value;
    },
    skillGroupsLoaded(state: DictLoadedState, groups: Array<SimpleDict>) {
        state.skillGroups = groups;
    },
    sharedSkillsLoaded(state: DictLoadedState, names: Array<SharedSkillName>) {
        state.sharedSkillsNames = names;
    },
    currentProjectRolesLoaded(state: DictLoadedState, roles: Array<CurrentProjectRole>) {
        state.currentProjectRoles = roles;
    }
}


export const dictGetters: GetterTree<DictLoadedState, RootState> = {
    projects(state: DictLoadedState): Array<ProjectDictDto> {
        return state.projects;
    },
    businessAccounts(state: DictLoadedState): Array<SimpleDict> {
        return state.businessAccounts;
    },
    departments(state: DictLoadedState): Array<SimpleDict> {
        return state.departments;
    },
    organizations(state: DictLoadedState): Array<SimpleDict> {
        return state.organizations;
    },
    positions(state: DictLoadedState): Array<SimpleDict> {
        return state.positions;
    },
    levels(state: DictLoadedState): Array<SimpleDict> {
        return state.levels;
    },
    offices(state: DictLoadedState): Array<SimpleDict> {
        return state.offices;
    },
    officeLocations(state: DictLoadedState): Array<SimpleDict> {
        return state.officeLocations;
    },
    skillGroups(state: DictLoadedState): Array<SimpleDict> {
        return state.skillGroups;
    },
    sharedSkills(state: DictLoadedState): Array<SharedSkillName> {
        return state.sharedSkillsNames;
    },
    currentProjectRoles(state: DictLoadedState): Array<CurrentProjectRole> {
        return state.currentProjectRoles;
    }
};

const namespaced = true;


export const dict: Module<DictLoadedState, RootState> = {
    namespaced,
    state: dictState,
    getters: dictGetters,
    actions: dictActions,
    mutations: dictMutations
};

export default dict;

