<template>
  <hreasy-table :data="data" :create-new-title="$t('Добавление в реестр')"
                :update-title="$t('Обновление записи в реестре')">
    <template v-slot:additionalActions>
      <hreasy-table-export-to-excel-action :data=data></hreasy-table-export-to-excel-action>
    </template>
    <template v-slot:createFormFields>
      <junior-add-update-form-fields :create-or-update-body="data.createBody"
                                     mode="add"></junior-add-update-form-fields>
    </template>
    <template v-slot:updateFormFields>
      <junior-add-update-form-fields :create-or-update-body="data.updateBody"
                                     mode="update"></junior-add-update-form-fields>
    </template>
    <template v-slot:[`item.progress`]="{ item }">
      <span v-for="report in item.reports" v-bind:key="report.id">
        <span v-if="report.progress == 1"><v-icon color="error">mdi-arrow-bottom-left</v-icon></span>
        <span v-else-if="report.progress == 2"><v-icon>mdi-minus</v-icon></span>
        <span v-else-if="report.progress == 3"><v-icon color="success">mdi-arrow-top-right</v-icon></span>
        <span v-else-if="report.progress == 4"><v-icon color="success">mdi-arrow-top</v-icon></span>
        <span v-else>UNKNOWN_PROGRESS: {{ report.progress }}</span>
      </span>
    </template>
    <template v-slot:[`item.latestReport.createdAt`]="{ item }">
      {{ formatDateTime(item.latestReport?.createdAt) }}
    </template>
  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import logger from "@/logger";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {DataTableHeader} from "vuetify";
import {JuniorRegistryDataContainer} from "@/components/udr/junior_registry.data.container";
import JuniorAddUpdateFormFields from "@/components/udr/JuniorAddUpdateFormFields.vue";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import HreasyTableExportToExcelAction from "@/components/shared/table/HreasyTableExportToExcelAction.vue";


@Component({
  components: {
    HreasyTableExportToExcelAction,
    HreasyTable,
    JuniorAddUpdateFormFields,
    HreasyTableDeleteConfimration,
    MyDateFormComponent,
    HreasyTableCreateForm
  }
})
export default class JuniorRegistry extends Vue {
  private data = new JuniorRegistryDataContainer(() => {
    const headers: DataTableHeader[] = [
      {text: this.$tc('Молодой специалист'), value: 'juniorEmpl.name'},
      {text: this.$tc('Ментор'), value: 'mentor.name'},
      {text: this.$tc('Роль'), value: 'role'},
      {text: this.$tc('Бюджет'), value: 'budgetingAccount.name'},
      {text: this.$tc('Текущий проект'), value: 'currentProject.name'},
      {text: this.$tc('Месяцев в компании'), value: 'juniorInCompanyMonths'},
      {text: this.$tc('Прогресс'), value: 'progress'},
      {text: this.$tc('Последний срез (Когда)'), value: 'latestReport.createdAt'},
      {text: this.$tc('Последний срез (Кто)'), value: 'latestReport.createdBy.name'},
      {text: this.$tc('Последний срез (Комментарий)'), value: 'latestReport.comment'}
    ];
    return headers;
  });

  /**
   * Lifecycle hook
   */
  created() {
    logger.log('Junior registry component created');
    this.data.init();
  }


  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
}
</script>

