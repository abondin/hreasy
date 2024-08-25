<template>
  <hreasy-table ref="juniorTable" :data="data" :create-new-title="$t('Добавление в реестр')"
                :update-title="$t('Обновление записи в реестре')"
                :load-data-on-init="false">
    <template v-slot:filters>
      <junior-table-filter :data="data"/>
    </template>
    <template v-slot:additionalActions>
      <hreasy-table-export-to-excel-action :data=data></hreasy-table-export-to-excel-action>
    </template>
    <template v-slot:createFormFields>
      <junior-add-update-form-fields :create-or-update-body="data.createBody"
                                     mode="add"></junior-add-update-form-fields>
    </template>
    <template v-slot:[`item.progress`]="{ item }">
      <v-tooltip bottom v-for="report in reportsOrderedAsc(item.reports)" v-bind:key="report.id" max-width="500px">
        <template v-slot:activator="{ on: ton, attrs: tattrs}">
          <v-icon v-bind="tattrs" v-on="ton" :set="icon=getIcon(report.progress)" :color="icon.color">{{
              icon.icon
            }}
          </v-icon>
        </template>
        <p>{{ report.createdBy.name }}<br>
          {{ formatDateTime(report?.createdAt) }}</p>
        <p>{{ report.comment }}</p>
      </v-tooltip>
    </template>
    <template v-slot:[`item.juniorInCompanyMonths.value`]="{ item }">
      <value-with-status-chip :value="item.juniorInCompanyMonths" dense></value-with-status-chip>
    </template>
    <template v-slot:[`item.monthsWithoutReport.value`]="{ item }">
      <value-with-status-chip :value="item.monthsWithoutReport" dense></value-with-status-chip>
    </template>
    <template v-slot:[`item.latestReport.createdAt`]="{ item }">
      {{ formatDateTime(item.latestReport?.createdAt) }}
    </template>
    <template v-slot:[`item.graduation.graduatedAt`]="{ item }">
      {{ formatDateTime(item.graduation?.graduatedAt) }}
    </template>
  </hreasy-table>
</template>

<script lang="ts">
import Component from "vue-class-component";
import {Vue} from "vue-property-decorator";
import {ReportPeriod} from "@/components/overtimes/overtime.service";
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {DataTableHeader} from "vuetify";
import {JuniorRegistryDataContainer} from "@/components/udr/junior-registry.data.container";
import JuniorAddUpdateFormFields from "@/components/udr/JuniorAddUpdateFormFields.vue";
import HreasyTable from "@/components/shared/table/HreasyTable.vue";
import HreasyTableExportToExcelAction from "@/components/shared/table/HreasyTableExportToExcelAction.vue";
import {JuniorProgressType, JuniorReport} from "@/components/udr/udr.service";
import ValueWithStatusChip from "@/components/shared/ValueWithStatusChip.vue";
import JuniorTableFilter from "@/components/udr/info/JuniorTableFilter.vue";

@Component({
  name: "JuniorRegistryTable",
  components: {
    JuniorTableFilter,
    ValueWithStatusChip,
    HreasyTableExportToExcelAction,
    HreasyTable,
    JuniorAddUpdateFormFields,
    HreasyTableDeleteConfimration,
    MyDateFormComponent,
    HreasyTableCreateForm
  }
})
export default class JuniorRegistryTable extends Vue {
  private data = new JuniorRegistryDataContainer(() => {
        const headers: DataTableHeader[] = [
          {text: this.$tc('Молодой специалист'), value: 'juniorEmpl.name'},
          {text: this.$tc('Ментор'), value: 'mentor.name'},
          {text: this.$tc('Роль'), value: 'role'},
          {text: this.$tc('Бюджет'), value: 'budgetingAccount.name'},
          {text: this.$tc('Текущий проект'), value: 'currentProject.name'},
          {text: this.$tc('Месяцев в компании'), value: 'juniorInCompanyMonths.value'},
          {text: this.$tc('Месяцев без отчёта'), value: 'monthsWithoutReport.value'},
          {text: this.$tc('Прогресс'), value: 'progress'},
          {text: this.$tc('Последний срез (Когда)'), value: 'latestReport.createdAt'},
          {text: this.$tc('Последний срез (Кто)'), value: 'latestReport.createdBy.name'},
          {text: this.$tc('Последний срез (Комментарий)'), value: 'latestReport.comment'},
          {text: this.$tc('Завершил обучение'), value: 'graduation.graduatedAt'}
        ];
        return headers;
      },
      (item) => {
        if (item?.id) {
          this.$router.push(`/juniors/${item.id}`);
        }
      });

  /**
   * Keep-alive hook
   */
  activated() {
    this.data.reloadData();
  }


  public static getIcon(type: JuniorProgressType): { icon: string, color: string } {
    switch (type) {
      case JuniorProgressType.DEGRADATION:
        return {icon: 'mdi-arrow-bottom-left', color: 'error'};
      case JuniorProgressType.NO_PROGRESS:
        return {icon: 'mdi-minus', color: ''};
      case JuniorProgressType.PROGRESS:
        return {icon: 'mdi-arrow-top-right', color: 'success'};
      case JuniorProgressType.GOOD_PROGRESS:
        return {icon: 'mdi-arrow-up-bold', color: 'success'};
      default:
        return {icon: 'mdi-question', color: 'error'};
    }
  }

  private reportsOrderedAsc(reports: JuniorReport[]) {
    if (reports) {
      return [...reports].sort((a, b) => (a.createdAt > b.createdAt) ? 1 : ((b.createdAt > a.createdAt) ? -1 : 0));
    }
    return reports;
  }

  getIcon = JuniorRegistryTable.getIcon;

  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
}
</script>

