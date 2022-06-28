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
      <v-text-field v-model="roleOnProject"
                    :counter="64"
                    :rules="[v=>(!v || v.length <= 64 || $t('Не более N символов', {n:64}))]"
                    :label="$t('Позиция на проекте')">


      </v-text-field>
      <div class="error" v-if="error">{{ error }}</div>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
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
import {Prop} from "vue-property-decorator";
import employeeService, {Employee} from "./employee.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import Component from "vue-class-component";

const namespace: string = 'dict';

@Component({})
export default class EmployeeUpdateCurrentProject extends Vue {
  @Prop({required: true})
  employee!: Employee;

  @Getter("projects", {namespace})
  private allProjects!: Array<SimpleDict>;

  private selectedProject: number | null = null;

  private roleOnProject: string | null = null;

  error: string | null = null;

  /**
   * Lifecycle hook
   */
  created() {
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
    if (!this.selectedProject){
      this.roleOnProject = null;
    }
    employeeService.updateCurrentProject(this.employee.id, this.selectedProject, this.roleOnProject)
        .then(() => {
          this.$emit('submit');
        })
        .catch(e => this.error = e);
  }
}
</script>

<style scoped>

</style>
