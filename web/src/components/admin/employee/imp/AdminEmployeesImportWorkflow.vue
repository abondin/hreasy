<!-- Employees short info table-->
<template>
  <div>
    <v-skeleton-loader v-if="loading" class="mx-auto" type="card"></v-skeleton-loader>
    <v-card v-if="error">
      <v-card-text>
        <v-alert class="error">{{ error }}</v-alert>
      </v-card-text>
    </v-card>

    <v-stepper v-model="step" v-if="workflow">
      <v-stepper-header>
        <v-stepper-step step="0" :complete="step>0">
          {{ $t('Загрузка файла') }}
        </v-stepper-step>
        <v-stepper-step step="1" :complete="step>1">
          {{ $t('Конфигурация') }}
        </v-stepper-step>
        <v-stepper-step step="2" :complete="step>2">
          {{ $t('Предпросмотр') }}
        </v-stepper-step>
      </v-stepper-header>
      <v-stepper-items>
        <v-stepper-content step="0">
          <file-upload
              :file-id="'process-'+workflow.id"
              :post-action="getUploadImportFileUrl()"
              @close="uploadComplete"
          ></file-upload>
        </v-stepper-content>
        <v-stepper-content step="1">
          Задайте конфигурацию
        </v-stepper-content>
        <v-stepper-content step="2">
          Подвердите результат
        </v-stepper-content>
      </v-stepper-items>
    </v-stepper>
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import adminEmployeeImportService, {ImportEmployeesWorkflow} from "@/components/admin/employee/imp/admin.employee.import.service";
import {errorUtils} from "@/components/errors";
import MyFileUploader from "@/components/shared/MyFileUploader.vue";


@Component({
  components: {'file-upload': MyFileUploader}
})
export default class AdminEmployeesImportWorkflowComponent extends Vue {
  loading = false;
  step = 0;
  workflow: ImportEmployeesWorkflow | null = null;
  error: string | null = null;

  /**
   * Lifecycle hook
   */
  created() {
    this.fetchData();
  }

  private fetchData() {
    this.loading = true;
    adminEmployeeImportService.getActiveOrStartNewImportProcess().then(data => {
      this.workflow = data;
      this.refreshStep(this.workflow)
    })
        .catch((er: any) => this.error = errorUtils.shortMessage(er))
        .finally(() => this.loading = false);
  }

  private getUploadImportFileUrl() {
    return adminEmployeeImportService.getUploadImportFileUrl(this.workflow!.id);
  }

  private uploadComplete() {
    return this.fetchData();
  }

  private refreshStep(workflow: ImportEmployeesWorkflow) {
    this.step = workflow.state;
  }

}
</script>

<style scoped>

</style>
