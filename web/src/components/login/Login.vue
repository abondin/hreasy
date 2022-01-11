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
            <v-form v-on:submit.prevent="login">
              <v-card-title>
                {{ $t('Вход_в_систему') }}
              </v-card-title>
              <v-card-text>
                <v-text-field
                    v-model="loginField"
                    :label="$t('E-mail')"
                    name="login"
                    autofocus
                    autocomplete="current-login"
                    prepend-icon="email"
                    type="text"
                />

                <v-text-field
                    id="password"
                    :label="$t('Пароль')"
                    name="password"
                    prepend-icon="lock"
                    type="password"
                    autocomplete="current-password"
                    v-model="passwordField"
                />
              </v-card-text>
              <v-card-text class="error--text">
                {{ responseError }}
              </v-card-text>
              <v-card-actions>
                <v-spacer/>
                <v-btn :disabled="loading" color="primary" type="submit">{{
                    $t('Войти')
                  }}
                </v-btn>
                <!--                                <v-btn color="primary" v-on:click="currentUser">Current User</v-btn>-->
                <!--                                <v-btn color="primary" v-on:click="getEmployees">Employees</v-btn>-->
                <!--                                <v-btn color="warning" v-on:click="logout">Logout</v-btn>-->
              </v-card-actions>
            </v-form>
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
import {errorUtils} from "@/components/errors";

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
    this.loading = true;
    return this.loginAction(new LoginRequest(this.loginField, this.passwordField))
        .then(() => {
          return this.$router.push("/");
        })
        .catch((error: any) => {
              this.responseError = errorUtils.shortMessage(error);
            }
        )
        .finally(() => {
              this.loading = false;
            }
        );
  }

}


</script>
