<!--
Employee basic information card
Uses in Employees Table (Employees.vue)
 -->

<template>
    <v-card
            class="mx-auto"
            max-width="434"
            tile
    >
        <v-img
                height="100%"
                src="../../assets/card-bg.jpg"
        >
            <v-row
                    align="end"
                    class="fill-height"
            >
                <v-col
                        align-self="start"
                        cols="12"
                >
                    <employee-avatar-uploader v-bind:employee="employee"/>
                </v-col>
                <v-col class="py-0">
                    <v-list-item
                            color="rgba(0, 0, 0, .4)"
                            dark
                    >
                        <v-list-item-content>
                            <v-list-item-title class="title">{{employee.displayName}}
                                <router-link :to="{ name: 'employeeEdit', params: {id:employee.id}}">
                                    <v-icon dense>edit</v-icon>
                                </router-link>
                            </v-list-item-title>
                            <v-list-item-subtitle>{{employee.currentProject?employee.currentProject.name:$tc('Проект не задан')}}
                            </v-list-item-subtitle>
                        </v-list-item-content>
                    </v-list-item>
                </v-col>
            </v-row>
        </v-img>
    </v-card>

</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService, {Employee} from "@/components/empl/employee.service";
    import {Prop} from "vue-property-decorator";
    import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";
    @Component({
        components: {EmployeeAvatarUploader}
    })
    export default class EmployeeCard extends Vue {
        @Prop({required: true})
        employee!: Employee;

        private getAvatarUrl(employeeId: number) {
            return employeeService.getAvatarUrl(employeeId);
        }

    }
</script>

<style scoped>

</style>
