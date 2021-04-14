<!--
Employee basic information card
Uses in Employees Table (Employees.vue)
 -->

<template>
  <v-card class="d-flex pa-2">
    <employee-avatar-uploader v-bind:employee="employee"/>
    <v-list-item class="align-self-center">
      <v-list-item-content>
        <v-list-item-title class="title">{{ employee.displayName }}
        </v-list-item-title>
        <v-list-item-subtitle v-if="employee.position">{{ employee.position.name }}</v-list-item-subtitle>
        <v-list-item-subtitle v-if="employee.officeLocation">{{ employee.officeLocation.name }}
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
          {{$t('Навыки')}}: <skills-chips
            :skills="employee.skills"
            :employee-id="employee.id"
          ></skills-chips>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
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

const namespace: string = 'auth';

@Component({
  components: {SkillsChips, EmployeeAvatarUploader, EmployeeUpdateCurrentProject}
})
export default class EmployeeCard extends Vue {

  @Prop({required: true})
  employee!: Employee;

  openUpdateCurrentProjectDialog = false;

  private getAvatarUrl(employeeId: number) {
    return employeeService.getAvatarUrl(employeeId);
  }

  private canUpdateCurrentProject(): boolean {
    return permissionService.canUpdateCurrentProject(this.employee.id);
  }
}
</script>

<style scoped>

</style>
