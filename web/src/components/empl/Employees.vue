<!-- Employees short info table-->
<template>
    <v-container fluid>
        <v-card>
            <v-card-title>
                <v-row dense>
                    <v-col lg="12" cols="12">
                        <v-text-field
                                v-model="search"
                                append-icon="mdi-magnify"
                                :label="$t('Поиск')"
                                single-line
                                hide-details
                        ></v-text-field>
                    </v-col>
                    <!-- TODO
                    <v-col lg="6" cols="12">
                        <v-select
                                item-value="id"
                                item-text="name"
                                :items="allProjects"
                                :label="$t('Фильтр по проекту')"
                                clearable
                                @change="filterProject"
                        />
                    </v-col>
                    -->
                </v-row>
            </v-card-title>
            <v-data-table
                    :loading="loading"
                    :loading-text="$t('Загрузка_данных')"
                    :headers="headers"
                    :items="employees"
                    :search="search"
                    sort-by="displayName"
                    :show-expand="$vuetify.breakpoint.mdAndUp"
                    disable-pagination
            >
                <template v-slot:expanded-item="{ headers, item }">
                    <td :colspan="headers.length">
                        <employee-card v-bind:employee="item">
                        </employee-card>
                    </td>
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
    import EmployeeCard from "@/components/empl/EmployeeCard.vue";

    @Component({
        components: {"employee-card": EmployeeCard}
    })
    export default class EmployeesComponent extends Vue {
        headers: DataTableHeader[] = [];
        loading: boolean = false;
        search = '';


        employees: Employee[] = [];

        /**
         * Lifecycle hook
         */
        created() {
            this.headers.push({text: this.$tc('ФИО'), value: 'displayName'});
            this.headers.push({text: this.$tc('Отдел'), value: 'department.name'});
            this.headers.push({text: this.$tc('E-mail'), value: 'email'});
            this.headers.push({text: this.$tc('Текущий проект'), value: 'currentProject.name'});
            // Reload projects dict to Vuex
            return this.fetchData().then(() => this.$store.dispatch('dict/reloadProjects'));
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

    }
</script>

<style scoped>

</style>
