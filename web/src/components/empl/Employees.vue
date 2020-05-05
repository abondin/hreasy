<!-- Employees short info table-->
<template>
    <v-container fluid>
        <v-data-iterator
                :loading="loading"
                :loading-text="$t('Загрузка_данных')"
                :items="conf.items"
                :items-per-page.sync="conf.itemsPerPage"
                :page="conf.page"
                :search="conf.search"
                :sort-by="conf.sortBy"
                :sort-desc="conf.sortDesc"
                hide-default-footer
        >
            <!-- Header -->
            <template v-slot:header>
                <v-toolbar
                        dark
                        color="blue darken-3"
                        class="mb-1"
                >
                    <v-text-field
                            v-model="conf.search"
                            clearable
                            flat
                            solo-inverted
                            hide-details
                            prepend-inner-icon="search"
                            :label="$t('Поиск')"
                    ></v-text-field>
                    <template v-if="$vuetify.breakpoint.mdAndUp">
                        <v-spacer></v-spacer>
                        <v-select
                                v-model="conf.sortBy"
                                flat
                                solo-inverted
                                hide-details
                                :items="conf.sortKeys"
                                prepend-inner-icon="sort"
                                :label="$t('Сортировать')"
                        ></v-select>
                        <v-spacer></v-spacer>
                        <v-btn-toggle
                                v-model="conf.sortDesc"
                                mandatory
                        >
                            <v-btn
                                    large
                                    depressed
                                    color="blue"
                                    :value="false"
                            >
                                <v-icon>mdi-arrow-up</v-icon>
                            </v-btn>
                            <v-btn
                                    large
                                    depressed
                                    color="blue"
                                    :value="true"
                            >
                                <v-icon>mdi-arrow-down</v-icon>
                            </v-btn>
                        </v-btn-toggle>
                    </template>
                </v-toolbar>
            </template>


            <!-- Шаблон сотрудника -->
            <template v-slot:default="props">
                <v-row>
                    <v-col
                            v-for="empl in props.items"
                            :key="empl.id"
                            cols="12"
                            sm="6"
                            md="6"
                            lg="4"
                    >
                        <v-card>
                            <v-row style="height: 150px">
                                <v-col cols="2">
                                    <v-avatar v-if="empl.hasAvatar"
                                              class="profile"
                                              color="grey"
                                              size="64">
                                        <v-img :src="getAvatarUrl(empl.id)"></v-img>
                                    </v-avatar>
                                </v-col>
                                <v-col cols="10">
                                    <v-list-item
                                            color="rgba(0, 0, 0, .4)"
                                            dark
                                    >
                                        <v-list-item-content>
                                            <v-list-item-title class="title">
                                                <router-link :to="{ name: 'employeeEdit', params: {id:empl.id}}">
                                                    {{empl.displayName}}
                                                </router-link>
                                            </v-list-item-title>
                                            <v-list-item-subtitle v-if="empl.department">{{empl.department.name}}
                                            </v-list-item-subtitle>
                                            <v-list-item-subtitle v-if="empl.currentProject">
                                                {{empl.currentProject.name}}
                                            </v-list-item-subtitle>
                                            <v-list-item-subtitle>{{empl.email}}</v-list-item-subtitle>
                                        </v-list-item-content>
                                    </v-list-item>
                                </v-col>
                            </v-row>
                        </v-card>
                    </v-col>
                </v-row>
            </template>

            <!-- Подвал -->
            <template v-slot:footer>
                <v-row class="mt-2" align="center" justify="center">
                    <v-spacer></v-spacer>

                    <span
                            class="mr-4
              grey--text"
                    >
              Страница {{ conf.page }} из {{ conf.numberOfPages }}
            </span>
                    <v-btn
                            fab
                            dark
                            color="blue darken-3"
                            class="mr-1"
                            @click="conf.formerPage"
                    >
                        <v-icon>mdi-chevron-left</v-icon>
                    </v-btn>
                    <v-btn
                            fab
                            dark
                            color="blue darken-3"
                            class="ml-1"
                            @click="conf.nextPage"
                    >
                        <v-icon>mdi-chevron-right</v-icon>
                    </v-btn>
                </v-row>
            </template>

        </v-data-iterator>
    </v-container>
</template>


<script lang="ts">
    import Vue from 'vue'
    import Component from 'vue-class-component';
    import employeeService, {Employee} from "@/components/empl/employee.service";
    import DefaultDataIteratorConf from "@/components/empl/dataiterator.conf";

    @Component
    export default class EmployeesComponent extends Vue {
        loading: boolean = false;

        conf = new DefaultDataIteratorConf<Employee>();

        /**
         * Lifecycle hook
         */
        created() {
            this.fetchData();
            this.conf.sortKeys.push({value: "displayName", text: this.$tc('ФИО')});
            this.conf.sortKeys.push({value: "currentProject.name", text: this.$tc('Текущий проект')});
        }

        private fetchData() {
            this.loading = true;
            return employeeService.findAll()
                .then(data => {
                        this.conf.setDataItems(data as Employee[]);
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
