<!-- Employees short info table-->
<template>
    <v-container fluid>
        <div v-if="selectedEmployee">
            <v-dialog v-model="employeeDialog" transition="dialog-bottom-transition"
                      @input="selectedEmployee=null">
                <v-card>
                    <v-card-title>
                        <v-btn text large icon @click="closeEmployeeDialog()">
                            <v-icon>close</v-icon>
                        </v-btn>
                        {{$t('Сверхурочные сотрудика', {employee: selectedEmployee.name,
                        period:selectedPeriod.toString()})}}
                    </v-card-title>
                    <v-card-text>
                        <employee-overtime-component
                                change-period-allowed="false"
                                :employee-id="selectedEmployee.id"
                                :selected-period="selectedPeriod"></employee-overtime-component>
                    </v-card-text>
                </v-card>
            </v-dialog>
        </div>
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
                    <v-btn text x-small
                           @click="showEmployeeDialog(item.employee)">{{item.employee.name}}
                    </v-btn>
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
    import EmployeeOvertimeComponent from "@/components/overtimes/EmployeeOvertimeComponent.vue";
    import {SimpleDict} from "@/store/modules/dict";

    @Component({
        components: {EmployeeOvertimeComponent, "employee-card": EmployeeCard}
    })
    export default class AllOvertimes extends Vue {
        headers: DataTableHeader[] = [];
        loading: boolean = false;
        search = '';

        selectedPeriod = ReportPeriod.currentPeriod();

        overtimes: OvertimeSummaryContainer[] = [];

        private selectedEmployee: SimpleDict | null = null;
        private employeeDialog = false;

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

        private showEmployeeDialog(employee: SimpleDict) {
            this.selectedEmployee = employee;
            this.employeeDialog = true;
        }

        private closeEmployeeDialog() {
            this.selectedEmployee = null;
            this.employeeDialog = false;
        }

    }
</script>

<style scoped>

</style>
