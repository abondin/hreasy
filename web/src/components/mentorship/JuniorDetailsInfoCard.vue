<template>
  <v-card :loading="loading">
    <v-card-title class="d-flex align-start justify-space-between ga-4">
      <div>
        <div class="text-h6">{{ junior.juniorEmpl.name }}</div>
      </div>
      <div v-if="canEditRegistry" class="d-flex align-center ga-1">
        <v-btn
          icon="mdi-pencil"
          variant="text"
          @click="$emit('edit')"
        />
        <v-btn
          icon="mdi-school"
          variant="text"
          :color="junior.graduation ? '' : 'success'"
          @click="$emit('graduate')"
        />
        <v-btn
          icon="mdi-delete"
          variant="text"
          color="error"
          :disabled="Boolean(junior.graduation)"
          @click="$emit('delete')"
        />
      </div>
    </v-card-title>

    <v-card-text class="pt-0">
      <v-row>
        <v-col cols="12" lg="8">
          <profile-summary
            v-if="employee"
            :employee="employee"
            :read-only="true"
            :show-name="false"
          />
          <div
            v-else
            class="pa-6 text-body-2 text-medium-emphasis"
          >
            {{ t("Отсутствуют данные") }}
          </div>
        </v-col>

        <v-col cols="12" lg="4" class="pt-0 pt-lg-3">
          <profile-summary-item
            v-for="item in detailItems"
            :key="item.label"
            :label="item.label"
          >
            <component :is="item.component" v-if="item.component" v-bind="item.componentProps">
              {{ item.value }}
            </component>
            <span v-else>{{ item.value }}</span>
          </profile-summary-item>

          <profile-summary-item v-if="junior.graduation" :label="t('Обучение завершил')">
            <span>
              {{ junior.graduation.graduatedBy?.name }}
              ({{ formatDateTime(junior.graduation.graduatedAt) }})
            </span>
          </profile-summary-item>

          <profile-summary-item v-if="junior.graduation" :label="t('Комментарий')">
            <span>{{ junior.graduation.comment || t("Нет") }}</span>
          </profile-summary-item>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { formatDate, formatDateTime } from "@/lib/datetime";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";
import ProfileSummaryItem from "@/views/profile/components/ProfileSummaryItem.vue";
import ProfileSummary from "@/views/profile/components/ProfileSummary.vue";
import type { JuniorDto } from "@/services/junior-registry.service";
import type { Employee } from "@/services/employee.service";

defineEmits<{
  edit: [];
  graduate: [];
  delete: [];
}>();

const { t } = useI18n();

const props = defineProps<{
  junior: JuniorDto;
  employee: Employee | null;
  loading: boolean;
  canEditRegistry: boolean;
}>();

const detailItems = computed(() => [
  { label: t("Ментор"), value: props.junior.mentor?.name || t("Нет") },
  { label: t("Бюджет"), value: props.junior.budgetingAccount?.name || t("Нет") },
  { label: t("Дата трудоустройства"), value: formatDate(props.junior.juniorDateOfEmployment) },
  {
    label: t("Месяцев в компании"),
    value: props.junior.juniorInCompanyMonths.value,
    component: ValueWithStatusChip,
    componentProps: { value: props.junior.juniorInCompanyMonths, dense: true },
  },
  {
    label: t("Месяцев без отчёта"),
    value: props.junior.monthsWithoutReport.value,
    component: ValueWithStatusChip,
    componentProps: { value: props.junior.monthsWithoutReport, dense: true },
  },
]);
</script>
