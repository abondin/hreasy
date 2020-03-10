<!-- All vacations table-->
<template>
    <v-container fluid>
        <v-card>
            <v-card-title>
                {{$t('График отпусков')}}
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
                    :items="vacations"
                    :search="search"
            >
            </v-data-table>
        </v-card>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import vacationService, {Vacation} from "@/components/vacations/vacation.service";
    import {DataTableHeader} from "vuetify";

    @Component
    export default class EmployeesComponent extends Vue {
        vacations: Vacation[] = [];
        loading: boolean = false;
        search: string = '';
        headers: DataTableHeader[] = [];

        /**
         * Lifecycle hook
         */
        created() {
            this.headers.push({text: this.$tc('ФИО'), value: 'emplyeeDisplayName'});
            this.headers.push({text: this.$tc('Год'), value: 'year'});
            this.headers.push({text: this.$tc('Начало'), value: 'startDate'});
            this.headers.push({text: this.$tc('Окончание'), value: 'endDate'});
            this.headers.push({text: this.$tc('Примечание'), value: 'notes'});
            this.fetchData()
        }

        private fetchData() {
            this.loading = true;
            return vacationService.findAll()
                .then(data => {
                        this.vacations = data as Vacation[];
                    }
                ).finally(() => {
                    this.loading = false
                });
        }
    }
</script>

<style scoped>

</style>
