<!-- Form to update current employee project assigment -->
<template>
  <v-card>
    <v-card-title>{{ $t('Обновление текущего проекта') }}</v-card-title>
    <v-card-subtitle>{{ employee.displayName }}</v-card-subtitle>
    <v-card-text>
      <v-autocomplete
          autofocus
          clearable
          v-model="selectedProject"
          item-text="name"
          item-value="id"
          :items="allProjects.filter(p=>p.active)"
          :label="$tc('Проекты')"
      ></v-autocomplete>
      <v-combobox
          @input.native="e => roleOnProject = e.target.value"
          :label="$t('Позиция на проекте')"
          :items="currentProjectsRoles()"
          clearable
          :multiple="false"
          :menu-props="{closeOnClick: true, closeOnContentClick:true}"
          :rules="[v=>(!v || v.length <= 64 || $t('Не более N символов', {n:64}))]"
          v-model="roleOnProject">
      </v-combobox>
      <div class="error" v-if="error">{{ error }}</div>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
          text
          @click="cancel"
      >
        {{ $t('Отменить') }}
      </v-btn>
      <v-btn
          color="primary"
          text
          @click="update"
      >
        {{ $t('Применить') }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Vue from "vue";
import {Prop, Watch} from "vue-property-decorator";
import employeeService, {Employee} from "./employee.service";
import {Getter} from "vuex-class";
import {CurrentProjectRole, SimpleDict} from "@/store/modules/dict";
import Component from "vue-class-component";

const namespace = 'dict';

@Component({})
export default class EmployeeUpdateCurrentProject extends Vue {
  @Prop({required: true})
  employee!: Employee;

  @Getter("projects", {namespace})
  private allProjects!: Array<SimpleDict>;

  @Getter("currentProjectRoles", {namespace})
  private allCurrentProjectRoles!: Array<CurrentProjectRole>;

  private selectedProject: number | null = null;

  private roleOnProject: string | null = null;

  error: string | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    this.reset();
  }

  @Watch("employee")
  watchEmployee() {
    this.reset();
  }

  private reset() {
    if (this.employee && this.employee.currentProject) {
      const currentProject = this.employee.currentProject
      this.selectedProject = currentProject.id;
      this.roleOnProject = currentProject.role;
    } else {
      this.selectedProject = null;
      this.roleOnProject = null;
    }
  }

  update() {
    if (!this.selectedProject) {
      this.roleOnProject = null;
    }
    employeeService.updateCurrentProject(this.employee.id, this.selectedProject, this.roleOnProject)
        .then(() => {
          // Update current project roles dictionary if new value added
          if (this.roleOnProject && this.currentProjectsRoles().indexOf(this.roleOnProject) < 0) {
            this.$nextTick(() => {
              this.$store.dispatch('dict/reloadCurrentProjectRoles');
            });
          }
          this.$emit('submit');
        })
        .catch(e => this.error = e);
  }

  cancel() {
    this.reset();
    this.$emit("cancel");
  }

  private currentProjectsRoles() {
    return this.allCurrentProjectRoles.map(r => r.value);
  }
}
</script>

<style scoped>

</style>
