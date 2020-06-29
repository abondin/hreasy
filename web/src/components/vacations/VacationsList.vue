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
        <v-row>
            <v-col cols="12" lg="8">
                <v-card>
                    <v-card-title>
                        <v-btn
                                icon
                                class="ma-2"
                                @click="prev"
                        >
                            <v-icon>mdi-chevron-left</v-icon>
                        </v-btn>
                        <span v-if="$refs.calendar">{{ $refs.calendar.title }}</span>
                        <v-btn outlined class="ma-2" @click="setToday">
                            {{$t('Сегодня')}}
                        </v-btn>
                        <v-spacer></v-spacer>
                        <v-btn icon
                               class="ma-2"
                               @click="next">
                            <v-icon>mdi-chevron-right</v-icon>
                        </v-btn>

                    </v-card-title>

                    <v-calendar
                            v-model="focus"
                            :weekdays=weekdays
                            event-name="employeeDisplayName"
                            event-end="endDate"
                            event-start="startDate"
                            :event-color="getVacationColor"
                            ref="calendar"
                            :event-more=false
                            :events="filteredVacations()"
                            color="primary"
                            type="month"
                    ></v-calendar>
                </v-card>
            </v-col>
            <v-col cols="12" lg="4">
                <v-card>
                    <v-card-title>{{$t('Проекты')}}</v-card-title>
                <v-list shaped dense>
                    <v-list-item-group
                            v-model="selectedProjects"
                            multiple
                    >
                        <template v-for="project in allProjects">
                            <v-list-item :key="project.id" :value="project.id"
                                         :style="'background-color:'+getColor(project.id)"
                                         class="ma-1">
                                <template v-slot:default="{ active, toggle }">
                                    <v-list-item-content>
                                        <v-list-item-title v-text="project.name"></v-list-item-title>
                                    </v-list-item-content>
                                    <v-list-item-action>
                                        <v-checkbox
                                                color="white"
                                                :input-value="active"
                                                :true-value="project.id"
                                                @click="toggle"
                                        ></v-checkbox>
                                    </v-list-item-action>
                                </template>
                            </v-list-item>
                        </template>
                    </v-list-item-group>
                </v-list>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import vacationService, {Vacation} from "@/components/vacations/vacation.service";
    import {Getter} from "vuex-class";
    import {SimpleDict} from "@/store/modules/dict";

    const namespace: string = 'dict';

    @Component
    export default class VacationsListComponent extends Vue {
        loading: boolean = false;
        vacations: Vacation[] = [];
        focus = '';
        private weekdays = [1, 2, 3, 4, 5, 6, 0];
        projectColors: Record<number, string> = {};
        allColors = ['#AA00FF', '#6200EA', '#0091EA', '#00B8D4', '#00BFA5', '#AEEA00'];

        @Getter("projects", {namespace})
        private allProjects!: Array<SimpleDict>;

        selectedProjects: Array<number> = [];


        /**
         * Lifecycle hook
         */
        created() {
            this.fetchData()
        }

        /**
         * Lifecycle hook
         */
        mounted() {
            (this.$refs.calendar as any).checkChange()
        }

        private setToday() {
            this.focus = '';
        }

        private next() {
            return (this.$refs.calendar as any).next();
        }

        private prev() {
            return (this.$refs.calendar as any).prev();
        }

        private filteredVacations() {
            return this.vacations.filter(v => !v.employeeCurrentProject ||
                this.selectedProjects.indexOf(v.employeeCurrentProject) >= 0);
        }

        /**
         * Vacation color depends on employee current project
         * @param vacation
         */
        private getVacationColor(vacation: Vacation) {
            return this.getColor(vacation.employeeCurrentProject);
        }

        private getColor(projectId: number) {
            const color = projectId ? this.projectColors[projectId] : undefined;
            return color;
        }

        private fetchData() {
            this.loading = true;
            return this.$store.dispatch('dict/reloadProjects').then(() => {
                this.selectedProjects = [...this.allProjects].map(p => p.id);
                this.allProjects.forEach((project, index) => {
                    const l = Math.min(this.allColors.length, this.allProjects.length);
                    this.projectColors[project.id] = this.allColors[index % l];
                });
            }).then(() => {
                return vacationService.findAll()
                    .then(data => {
                            this.vacations = (data as Vacation[]).filter(m => m.startDate && m.endDate && m.employeeDisplayName);
                            return;
                        }
                    )
            }).finally(() => {
                this.loading = false
            });
        }
    }
</script>

<style scoped>

</style>
