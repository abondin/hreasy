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
        <!-- vacations -->
        <v-list-item-subtitle>
          {{ $t('Текущие и планируемые отпуска') }}:
          <span v-if="employeeVacations && employeeVacations.length > 0">
            <v-chip v-for="v in employeeVacations"
                    v-bind:key="v.id" class="mr-1" :color="v.current?'primary':''">{{ formatDate(v.startDate) }} - {{ formatDate(v.endDate) }}</v-chip>
          </span>
          <v-btn v-else @click="loadVacations()" :disabled="employeeVacationsLoading"
                 x-small icon text color="primary">
            <v-icon>refresh</v-icon>
          </v-btn>
        </v-list-item-subtitle>

        <v-list-item-subtitle>{{
            employee.currentProject ? employee.currentProject.name : $tc('Проект не задан')
          }}
          <v-btn v-if="canUpdateCurrentProject()"
                 @click.stop="openUpdateCurrentProjectDialog=true" icon x-small>
            <v-icon small>edit</v-icon>
          </v-btn>
        </v-list-item-subtitle>
        <v-list-item-subtitle v-if="employee">
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
        max-width="500">
      <employee-update-current-project v-bind:employee="employee"
                                       v-on:close="openUpdateCurrentProjectDialog=false"/>
    </v-dialog>
  </v-card>


</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import employeeService, {Employee} from "@/components/empl/employee.service";
import {Prop} from "vue-property-decorator";
import EmployeeAvatarUploader from "@/components/empl/EmployeeAvatarUploader.vue";
import EmployeeUpdateCurrentProject from "@/components/empl/EmployeeUpdateCurrentProject.vue";
import permissionService from "@/store/modules/permission.service";
import SkillsChips from "@/components/empl/skills/SkillsChips.vue";
import vacationService, {EmployeeVacationShort} from "@/components/vacations/vacation.service";
import {DateTimeUtils} from "@/components/datetimeutils";

const namespace: string = 'auth';

@Component({
  components: {SkillsChips, EmployeeAvatarUploader, EmployeeUpdateCurrentProject}
})
export default class EmployeeCard extends Vue {

  @Prop({required: true})
  employee!: Employee;

  openUpdateCurrentProjectDialog = false;

  employeeVacations: EmployeeVacationShort[] = [];
  employeeVacationsLoading = false;

  private getAvatarUrl(employeeId: number) {
    return employeeService.getAvatarUrl(employeeId);
  }

  private canUpdateCurrentProject(): boolean {
    return permissionService.canUpdateCurrentProject(this.employee.id);
  }

  private loadVacations() {
    this.employeeVacationsLoading = true;
    this.employeeVacations.length = 0;
    vacationService.currentOrFutureVacations(this.employee.id).then(vacations => {
      this.employeeVacations = vacations;
    }).finally(() => {
      this.employeeVacationsLoading = false;
    });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }
}
</script>

<style scoped>

</style>
