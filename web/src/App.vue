<template>
  <v-app id="hreasy">
    <v-navigation-drawer
        app
        v-if="username"
        v-model="drawer">
      <v-list dense>
        <v-list-item @click.stop="drawer = !drawer">
          <span class="text-body-2">{{ userDisplayName }}</span>
        </v-list-item>

        <v-list-item link to="/profile/main">
          <v-list-item-action>
            <v-icon>mdi-account</v-icon>
          </v-list-item-action>
          <v-list-item-title>
            {{ $tc('Мой профиль') }}
          </v-list-item-title>
        </v-list-item>

        <v-divider></v-divider>


        <v-list-item to="/employees">
          <v-list-item-action>
            <v-icon>mdi-account-multiple</v-icon>
          </v-list-item-action>
          <v-list-item-title>
            {{ $tc('Сотрудники') }}
          </v-list-item-title>
        </v-list-item>

        <v-list-item to="/overtimes" v-if="canViewAllOvertimes()">
          <v-list-item-action>
            <v-icon>mdi-briefcase-clock</v-icon>
          </v-list-item-action>
          <v-list-item-title>
            {{ $tc('Овертаймы') }}
          </v-list-item-title>
        </v-list-item>

        <v-list-item link to="/vacations" v-if="canViewVacations()">
          <v-list-item-action>
            <v-icon>mdi-calendar-text</v-icon>
          </v-list-item-action>
          <v-list-item-title>
            {{ $tc('Отпуска') }}
          </v-list-item-title>
        </v-list-item>

        <v-list-item link to="/assessments" v-if="canCreateAssessments()">
          <v-list-item-action>
            <v-icon>mdi-book-check-outline</v-icon>
          </v-list-item-action>
          <v-list-item-title>
            {{ $tc('Ассессменты') }}
          </v-list-item-title>
        </v-list-item>

        <v-divider></v-divider>
        <v-list-group
            no-action
            v-if="canAdminEmployees() || canAdminProjects() || canAdminUsers() || canAdminBusinessAccounts() || canAdminArticles()"
        >
          <template v-slot:activator>
            <v-list-item-action>
              <v-icon>mdi-cogs</v-icon>
            </v-list-item-action>
            <v-list-item-content>
              <v-list-item-title>{{ $t('Админка') }}</v-list-item-title>
            </v-list-item-content>
          </template>

          <v-list-item to="/admin/employees" v-if="canAdminEmployees()">
            <v-list-item-title>
              {{ $t('Админка сотрудников') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item :to="'/admin/dicts/'+firstAvialableDict()" v-if="firstAvialableDict()">
            <v-list-item-title>
              {{ $t('Справочники') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item to="/admin/projects" v-if="canAdminProjects()">
            <v-list-item-title>
              {{ $t('Все проекты') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item to="/admin/ba" v-if="canAdminBusinessAccounts()">
            <v-list-item-title>
              {{ $t('Бизнес Аккаунты') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item to="/admin/users" v-if="canAdminUsers()">
            <v-list-item-title>
              {{ $t('Пользователи и роли') }}
            </v-list-item-title>
          </v-list-item>

          <v-list-item to="/admin/articles" v-if="canAdminArticles()">
            <v-list-item-title>
              {{ $t('Статьи и новости') }}
            </v-list-item-title>
          </v-list-item>

          <v-list-item to="/admin/managers" v-if="canAdminManagers()">
            <v-list-item-title>
              {{ $t('Все менеджеры') }}
            </v-list-item-title>
          </v-list-item>


        </v-list-group>


        <v-divider></v-divider>
        <v-list-item link v-on:click.stop="logout">
          <v-list-item-action>
            <v-icon>mdi-logout</v-icon>
          </v-list-item-action>
          <v-list-item-title>
            {{ $tc('Выход') }}
          </v-list-item-title>
        </v-list-item>
      </v-list>
      <template v-slot:append>
        <v-img src="@/assets/illustration.jpg"></v-img>
      </template>
    </v-navigation-drawer>
    <v-app-bar
        app>
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"/>
      <v-icon class="mx-4">fab fa-youtube</v-icon>
      <v-toolbar-title class="mr-12 align-center">
        <span class="title">{{ $t('HR_Easy') }}</span>
      </v-toolbar-title>
      <v-spacer/>
      <v-row
          align="center"
          style="max-width: 650px"
      >
      </v-row>
      <v-spacer/>

      <v-row v-if="!username">
        <router-link to="/login">{{ $t('Вход') }}</router-link>
      </v-row>
      <v-img v-else max-width="70px" max-height="50px" contain src="@/assets/logo-nav.png"></v-img>

      <v-alert
          v-if="unhandledrejection"
          type="error"
          dismissible>
        {{ unhandledrejection }}
      </v-alert>
    </v-app-bar>

    <v-main>
      <v-container>
        <keep-alive include="AdminProjects,VacationsList">
          <router-view></router-view>
        </keep-alive>
      </v-container>
    </v-main>

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
import moment from "moment";
import permissionService from "@/store/modules/permission.service";

const namespace_auth = 'auth';
const namespace_error = 'error';

@Component
export default class App extends Vue {
  drawer = false;

  @Action("logout", {namespace: namespace_auth})
  logoutAction!: () => Promise<any>;

  @Getter("displayName", {namespace: namespace_auth})
  userDisplayName: string | undefined;

  @Getter("username", {namespace: namespace_auth})
  username: string | undefined;

  @Getter("unhandledrejection", {namespace: namespace_error})
  unhandledrejection: undefined;

  created() {
    this.$vuetify.theme.dark = false;
    this.$vuetify.theme.themes.light.primary = '#E74C05';
    this.$vuetify.theme.themes.light.secondary = '#941680';
    moment.locale(this.$i18n.locale);
  }


  private logout() {
    return this.logoutAction().then(() => this.$router.push('/login'));
  }

  private canViewAllOvertimes() {
    return permissionService.canViewAllOvertimes();
  }

  private canViewVacations() {
    return permissionService.canViewAllVacations();
  }

  private canCreateAssessments() {
    return permissionService.canCreateAssessments();
  }

  private canAdminProjects() {
    return permissionService.canAdminProjects();
  }

  private canAdminEmployees() {
    return permissionService.canAdminEmployees();
  }

  private canAdminManagers() {
    return permissionService.canAdminManagers();
  }

  private canAdminBusinessAccounts() {
    return permissionService.canAdminBusinessAccounts();
  }

  private canAdminUsers() {
    return permissionService.canAdminUsers();
  }

  private canAdminArticles() {
    return permissionService.canAdminArticles();
  }

  private firstAvialableDict(): string | undefined {
    if (permissionService.canAdminDictDepartments()) {
      return "departments";
    }
    if (permissionService.canAdminDictLevels()) {
      return "levels";
    }
    if (permissionService.canAdminDictPositions()) {
      return "positions";
    }
    if (permissionService.canAdminDictOfficeLocations()) {
      return "office_locations";
    }
    return undefined;
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
