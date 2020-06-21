<!-- Employees short info table-->
<template>
    <v-container fluid>
        <v-card>
            <v-card-title>
                <v-text-field
                        v-model="search"
                        append-icon="mdi-magnify"
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
                    sort-by="displayName"
                    :show-expand="$vuetify.breakpoint.mdAndUp"
                    disable-pagination="true"
            >
                <template v-slot:expanded-item="{ headers, item }">
                    <td :colspan="headers.length">
                        <v-card
                                class="mx-auto"
                                max-width="434"
                                tile
                        >
                            <v-img
                                    height="100%"
                                    src="https://cdn.vuetifyjs.com/images/cards/server-room.jpg"
                            >
                                <v-row
                                        align="end"
                                        class="fill-height"
                                >
                                    <v-col
                                            align-self="start"
                                            class="pa-0"
                                            cols="12"
                                    >
                                        <v-avatar v-if="item.hasAvatar"
                                                  class="profile"
                                                  color="grey"
                                                  size="164"
                                                  tile>
                                            <v-img :src="getAvatarUrl(item.id)"></v-img>
                                        </v-avatar>
                                    </v-col>
                                    <v-col class="py-0">
                                        <v-list-item
                                                color="rgba(0, 0, 0, .4)"
                                                dark
                                        >
                                            <v-list-item-content>
                                                <v-list-item-title class="title">{{item.displayName}}
                                                    <router-link :to="{ name: 'employeeEdit', params: {id:item.id}}">
                                                        <v-icon>edit</v-icon>
                                                    </router-link></v-list-item-title>
                                                <v-list-item-subtitle>{{item.currentProject?item.currentProject.name:$tc('Проект не задан')}}</v-list-item-subtitle>
                                            </v-list-item-content>
                                        </v-list-item>
                                    </v-col>
                                </v-row>
                            </v-img>
                        </v-card>
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

    @Component
    export default class EmployeesComponent extends Vue {
        headers: DataTableHeader[] = [];
        loading: boolean = false;
        search = '';

        employees: Employee[] = [];

        /**
         * Lifecycle hook
         */
        created() {
            this.fetchData();
            this.headers.push({text: this.$tc('ФИО'), value: 'displayName'});
            this.headers.push({text: this.$tc('Отдел'), value: 'department.name'});
            this.headers.push({text: this.$tc('E-mail'), value: 'email'});
            this.headers.push({text: this.$tc('Текущий проект'), value: 'currentProject.name'});
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

        private getAvatarUrl(employeeId: number) {
            return employeeService.getAvatarUrl(employeeId);
        }

    }
</script>

<style scoped>

</style>
