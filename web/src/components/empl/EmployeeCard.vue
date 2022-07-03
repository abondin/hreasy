<!--
Employee basic information card
Uses in Employees Table (Employees.vue)
 -->

<template>
  <v-card class="d-flex pa-2">
    <employee-avatar-uploader v-bind:employee="employee"/>
    <v-list-item class="align-self-baseline">
      <v-list-item-content>
        <!-- User display name -->
        <v-list-item-title class="title">{{ employee.displayName }}
        </v-list-item-title>
        <!-- Position and office location -->
        <v-list-item-subtitle v-if="employee.position">{{ employee.position.name }}</v-list-item-subtitle>
        <v-list-item-subtitle v-if="employee.officeLocation">{{ employee.officeLocation.name }}
        </v-list-item-subtitle>
        <v-list-item-subtitle v-if="employee.telegram">
          <a :href="$t('telegram_url', {account:employee.telegram})" target="_blank">
            <v-icon>telegram</v-icon>
            {{ employee.telegram }}
          </a>
        </v-list-item-subtitle>

        <!-- Current Project -->
        <v-list-item-subtitle>
          <span v-if="employee.currentProject">
            {{ employee.currentProject.name }}
            <span v-if="employee.ba">({{ employee.ba.name }})</span>
            <span v-if="employee.currentProject.role"> - {{ employee.currentProject.role }}</span>
          </span>
          <span v-else>{{ $tc('Проект не задан') }}</span>

          <v-btn v-if="canUpdateCurrentProject()"
                 @click.stop="openUpdateCurrentProjectDialog=true" icon x-small>
            <v-icon small>edit</v-icon>
          </v-btn>
        </v-list-item-subtitle>


        <!-- Additional Information (loaded on mount) -->
        <!-- Vacations -->
        <v-list-item-subtitle>
          {{ $t('Текущие и планируемые отпуска') }}:
          <v-progress-circular indeterminate v-if="vacationsLoading"></v-progress-circular>
          <span v-if="employeeVacations && employeeVacations.length > 0">
            <v-chip v-for="v in employeeVacations"
                    v-bind:key="v.id" class="mr-1" :color="v.current?'primary':''">{{
                formatDate(v.startDate)
              }} - {{ formatDate(v.endDate) }}</v-chip>
          </span>
        </v-list-item-subtitle>

        <!-- tech profiles -->
        <v-list-item-subtitle v-if="canDownloadTechProfiles()">
          {{ $t('Квалификационные карточки') }}:
          <tech-profiles-chips ref="techProfileChips" :employee-id="employee.id"/>
        </v-list-item-subtitle>

        <!-- Skills -->
        <v-list-item-subtitle v-if="employee && canViewSkills()">
          {{ $t('Навыки') }}:
          <skills-chips
              :skills="employee.skills"
              :employee-id="employee.id"
          ></skills-chips>
        </v-list-item-subtitle>


      </v-list-item-content>
    </v-list-item>


    <!-- Update current project dialog -->
    <v-dialog
        v-model="openUpdateCurrentProjectDialog"
        persistent
        max-width="500">
      <employee-update-current-project v-bind:employee="employee"
                                       v-on:submit="emitEmployeeUpdated();openUpdateCurrentProjectDialog=false"
                                      v-on:cancel="openUpdateCurrentProjectDialog=false"/>
    </v-dialog>
  </v-card>


</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Prop, Watch} from "vue-property-decorator";
import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";
import EmployeeUpdateCurrentProject from "@/components/empl/EmployeeUpdateCurrentProject.vue";
import permissionService from "@/store/modules/permission.service";
import SkillsChips from "@/components/empl/skills/SkillsChips.vue";
import vacationService, {EmployeeVacationShort} from "@/components/vacations/vacation.service";
import TechProfilesChips from "@/components/empl/TechProfilesChips.vue";
import {DateTimeUtils} from "@/components/datetimeutils";

@Component({
  components: {
    SkillsChips,
    TechProfilesChips,
    EmployeeAvatarUploader,
    EmployeeUpdateCurrentProject
  }
})
export default class EmployeeCard extends Vue {

  @Prop({required: true})
  employee!: Employee;

  openUpdateCurrentProjectDialog = false;

  vacationsLoading = false;

  employeeVacations: EmployeeVacationShort[] = [];

  private mounted() {
    this.loadAdditionalData();
  }

  @Watch("employee")
  private watchEmployee() {
    this.$nextTick(() => this.loadAdditionalData());
  }

  private loadAdditionalData() {
    this.loadVacations()
        .then(this.loadTechProfiles());
  }

  private loadVacations(): any {
    this.employeeVacations.length = 0;
    this.vacationsLoading = true;
    return vacationService.currentOrFutureVacations(this.employee.id).then(vacations => {
      this.employeeVacations = vacations;
      return vacations;
    }).finally(() => {
      this.vacationsLoading = false;
    });
  }

  private loadTechProfiles(): any {
    if (this.canDownloadTechProfiles() && this.$refs.techProfileChips) {
      return (this.$refs.techProfileChips as TechProfilesChips).loadTechProfiles();
    }
  }

  private canDownloadTechProfiles(): boolean {
    return permissionService.canDownloadTechProfiles(this.employee.id);
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private getAvatarUrl(employeeId: number) {
    return employeeService.getAvatarUrl(employeeId);
  }


  private canUpdateCurrentProject(): boolean {
    return permissionService.canUpdateCurrentProject(this.employee.id);
  }

  private emitEmployeeUpdated() {
    return this.$emit("employeeUpdated");
  }

  private canViewSkills() {
    return permissionService.canViewEmplSkills(this.employee.id);
  }

}
</script>

<style scoped>

</style>
