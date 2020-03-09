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
            this.headers.push({text: this.$tc('ФИО'), value: 'displayName'});
            this.headers.push({text: this.$tc('E-mail'), value: 'email'});
            this.fetchData()
        }

        private fetchData() {
            this.loading = true;
            return employeeService.findAll()
                .then(data => new Promise((resolve, reject) => setTimeout(() => resolve(data), 5000)))
                .then(data => {
                        this.employees = data as Employee[];
                    }
                ).finally(() => {
                    this.loading = false
                });
        }
    }
</script>

<style scoped>

</style>
