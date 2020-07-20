<!-- All vacations table-->
<template>
    <v-container fluid>
        <v-card>
            <v-card-title>
                <div class="mr-2">{{ $tc('Сверхурочные')}}</div>
                <div v-if="changePeriodAllowed==true">
                    <v-btn @click.stop="decrementPeriod()" link>
                        <v-icon>mdi-chevron-left</v-icon>
                    </v-btn>
                    <span class="ml-1 mr-2">{{selectedPeriod}}</span>
                    <v-btn @click.stop="incrementPeriod()" link>
                        <v-icon>mdi-chevron-right</v-icon>
                    </v-btn>
                </div>
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
                    hide-default-footer
                    disable-pagination>
                <template v-slot:item.date="{ item }">
                    <v-btn @click.stop="openDeleteDialog(item)" icon>
                        <v-icon>mdi-delete</v-icon>
                    </v-btn>
                    <span>{{formatDate(item.date)}}</span>
                </template>
                <template v-slot:item.updatedAt="{ item }">
                    {{formatDateTime(item.updatedAt)}}
                </template>
                <template v-slot:item.projectId="{ item }">
                    {{projectName(item.projectId)}}
                </template>
            </v-data-table>
        </v-card>

        <v-dialog v-if="itemToDelete"
                v-model="deleteDialog"
                width="500">
            <v-card>
                <v-card-title primary-title>
                    {{$t('Удаление')}}
                </v-card-title>

                <v-card-text>
                    {{$t('Вы уверены что хотите удалить запись?')}}
                </v-card-text>

                <v-divider></v-divider>

                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn
                            text
                            @click="deleteDialog = false">
                        {{$t('Нет')}}
                    </v-btn>
                    <v-btn
                            color="primary"
                            @click="deleteItem()">
                        {{$t('Да')}}
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>
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
    import {Prop} from "vue-property-decorator";

    const namespace_dict: string = 'dict';
    const namespace_auth: string = 'auth';
    @Component({
        components: {OvertimeAddOrEdit: OvertimeAddOrEditDialog}
    })
    export default class EmployeeOvertimeComponent extends Vue {
        loading: boolean = false;
        overtimeReport!: OvertimeReport;
        headers: DataTableHeader[] = [];

        @Getter("projects", {namespace: namespace_dict})
        private allProjects!: Array<SimpleDict>;

        @Prop({required: true})
        employeeId!: number;

        @Prop({required: false, default: true})
        changePeriodAllowed!: boolean;


        @Prop({required: true})
        selectedPeriod!: ReportPeriod;

        private overtimes: OvertimeItem[] = [];

        private deleteDialog = false;
        private itemToDelete:OvertimeItem|null = null;


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
                        return this.refreshReport(report);
                    }
                ).finally(() => {
                    if (showLoading) {
                        this.loading = false
                    }
                });
        }

        private refreshReport(report: OvertimeReport | undefined): OvertimeReport {
            if (report) {
                this.overtimeReport = report;
            } else {
                const periodId = this.selectedPeriod.periodId();
                this.overtimeReport = {
                    employeeId: this.employeeId,
                    period: periodId,
                    items: []
                }
            }
            this.overtimes = this.overtimeReport.items;
            return this.overtimeReport;
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

        private deleteItem() {
            if (this.itemToDelete) {
                overtimeService.deleteItem(this.overtimeReport.employeeId, this.overtimeReport.period, this.itemToDelete.id!).then((report) => {
                    this.deleteDialog = false;
                    return this.refreshReport(report);
                });
            } else {
                alert('Some error occurs. Item to delete not selected... Please contact administrator')
            }
        }

        private onItemDialogClose() {
            // Do nothing?
        }

        private incrementPeriod() {
            this.selectedPeriod.increment();
            this.fetchReport(true);
        }

        private decrementPeriod() {
            this.selectedPeriod.decrement();
            this.fetchReport(true);
        }

        private openDeleteDialog(item : OvertimeItem){
            this.itemToDelete = item;
            this.$nextTick(()=>{
                this.deleteDialog = true;
            });
        }

    }
</script>

<style scoped>

</style>
