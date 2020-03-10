<template>
    <v-container>
        <v-container
                class="fill-height"
                fluid
        >
            <v-row
                    align="center"
                    justify="center"
            >
                <v-col
                        cols="12"
                        sm="8"
                        md="4"
                >
                    <v-card class="elevation-12" :loading="loading">
                        <v-card-title>
                            {{ $t('Вход_в_систему')}}
                        </v-card-title>
                        <v-card-text>
                            <v-form>
                                <v-text-field
                                        v-model="loginField"
                                        :label="$t('Пользователь')"
                                        name="login"
                                        prepend-icon="person"
                                        type="text"
                                />

                                <v-text-field
                                        id="password"
                                        :label="$t('Пароль')"
                                        name="password"
                                        prepend-icon="lock"
                                        type="password"
                                        v-model="passwordField"
                                />
                            </v-form>
                        </v-card-text>
                        <v-card-text class="error--text">
                            {{responseError}}
                        </v-card-text>
                        <v-card-actions>
                            <v-spacer/>
                            <v-btn :disabled="loading" color="primary" v-on:click="login" type="submit">{{
                                $t('Войти')}}
                            </v-btn>
                            <!--                                <v-btn color="primary" v-on:click="currentUser">Current User</v-btn>-->
                            <!--                                <v-btn color="primary" v-on:click="getEmployees">Employees</v-btn>-->
                            <!--                                <v-btn color="warning" v-on:click="logout">Logout</v-btn>-->
                        </v-card-actions>
                    </v-card>
                </v-col>
            </v-row>
        </v-container>
    </v-container>
</template>

<script lang="ts">

    import Vue from 'vue'
    import Component from 'vue-class-component';
    import {AuthState} from "@/store/modules/auth";
    import {Action, State} from 'vuex-class';
    import {LoginRequest} from "@/store/modules/auth.service";
    import {AuthenticationError} from "@/components/errors";

    const namespace: string = 'auth';

    @Component
    export default class LoginComponent extends Vue {
        loginField: string = '';
        passwordField: string = '';

        private loading = false;

        public responseError: string = '';

        @State("auth")
        authState: AuthState | undefined;

        @Action("login", {namespace})
        loginAction: any;


        login() {
            this.responseError = '';
            this.wrapResponse(this.loginAction(new LoginRequest(this.loginField, this.passwordField)))
                .then(loginResponse => {
                    return this.$router.push("/");
                })

        }

        private wrapResponse(p: Promise<any>): Promise<any> {
            this.loading = true;
            return p.catch(error => {
                this.responseError = error.message;
                if (!(error instanceof AuthenticationError)) {
                    return Promise.reject(error);
                }
                return Promise.resolve();
            }).finally(() => {
                this.loading = false;
            });
        }
    }


</script>
