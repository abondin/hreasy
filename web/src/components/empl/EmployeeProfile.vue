<!-- Employees own profile page-->
<template>
  <v-container v-if="employee">
    <v-card class="d-flex flex-column flex-lg-row pa-5">
      <employee-avatar v-bind:employee="employee"></employee-avatar>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title">{{ employee.displayName }}</v-list-item-title>
          <v-list-item-subtitle>{{ $t('Отдел') }} :
            {{ employee.department ? employee.department.name : $t("Не задан") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>{{ $t('Текущий проект') }} :
            <span v-if="employee.currentProject">
              <v-btn
                  @click.stop="projectCardDialog=true" icon x-small color="info">
              <v-icon small>info</v-icon>
            </v-btn>
              {{ employee.currentProject.name }}
            </span>
            <span v-else>{{ $t("Не задан") }}</span>
          </v-list-item-subtitle>
          <v-list-item-subtitle>{{ $t('Роль на текущем проекте') }} :
            {{
              (employee.currentProject && employee.currentProject.role) ? employee.currentProject.role : $t("Не задана")
            }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>{{ $t('Бизнес Аккаунт') }} :
            {{ employee.ba ? employee.ba.name : $t("Не задан") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            {{ $t('Почтовый адрес') }} : {{ employee.email ? employee.email : $t("Не задан") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            {{ $t('Позиция') }} : {{ employee.position ? employee.position.name : $t("Не задана") }}
          </v-list-item-subtitle>
          <v-list-item-subtitle>
            {{ $t('Кабинет') }} : {{ employee.officeLocation ? employee.officeLocation.name : $t("Не задан") }}
          </v-list-item-subtitle>
          <!-- Telegram -->
          <v-list-item-subtitle>
            {{ $t('Телеграм') }} : {{ employee.telegram ? employee.telegram : $t("Не задан") }}
            <v-btn v-if="canUpdateTelegram()"
                   @click.stop="openUpdateTelegramDialog=true" icon x-small>
              <v-icon small>edit</v-icon>
            </v-btn>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-card>

    <!-- Project card dialog -->
    <v-dialog v-if="employee && employee.currentProject" v-model="projectCardDialog">
      <project-info-card-component :project-id="employee.currentProject.id"
                                   @close="projectCardDialog=false"></project-info-card-component>
    </v-dialog>

    <employee-overtime-component
        class="mt-5"
        :employee-id="employeeId"
        :selected-period="currentOvertimePeriod"
        :closed-periods="closedOvertimePeriods"></employee-overtime-component>

    <my-vacations class="mt-5"></my-vacations>

    <my-skills class="mt-5"></my-skills>

    <v-card class="mt-5">
      <v-card-title>
        <div>{{ $t('Квалификационные карточки') }}</div>
      </v-card-title>
      <v-card-text>
        <tech-profiles-chips ref="techProfileChips" :employee-id="employee.id"></tech-profiles-chips>
      </v-card-text>
    </v-card>


    <my-shared-articles class="mt-5"></my-shared-articles>

    <v-dialog
        v-model="openUpdateTelegramDialog"
        max-width="500">
      <employee-update-telegram v-bind:employee="employee"
                                v-on:close="openUpdateTelegramDialog=false"/>
    </v-dialog>

  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import employeeService, {Employee} from "@/components/empl/employee.service";
import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";
import {Getter} from "vuex-class";
import overtimeService, {ClosedOvertimePeriod, ReportPeriod} from "@/components/overtimes/overtime.service";
import EmployeeOvertimeComponent from "@/components/overtimes/EmployeeOvertimeComponent.vue";
import MyVacations from "@/components/vacations/MyVacations.vue";
import MySkills from "@/components/empl/skills/MySkills.vue";
import MySharedArticles from "@/components/article/MySharedArticles.vue";
import TechProfilesChips from "@/components/empl/TechProfilesChips.vue";
import EmployeeUpdateTelegram from "@/components/empl/EmployeeUpdateTelegram.vue";
import ProjectInfoCardComponent from "@/components/shared/ProjectInfoCardComponent.vue";

const namespace: string = 'auth';

@Component({
  components: {
    ProjectInfoCardComponent,
    EmployeeUpdateTelegram,
    TechProfilesChips,
    MySharedArticles,
    MySkills, "employee-avatar": EmployeeAvatarUploader, EmployeeOvertimeComponent, MyVacations
  }
})
export default class EmployeeProfile extends Vue {
  loading: boolean = false;

  private employee: Employee | null = null;
  private currentOvertimePeriod = ReportPeriod.currentPeriod();
  private closedOvertimePeriods: ClosedOvertimePeriod[] = [];
  private openUpdateTelegramDialog = false;

  private projectCardDialog = false;

  @Getter("employeeId", {namespace})
  employeeId!: number;

  /**
   * Lifecycle hook
   */
  created() {
    this.fetchData(this.employeeId);
  }

  private fetchData(employeeId: number) {
    this.loading = true;
    return employeeService.find(employeeId)
        .then(data => {
              this.employee = data;
              return this.employee;
            }
        ).then(() => {
          return overtimeService.getClosedOvertimes().then((data) => {
            this.closedOvertimePeriods = data;
          });
        }).then(() => {
          return this.$nextTick(()=>(this.$refs.techProfileChips as TechProfilesChips).loadTechProfiles());
        })
        .finally(() => {
          this.loading = false
        });
  }

  /**
   *
   * @private
   */
  private canUpdateTelegram() {
    return true;
  }

}
</script>

<style scoped>

</style>
