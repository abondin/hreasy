<template>
  <v-container class="py-6" data-testid="salary-request-details-view">
      <v-skeleton-loader v-if="loading" type="heading, paragraph, paragraph" class="mt-4" />
      <v-alert v-else-if="error" type="error" variant="tonal" class="mt-4">{{ error }}</v-alert>

      <template v-else-if="request">
        <v-breadcrumbs
          class="mt-4 mb-4 px-0"
          density="compact"
          divider="/"
          :items="breadcrumbs"
        >
          <template #item="{ item }">
            <router-link v-if="item.to" :to="item.to" class="text-decoration-none">
              {{ item.title }}
            </router-link>
            <span v-else>{{ item.title }}</span>
          </template>
        </v-breadcrumbs>

        <v-card class="mt-4">
          <v-card-item>
            <template #title>
              <div class="d-flex align-center ga-2 flex-wrap">
                <span>{{ request.type === 1 ? t("Запрос на повышение") : t("Запрос на бонус") }}</span>
                <span class="text-subtitle-1">(+{{ formatMoneyCompact(request.req.increaseAmount) }})</span>
                <v-chip size="small" :color="request.impl?.state === 2 ? 'error' : request.impl ? 'success' : 'default'" variant="tonal">
                  {{ request.impl?.state ? t(`SALARY_REQUEST_STAT.${request.impl.state}`) : t("На рассмотрении") }}
                </v-chip>
                <v-chip v-if="periodClosed" color="warning" variant="tonal" size="small">
                  {{ t("Период закрыт для внесения изменений") }}
                </v-chip>
              </div>
            </template>
            <template #append>
              <v-btn
                v-if="canUpdateRequest"
                icon="mdi-pencil"
                variant="text"
                :disabled="requestActionDisabled"
                data-testid="salary-request-edit"
                @click="openUpdateDialog"
              />
              <v-btn
                v-if="canDeleteRequest"
                icon="mdi-delete"
                color="error"
                variant="text"
                :disabled="requestActionDisabled"
                data-testid="salary-request-delete"
                @click="deleteDialog = true"
              />
            </template>
          </v-card-item>
          <v-card-subtitle>{{ formatPeriod(request.req.increaseStartPeriod) }}</v-card-subtitle>
          <v-card-text class="pa-6">
            <v-row class="salary-request-details__summary" align="start">
              <v-col cols="12" sm="auto" class="salary-request-details__avatar-col">
                <profile-avatar
                  v-if="employee"
                  :owner="employee"
                  :read-only="true"
                />
              </v-col>

              <v-col cols="12" sm class="min-width-0">
                <v-row align="start">
                  <v-col cols="12" xl="6">
                    <profile-summary
                      v-if="employee"
                      :employee="employee"
                      :read-only="true"
                      :show-avatar="false"
                    />
                    <div v-else class="pa-6 text-body-2 text-medium-emphasis">{{ t("Отсутствуют данные") }}</div>
                  </v-col>

                  <v-col cols="12" xl="6" class="pt-0 pt-xl-12">
                    <property-list variant="aligned" density="wide">
                      <profile-summary-item :label="t('Дата трудоустройства')">
                        {{ formatDate(request.employeeInfo.dateOfEmployment) || "-" }}
                      </profile-summary-item>
                      <profile-summary-item v-if="request.type === 1" :label="t('Предыдущий реализованный пересмотр')">
                        {{ formatDate(request.employeeInfo.previousSalaryIncreaseDate) || "-" }}
                        <span v-if="request.employeeInfo.previousSalaryIncreaseText">
                          ({{ request.employeeInfo.previousSalaryIncreaseText }})
                        </span>
                      </profile-summary-item>
                      <profile-summary-item v-if="request.type === 1" :label="t('Текущая заработная плата')">
                        {{ formatMoney(request.employeeInfo.currentSalaryAmount) || "-" }}
                      </profile-summary-item>
                      <profile-summary-item v-if="request.type === 1" :label="t('Ассессмент')">
                        <router-link
                          v-if="request.assessment"
                          target="_blank"
                          :to="`/assessments/${request.employee.id}/${request.assessment.id}`"
                        >
                          {{ request.assessment.name }}
                        </router-link>
                        <span v-else>-</span>
                      </profile-summary-item>
                    </property-list>

                    <v-divider class="my-3" />

                    <property-list variant="aligned" density="wide">
                      <profile-summary-item :label="t('Инициатор')">
                        {{ request.createdBy.name }} ({{ formatDateTime(request.createdAt) }})
                      </profile-summary-item>
                      <profile-summary-item :label="t('Бюджет из бизнес аккаунта')">
                        {{ request.budgetBusinessAccount?.name ?? "-" }}
                      </profile-summary-item>
                      <profile-summary-item v-if="request.type === 1" :label="t('Перспективы биллинга')">
                        {{ formatDate(request.budgetExpectedFundingUntil) || "-" }}
                      </profile-summary-item>
                      <profile-summary-item :label="request.type === 1 ? t('Изменение на') : t('Сумма бонуса')">
                        {{ formatMoney(request.req.increaseAmount) }}
                      </profile-summary-item>
                      <profile-summary-item v-if="request.type === 1" :label="t('Заработная плата после повышения')">
                        {{ formatMoney(request.req.plannedSalaryAmount) || "-" }}
                      </profile-summary-item>
                      <profile-summary-item v-if="request.type === 1" :label="t('Запрошенная позиция')">
                        {{ request.req.newPosition?.name || "-" }}
                      </profile-summary-item>
                      <profile-summary-item :label="t('Обоснование')" :truncate-value="false">
                        <markdown-text-renderer v-if="request.req.reason" :content="request.req.reason" />
                        <span v-else>-</span>
                      </profile-summary-item>
                      <profile-summary-item :label="t('Примечание')" :truncate-value="false">
                        <markdown-text-renderer v-if="request.req.comment" :content="request.req.comment" />
                        <span v-else>-</span>
                      </profile-summary-item>
                    </property-list>
                  </v-col>
                </v-row>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-card class="mt-5">
          <v-card-item>
            <template #title>{{ t("Реализация") }}</template>
            <template #append>
              <template v-if="canAdminSalaryRequests">
                <v-btn v-if="!request.impl" icon="mdi-pen-plus" color="success" variant="text" :disabled="periodClosed || actionLoading" @click="openImplementationDialog('implement')" />
                <v-btn v-if="!request.impl" icon="mdi-pen-minus" color="error" variant="text" :disabled="periodClosed || actionLoading" @click="openImplementationDialog('reject')" />
                <v-btn v-if="request.impl" icon="mdi-pencil-off" color="error" variant="text" :disabled="periodClosed || actionLoading" @click="openImplementationDialog('reset')" />
                <v-btn v-if="request.impl && request.type === 1" icon="mdi-email-edit" variant="text" :disabled="periodClosed || actionLoading" @click="openImplementationTextDialog" />
              </template>
            </template>
          </v-card-item>
          <v-card-text v-if="request.impl">
            <property-list variant="aligned" density="wide">
              <profile-summary-item :label="t('Результат')">{{ t(`SALARY_REQUEST_STAT.${request.impl.state}`) }}</profile-summary-item>
              <profile-summary-item v-if="request.impl.implementedBy" :label="t('Принял решение')">
                {{ request.impl.implementedBy.name }} ({{ formatDateTime(request.impl.implementedAt) }})
              </profile-summary-item>
              <profile-summary-item v-if="request.impl.increaseAmount" :label="request.type === 1 ? t('Изменение на') : t('Сумма бонуса')">{{ formatMoney(request.impl.increaseAmount) }}</profile-summary-item>
              <profile-summary-item v-if="request.impl.salaryAmount" :label="t('Заработная плата после повышения')">{{ formatMoney(request.impl.salaryAmount) }}</profile-summary-item>
              <profile-summary-item v-if="request.impl.newPosition" :label="t('Новая позиция')">{{ request.impl.newPosition.name }}</profile-summary-item>
              <profile-summary-item v-if="request.impl.rejectReason" :label="t('Обоснование отказа')">{{ request.impl.rejectReason }}</profile-summary-item>
              <profile-summary-item v-if="request.impl.increaseStartPeriod" :label="t('Месяц старта изменений')">{{ formatPeriod(request.impl.increaseStartPeriod) }}</profile-summary-item>
              <profile-summary-item v-if="request.impl.comment" :label="t('Примечание')" :truncate-value="false">
                <markdown-text-renderer :content="request.impl.comment" />
              </profile-summary-item>
              <profile-summary-item v-if="request.impl.increaseText" :label="t('Сообщение об изменениях')">{{ request.impl.increaseText }}</profile-summary-item>
            </property-list>
          </v-card-text>
          <v-card-text v-else>{{ t("На рассмотрении") }}</v-card-text>
        </v-card>

        <v-card class="mt-5">
          <v-card-item>
            <template #title>{{ t("Согласования и комментарии") }}</template>
            <template #append>
              <template v-if="canApproveRequest">
                <v-btn icon="mdi-checkbox-marked-circle" color="success" variant="text" :disabled="periodClosed || actionLoading" @click="openApprovalDialog('approve')" />
                <v-btn icon="mdi-alert-circle" color="error" variant="text" :disabled="periodClosed || actionLoading" @click="openApprovalDialog('decline')" />
                <v-btn icon="mdi-comment" variant="text" :disabled="actionLoading" @click="openApprovalDialog('comment')" />
              </template>
            </template>
          </v-card-item>
          <v-card-text>
            <v-list v-if="request.approvals.length">
              <v-list-item v-for="approval in approvalsOrderedAsc" :key="approval.id">
                <template #prepend>
                  <v-icon :color="approval.state === 2 ? 'success' : approval.state === 3 ? 'error' : undefined">
                    {{ approval.state === 2 ? "mdi-checkbox-marked-circle" : approval.state === 3 ? "mdi-alert-circle" : "mdi-comment" }}
                  </v-icon>
                </template>
                <v-list-item-title>{{ approval.createdBy.name }} - {{ formatDateTime(approval.createdAt) }}</v-list-item-title>
                <div class="text-body-2 mt-1">
                  <markdown-text-renderer v-if="approval.comment" :content="approval.comment" />
                  <span v-else>-</span>
                </div>
                <template #append>
                  <v-btn v-if="canDeleteApproval(approval)" icon="mdi-close" size="small" variant="text" @click="openDeleteApprovalDialog(approval)" />
                </template>
              </v-list-item>
            </v-list>
            <div v-else>{{ t("Добавьте первый комментарий или согласование") }}</div>
          </v-card-text>
        </v-card>

        <v-card v-if="canAdminSalaryRequests" class="mt-5">
          <v-card-item>
            <template #title>{{ t("История всех запросов") }}</template>
            <template #append>
              <v-btn-toggle v-model="historyModes" density="comfortable" multiple>
                <v-btn :value="0">{{ t("Повышения") }}</v-btn>
                <v-btn :value="1">{{ t("Бонусы") }}</v-btn>
              </v-btn-toggle>
            </template>
          </v-card-item>
          <v-card-text>
            <v-alert v-if="historyError" type="error" variant="tonal" class="mb-3">{{ historyError }}</v-alert>
            <HREasyTableBase
              :headers="historyHeaders"
              :items="filteredHistoryItems"
              :loading="historyLoading"
              :loading-text="t('Загрузка_данных')"
              :no-data-text="t('Отсутствуют данные')"
              density="compact"
              table-class="text-truncate"
              data-testid="salary-request-history-table"
            >
              <template #[`item.req.increaseStartPeriod`]="{ item }">
                <router-link v-if="item.id !== request.id" :to="{ name: 'salary-request-details', params: { period: String(item.req.increaseStartPeriod), requestId: String(item.id) }, query: historyDetailQuery }">
                  {{ formatPeriod(item.req.increaseStartPeriod) }}
                </router-link>
                <span v-else>{{ formatPeriod(item.req.increaseStartPeriod) }}</span>
              </template>
              <template #[`item.links`]="{ item }">
                <div class="history-links-cell">
                  <div
                    v-for="link in linksFor(item.id)"
                    :key="link.id"
                    class="history-link-chip-wrap"
                  >
                    <v-chip
                      size="small"
                      variant="outlined"
                      :color="historyLinkColor(link)"
                      class="history-link-chip"
                    >
                      {{ historyLinkLabel(link) }}
                      <template #append>
                        <v-icon
                          size="16"
                          class="history-link-chip-close"
                          @click.stop.prevent="openDeleteLinkDialog(link.id)"
                        >
                          mdi-close-circle
                        </v-icon>
                      </template>
                    </v-chip>
                    <v-tooltip activator="parent" location="bottom" max-width="420">
                      <div class="text-body-2">{{ historyLinkTitle(link) }}</div>
                      <div class="text-body-2">{{ link.createdBy.name }} ({{ formatDateTime(link.createdAt) }})</div>
                      <div v-if="link.comment" class="text-body-2">{{ link.comment }}</div>
                    </v-tooltip>
                  </div>
                  <v-btn
                    v-if="item.id !== request.id"
                    icon="mdi-plus"
                    size="x-small"
                    variant="text"
                    class="history-link-add-btn"
                    @click="openAddLinkDialog(item.id)"
                  />
                </div>
              </template>
              <template #[`item.req.increaseAmount`]="{ item }">
                {{ formatMoney(item.req.increaseAmount) }}
                <span v-if="item.req.plannedSalaryAmount"> / {{ formatMoney(item.req.plannedSalaryAmount) }}</span>
              </template>
              <template #[`item.impl.state`]="{ item }">
                <span v-if="item.impl" :class="item.impl.state === 2 ? 'text-error' : 'text-success'">{{ t(`SALARY_REQUEST_STAT.${item.impl.state}`) }}</span>
              </template>
              <template #[`item.impl.increaseAmount`]="{ item }">
                {{ formatMoney(item.impl?.increaseAmount) }}
                <span v-if="item.impl?.salaryAmount"> / {{ formatMoney(item.impl.salaryAmount) }}</span>
              </template>
            </HREasyTableBase>
          </v-card-text>
        </v-card>
      </template>

    <v-dialog v-model="deleteDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Удалить запрос") }}</v-card-title>
        <v-card-text>{{ t("Вы уверены, что хотите удалить запрос?") }}</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="error" :loading="actionLoading" @click="submitDelete">{{ t("Удалить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="updateDialog" max-width="820">
      <v-card>
        <v-card-title>{{ t("Внести изменения") }}</v-card-title>
        <v-card-text>
          <v-alert v-if="actionError" type="error" variant="tonal" class="mb-3">{{ actionError }}</v-alert>
          <v-form ref="updateFormRef">
            <v-text-field v-if="request?.type === 1" v-model.number="updateForm.currentSalaryAmount" type="number" hide-spin-buttons :label="t('Текущая заработная плата')" />
            <v-text-field v-if="request?.type === 1" v-model.number="updateForm.plannedSalaryAmount" type="number" hide-spin-buttons :label="t('Планируемая заработная плата')" />
            <v-autocomplete v-if="request?.type === 1" v-model="updateForm.newPosition" :items="positions.filter((item) => item.active !== false)" item-title="name" item-value="id" clearable :label="t('Запрошенная позиция')" />
            <v-autocomplete v-if="request?.type === 1 && employeeAssessments.length > 0" v-model="updateForm.assessmentId" :items="employeeAssessments" item-title="plannedDate" item-value="id" clearable :label="t('Ассессмент')" />
            <my-date-form-component v-if="request?.type === 1" v-model="updateForm.budgetExpectedFundingUntil" :label="t('Планируемая дата окончания финансирования')" />
            <my-date-form-component v-if="request?.type === 1" v-model="updateForm.previousSalaryIncreaseDate" :label="t('Предыдущий реализованный пересмотр (дата)')" />
            <v-text-field v-if="request?.type === 1" v-model="updateForm.previousSalaryIncreaseText" :label="t('Предыдущий реализованный пересмотр (примечание)')" :rules="[previousTextRule]" counter="1024" />
            <markdown-text-editor
              v-model="updateCommentModel"
              :label="t('Примечание')"
              :rules="[commentRule]"
              :counter="4096"
              :min-height="220"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="updateDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitUpdateDialog">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="implementationDialog" max-width="820">
      <v-card>
        <v-card-title>{{ implementationDialogTitle }}</v-card-title>
        <v-card-text>
          <v-alert v-if="actionError" type="error" variant="tonal" class="mb-3">{{ actionError }}</v-alert>
          <template v-if="implementationMode === 'reset'">
            {{ t("Вы уверены, что хотите сбросить принятое решение по реализации?") }}
          </template>
          <v-form v-else ref="implementationFormRef">
            <template v-if="implementationMode === 'implement'">
              <v-text-field v-model.number="implementationForm.increaseAmount" type="number" hide-spin-buttons :rules="[requiredNumberRule]" :label="request?.type === 1 ? t('Итоговое изменение на') : t('Сумма бонуса')" />
              <v-text-field v-if="request?.type === 1" v-model.number="implementationForm.salaryAmount" type="number" hide-spin-buttons :label="t('Итоговая сумма')" />
              <v-select v-model.number="implementationForm.increaseStartPeriod" :items="periodOptions" item-title="label" item-value="id" :label="t('Исполнить в периоде')" />
              <v-autocomplete v-if="request?.type === 1" v-model="implementationForm.newPosition" :items="positions.filter((item) => item.active !== false)" item-title="name" item-value="id" clearable :label="t('Изменить позицию')" />
            </template>
            <template v-else-if="implementationMode === 'reject'">
              <v-select v-model.number="implementationForm.rescheduleToNewPeriod" :items="reschedulePeriodOptions" item-title="label" item-value="id" clearable :label="t('Перенести запрос в другой период')" />
              <v-text-field v-model="implementationForm.rejectReason" :label="t('Обоснование отказа')" :rules="[requiredRejectReasonRule]" counter="128" />
            </template>
            <markdown-text-editor
              v-model="implementationCommentModel"
              :label="t('Примечание')"
              :rules="[maxCommentRule]"
              :counter="256"
              :min-height="220"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="implementationDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitImplementationDialog">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="implementationTextDialog" max-width="760">
      <v-card>
        <v-card-title>{{ t("Изменить сообщение") }}</v-card-title>
        <v-card-text>
          <v-alert v-if="actionError" type="error" variant="tonal" class="mb-3">{{ actionError }}</v-alert>
          <v-form ref="implementationTextFormRef">
            <v-text-field v-model="implementationTextForm.increaseText" :label="t('Сообщение об изменениях')" :rules="[requiredImplementationTextRule]" counter="128" />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="implementationTextDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitImplementationTextDialog">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="approvalDialog" max-width="760">
      <v-card>
        <v-card-title>{{ t("Добавить согласование/комментарий") }}</v-card-title>
        <v-card-text>
          <v-alert v-if="actionError" type="error" variant="tonal" class="mb-3">{{ actionError }}</v-alert>
          <v-form ref="approvalFormRef">
            <v-select v-model="approvalForm.action" :items="approvalActionOptions" item-title="title" item-value="value" :label="t('Действие')" :disabled="periodClosed" />
            <markdown-text-editor
              v-model="approvalCommentModel"
              :label="t('Комментарий')"
              :rules="[approvalCommentRule]"
              :counter="4096"
              :min-height="220"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="approvalDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitApprovalDialog">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteApprovalDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Удалить согласование/комментарий") }}</v-card-title>
        <v-card-text>{{ t("Вы уверены, что хотите удалить согласование/комментарий?") }}</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="deleteApprovalDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="error" :loading="actionLoading" @click="submitDeleteApproval">{{ t("Удалить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="addLinkDialog" max-width="760">
      <v-card>
        <v-card-title>{{ t("Добавить связанный запрос") }}</v-card-title>
        <v-card-text>
          <v-alert v-if="actionError" type="error" variant="tonal" class="mb-3">{{ actionError }}</v-alert>
          <v-form ref="addLinkFormRef">
            <v-select v-model="addLinkForm.type" :items="linkTypeOptions" item-title="title" item-value="value" :label="t('Тип связи')" />
            <v-textarea v-model="addLinkForm.comment" :label="t('Примечание')" :rules="[commentRule]" />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="addLinkDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="primary" :loading="actionLoading" @click="submitAddLinkDialog">{{ t("Применить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteLinkDialog" max-width="480">
      <v-card>
        <v-card-title>{{ t("Удалить связь") }}</v-card-title>
        <v-card-text>{{ t("Вы уверены, что хотите удалить связь?") }}</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="actionLoading" @click="deleteLinkDialog = false">{{ t("Отмена") }}</v-btn>
          <v-btn color="error" :loading="actionLoading" @click="submitDeleteLinkDialog">{{ t("Удалить") }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRoute, useRouter, type RouteLocationRaw } from "vue-router";
import type { VForm } from "vuetify/components";
import { usePermissions } from "@/lib/permissions";
import { errorUtils } from "@/lib/errors";
import { formatDate, formatDateTime } from "@/lib/datetime";
import { ReportPeriod } from "@/services/overtime.service";
import { findEmployee, type Employee } from "@/services/employee.service";
import { fetchPositions, type DictItem } from "@/services/dict.service";
import { fetchEmployeeAssessments, type AssessmentBase } from "@/services/assessment.service";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import MarkdownTextEditor from "@/components/shared/MarkdownTextEditor.vue";
import MarkdownTextRenderer from "@/components/shared/MarkdownTextRenderer.vue";
import HREasyTableBase from "@/components/shared/HREasyTableBase.vue";
import PropertyList from "@/components/shared/PropertyList.vue";
import ProfileAvatar from "@/views/profile/components/ProfileAvatar.vue";
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";
import {
  addSalaryRequestLink,
  approveSalaryRequest,
  commentSalaryRequest,
  declineSalaryRequest,
  deleteSalaryRequest,
  deleteSalaryRequestApproval,
  deleteSalaryRequestLink,
  fetchClosedSalaryRequestPeriods,
  fetchSalaryRequest,
  type SalaryIncreaseRequest,
  type SalaryRequestApproval,
  type SalaryRequestLink,
  updateSalaryRequest,
} from "@/services/salary.service";
import {
  fetchEmployeeSalaryRequestsForAllPeriods,
  markSalaryRequestImplemented,
  rejectSalaryRequest,
  resetSalaryRequestImplementation,
  updateSalaryRequestImplementationText,
} from "@/services/admin-salary.service";

type VFormInstance = InstanceType<typeof VForm>;
type ImplementationMode = "implement" | "reject" | "reset";
type ApprovalDialogMode = "approve" | "decline" | "comment";

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const permissions = usePermissions();

const loading = ref(false);
const actionLoading = ref(false);
const error = ref("");
const actionError = ref("");
const historyError = ref("");
const request = ref<SalaryIncreaseRequest | null>(null);
const employee = ref<Employee | null>(null);
const periodClosed = ref(false);
const historyLoading = ref(false);
const historyItems = ref<SalaryIncreaseRequest[]>([]);
const historyModes = ref<number[]>([0]);

const positions = ref<DictItem[]>([]);
const employeeAssessments = ref<AssessmentBase[]>([]);

const deleteDialog = ref(false);
const updateDialog = ref(false);
const implementationDialog = ref(false);
const implementationTextDialog = ref(false);
const approvalDialog = ref(false);
const deleteApprovalDialog = ref(false);
const addLinkDialog = ref(false);
const deleteLinkDialog = ref(false);

const implementationMode = ref<ImplementationMode>("implement");
const pendingApprovalId = ref<number | null>(null);
const pendingDeleteLinkId = ref<number | null>(null);
const updateFormRef = ref<VFormInstance | null>(null);
const implementationFormRef = ref<VFormInstance | null>(null);
const implementationTextFormRef = ref<VFormInstance | null>(null);
const approvalFormRef = ref<VFormInstance | null>(null);
const addLinkFormRef = ref<VFormInstance | null>(null);

const updateForm = reactive({
  currentSalaryAmount: null as number | null,
  plannedSalaryAmount: null as number | null,
  newPosition: null as number | null,
  assessmentId: null as number | null,
  budgetExpectedFundingUntil: "",
  previousSalaryIncreaseDate: "",
  previousSalaryIncreaseText: "",
  comment: "" as string | null,
});

const implementationForm = reactive({
  increaseAmount: null as number | null,
  salaryAmount: null as number | null,
  increaseStartPeriod: null as number | null,
  newPosition: null as number | null,
  rejectReason: "",
  rescheduleToNewPeriod: null as number | null,
  comment: "" as string | null,
});

const implementationTextForm = reactive({
  increaseText: "",
});

const approvalForm = reactive({
  action: 1 as 1 | 2 | 3,
  comment: "" as string | null,
});

const addLinkForm = reactive({
  destination: null as number | null,
  type: 2 as 1 | 2,
  comment: "" as string | null,
});

const canAdminSalaryRequests = computed(() => permissions.canAdminSalaryRequests());
const canUpdateRequest = computed(() => (request.value ? permissions.canUpdateSalaryRequests(request.value.createdBy.id) : false));
const canDeleteRequest = computed(() => (request.value ? permissions.canDeleteSalaryRequests(request.value.createdBy.id) : false));
const canApproveRequest = computed(() => {
  if (canAdminSalaryRequests.value) {
    return true;
  }
  const businessAccountId = request.value?.budgetBusinessAccount?.id;
  return Number.isFinite(businessAccountId)
    ? permissions.canApproveSalaryRequest(Number(businessAccountId))
    : false;
});
const currentSource = computed(() => (route.query.source === "salary-latest" ? "salary-latest" : "salary-requests"));
const currentListTab = computed(() => (route.query.tab === "bonuses" ? "bonuses" : "requests"));
const backToListRoute = computed(() => {
  if (currentSource.value === "salary-latest") {
    return { name: "salary-latest" };
  }
  return {
    name: "salary-requests",
    query: {
      tab: currentListTab.value,
    },
  };
});
const breadcrumbs = computed<Array<{ title: string; to?: RouteLocationRaw }>>(() => {
  if (currentSource.value === "salary-latest") {
    return [
      {
        title: t("Последние повышения"),
        to: { name: "salary-latest" },
      },
      {
        title: request.value?.employee?.name ?? "-",
      },
    ];
  }

  return [
    {
      title: t("Повышения и бонусы"),
      to: { name: "salary-requests", query: { tab: currentListTab.value } },
    },
    {
      title: currentListTab.value === "bonuses" ? t("Бонусы") : t("Повышения"),
    },
    {
      title: request.value?.employee?.name ?? "-",
    },
  ];
});
const historyDetailQuery = computed(() => (
  currentSource.value === "salary-latest"
    ? { source: "salary-latest" }
    : { source: "salary-requests", tab: currentListTab.value }
));
const requestActionDisabled = computed(() => periodClosed.value || Boolean(request.value?.impl));
const approvalsOrderedAsc = computed(() =>
  [...(request.value?.approvals ?? [])].sort((a, b) => a.createdAt.localeCompare(b.createdAt)),
);
const updateCommentModel = computed({
  get: () => updateForm.comment ?? "",
  set: (value: string) => {
    updateForm.comment = value;
  },
});
const implementationCommentModel = computed({
  get: () => implementationForm.comment ?? "",
  set: (value: string) => {
    implementationForm.comment = value;
  },
});
const approvalCommentModel = computed({
  get: () => approvalForm.comment ?? "",
  set: (value: string) => {
    approvalForm.comment = value;
  },
});

const periodOptions = computed(() =>
  ReportPeriod.currentAndNextPeriods().map((period) => ({ id: period.id, label: period.toString() })),
);

const reschedulePeriodOptions = computed(() =>
  ReportPeriod.currentAndNextPeriods(1).map((period) => ({ id: period.id, label: period.toString() })),
);

const filteredHistoryItems = computed(() => {
  const showIncreases = historyModes.value.length === 0 || historyModes.value.includes(0);
  const showBonuses = historyModes.value.length === 0 || historyModes.value.includes(1);
  return historyItems.value.filter((item) => (item.type === 1 ? showIncreases : showBonuses));
});

const historyHeaders = computed(() => [
  { title: t("Период"), key: "req.increaseStartPeriod", width: "160px" },
  { title: t("Ссылки"), key: "links", width: "260px" },
  { title: t("Запрошенное повышение или бонус / заработная плата после повышения"), key: "req.increaseAmount" },
  { title: t("Решение"), key: "impl.state", width: "140px" },
  { title: t("Реализованное повышение или бонус / заработная плата после повышения"), key: "impl.increaseAmount" },
]);

const implementationDialogTitle = computed(() => {
  if (implementationMode.value === "implement") {
    return request.value?.type === 1 ? t("Реализация запроса на повышение") : t("Реализация запроса на бонус");
  }
  if (implementationMode.value === "reject") {
    return t("Отклонить запрос");
  }
  return t("Сбросить решение");
});

const approvalActionOptions = computed(() => [
  { title: t("Комментарий"), value: 1 },
  { title: t("Согласовать"), value: 2 },
  { title: t("Отклонить"), value: 3 },
]);

const linkTypeOptions = computed(() => [
  { title: t("Перенос"), value: 1 },
  { title: t("Связанный"), value: 2 },
]);

watch(() => [route.params.period, route.params.requestId], () => load().catch(() => undefined));
watch(
  () => implementationForm.rescheduleToNewPeriod,
  (periodId) => {
    if (!periodId || implementationForm.rejectReason.trim()) {
      return;
    }
    implementationForm.rejectReason = t("Перенос решения на ПЕРИОД", { period: ReportPeriod.fromPeriodId(periodId).toString() });
  },
);

onMounted(() => {
  load().catch(() => undefined);
});

const commentRule = (value: unknown) => (typeof value !== "string" || value.length <= 4096) || t("Не более N символов", { n: 4096 });
const maxCommentRule = (value: unknown) => (typeof value !== "string" || value.length <= 256) || t("Не более N символов", { n: 256 });
const previousTextRule = (value: string | null) => (!value || value.length <= 1024) || t("Не более N символов", { n: 1024 });
const requiredNumberRule = (value: unknown) => (value != null && value !== "" && !Number.isNaN(Number(value))) || t("Обязательное числовое поле");
const requiredRejectReasonRule = (value: string) => (Boolean(value?.trim()) && value.length <= 128) || t("Обязательное поле. Не более N символов", { n: 128 });
const requiredImplementationTextRule = (value: string) => (Boolean(value?.trim()) && value.length <= 128) || t("Обязательное поле. Не более N символов", { n: 128 });
const approvalCommentRule = (value: unknown) => (typeof value !== "string" || value.length <= 4096) || t("Не более N символов", { n: 4096 });

async function ensurePositions(): Promise<void> {
  if (positions.value.length > 0) {
    return;
  }
  positions.value = await fetchPositions();
}

async function loadEmployeeAssessments(employeeId: number): Promise<void> {
  const assessments = await fetchEmployeeAssessments(employeeId);
  employeeAssessments.value = assessments.filter((item) => !item.canceledAt);
}

async function load(): Promise<void> {
  const period = Number(route.params.period);
  const requestId = Number(route.params.requestId);
  if (!Number.isInteger(period) || !Number.isInteger(requestId)) {
    return;
  }

  loading.value = true;
  error.value = "";
  historyError.value = "";
  try {
    const [closedPeriods, details] = await Promise.all([
      fetchClosedSalaryRequestPeriods(),
      fetchSalaryRequest(period, requestId),
    ]);
    request.value = details;
    periodClosed.value = closedPeriods.some((item) => item.period === period);
    employee.value = await findEmployee(details.employee.id).catch(() => null);

    if (permissions.canAdminSalaryRequests()) {
      historyLoading.value = true;
      try {
        historyItems.value = await fetchEmployeeSalaryRequestsForAllPeriods(details.employee.id);
      } catch (err: unknown) {
        historyError.value = errorUtils.shortMessage(err);
      } finally {
        historyLoading.value = false;
      }
    }
  } catch (err: unknown) {
    error.value = errorUtils.shortMessage(err);
  } finally {
    loading.value = false;
  }
}

function formatMoney(value: number | null | undefined): string {
  if (value == null) {
    return "";
  }
  return `${Number(value).toLocaleString("ru-RU")} руб.`;
}

function formatMoneyCompact(value: number | null | undefined): string {
  if (value == null) {
    return "";
  }
  return Number(value).toLocaleString("ru-RU");
}

function formatPeriod(period: number): string {
  return ReportPeriod.fromPeriodId(period).toString();
}

function canDeleteApproval(approval: SalaryRequestApproval): boolean {
  return canApproveRequest.value && (!periodClosed.value || approval.state === 1);
}

function linksFor(requestId: number): SalaryRequestLink[] {
  return (request.value?.links ?? []).filter((link) => link.linkedRequest.id === requestId);
}

function historyLinkLabel(link: SalaryRequestLink): string {
  if (link.type === 1) {
    return link.initiator ? t("Перенесено на") : t("Перенесено с");
  }
  return t("Связанный");
}

function historyLinkColor(link: SalaryRequestLink): string {
  if (link.type === 1 && link.initiator) {
    return "error";
  }
  if (link.type === 2) {
    return "secondary";
  }
  return "default";
}

function historyLinkTitle(link: SalaryRequestLink): string {
  if (link.type === 1) {
    return link.initiator
      ? t("Перенос решения на ПЕРИОД", { period: formatPeriod(link.linkedRequest.period) })
      : `${t("Перенесено с")} ${formatPeriod(link.linkedRequest.period)}`;
  }
  return `${t("Связанный запрос")} ${formatPeriod(link.linkedRequest.period)}`;
}

async function submitDelete(): Promise<void> {
  if (!request.value) {
    return;
  }
  actionLoading.value = true;
  actionError.value = "";
  try {
    await deleteSalaryRequest(request.value.id);
    await router.push(backToListRoute.value);
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

async function openUpdateDialog(): Promise<void> {
  if (!request.value) {
    return;
  }
  actionError.value = "";
  await Promise.all([
    ensurePositions(),
    request.value.type === 1 ? loadEmployeeAssessments(request.value.employee.id) : Promise.resolve(),
  ]);

  updateForm.currentSalaryAmount = request.value.employeeInfo.currentSalaryAmount;
  updateForm.plannedSalaryAmount = request.value.req.plannedSalaryAmount;
  updateForm.newPosition = request.value.req.newPosition?.id ?? null;
  updateForm.assessmentId = request.value.assessment?.id ?? null;
  updateForm.budgetExpectedFundingUntil = request.value.budgetExpectedFundingUntil ?? "";
  updateForm.previousSalaryIncreaseDate = request.value.employeeInfo.previousSalaryIncreaseDate ?? "";
  updateForm.previousSalaryIncreaseText = request.value.employeeInfo.previousSalaryIncreaseText ?? "";
  updateForm.comment = request.value.req.comment ?? "";

  updateDialog.value = true;
}

async function submitUpdateDialog(): Promise<void> {
  if (!request.value || !updateFormRef.value) {
    return;
  }
  const validation = await updateFormRef.value.validate();
  if (!validation.valid) {
    return;
  }

  actionLoading.value = true;
  actionError.value = "";
  try {
    await updateSalaryRequest(request.value.id, {
      budgetExpectedFundingUntil: request.value.type === 1 ? toNullable(updateForm.budgetExpectedFundingUntil) : request.value.budgetExpectedFundingUntil,
      currentSalaryAmount: request.value.type === 1 ? updateForm.currentSalaryAmount : request.value.employeeInfo.currentSalaryAmount,
      plannedSalaryAmount: request.value.type === 1 ? updateForm.plannedSalaryAmount : request.value.req.plannedSalaryAmount,
      previousSalaryIncreaseText: request.value.type === 1 ? toNullable(updateForm.previousSalaryIncreaseText) : request.value.employeeInfo.previousSalaryIncreaseText,
      previousSalaryIncreaseDate: request.value.type === 1 ? toNullable(updateForm.previousSalaryIncreaseDate) : request.value.employeeInfo.previousSalaryIncreaseDate,
      assessmentId: request.value.type === 1 ? updateForm.assessmentId : null,
      comment: toNullable(updateForm.comment),
      newPosition: request.value.type === 1 ? updateForm.newPosition : null,
      budgetBusinessAccount: request.value.budgetBusinessAccount?.id ?? null,
      increaseAmount: request.value.req.increaseAmount,
      increaseStartPeriod: request.value.req.increaseStartPeriod,
      reason: request.value.req.reason,
    });
    updateDialog.value = false;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function openApprovalDialog(mode: ApprovalDialogMode): void {
  approvalForm.action = mode === "approve" ? 2 : mode === "decline" ? 3 : 1;
  approvalForm.comment = "";
  actionError.value = "";
  approvalDialog.value = true;
}

async function submitApprovalDialog(): Promise<void> {
  if (!request.value || !approvalFormRef.value) {
    return;
  }
  const validation = await approvalFormRef.value.validate();
  if (!validation.valid) {
    return;
  }

  actionLoading.value = true;
  actionError.value = "";
  try {
    const body = { comment: toNullable(approvalForm.comment) };
    if (approvalForm.action === 2) {
      await approveSalaryRequest(request.value.id, body);
    } else if (approvalForm.action === 3) {
      await declineSalaryRequest(request.value.id, body);
    } else {
      await commentSalaryRequest(request.value.id, body);
    }
    approvalDialog.value = false;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function openDeleteApprovalDialog(approval: SalaryRequestApproval): void {
  pendingApprovalId.value = approval.id;
  actionError.value = "";
  deleteApprovalDialog.value = true;
}

async function submitDeleteApproval(): Promise<void> {
  if (!request.value || !pendingApprovalId.value) {
    return;
  }
  actionLoading.value = true;
  actionError.value = "";
  try {
    await deleteSalaryRequestApproval(request.value.id, pendingApprovalId.value);
    deleteApprovalDialog.value = false;
    pendingApprovalId.value = null;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

async function openImplementationDialog(mode: ImplementationMode): Promise<void> {
  if (!request.value) {
    return;
  }
  actionError.value = "";
  implementationMode.value = mode;

  if (mode !== "reset") {
    await ensurePositions();
  }

  implementationForm.increaseAmount = request.value.impl?.increaseAmount ?? request.value.req.increaseAmount;
  implementationForm.salaryAmount = request.value.impl?.salaryAmount ?? request.value.req.plannedSalaryAmount;
  implementationForm.increaseStartPeriod = request.value.impl?.increaseStartPeriod ?? request.value.req.increaseStartPeriod;
  implementationForm.newPosition = request.value.impl?.newPosition?.id ?? request.value.req.newPosition?.id ?? null;
  implementationForm.rejectReason = request.value.impl?.rejectReason ?? "";
  implementationForm.rescheduleToNewPeriod = null;
  implementationForm.comment = request.value.impl?.comment ?? "";

  implementationDialog.value = true;
}

async function submitImplementationDialog(): Promise<void> {
  if (!request.value) {
    return;
  }
  if (implementationMode.value !== "reset" && implementationFormRef.value) {
    const validation = await implementationFormRef.value.validate();
    if (!validation.valid) {
      return;
    }
  }

  actionLoading.value = true;
  actionError.value = "";
  try {
    if (implementationMode.value === "reset") {
      await resetSalaryRequestImplementation(request.value.id);
    } else if (implementationMode.value === "reject") {
      await rejectSalaryRequest(request.value.id, {
        reason: implementationForm.rejectReason.trim(),
        comment: toNullable(implementationForm.comment),
        rescheduleToNewPeriod: implementationForm.rescheduleToNewPeriod,
      });
    } else {
      await markSalaryRequestImplemented(request.value.id, {
        increaseAmount: Number(implementationForm.increaseAmount ?? 0),
        salaryAmount: request.value.type === 1 ? implementationForm.salaryAmount : null,
        increaseStartPeriod: Number(implementationForm.increaseStartPeriod ?? request.value.req.increaseStartPeriod),
        newPosition: request.value.type === 1 ? implementationForm.newPosition : null,
        reason: request.value.req.reason,
        comment: toNullable(implementationForm.comment),
      });
    }
    implementationDialog.value = false;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function openImplementationTextDialog(): void {
  if (!request.value?.impl) {
    return;
  }
  implementationTextForm.increaseText = request.value.impl.increaseText ?? "";
  actionError.value = "";
  implementationTextDialog.value = true;
}

async function submitImplementationTextDialog(): Promise<void> {
  if (!request.value || !implementationTextFormRef.value) {
    return;
  }
  const validation = await implementationTextFormRef.value.validate();
  if (!validation.valid) {
    return;
  }

  actionLoading.value = true;
  actionError.value = "";
  try {
    await updateSalaryRequestImplementationText(request.value.id, {
      increaseText: toNullable(implementationTextForm.increaseText),
    });
    implementationTextDialog.value = false;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function openAddLinkDialog(destination: number): void {
  addLinkForm.destination = destination;
  addLinkForm.type = 2;
  addLinkForm.comment = "";
  actionError.value = "";
  addLinkDialog.value = true;
}

async function submitAddLinkDialog(): Promise<void> {
  if (!request.value || !addLinkForm.destination || !addLinkFormRef.value) {
    return;
  }
  const validation = await addLinkFormRef.value.validate();
  if (!validation.valid) {
    return;
  }

  actionLoading.value = true;
  actionError.value = "";
  try {
    await addSalaryRequestLink({
      source: request.value.id,
      destination: addLinkForm.destination,
      type: addLinkForm.type,
      comment: toNullable(addLinkForm.comment),
    });
    addLinkDialog.value = false;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function openDeleteLinkDialog(linkId: number): void {
  pendingDeleteLinkId.value = linkId;
  actionError.value = "";
  deleteLinkDialog.value = true;
}

async function submitDeleteLinkDialog(): Promise<void> {
  if (!pendingDeleteLinkId.value) {
    return;
  }
  actionLoading.value = true;
  actionError.value = "";
  try {
    await deleteSalaryRequestLink(pendingDeleteLinkId.value);
    pendingDeleteLinkId.value = null;
    deleteLinkDialog.value = false;
    await load();
  } catch (err: unknown) {
    actionError.value = errorUtils.shortMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

function toNullable(value: string | null | undefined): string | null {
  if (value == null) {
    return null;
  }
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}
</script>

<style scoped>
.salary-request-details__summary {
  align-items: start;
}

.salary-request-details__avatar-col {
  flex: 0 0 auto;
}

.history-links-cell {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 6px;
  min-height: 24px;
  white-space: nowrap;
}

.history-link-chip {
  margin: 0;
}

.history-link-chip-close {
  margin-left: 4px;
  cursor: pointer;
}

.history-link-chip-wrap {
  display: inline-flex;
}

.history-link-add-btn {
  opacity: 0;
  pointer-events: none;
  flex-shrink: 0;
  transition: opacity 120ms ease-in-out;
}

:deep(tr:hover .history-link-add-btn),
:deep(tr:focus-within .history-link-add-btn),
.history-links-cell:hover .history-link-add-btn,
.history-links-cell:focus-within .history-link-add-btn {
  opacity: 1;
  pointer-events: auto;
}
</style>
