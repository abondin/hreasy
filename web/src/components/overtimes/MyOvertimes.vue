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
                <div class="mr-2">{{ $tc('Сверхурочные')}}</div>
                <v-btn @click.stop="decrementPeriod()" link>
                    <v-icon>mdi-chevron-left</v-icon>
                </v-btn>
                <div class="ml-1 mr-2">{{selectedPeriod}}</div>
                <v-btn @click.stop="incrementPeriod()" link>
                    <v-icon>mdi-chevron-right</v-icon>
                </v-btn>
                <v-spacer></v-spacer>
                <v-card-actions>
                    <overtime-add-or-edit
                            v-bind:employee-id="employeeId"
                            v-bind:period="selectedPeriod"
                            v-bind:all-projects="allProjects"
                            @submit="onItemSubmit"
                            @close="onItemDialogClose"></overtime-add-or-edit>
                </v-card-actions>
            </v-card-title>
            <v-data-table
                    :loading="loading"
                    :loading-text="$t('Загрузка_данных')"
                    :headers="headers"
                    :items="overtimes"
                    sort-by="updatedAt"
                    sort-desc
                    disable-pagination>
                <template v-slot:item.date="{ item }">
                    {{formatDate(item.date)}}
                </template>
                <template v-slot:item.updatedAt="{ item }">
                    {{formatDateTime(item.updatedAt)}}
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
    import overtimeService, {
        OvertimeItem,
        OvertimeReport,
        OvertimeUtils,
        ReportPeriod
    } from "@/components/overtimes/overtime.service";
    import {DataTableHeader} from "vuetify";
    import OvertimeAddOrEditDialog from "@/components/overtimes/OvertimeAddOrEdit.vue";

    const namespace_dict: string = 'dict';
    const namespace_auth: string = 'auth';
    @Component({
        components: {OvertimeAddOrEdit: OvertimeAddOrEditDialog}
    })
    export default class MyOvertimes extends Vue {
        loading: boolean = false;
        overtimeReport!: OvertimeReport;
        headers: DataTableHeader[] = [];

        @Getter("projects", {namespace: namespace_dict})
        private allProjects!: Array<SimpleDict>;

        @Getter("employeeId", {namespace: namespace_auth})
        employeeId!: number;

        selectedPeriod = ReportPeriod.currentPeriod();

        private overtimes: OvertimeItem[] = [];


        /**
         * Lifecycle hook
         */
        created() {
            this.headers.push({text: this.$tc('Дата'), value: 'date'});
            this.headers.push({text: this.$tc('Проект'), value: 'projectId'});
            this.headers.push({text: this.$tc('Часы'), value: 'hours'});
            this.headers.push({text: this.$tc('Комментарий'), value: 'notes'});
            this.headers.push({text: this.$tc('Последнее обновление'), value: 'updatedAt'});
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
            const periodId = this.selectedPeriod.periodId();
            return overtimeService.get(this.employeeId, periodId)
                .then(report => {
                        if (report) {
                            this.overtimeReport = report;
                        } else {
                            this.overtimeReport = {
                                employeeId: this.employeeId,
                                reportPeriod: periodId,
                                items: []
                            }
                        }
                        this.overtimes = this.overtimeReport.items;
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

        private formatDateTime(date: Date): string | undefined {
            return OvertimeUtils.formatDateTime(date);
        }

        private projectName(projectId: number): string {
            const projects = this.allProjects.filter(p => p.id == projectId);
            if (projects && projects.length > 0) {
                return projects[0].name;
            }
            return this.$tc(`Неизвестный проект ${projectId}`);
        }

        private onItemSubmit(event: any) {
            this.fetchReport();
        }

        private onItemDialogClose() {
            // Do nothing?
        }

        private incrementPeriod(){
            this.selectedPeriod.increment();
            this.fetchReport(true);
        }

        private decrementPeriod(){
            this.selectedPeriod.decrement();
            this.fetchReport(true);
        }

    }
</script>

<style scoped>

</style>
