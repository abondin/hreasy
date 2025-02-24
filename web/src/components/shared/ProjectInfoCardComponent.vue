<template>
  <v-card :loading="loading">
    <v-card-title>
      {{ $t('Подробная информация по проекту ') }}
      <v-spacer></v-spacer>
      <v-btn icon @click="emitClose()">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>
    <v-alert type="error" v-if="error">{{ error }}</v-alert>
    <project-card v-if="project" :project="project" max-height="400px"></project-card>


    <v-timeline>
      <v-timeline-item
          class="pl-5 pr-5"
          v-for="change in projectChanges"
          :key="change.id"
      >
        <template v-slot:opposite>
           <span>{{ formatDateTime(change.changedAt) }}</span>
          <p class="text-caption grey--text" v-if="change.changedBy">{{ change.changedBy?.name }}</p>
        </template>
        <v-card class="pa-1">
          <v-card-text class="pa-1">
            <p class="mb-0">
              <v-icon small class="mr-1">mdi-briefcase</v-icon>
              {{ change.project?.name || '-' }}
              / <span v-if="change.project?.role" class="mb-0">{{ change.project.role }}</span>
            </p>
            <p class="mb-0" v-if="change.ba">
              <v-icon small class="mr-1">mdi-account-tie</v-icon>
              {{ change.ba.name }}
            </p>
          </v-card-text>
        </v-card>
      </v-timeline-item>
    </v-timeline>
  </v-card>
</template>

<script lang="ts">
import Vue from "vue";
import {ProjectFullInfo} from "../admin/project/admin.project.service";
import {Prop, Watch} from "vue-property-decorator";
import Component from "vue-class-component";
import dictService, {ProjectInfo} from "@/store/modules/dict.service";
import {errorUtils} from "@/components/errors";
import ProjectInfoComponent from "@/components/shared/ProjectInfoComponent.vue";
import logger from "@/logger";
import employeeService, {EmployeeProjectChanges} from "@/components/empl/employee.service";
import {DateTimeUtils} from "@/components/datetimeutils";

@Component({
  components: {ProjectCard: ProjectInfoComponent}
})
export default class ProjectInfoCardComponent extends Vue {
  private project: ProjectFullInfo | ProjectInfo | null = null;
  private projectChanges: EmployeeProjectChanges[] = [];

  @Prop({required: true})
  private employeeId!: number;

  @Prop({required: true})
  private projectId!: number;

  private loading = false;

  private error = '';


  /**
   * Lifecycle hook
   */
  created() {
    this.fetch();
  }

  @Watch("projectId")
  private watchProjectId() {
    this.fetch();
  }


  private fetch() {
    logger.log(`Fetch project card ${this.projectId}`)
    this.loading = true;
    this.error = '';
    dictService.getProjectCard(this.projectId)
        .then(data => {
          this.project = data;
          return employeeService.employeeProjectChanges(this.employeeId).then(d => {
            this.projectChanges = d;
          })
        })
        .catch(error => this.error = errorUtils.shortMessage(error))
        .finally(() => this.loading = false);
  }

  private emitClose() {
    this.$emit('close');
  }

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

}
</script>

<style scoped>

</style>
