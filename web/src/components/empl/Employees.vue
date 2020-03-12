<!-- Employees short info table-->
<template>
    <v-container fluid>
        <v-card>
            <v-card-title>
                {{$t('Сотрудники')}}
                <v-spacer></v-spacer>
                <v-text-field
                        v-model="search"
                        append-icon="search"
                        :label="$t('Поиск')"
                        single-line
                        hide-details
                ></v-text-field>
            </v-card-title>
            <v-data-table
                    :loading="loading"
                    :loading-text="$t('Загрузка_данных')"
                    :headers="headers"
                    :items="employees"
                    :search="search"
            >
                <template v-slot:item.displayName="{ item }">
                    <v-row align="center"
                           class="spacer"
                           no-gutters>
                        <v-col cols="2">
                            <v-avatar size="64px">
                                <img :src="getAvatarUrl(item.id)"/>
                            </v-avatar>
                        </v-col>
                        <v-col
                                class="hidden-xs-only"
                               cols="10"
                        >
                            <strong v-html="item.displayName"></strong>
                        </v-col>
                    </v-row>
                </template>
            </v-data-table>
        </v-card>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService, {Employee} from "@/components/empl/employee.service";
    import {DataTableHeader} from "vuetify";

    @Component
    export default class EmployeesComponent extends Vue {
        employees: Employee[] = [];
        loading: boolean = false;
        search: string = '';
        headers: DataTableHeader[] = [];

        /**
         * Lifecycle hook
         */
        created() {
            this.headers.push({text: this.$tc('ФИО'), value: 'displayName', width: "40%"});
            this.headers.push({text: this.$tc('E-mail'), value: 'email', width: "20%"});
            this.headers.push({text: this.$tc('Отдел'), value: 'department.name', width: "20%"});
            this.headers.push({text: this.$tc('Текущий Проект'), value: 'currentProject.name', width: "20%"});
            this.fetchData()
        }

        private fetchData() {
            this.loading = true;
            return employeeService.findAll()
                .then(data => {
                        this.employees = data as Employee[];
                    }
                ).finally(() => {
                    this.loading = false
                });
        }

        private getAvatarUrl(employeeId: number) {
            return employeeService.getAvatarUrl(employeeId);
        }
    }
</script>

<style scoped>

</style>
