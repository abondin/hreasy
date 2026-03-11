<template>
  <v-container fluid class="py-6">
    <div class="mx-auto" style="max-width: 1360px">
      <v-btn variant="text" prepend-icon="mdi-arrow-left" :to="{ name: 'mentorship' }">
        {{ t("Менторство") }}
      </v-btn>

      <v-alert v-if="error" type="error" class="mt-4" variant="tonal" border="start">
        {{ error }}
      </v-alert>

      <template v-else-if="junior">
        <junior-details-info-card
          class="mt-4"
          :junior="junior"
          :employee="employee"
          :loading="loading"
          :can-edit-registry="canEditRegistry"
          @edit="openEditDialog"
          @graduate="graduateDialog = true"
          @delete="deleteDialog = true"
        />

        <div class="mt-8">
          <junior-reports-section
            :junior="junior"
            :loading="loading"
            :action-loading="actionLoading"
            :can-view-mentorship="canViewMentorship"
            :sorted-reports="sortedReports"
            :can-edit-report="canEditReport"
            :can-delete-report="canDeleteReport"
            :get-progress-icon="getProgressIcon"
            :report-dialog="reportDialog"
            :delete-report-dialog="deleteReportDialog"
            :editing-report-id="editingReportId"
            :report-form="reportForm"
            :report-form-error="reportFormError"
            :progress-options="progressOptions"
            :rating-fields="ratingFields"
            @open-create-report="openCreateReport"
            @open-edit-report="openEditReport"
            @open-delete-report="openDeleteReport"
            @close-report="closeReportDialog"
            @submit-report="submitReport"
            @close-delete-report="closeDeleteReport"
            @confirm-delete-report="confirmDeleteReport"
          />
        </div>
      </template>
    </div>

    <v-dialog v-model="editDialog" max-width="760">
      <v-card>
        <v-card-title>{{ t("Обновление записи в реестре") }}</v-card-title>
        <v-card-text>
          <junior-registry-form-fields
            :form="updateForm"
            mode="update"
            :employees="employees"
            :business-accounts="allBusinessAccounts"
            :project-roles="projectRoles"
            :current-mentor="junior?.mentor ?? null"
            :current-budgeting-account="junior?.budgetingAccount ?? null"
            :current-role="junior?.role ?? null"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="editDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitUpdateJunior">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <confirm-delete-dialog
      :open="deleteDialog"
      :title="t('удаление')"
      :message="t('Вы уверены, что хотите удалить запись?')"
      :loading="actionLoading"
      @close="deleteDialog = false"
      @confirm="deleteJunior"
    />

    <v-dialog v-model="graduateDialog" max-width="620">
      <v-card>
        <v-card-title>
          {{ junior?.graduation ? t("Отменить факт окончания обучения") : t("Завершение обучения") }}
        </v-card-title>
        <v-card-text>
          <v-textarea
            v-if="!junior?.graduation"
            v-model="graduationComment"
            :label="t('Комментарий')"
            :counter="1024"
          />
          <p v-else>{{ t("Вы уверены, что хотите отменить решение об окончании обучения?") }}</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="graduateDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitGraduation">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import ConfirmDeleteDialog from "@/components/shared/ConfirmDeleteDialog.vue";
import JuniorRegistryFormFields from "@/components/mentorship/JuniorRegistryFormFields.vue";
import JuniorDetailsInfoCard from "@/components/mentorship/JuniorDetailsInfoCard.vue";
import JuniorReportsSection from "@/components/mentorship/JuniorReportsSection.vue";
import { useJuniorDetails } from "@/composables/useJuniorDetails";

const { t } = useI18n();
const {
  loading,
  actionLoading,
  error,
  junior,
  employee,
  deleteDialog,
  graduateDialog,
  deleteReportDialog,
  reportDialog,
  editDialog,
  editingReportId,
  reportFormError,
  graduationComment,
  employees,
  allBusinessAccounts,
  projectRoles,
  ratingFields,
  reportForm,
  updateForm,
  canViewMentorship,
  canEditRegistry,
  sortedReports,
  progressOptions,
  getProgressIcon,
  canEditReport,
  canDeleteReport,
  openCreateReport,
  openEditReport,
  closeReportDialog,
  openEditDialog,
  submitUpdateJunior,
  submitReport,
  deleteJunior,
  openDeleteReport,
  closeDeleteReport,
  confirmDeleteReport,
  submitGraduation,
} = useJuniorDetails(t);
</script>
