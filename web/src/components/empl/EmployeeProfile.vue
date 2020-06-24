<!-- Employees own profile page-->
<template>
    <v-container v-if="employee">
        <!-- Work in progress -->
        <v-alert
                color="red"
                dark
                icon="mdi-grill"
                border="right">
            Work in progress
        </v-alert>

        <v-card>
            <v-row align="start">
                <v-col cols="4" lg="2">
                    <div style="max-width: 164px; margin-left: 10px" align="center">
                        <employee-avatar v-bind:employee="employee"></employee-avatar>
                    </div>
                </v-col>
                <v-col cols="8" lg="10">
                    <v-list-item>
                        <v-list-item-content>
                            <v-list-item-title class="title">{{employee.displayName}}</v-list-item-title>
                            <v-list-item-subtitle>{{$tc('Отдел')}} :
                                {{employee.department?employee.department.name:$tc("Не задан")}}
                            </v-list-item-subtitle>
                            <v-list-item-subtitle>{{$tc('Текущий проект')}} :
                                {{employee.currentProject?employee.currentProject.name:$tc("Не задан")}}
                            </v-list-item-subtitle>
                            <v-list-item-subtitle>
                                {{$tc('Почтовый адрес')}} : {{employee.email?employee.email:$tc("Не задан")}}
                            </v-list-item-subtitle>
                        </v-list-item-content>
                    </v-list-item>
                </v-col>

            </v-row>
        </v-card>

    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService, {Employee} from "@/components/empl/employee.service";
    import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";
    import {Getter} from "vuex-class";

    const namespace: string = 'auth';

    @Component({
        components: {"employee-avatar": EmployeeAvatarUploader}
    })
    export default class EmployeeProfile extends Vue {
        loading: boolean = false;

        private employee: Employee | null = null;

        @Getter("employeeId", {namespace})
        employeeId!: number;

        /**
         * Lifecycle hook
         */
        created() {
            this.fetchData(this.employeeId);
        }

        private fetchData(employeeId: number) {
            this.loading = true;
            return employeeService.find(employeeId)
                .then(data => {
                        this.employee = data;
                    }
                ).finally(() => {
                    this.loading = false
                });
        }

    }
</script>

<style scoped>

</style>
