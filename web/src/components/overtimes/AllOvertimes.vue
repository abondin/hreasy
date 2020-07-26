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
                <v-btn text icon @click="fetchData()" class="mr-5">
                    <v-icon>refresh</v-icon>
                </v-btn>
                <v-btn @click.stop="decrementPeriod()" text x-small>
                    <v-icon>mdi-chevron-left</v-icon>
                </v-btn>
                <span class="ml-1 mr-2">{{selectedPeriod}}</span>
                <v-btn @click.stop="incrementPeriod()" text x-small class="mr-5">
                    <v-icon>mdi-chevron-right</v-icon>
                </v-btn>
                <v-select
                        @input="applyFilters()"
                        clearable
                        class="mr-5"
                        v-model="filter.selectedProjects"
                        :items="filter.allProjects"
                        item-value="id"
                        item-text="name"
                        :label="$t('Проект списания сверхурочных')"
                        multiple
                ></v-select>
                <v-checkbox :label="$t('Сотрудники без сверхурочных')" v-model="filter.showEmpty" class="mr-5">

                </v-checkbox>
                <v-text-field
                        v-model="search"
                        :label="$t('Поиск')"></v-text-field>

            </v-card-title>
            <v-data-table
                    :loading="loading"
                    :loading-text="$t('Загрузка_данных')"
                    :headers="headers"
                    :items="filteredOvertimes()"
                    hide-default-footer
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
    import employeeService, {Employee} from "@/components/empl/employee.service";
    import {DataTableHeader} from "vuetify";
    import EmployeeCard from "@/components/empl/EmployeeCard.vue";
    import overtimeService, {
        OvertimeEmployeeSummary,
        OvertimeSummaryContainer,
        ReportPeriod
    } from "@/components/overtimes/overtime.service";
    import logger from "@/logger";
    import EmployeeOvertimeComponent from "@/components/overtimes/EmployeeOvertimeComponent.vue";
    import {SimpleDict} from "@/store/modules/dict";
    import {Getter} from "vuex-class";

    const namespace_dict: string = 'dict';

    class RawData {
        public employees: Employee[] = [];
        public overtimes: OvertimeEmployeeSummary[] = [];
    }

    class Filter {
        public showEmpty = true;
        public selectedProjects: number[] = [];
        public allProjects: SimpleDict[] = [];
    }

    @Component({
        components: {EmployeeOvertimeComponent, "employee-card": EmployeeCard}
    })
    export default class AllOvertimes extends Vue {
        headers: DataTableHeader[] = [];
        loading: boolean = false;
        search = '';

        selectedPeriod = ReportPeriod.currentPeriod();

        overtimes: OvertimeSummaryContainer[] = [];
        private rawData = new RawData();

        @Getter("projects", {namespace: namespace_dict})
        private allProjects!: Array<SimpleDict>;

        private filter = new Filter();
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
            return this.$store.dispatch('dict/reloadProjects').then(() => {
                logger.log('All Overtimes fetch data: Loaded projects', this.allProjects);
                employeeService.findAll()
                    .then(employees => {
                        this.rawData.employees = employees;
                        logger.log('All Overtimes fetch data: Loaded employees', this.rawData.employees);
                        return overtimeService.getSummary(this.selectedPeriod.periodId()).then((overtimes) => {
                            this.rawData.overtimes = overtimes;
                            logger.log('All Overtimes fetch data: Loaded overtimes', this.rawData.overtimes);
                            this.applyFilters();
                        })
                    }).finally(() => {
                    this.loading = false
                });
            });
        }

        /**
         * Recalculate aggregates based on given filters
         */
        private applyFilters() {
            this.overtimes.length = 0;
            const projectsWithOvertimes: number [] = [];
            this.rawData.employees.forEach(e => this.overtimes.push(new OvertimeSummaryContainer({
                id: e.id,
                name: e.displayName,
                active: true
            }, {
                selectedProjects: this.filter.selectedProjects
            })));
            logger.log('All Overtimes applyFilters: Empty overtimes', this.overtimes);

            this.rawData.overtimes.forEach(serverOvertime => {
                const existing = this.overtimes.find(o => o.employee.id == serverOvertime.employeeId);
                serverOvertime.items.forEach(i => {
                    if (projectsWithOvertimes.indexOf(i.projectId) == -1) {
                        projectsWithOvertimes.push(i.projectId);
                    }
                });
                if (existing) {
                    existing.addDays(serverOvertime.items);
                } else {
                    logger.error(`Unable to find overtime for employee ${serverOvertime.employeeId}`);
                }
            });
            logger.log('All Overtimes applyFilters: Populated overtimes', this.overtimes);

            this.filter.allProjects = this.allProjects.filter(p => projectsWithOvertimes.indexOf(p.id) >= 0);

            logger.log('All Overtimes applyFilters: Filtered projects', this.filter.allProjects);
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

        private filteredOvertimes(): OvertimeSummaryContainer[] {
            return this.overtimes.filter(i => {
                let passed = true;
                // Check showEmpty filter
                passed = passed && (this.filter.showEmpty || i.totalHours > 0)
                return passed;
            });
        }

    }
</script>

<style scoped>

</style>
