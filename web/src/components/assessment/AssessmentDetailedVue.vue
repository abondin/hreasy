<!-- Detailed information for assessment. Forms and attachments-->
<template>
  <v-container>
    <v-card v-if="accessDenied">
      <v-card-text>
        <v-alert class="error">{{ $t('Недостаточно прав для просмотра детальной информации по ассессменту') }}</v-alert>
        <router-link :to="`/assessments/${employeeId}`">{{ $t('Вернуться к списку ассессментов') }}</router-link>
      </v-card-text>
    </v-card>
    <v-card v-else-if="assessment">
      <v-card-title>
        <router-link to="/assessments/${employeeId}">
          <v-icon class="mr-5">mdi-arrow-left</v-icon>
        </router-link>
        {{ $t('Ассессмент сотрудника') }}
      </v-card-title>
      <v-card>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title">{{ assessment.employee.name }}</v-list-item-title>
            <v-list-item-subtitle>{{ $t('Дата ассессмента') }} :
              {{ formatDate(assessment.plannedDate) }}
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
            <v-icon @click="deleteAttachment(attachment)">mdi-delete</v-icon>
          </v-chip>
          <v-chip outlined link class="ml-5" @click="uploadAttachmentDialog=true">
            <v-icon>mdi-plus</v-icon>
            {{ $t('Прикрепить файл') }}
          </v-chip>
          <v-dialog v-model="uploadAttachmentDialog" persistent>
            <file-upload
                :post-action="getAttachmentUploadPath()"
                @close="uploadComplete"></file-upload>
          </v-dialog>
        </v-card-text>
      </v-card>
    </v-card>
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


@Component({
  components: {'file-upload': MyFileUploader}
})
export default class AssessmentDetailedVue extends Vue {
  uploadAttachmentDialog = false;
  loading: boolean = false;
  accessDenied = false;
  assessment: AssessmentWithFormsAndFiles | null = null;

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
    return assessmentService.assessmentWithFormsAndFiles(this.employeeId, this.assessmentId)
        .then(data => {
              this.assessment = data;
              return this.assessment;
            }
        ).catch((error: any) => {
          if (error) {
            this.accessDenied = true;
          } else {
            throw error;
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

  private deleteAttachment(fileName: string) {
    return assessmentService.deleteAttachment(this.employeeId, this.assessmentId, fileName)
        .then(result => {
          if (result.deleted) {
            return this.fetchData();
          } else {
            logger.error(`Unable to delete file ${fileName}`);
          }
        });
  }

  private uploadComplete(event: UploadCompleteEvent) {
    logger.log(`Upload complete ${event}`);
    this.uploadAttachmentDialog = false;
    if (event && event.uploaded) {
      this.fetchData();
    }
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
