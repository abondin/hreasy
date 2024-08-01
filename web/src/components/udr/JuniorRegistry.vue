<template>
  <hreasy-table :data="data" :create-new-title="$t('Добавление в реестр')"
                :update-title="$t('Обновление записи в реестре')">
    <template v-slot:filters>
      <v-col>
        <v-text-field v-if="data.filter"
                      v-model="data.filter.search"
                      append-icon="mdi-magnify"
                      :label="$t('Поиск')"
                      single-line
                      hide-details
        ></v-text-field>
      </v-col>

      <v-col cols="auto">
        <v-select
            v-model="data.filter.onlyNotGraduated"
            :label="$t('Скрыть закончивших обучение')"
            :items="[{value:false, text:$t('Нет')}, {value:true, text:$t('Да')}]">
        </v-select>
      </v-col>
    </template>
    <template v-slot:additionalActions>
      <hreasy-table-export-to-excel-action :data=data></hreasy-table-export-to-excel-action>
    </template>
    <template v-slot:createFormFields>
      <junior-add-update-form-fields :create-or-update-body="data.createBody"
                                     mode="add"></junior-add-update-form-fields>
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
import MyDateFormComponent from "@/components/shared/MyDateFormComponent.vue";
import HreasyTableCreateForm from "@/components/shared/table/HreasyTableCreateForm.vue";
import {DateTimeUtils} from "@/components/datetimeutils";
import HreasyTableDeleteConfimration from "@/components/shared/table/HreasyTableDeleteConfimration.vue";
import {DataTableHeader} from "vuetify";
import {JuniorRegistryDataContainer} from "@/components/udr/junior-registry.data.container";
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
      },
      (item) => {
        if (item?.id) {
          this.$router.push(`/juniors/${item.id}`);
        }
      });

  /**
   * Lifecycle hook
   */
  created() {
    this.data.init();
  }


  formatDateTime = (v: string | undefined) => DateTimeUtils.formatDateTimeFromIso(v);

  formatDate = (v: string | undefined) => DateTimeUtils.formatFromIso(v);

  formatMoney = (v: string | number | null | undefined) => v ? Number(v).toLocaleString() : '';

  fromPeriodId = (v: number | null | undefined) => v && !isNaN(v) ? ReportPeriod.fromPeriodId(v) : null;
}
</script>

