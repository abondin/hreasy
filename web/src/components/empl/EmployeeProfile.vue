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

        <v-card class="d-flex flex-column flex-lg-row pa-5">
            <employee-avatar v-bind:employee="employee"></employee-avatar>
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
                    <v-list-item-subtitle>
                        {{$tc('Позиция')}} : {{employee.position?employee.position.name:$tc("Не задана")}}
                    </v-list-item-subtitle>
                    <v-list-item-subtitle>
                        {{$tc('Кабинет')}} : {{employee.officeLocation?employee.officeLocation.name:$tc("Не задан")}}
                    </v-list-item-subtitle>
                </v-list-item-content>
            </v-list-item>
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
