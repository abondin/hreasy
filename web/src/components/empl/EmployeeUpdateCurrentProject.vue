<!-- Form to update current employee project assigment -->
<template>
    <v-card>
        <v-card-title>{{$t('Обновление текущего проекта')}}</v-card-title>
        <v-card-subtitle>{{employee.displayName}}</v-card-subtitle>
        <v-card-text>
            <v-select
                    clearable
                    v-model="selectedProject"
                    item-text="name"
                    item-value="id"
                    :items="allProjects.filter(p=>p.active)"
                    :label="$tc('Проекты')"
            ></v-select>
            <div class="error" v-if="error">{{error}}</div>
        </v-card-text>
        <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
                    color="primary"
                    text
                    @click="update"
            >
                {{$t('Применить')}}
            </v-btn>
        </v-card-actions>
    </v-card>
</template>

<script lang="ts">
    import Vue from "vue";
    import {Prop} from "vue-property-decorator";
    import employeeService, {Dict, Employee} from "./employee.service";
    import {Getter} from "vuex-class";
    import {SimpleDict} from "@/store/modules/dict";
    import Component from "vue-class-component";

    const namespace: string = 'dict';

    @Component({})
    export default class EmployeeUpdateCurrentProject extends Vue {
        @Prop({required: true})
        employee!: Employee;

        @Getter("projects", {namespace})
        private allProjects!: Array<SimpleDict>;

        private selectedProject: number | null = null;

        error: string | null = null;

        /**
         * Lifecycle hook
         */
        created() {
            if (this.employee && this.employee.currentProject) {
                const currentProject = this.employee.currentProject
                this.selectedProject = currentProject.id;
            } else {
                this.selectedProject = null;
            }
        }

        update() {
            employeeService.updateCurrentProject(this.employee.id, this.selectedProject)
                .then((response) => {
                    if (this.selectedProject) {
                        var filtered = this.allProjects.filter(p => p.id == this.selectedProject);
                        if (filtered) {
                            this.employee.currentProject = new Dict(this.selectedProject, filtered[0].name);
                        }
                    } else {
                        this.employee.currentProject = undefined;
                    }
                    this.$emit('close');
                })
                .catch(e => this.error = e);
        }
    }
</script>

<style scoped>

</style>
