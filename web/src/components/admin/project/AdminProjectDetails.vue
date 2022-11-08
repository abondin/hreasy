<!-- Project detailed page -->
<template>
  <v-container>
    <v-card v-if="project">

      <v-card-title>
        <v-btn text icon to="/admin/projects">
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
        <!-- Refresh button -->
        <v-btn text icon @click="fetchDetails()" :loading="loading">
          <v-icon>refresh</v-icon>
        </v-btn>
        <div>{{ $t('Подробная информация по проекту ') + project.name }}</div>
        <v-spacer></v-spacer>

        <!-- Update project -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on: ton, attrs: tattrs}">
            <div v-bind="tattrs" v-on="ton" class="col-auto">
              <v-btn text color="primary" :disabled="loading" @click="openUpdateDialog()" icon>
                <v-icon>mdi-pencil</v-icon>
              </v-btn>
            </div>
          </template>
          <span>{{ $t('Редактировать проект') }}</span>
        </v-tooltip>
      </v-card-title>
      <v-card-text>
        <project-card :project="project"></project-card>
      </v-card-text>
    </v-card>

    <v-dialog v-model="updateDialog">
      <admin-project-form
          ref="adminProjectForm"
          v-bind:input="project"
          :all-departments="allDepartments"
          :all-business-accounts="allBas"
          @close="updateDialog=false;fetchDetails()"></admin-project-form>
    </v-dialog>

    <!-- Managers -->
    <v-card class="mt-5 mb-5">
      <admin-managers
          ref="projectsManagersTable"
          v-if="project"
          :selected-object="responsibleObject"
          mode="compact"
          :title="$t('Менеджеры проекта')"></admin-managers>
    </v-card>


  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from "vue-class-component";
import logger from "@/logger";
import {Prop} from "vue-property-decorator";
import AdminManagers from "@/components/admin/manager/AdminManagers.vue";
import {ManagerResponsibilityObjectId} from "@/components/admin/manager/admin.manager.service";
import adminProjectService, {ProjectFullInfo} from "@/components/admin/project/admin.project.service";
import {Getter} from "vuex-class";
import {SimpleDict} from "@/store/modules/dict";
import {DateTimeUtils} from "@/components/datetimeutils";
import AdminProjectForm from "@/components/admin/project/AdminProjectForm.vue";
import ProjectInfoComponent from "@/components/shared/ProjectInfoComponent.vue";
import {AccessDeniedError} from "@/components/errors";
import router from "@/router";


const namespace_dict = 'dict';

@Component({
      components: {ProjectCard: ProjectInfoComponent, AdminProjectForm, AdminManagers}
    }
)
export default class AdminProjectDetails extends Vue {
  loading = false;
  updateDialog = false;

  @Prop({required: true})
  private projectId!: number;

  private project: ProjectFullInfo | null = null;

  @Getter("businessAccounts", {namespace: namespace_dict})
  private allBas!: Array<SimpleDict>;

  @Getter("departments", {namespace: namespace_dict})
  private allDepartments!: Array<SimpleDict>;

  /**
   * Lifecycle hook
   */
  created() {
    logger.log(`Admin project details component created for project with ID ${this.projectId}`);
    this.loading = true;
    this.$store.dispatch('dict/reloadDepartments')
        .then(() => this.$store.dispatch('dict/reloadBusinessAccounts'))
        .then(() => this.fetchDetails(false))
        .finally(() => this.loading = false);
  }

  private fetchDetails(showLoadingBar = true) {
    if (showLoadingBar) this.loading = true;
    this.project = null;
    return adminProjectService.get(this.projectId)
        .then(project => {
          this.project = project;
          if (this.$refs.projectsManagersTable) {
            (this.$refs.projectsManagersTable as AdminManagers).refresh();
          }
        })
        .catch(ex => {
          if (ex instanceof AccessDeniedError) {
            logger.error(`Has no access to see project ${this.projectId}. Return to project list`)
            this.$nextTick(() => router.push("/admin/projects"))
          }
          throw ex;
        })
        .finally(() => {
          if (showLoadingBar) this.loading = false;
        });
  }


  public openUpdateDialog() {
    this.updateDialog = true;
  }

  private get responsibleObject(): ManagerResponsibilityObjectId | null {
    return this.project ? {
      id: this.project.id,
      type: 'project'
    } : null;
  }

  private formatDate(date: string | undefined): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

}
</script>

