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
        <v-stepper-step step="1">
          {{ $t('Загрузка файла') }}
        </v-stepper-step>
        <v-stepper-step step="2">
          {{ $t('Конфигурация') }}
        </v-stepper-step>
        <v-stepper-step step="3">
          {{ $t('Предпросмотр') }}
        </v-stepper-step>
      </v-stepper-header>
      <v-stepper-items>
        <!--<editor-fold desc="File uploader">-->
        <v-stepper-content step="1">
          <file-upload
              :file-id="'process-'+workflow.id"
              :post-action="getUploadImportFileUrl()"
              @close="uploadComplete"
          ></file-upload>
        </v-stepper-content>
        <!--</editor-fold>-->

        <!--<editor-fold desc="Configuration">-->
        <v-stepper-content step="2">
          <admin-employees-import-config-form :config="config" @back="step=1" @apply="updateConfig()">
          </admin-employees-import-config-form>
        </v-stepper-content>
        <!--</editor-fold>-->

        <!--<editor-fold desc="Preview">-->
        <v-stepper-content step="3">
          Подвердите результат
        </v-stepper-content>

        <!--</editor-fold>-->
      </v-stepper-items>
    </v-stepper>
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import adminEmployeeImportService, {
  EmployeeImportConfig,
  ImportEmployeesWorkflow
} from "@/components/admin/employee/imp/admin.employee.import.service";
import {errorUtils} from "@/components/errors";
import MyFileUploader from "@/components/shared/MyFileUploader.vue";
import AdminEmployeesImportConfigForm from "@/components/admin/employee/imp/AdminEmployeesImportConfigForm.vue";


@Component({
  components: {AdminEmployeesImportConfigForm, 'file-upload': MyFileUploader}
})
export default class AdminEmployeesImportWorkflowComponent extends Vue {
  loading = false;
  step = 1;
  config: EmployeeImportConfig = this.defaultConfig();
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
      this.config = data.config || this.defaultConfig();
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

  private updateConfig() {
    return adminEmployeeImportService
        .applyConfigAndPreview(this.workflow!.id, this.config!)
        .then(data => {
          this.workflow = data;
          this.refreshStep(this.workflow);
        });
  }

  private refreshStep(workflow: ImportEmployeesWorkflow) {
    this.step = Math.max(1, workflow.state + 1);
  }

  defaultConfig(): EmployeeImportConfig {
    return {
      sheetNumber: 1,
      tableStartRow: 11,
      columns: {
        displayName: 'B',
        externalErpId: 'H',
        email: 'Q',
        phone: 'G',
        department: 'H',
        position: 'I',
        dateOfEmployment: 'J',
        dateOfDismissal: 'K',
        birthday: 'M',
        sex: 'N',
        documentSeries: 'P',
        documentNumber: 'Q',
        documentIssuedDate: 'R',
        documentIssuedBy: 'S',
        registrationAddress: 'U',
      }
    }
  }

}
</script>

<style scoped>

</style>