<template>
    <v-app id="hreasy">
        <v-navigation-drawer
                v-if="username"
                v-model="drawer"
                app
                clipped
        >
            <v-list dense>
                <v-list-item link>
                    <v-list-item-action>
                        <v-icon color="grey darken-1">mdi-account-multiple</v-icon>
                    </v-list-item-action>
                    <v-list-item-title class="grey--text text--darken-1">
                        <router-link to="/employees">{{ $tc('Сотрудники')}}</router-link>
                    </v-list-item-title>
                </v-list-item>

                <v-list-item link>
                    <v-list-item-action>
                        <v-icon color="grey darken-1">mdi-calendar-text</v-icon>
                    </v-list-item-action>
                    <v-list-item-title class="grey--text text--darken-1">
                        <router-link to="/vacations">{{ $tc('Отпуска')}}</router-link>
                    </v-list-item-title>
                </v-list-item>
            </v-list>
        </v-navigation-drawer>

        <v-app-bar
                app
                clipped-left
                dense
        >
            <v-app-bar-nav-icon @click.stop="drawer = !drawer"/>
            <v-icon class="mx-4">fab fa-youtube</v-icon>
            <v-toolbar-title class="mr-12 align-center">
                <span class="title">{{ $t('HR_Easy')}}</span>
            </v-toolbar-title>
            <v-spacer/>
            <v-row
                    align="center"
                    style="max-width: 650px"
            >
            </v-row>
            <v-spacer/>
            <v-row v-if="username">
                <span class="v-toolbar__content">
                {{userDisplayName}}
                    </span>
                <v-btn v-on:click="logoutAction">{{ $t('Выход')}}</v-btn>
            </v-row>
            <v-row v-if="!username">
                <router-link to="/login" tag="button">{{ $t('Вход')}}</router-link>
            </v-row>

        </v-app-bar>

        <v-content>
            <v-container class="fill-height align-baseline">
                <router-view></router-view>
            </v-container>
        </v-content>

        <v-footer>
            <v-col
                    class="text-right"
                    cols="12"
            >
                {{ new Date().getFullYear() }} — <strong>Alexander Bondin</strong>
            </v-col>
        </v-footer>
    </v-app>
</template>

<script lang="ts">
    import {Component, Vue} from 'vue-property-decorator';
    import {Action, Getter} from "vuex-class";

    const namespace: string = 'auth';

    @Component
    export default class App extends Vue {
        drawer: boolean = true;

        @Action("logout", {namespace})
        logoutAction: any;

        @Getter("displayName", {namespace})
        userDisplayName: string | undefined;

        @Getter("username", {namespace})
        username: string | undefined;

        created() {
            this.$vuetify.theme.dark = true;
        }
    }
</script>

<style lang="scss">
    #app {
        font-family: 'Avenir', Helvetica, Arial, sans-serif;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        text-align: center;
        color: #2c3e50;
        margin-top: 60px;
    }
</style>
