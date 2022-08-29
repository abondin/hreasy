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

@Component({
  components: {ProjectCard: ProjectInfoComponent}
})
export default class ProjectInfoCardComponent extends Vue {
  private project: ProjectFullInfo | ProjectInfo | null = null;

  @Prop({required: true})
  private projectId!: number;

  private loading = false;

  private error: string = '';

  /**
   * Lifecycle hook
   */
  created() {
    this.fetch();
  }

  @Watch("projectId")
  private watchProjectId(){
    this.fetch();
  }


  private fetch(){
    logger.log(`Fetch project card ${this.projectId}`)
    this.loading = true;
    this.error = '';
    dictService.getProjectCard(this.projectId)
        .then(data => {
          this.project = data
        })
        .catch(error => this.error = errorUtils.shortMessage(error))
        .finally(() => this.loading = false);
  }

  private emitClose() {
    this.$emit('close');
  }

}
</script>

<style scoped>

</style>
