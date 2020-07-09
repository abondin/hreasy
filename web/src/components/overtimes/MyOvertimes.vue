<!-- All vacations table-->
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
                <div>{{ $tc('Сверхурочные')}}</div>
                <v-spacer></v-spacer>
                <v-card-actions>
                    <v-btn>${{Добавить}}</v-btn>
                </v-card-actions>
            </v-card-title>
            <v-data-table
                    :loading="loading"
                    :loading-text="$t('Загрузка_данных')"
                    :headers="headers"
                    :items="overtimes()"
                    sort-by="date"
                    sort-desc
                    disable-pagination>
                <template v-slot:item.date="{ item }">
                    {{formatDate(item.date)}}
                </template>
                <template v-slot:item.projectId="{ item }">
                    {{projectName(item.projectId)}}
                </template>
            </v-data-table>
        </v-card>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import {Getter} from "vuex-class";
    import {SimpleDict} from "@/store/modules/dict";
    import overtimeService, {OvertimeReport, OvertimeUtils} from "@/components/overtimes/overtime.service";
    import {DataTableHeader} from "vuetify";
    import moment from "moment";

    const namespace_dict: string = 'dict';
    const namespace_auth: string = 'auth';

    @Component
    export default class MyOvertimes extends Vue {
        loading: boolean = false;
        overtimeReport!: OvertimeReport;
        headers: DataTableHeader[] = [];

        @Getter("projects", {namespace: namespace_dict})
        private allProjects!: Array<SimpleDict>;

        @Getter("employeeId", {namespace: namespace_auth})
        employeeId!: number;

        selectedPeriod = OvertimeUtils.getPeriod(new Date());

        private overtimes() {
            return this.overtimeReport ? this.overtimeReport.overtimes : [];
        }

        /**
         * Lifecycle hook
         */
        created() {
            this.headers.push({text: this.$tc('Дата'), value: 'date'});
            this.headers.push({text: this.$tc('Проект'), value: 'projectId'});
            this.headers.push({text: this.$tc('Часы'), value: 'hours'});
            this.headers.push({text: this.$tc('Комментарий'), value: 'notes'});
            this.fetchData()
        }


        private fetchData() {
            this.loading = true;
            return this.$store.dispatch('dict/reloadProjects').then(() => {
                return this.fetchReport(false);
            }).finally(() => {
                this.loading = false
            });
        }

        private fetchReport(showLoading = true) {
            if (showLoading) {
                this.loading = true;
            }
            return overtimeService.get(this.employeeId, this.selectedPeriod)
                .then(report => {
                        if (report) {
                            this.overtimeReport = report;
                        } else {
                            this.overtimeReport = {
                                employeeId: this.employeeId,
                                reportPeriod: this.selectedPeriod,
                                overtimes: []
                            }
                        }
                        return this.overtimeReport;
                    }
                ).finally(() => {
                    if (showLoading) {
                        this.loading = false
                    }
                });
        }

        private formatDate(date: Date): string | undefined {
            return OvertimeUtils.formatDate(date);
        }

        private projectName(projectId: number) : string {
            const projects = this.allProjects.filter(p=>p.id == projectId);
            if (projects && projects.length > 0){
                return projects[0].name;
            }
            return this.$tc(`Неизвестный проект ${projectId}`);
        }

    }
</script>

<style scoped>

</style>
