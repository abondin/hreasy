<!-- Employees short info table-->
<template>
    <v-container fluid>
        <!-- Work in progress -->
        <v-alert
                color="red"
                dark
                icon="mdi-grill"
                border="right">
            Work in progress
        </v-alert>
        <v-card>
            <v-card-title>
                <v-row dense>
                    <v-col lg="12" cols="12">
                        <v-text-field
                                v-model="search"
                                :label="$t('Поиск')"></v-text-field>
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
                    :items="overtimes"
                    :search="search"
                    sort-by="totalHours"
                    sort-desc
                    disable-pagination>
                <template
                        v-slot:item.employee.name="{ item }">
                    <v-btn text x-small>{{item.employee.name}}</v-btn>
                </template>

            </v-data-table>
        </v-card>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService from "@/components/empl/employee.service";
    import {DataTableHeader} from "vuetify";
    import EmployeeCard from "@/components/empl/EmployeeCard.vue";
    import overtimeService, {OvertimeSummaryContainer, ReportPeriod} from "@/components/overtimes/overtime.service";
    import logger from "@/logger";

    @Component({
        components: {"employee-card": EmployeeCard}
    })
    export default class AllOvertimes extends Vue {
        headers: DataTableHeader[] = [];
        loading: boolean = false;
        search = '';

        selectedPeriod = ReportPeriod.currentPeriod();

        overtimes: OvertimeSummaryContainer[] = [];

        /**
         * Lifecycle hook
         */
        created() {
            this.reloadHeaders();
            // Reload projects dict to Vuex
            return this.$store.dispatch('dict/reloadProjects').then(() => this.fetchData());
        }

        private fetchData() {
            this.loading = true;
            return employeeService.findAll()
                .then(employees => {
                    employees.forEach(e => this.overtimes.push(new OvertimeSummaryContainer({
                        id: e.id,
                        name: e.displayName
                    })));
                    return overtimeService.getSummary(this.selectedPeriod.periodId()).then((overtimes) => {
                        overtimes.forEach(serverOvertime => {
                            const existing = this.overtimes.find(o => o.employee.id == serverOvertime.employeeId);
                            if (existing) {
                                existing.addDays(serverOvertime.items);
                            } else {
                                logger.error(`Unable to find overtime for employee ${serverOvertime.employeeId}`);
                            }
                        });
                    })
                }).finally(() => {
                    this.loading = false
                });
        }

        private incrementPeriod() {
            this.selectedPeriod.increment();
            this.reloadHeaders();
            this.fetchData();
        }

        private decrementPeriod() {
            this.selectedPeriod.decrement();
            this.reloadHeaders();
            this.fetchData();
        }

        private reloadHeaders() {
            this.headers.length = 0;
            this.headers.push({text: this.$tc('Сотрудник'), value: 'employee.name'});
            this.headers.push({text: this.$tc('Всего'), value: 'totalHours'});
        }

    }
</script>

<style scoped>

</style>
