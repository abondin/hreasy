<!-- Detailed information for assessment. Forms and attachments-->
<template>
  <v-container>
    <v-card v-if="fetchError">
      <v-card-text>
        <v-alert class="error">{{ fetchError }}</v-alert>
      </v-card-text>
    </v-card>
    <v-card v-else-if="accessDenied">
      <v-card-text>
        <v-alert class="error">{{ $t('Недостаточно прав для просмотра детальной информации по ассессменту') }}</v-alert>
        <router-link :to="'/assessments/'+employeeId">
          {{ $t('Вернуться к карточке сотрудника') }}
        </router-link>
      </v-card-text>
    </v-card>
    <div v-else-if="assessment">
      <router-link to="/assessments">{{ $t('Ассессменты') }}</router-link>
      /
      <router-link :to="'/assessments/'+employeeId"> {{ assessment.employee.name }}</router-link>
      / {{ formatDate(assessment.plannedDate) }}
      <v-card class="mt-5">
        <v-card-text>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="title d-flex">
                <span>{{ assessment.employee.name }}</span>
                <v-spacer></v-spacer>
                <!-- Complete Assessment -->
                <v-tooltip bottom>
                  <template v-slot:activator="{ on: ton, attrs: tattrs}">
                    <v-btn @click="completeAssessmentDialog=true"
                           v-bind="tattrs" v-on="ton" class="col-auto" color="success"
                           :disabled="Boolean(loading || assessment.completedBy)" icon>
                      <v-icon>mdi-checkbox-marked-circle</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ $t('Отметить ассессмент как завершенный') }}</span>
                </v-tooltip>

                <!-- Cancel Assessment -->
                <v-tooltip bottom>
                  <template v-slot:activator="{ on: ton, attrs: tattrs}">
                    <v-btn @click="cancelAssessmentDialog=true"
                           v-bind="tattrs" v-on="ton" class="col-auto" color="error"
                           :disabled="Boolean(loading || assessment.canceledBy)" icon>
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ $t('Отменить ассессмент') }}</span>
                </v-tooltip>
              </v-list-item-title>
              <v-list-item-subtitle>{{ $t('Дата ассессмента') }} :
                <span :class="{'grey--text text-decoration-line-through': assessment.canceledBy}">
                  {{ formatDate(assessment.plannedDate) }}
                </span>
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Создано') }} :
                {{
                  assessment.createdBy ? `${formatDateTime(assessment.createdAt)} (${assessment.createdBy.name})` : '-'
                }}
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Завершено') }} :
                {{
                  assessment.completedBy ? `${formatDateTime(assessment.completedAt)} (${assessment.completedBy.name})` : '-'
                }}
              </v-list-item-subtitle>
              <v-list-item-subtitle>{{ $t('Отменено') }} :
                {{
                  assessment.canceledBy ? `${formatDateTime(assessment.canceledAt)} (${assessment.canceledBy.name})` : '-'
                }}
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
          <v-card-text>


            <!-- Attachments -->
            <span class="font-weight-bold">{{ $t('Вложения') }}:</span>
            <v-chip outlined link class="ml-5"
                    v-for="attachment in assessment.attachmentsFilenames" v-bind:key="attachment">
              <a :href="getAttachmentPath(attachment)" :download="attachment">{{ attachment }}</a>
              <v-icon @click="openDeleteAttachmentDialog(attachment)" class="ml-3">mdi-delete</v-icon>
            </v-chip>
            <v-chip outlined link class="ml-5" @click="uploadAttachmentDialog=true">
              <v-icon>mdi-plus</v-icon>
              {{ $t('Прикрепить файл') }}
            </v-chip>
            <v-dialog v-model="uploadAttachmentDialog" persistent>
              <file-upload
                  :file-id="'assessment-'+assessmentId"
                  :post-action="getAttachmentUploadPath()"
                  @close="uploadComplete"></file-upload>
            </v-dialog>

          </v-card-text>
        </v-card-text>

        <!-- Delete attachment dialog-->
        <v-dialog v-model="deleteAttachmentDialog" width="500">
          <v-card>
            <v-card-title primary-title>
              {{ $t('Удаление') }}
            </v-card-title>

            <v-card-text>
              <p>
                {{ $t('Вы уверены, что хотите удалить вложение?') }}
              </p>
              {{ attachmentToDelete }}
              <v-alert v-if="deleteAttachmentError" class="error">{{ deleteAttachmentError }}</v-alert>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn class="primary" text @click="deleteAttachment">
                {{ $t('Да') }}
              </v-btn>
              <v-btn text @click="attachmentToDelete=null;deleteAttachmentDialog=false">
                {{ $t('Нет') }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>


        <!-- Cancel assessment dialog-->
        <v-dialog v-model="cancelAssessmentDialog" width="500">
          <v-card>
            <v-card-title primary-title>
              {{ $t('Отмена') }}
            </v-card-title>

            <v-card-text>
              <p>
                {{ $t('Вы уверены, что хотите отменить ассессмент?') }}
              </p>
              <v-alert v-if="cancelAssessmentError" class="error">{{ cancelAssessmentError }}</v-alert>
            </v-card-text>
            <v-card-actions>
              <v-progress-circular class="mr-2" v-if="loading" indeterminate></v-progress-circular>
              <v-spacer></v-spacer>
              <v-btn class="primary" text @click="cancelAssessment">
                {{ $t('Да') }}
              </v-btn>
              <v-btn text @click="cancelAssessmentDialog=false">
                {{ $t('Нет') }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>


        <!-- Complete assessment dialog-->
        <v-dialog v-model="completeAssessmentDialog" width="500">
          <v-card>
            <v-card-title primary-title>
              {{ $t('Отметить как завершенный') }}
            </v-card-title>

            <v-card-text>
              <p>
                {{ $t('Отметить ассессмент как завершенный?') }}
              </p>
              <v-alert v-if="completeAssessmentError" class="error">{{ completeAssessmentError }}</v-alert>
            </v-card-text>
            <v-card-actions>
              <v-progress-circular class="mr-2" v-if="loading" indeterminate></v-progress-circular>
              <v-spacer></v-spacer>
              <v-btn class="primary" text @click="completeAssessment">
                {{ $t('Да') }}
              </v-btn>
              <v-btn text @click="completeAssessmentDialog=false">
                {{ $t('Нет') }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>


      </v-card>
    </div>
  </v-container>
</template>


<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component';
import {DateTimeUtils} from "@/components/datetimeutils";
import assessmentService, {AssessmentWithFormsAndFiles} from "@/components/assessment/assessment.service";
import {Prop} from "vue-property-decorator";
import MyFileUploader, {UploadCompleteEvent} from "@/components/shared/MyFileUploader.vue";
import logger from "@/logger";
import {errorUtils} from "@/components/errors";


@Component({
  components: {'file-upload': MyFileUploader}
})
export default class AssessmentDetailedVue extends Vue {
  uploadAttachmentDialog = false;
  deleteAttachmentDialog = false;

  attachmentToDelete: string | null = null;
  deleteAttachmentError = '';

  cancelAssessmentDialog = false;
  cancelAssessmentError : string|null = null;

  completeAssessmentDialog = false;
  completeAssessmentError : string|null = null;

  loading = false;
  accessDenied = false;
  assessment: AssessmentWithFormsAndFiles | null = null;

  fetchError: string | null = null;

  @Prop({required: true})
  employeeId!: number;

  @Prop({required: true})
  assessmentId!: number;

  /**
   * Lifecycle hook
   */
  created() {
    return this.fetchData();
  }


  private fetchData() {
    this.loading = true;
    this.accessDenied = false;
    this.fetchError = null;
    return assessmentService.assessmentWithFormsAndFiles(this.employeeId, this.assessmentId)
        .then(data => {
              this.assessment = data;
              return this.assessment;
            }
        ).catch((error: any) => {
          if (errorUtils.isAccessDenied(error)) {
            this.accessDenied = true;
          } else {
            this.fetchError = errorUtils.shortMessage(error);
          }
        })
        .finally(() => {
          this.loading = false
        });
  }

  private getAttachmentUploadPath() {
    return assessmentService.getUploadAttachmentUrl(this.employeeId, this.assessmentId)
  }

  private getAttachmentPath(fileName: string) {
    return assessmentService.getAttachmentPath(this.employeeId, this.assessmentId, fileName, this.assessment!.attachmentsAccessToken)
  }

  private openDeleteAttachmentDialog(fileName: string) {
    this.attachmentToDelete = fileName;
    this.deleteAttachmentDialog = true;
  }

  private deleteAttachment() {
    this.deleteAttachmentError = '';
    if (this.attachmentToDelete) {
      return assessmentService.deleteAttachment(this.employeeId, this.assessmentId, this.attachmentToDelete)
          .then(result => {
            if (result.deleted) {
              this.deleteAttachmentDialog = false;
              return this.fetchData();
            } else {
              logger.error(`Unable to delete file ${this.attachmentToDelete}`);
              //TODO Send reason from backend
              this.deleteAttachmentError = 'Unable to delete file';
            }
          });
    }
  }

  private uploadComplete(event: UploadCompleteEvent) {
    logger.log(`Upload complete ${event}`);
    this.uploadAttachmentDialog = false;
    if (event && event.uploaded) {
      this.fetchData();
    }
  }

  private cancelAssessment() {
    logger.log(`Cancel assessment ${this.employeeId}:${this.assessmentId}`);
    this.cancelAssessmentError=null;
    this.loading = true;
    assessmentService.cancelAssessment(this.employeeId, this.assessmentId)
        .then(() => {
          this.cancelAssessmentDialog = false;
          this.fetchData();
        })
        .catch((error: any)=>{
          this.cancelAssessmentError = errorUtils.shortMessage(error);
        })
        .finally(()=>{
          this.loading = false;
    });
  }

  private completeAssessment() {
    logger.log(`Mark assessment as completed ${this.employeeId}:${this.assessmentId}`);
    this.completeAssessmentError=null;
    this.loading = true;
    assessmentService.completeAssessment(this.employeeId, this.assessmentId)
        .then(() => {
          this.completeAssessmentDialog = false;
          this.fetchData();
        })
        .catch((error: any)=>{
          this.completeAssessmentError = errorUtils.shortMessage(error);
        })
        .finally(()=>{
          this.loading = false;
        });
  }

  private formatDate(date: string): string | undefined {
    return DateTimeUtils.formatFromIso(date);
  }

  private formatDateTime(date: string): string | undefined {
    return DateTimeUtils.formatDateTimeFromIso(date);
  }

}
</script>

<style scoped>

</style>
