<template>
  <div>
    <v-skeleton-loader v-if="loading" class="mx-auto" type="card"></v-skeleton-loader>
    <v-stepper v-model="step" v-else-if="workflow">
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
        <v-alert v-if="error" class="error">{{ error }}</v-alert>
        <!--<editor-fold desc="File uploader">-->
        <v-stepper-content step="1">
          <div class="text-center font-weight-bold" v-if="workflow.filename">
            {{ $t('Выбранный файл') }}: {{ workflow.filename }}
          </div>
          <file-upload
              :file-id="'process-'+workflow.id"
              :post-action="getUploadImportFileUrl()"
              @close="uploadComplete"
          ></file-upload>
        </v-stepper-content>
        <!--</editor-fold>-->
        <!--<editor-fold desc="Configuration">-->
        <v-stepper-content step="2" class="pa-0">
          <import-config-form :config="config" @back="step=1" @apply="updateConfig()">
          </import-config-form>
        </v-stepper-content>
        <!--</editor-fold>-->
        <!--<editor-fold desc="Preview">-->
        <v-stepper-content step="3" class="pa-0">
          <import-preview-component :workflow="workflow"
                                    :headers-loader="previewHeadersLoader"
                                    :filter-function="previewFilterFunction"
                                    v-if="workflow.importedRows"
                                    @back="step=2" @apply="applyChanges()"></import-preview-component>
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
      <!--<editor-fold desc="Actions buttons">-->
      <v-card-actions>
        <v-btn v-if="step>1 && step < 4" @click="goBack()">
          <v-icon>mdi-arrow-left</v-icon>
          {{ $t('Назад') }}
        </v-btn>
        <v-spacer></v-spacer>
        <v-btn v-if="step>1 || workflow.state>0" @click="apply()" :class="{primary:step==3}">
          {{ step == 3 ? $t('Применить') : (step == 4 ? $t('Закрыть') : $t('Далее')) }}
          <v-icon v-if="step!=4 && step!=3">mdi-arrow-right</v-icon>
        </v-btn>
      </v-card-actions>
      <!--</editor-fold>-->
    </v-stepper>

    <!--<editor-fold desc="Apply Dialog">-->
    <v-dialog v-model="applyDialog" width="500">
      <v-card>
        <v-card-title primary-title>{{ $t('Применение изменений в системе') }}</v-card-title>

        <v-card-text>
          {{ $t('Вы проверили все вносимые изменения и готовы применить их?') }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="applyDialog = false">{{ $t('Нет') }}</v-btn>
          <v-btn color="primary" @click="commit()">{{ $t('Да') }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <!--</editor-fold>-->
  </div>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {errorUtils} from "@/components/errors";
import MyFileUploader from "@/components/shared/MyFileUploader.vue";
import {ImportConfig, ImportExcelRow, ImportService, ImportWorkflow} from "@/components/admin/imp/import.base";
import ImportConfigForm from "@/components/admin/imp/ImportConfigForm.vue";
import {Prop} from "vue-property-decorator";
import ImportPreviewComponent, {
  ImportPreviewDataHeader,
  ImportPreviewFilter
} from "@/components/admin/imp/ImportPreviewComponent.vue";


@Component({
  components: {ImportPreviewComponent, ImportConfigForm, 'file-upload': MyFileUploader}
})
export default class ImportWorkflowComponent<C extends ImportConfig, R extends ImportExcelRow> extends Vue {
  @Prop({required: true})
  private onCompleteAction!: () => void;

  @Prop({required: true})
  private importService!: ImportService<C, R>;

  @Prop({required: true})
  private defaultConfigProvider!: () => C;

  @Prop({required: true})
  private previewHeadersLoader!: () => ImportPreviewDataHeader[];

  @Prop({required: true})
  private previewFilterFunction!: (rows: R[], filter: ImportPreviewFilter) => R[];

  loading = false;
  step: 1 | 2 | 3 | 4 = 1;
  config!: C;
  workflow: ImportWorkflow<C, R> | null = null;
  error: string | null = null;
  applyDialog = false;


  /**
   * Lifecycle hook
   */
  created() {
    console.log(`ImportWorkflowComponent created. importService=${this.importService}, defaultConfigProvider=${this.defaultConfigProvider}`, this);
    this.config = this.defaultConfigProvider();
    this.fetchData();
  }

  private fetchData() {
    return this.wrapServerRequest(
        this.importService.getActiveOrStartNewImportProcess().then(data => {
          this.workflow = data;
          this.config = data.config || this.defaultConfigProvider();
          this.refreshStep(this.workflow)
        }));
  }

  private getUploadImportFileUrl() {
    return this.importService.getUploadImportFileUrl(this.workflow!.id);
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
        this.applyDialog = true;
        break;
      case 4:
        this.onCompleteAction();
        break;
    }
  }

  private uploadComplete() {
    return this.fetchData();
  }

  private applyConfig() {
    return this.wrapServerRequest(this.importService
        .applyConfigAndPreview(this.workflow!.id, this.config!)
        .then(data => {
          this.workflow = data;
          this.refreshStep(this.workflow);
        }));
  }

  private commit() {
    this.applyDialog = false;
    return this.wrapServerRequest(
        this.importService.commit(this.workflow!.id)
            .then(data => {
              this.workflow = data;
              this.refreshStep(this.workflow);
            })
    );
  }

  private refreshStep(workflow: ImportWorkflow<C, R>) {
    this.step = Math.max(1, workflow.state + 1) as (1 | 2 | 3 | 4);
  }

  private goBack() {
    if (this.step > 0) {
      this.error = null;
      this.step = (this.step - 1) as (1 | 2 | 3 | 4);
    }
  }


  private wrapServerRequest<T>(request: Promise<T>): Promise<string | T> {
    this.loading = true;
    this.error = null;
    return request
        .catch((er: any) => this.error = errorUtils.shortMessage(er))
        .finally(() => this.loading = false);
  }
}
</script>

<style scoped>

</style>
