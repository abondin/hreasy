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
            <v-stepper-step step="1" editable>
              {{ $t('Загрузка файла') }}
            </v-stepper-step>
            <v-stepper-step step="2" editable>
              {{ $t('Конфигурация') }}
            </v-stepper-step>
            <v-stepper-step step="3" editable>
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
              <v-skeleton-loader v-if="loading"></v-skeleton-loader>
              <admin-employees-import-preview :workflow="workflow" v-else-if="workflow.importedRows"
                                              @back="step=2" @apply="applyChanges()"></admin-employees-import-preview>
              <v-alert v-else type="warning">
                {{ $t('Не удалось корректно обратотать файл. Загрузите другой или измените конфигурацию') }}
              </v-alert>
            </v-stepper-content>
            <!--</editor-fold>-->
            <!--<editor-fold desc="Success">-->
            <v-stepper-content step="4">
              <v-alert type="success">
                {{ $t('Изменения успешно применились!') }}
              </v-alert>
            </v-stepper-content>

            <!--</editor-fold>-->
          </v-stepper-items>
          <v-card-actions>
            <v-btn v-if="step>1 && step < 4" @click="step=(step-1)">
              <v-icon>mdi-arrow-left</v-icon>
                  {{ $t('Назад') }}</v-btn>
            <v-spacer></v-spacer>
            <v-btn v-if="step>1 || workflow.config" @click="apply()" :class="{primary:step==3}">
              {{ step==3 ? $t('Применить'): (step == 4 ? $t('Закрыть') : $t('Далее')) }}
              <v-icon>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-actions>
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
import AdminEmployeesImportPreview from "@/components/admin/employee/imp/AdminEmployeesImportPreview.vue";


@Component({
  components: {AdminEmployeesImportPreview, AdminEmployeesImportConfigForm, 'file-upload': MyFileUploader}
})
export default class AdminEmployeesImportWorkflowComponent extends Vue {
  loading = false;
  step: 1 | 2 | 3 | 4= 1;
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
    return this.wrapServerRequest(
        adminEmployeeImportService.getActiveOrStartNewImportProcess().then(data => {
          this.workflow = data;
          this.config = data.config || this.defaultConfig();
          this.refreshStep(this.workflow)
        }));
  }

  private getUploadImportFileUrl() {
    return adminEmployeeImportService.getUploadImportFileUrl(this.workflow!.id);
  }

  private apply() {
    switch (this.step) {
      case 1:
        this.uploadComplete();
        break;
      case 2:
        this.applyConfig()
        break;
      case 3:
        this.applyChanges();
        break;
      case 4:
        this.$router.push('/admin/employees');
    }
  }

  private uploadComplete() {
    return this.fetchData();
  }

  private applyConfig() {
    return this.wrapServerRequest(adminEmployeeImportService
        .applyConfigAndPreview(this.workflow!.id, this.config!)
        .then(data => {
          this.workflow = data;
          this.refreshStep(this.workflow);
        }));
  }

  private applyChanges() {
    return this.wrapServerRequest(
        adminEmployeeImportService.commit(this.workflow!.id)
            .then(data => {
              this.workflow = data;
              this.refreshStep(this.workflow);
            })
    );
  }

  private refreshStep(workflow: ImportEmployeesWorkflow) {
    this.step = Math.max(1, workflow.state + 1) as (1 | 2 | 3 | 4);
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

  private wrapServerRequest<T>(request: Promise<T>): Promise<string | T> {
    this.loading = true;
    return request
        .catch((er: any) => this.error = errorUtils.shortMessage(er))
        .finally(() => this.loading = false);
  }
}
</script>

<style scoped>

</style>
